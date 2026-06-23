package org.devore.lang.token;

import org.devore.exception.DevoreRuntimeException;
import org.devore.lang.Env;
import org.devore.parser.Ast;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.IntStream;

/**
 * 过程
 */
public class DProcedure extends DToken {
    private final BiFunction<Ast, Env, DToken> procedure;   // 过程
    private final String name;                              // 过程名
    private final int argc;                                 // 参数数量
    private final List<DProcedure> children;                // 子过程
    private final boolean vararg;                           // 是否为可变参数

    private DProcedure(String name, BiFunction<Ast, Env, DToken> procedure, List<DProcedure> children, int argc, boolean vararg) {
        this.name = name;
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
    public static DProcedure newProcedure(String name, BiFunction<Ast, Env, DToken> procedure, List<DProcedure> children, int argc, boolean vararg) {
        return new DProcedure(name, procedure, children, argc, vararg);
    }

    /**
     * 创建新过程
     *
     * @param name      过程名
     * @param procedure 过程
     * @param argc      参数数量
     * @param vararg    是否为可变参数
     * @return this
     */
    public static DProcedure newProcedure(String name, BiFunction<Ast, Env, DToken> procedure, int argc, boolean vararg) {
        return newProcedure(name, procedure, new ArrayList<>(), argc, vararg);
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
        return newProcedure("<procedure>", procedure, argc, vararg);
    }

    /**
     * 添加新过程
     *
     * @param name      过程名
     * @param procedure 过程
     * @return this
     */
    public DProcedure addProcedure(String name, DProcedure procedure) {
        if (this.match(procedure.argc) != null)
            throw new DevoreRuntimeException("过程定义冲突: " + name);
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
        DProcedure exact = this.children.stream()
                .map(df -> df.match(argc))
                .filter(temp -> temp != null && temp.argc == argc && !temp.vararg)
                .findFirst()
                .orElse(null);
        if (exact != null)
            return exact;
        DProcedure bestVararg = this.children.stream()
                .map(df -> df.match(argc))
                .filter(temp -> temp != null && argc >= temp.argc && temp.vararg)
                .max(Comparator.comparingInt(left -> left.argc))
                .orElse(null);
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
            throw new DevoreRuntimeException("找不到匹配条件的过程: " + this.name);
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
            throw new DevoreRuntimeException("找不到匹配条件的过程: " + this.name);
        Ast ast = Ast.empty.copy();
        args.stream()
                .map(Ast::new)
                .forEach(ast::add);
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
    public int compareTo(DToken t) {
        if (this == t)
            return 0;
        if (!(t instanceof DProcedure))
            return -1;
        DProcedure other = (DProcedure) t;
        if (!this.name.equals(other.name))
            return -1;
        if (this.procedure != other.procedure)
            return -1;
        if (this.argc != other.argc)
            return -1;
        if (this.vararg != other.vararg)
            return -1;
        if (this.children.size() != other.children.size())
            return -1;
        return IntStream.range(0, children.size())
                .allMatch(i -> children.get(i).compareTo(other.children.get(i)) == 0) ? 0 : -1;
    }

    @Override
    public int hashCode() {
        int result = this.type().hashCode();
        result = 31 * result + this.name.hashCode();
        result = 31 * result + System.identityHashCode(this.procedure);
        result = 31 * result + this.argc;
        result = 31 * result + Boolean.hashCode(this.vararg);
        result = 31 * result + this.children.hashCode();
        return result;
    }
}
