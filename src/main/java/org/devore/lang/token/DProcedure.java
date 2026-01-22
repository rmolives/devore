package org.devore.lang.token;

import org.devore.exception.DevoreRuntimeException;
import org.devore.lang.Env;
import org.devore.parser.Ast;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

/**
 * 过程
 */
public class DProcedure extends DToken {
    private final BiFunction<Ast, Env, DToken> procedure;   // 过程
    private final int argc;                                 // 参数数量
    private final List<DProcedure> children;                // 子过程
    private final boolean vararg;                           // 是否为可变参数

    private DProcedure(BiFunction<Ast, Env, DToken> procedure, List<DProcedure> children, int argc, boolean vararg) {
        this.procedure = procedure;
        this.children = children;
        this.argc = argc;
        this.vararg = vararg;
    }

    /**
     * 创建新过程
     *
     * @param procedure 过程
     * @param children  子过程
     * @param argc      参数数量
     * @param vararg    是否为可变参数
     * @return this
     */
    public static DProcedure newProcedure(BiFunction<Ast, Env, DToken> procedure, List<DProcedure> children, int argc, boolean vararg) {
        return new DProcedure(procedure, children, argc, vararg);
    }

    /**
     * 创建新过程
     *
     * @param procedure 过程
     * @param argc      参数数量
     * @param vararg    是否为可变参数
     * @return this
     */
    public static DProcedure newProcedure(BiFunction<Ast, Env, DToken> procedure, int argc, boolean vararg) {
        return new DProcedure(procedure, new ArrayList<>(), argc, vararg);
    }

    /**
     * 添加新过程
     *
     * @param procedure 过程
     * @return this
     */
    public DProcedure addProcedure(DProcedure procedure) {
        if (this.match(procedure.argc) != null)
            throw new DevoreRuntimeException("过程定义冲突.");
        this.children.add(procedure);
        return this;
    }

    /**
     * 匹配符合条件的过程
     *
     * @param argc 参数数量
     * @return 符合条件的过程
     */
    private DProcedure match(int argc) {
        if (this.argc == argc && !vararg)
            return this;
        for (DProcedure df : this.children) {
            DProcedure temp = df.match(argc);
            if (temp != null && temp.argc == argc && !temp.vararg)
                return temp;
        }
        DProcedure bestVararg = null;
        for (DProcedure df : this.children) {
            DProcedure temp = df.match(argc);
            if (temp != null && argc >= temp.argc && temp.vararg)
                if (bestVararg == null || temp.argc > bestVararg.argc)
                    bestVararg = temp;
        }
        if (bestVararg != null)
            return bestVararg;
        if (argc >= this.argc && this.vararg)
            return this;
        return null;
    }

    /**
     * 执行过程
     *
     * @param node node
     * @param env  环境
     * @return 结果
     */
    public DToken call(Ast node, Env env) {
        DProcedure df = this.match(node.size());
        if (df == null)
            throw new DevoreRuntimeException("找不到匹配条件的过程.");
        return df.procedure.apply(node, env);
    }

    /**
     * 执行过程
     *
     * @param args 参数
     * @param env  环境
     * @return 结果
     */
    public DToken call(List<DToken> args, Env env) {
        DProcedure df = this.match(args.size());
        if (df == null)
            throw new DevoreRuntimeException("找不到匹配条件的过程.");
        Ast ast = Ast.empty.copy();
        for (DToken arg : args)
            ast.add(new Ast(arg));
        return df.procedure.apply(ast, env);
    }

    @Override
    public String type() {
        return "procedure";
    }

    @Override
    protected String str() {
        return "<procedure>";
    }

    @Override
    public DToken copy() {
        List<DProcedure> temp = new ArrayList<>();
        for (DProcedure proc : this.children)
            temp.add((DProcedure) proc.copy());
        return newProcedure(this.procedure, temp, this.argc, this.vararg);
    }

    @Override
    public int compareTo(DToken t) {
        if (this == t)
            return 0;
        if (!(t instanceof DProcedure))
            return -1;
        DProcedure other = (DProcedure) t;
        if (this.procedure != other.procedure)
            return -1;
        if (this.argc != other.argc)
            return -1;
        if (this.vararg != other.vararg)
            return -1;
        if (this.children.size() != other.children.size())
            return -1;
        for (int i = 0; i < children.size(); ++i)
            if (children.get(i).compareTo(other.children.get(i)) != 0)
                return -1;
        return 0;
    }

    @Override
    public int hashCode() {
        int result = this.type().hashCode();
        result = 31 * result + System.identityHashCode(this.procedure);
        result = 31 * result + this.argc;
        result = 31 * result + Boolean.hashCode(this.vararg);
        result = 31 * result + this.children.hashCode();
        return result;
    }
}
