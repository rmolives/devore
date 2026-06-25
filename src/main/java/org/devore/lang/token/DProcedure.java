package org.devore.lang.token;

import org.devore.exception.DevoreRuntimeException;
import org.devore.lang.Env;
import org.devore.parser.Ast;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;

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
            throw new DevoreRuntimeException("定义冲突: " + name);
        this.children.add(procedure);
        return this;
    }

    /**
     * 替换相同参数数量的过程，不存在时添加为新重载
     *
     * @param procedure 过程
     * @return this
     */
    public DProcedure setProcedure(DProcedure procedure) {
        if (this.argc == procedure.argc) {
            procedure.children.addAll(this.children);
            return procedure;
        }
        for (int i = 0; i < this.children.size(); ++i) {
            if (this.children.get(i).argc == procedure.argc) {
                this.children.set(i, procedure);
                return this;
            }
        }
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
                .filter(temp -> temp.argc == argc && !temp.vararg)
                .findFirst()
                .orElse(null);
        if (exact != null)
            return exact;
        DProcedure bestVararg = this.children.stream()
                .filter(temp -> argc >= temp.argc && temp.vararg)
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
        return this.vararg == other.vararg ? 0 : -1;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.type(), this.name, this.procedure, this.argc, this.vararg);
    }
}
