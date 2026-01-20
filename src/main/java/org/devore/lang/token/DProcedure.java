package org.devore.lang.token;

import org.devore.exception.DevoreRuntimeException;
import org.devore.lang.Env;
import org.devore.parser.AstNode;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

/**
 * 过程
 */
public class DProcedure extends Token {
    private final BiFunction<AstNode, Env, Token> procedure;
    private final int argc;
    private final List<DProcedure> children;
    private final boolean vararg;

    private DProcedure(BiFunction<AstNode, Env, Token> procedure, List<DProcedure> children, int argc, boolean vararg) {
        this.procedure = procedure;
        this.argc = argc;
        this.children = children;
        this.vararg = vararg;
    }

    /**
     * 创建新过程
     * @param procedure 过程
     * @param children  子过程集
     * @param argc      参数数量
     * @param vararg    是否为可变参数
     * @return this
     */
    public static DProcedure newProcedure(BiFunction<AstNode, Env, Token> procedure, List<DProcedure> children, int argc, boolean vararg) {
        return new DProcedure(procedure, children, argc, vararg);
    }

    /**
     * 创建新过程
     * @param procedure 过程
     * @param argc      参数数量
     * @param vararg    是否为可变参数
     * @return this
     */
    public static DProcedure newProcedure(BiFunction<AstNode, Env, Token> procedure, int argc, boolean vararg) {
        return new DProcedure(procedure, new ArrayList<>(), argc, vararg);
    }

    /**
     * 添加新过程
     * @param procedure 过程
     * @return this
     */
    public DProcedure addProcedure(DProcedure procedure) {
        if (match(procedure.argc) != null) throw new DevoreRuntimeException("过程定义冲突.");
        this.children.add(procedure);
        return this;
    }

    /**
     * 匹配符合条件的过程
     * @param argc  参数数量
     * @return 符合条件的过程
     */
    private DProcedure match(int argc) {
        DProcedure procedure = null;
        if (this.argc == argc || (this.vararg && argc >= this.argc)) procedure = this;
        else {
            for (DProcedure df : children) {
                DProcedure temp = df.match(argc);
                if (temp != null) procedure = temp;
            }
        }
        return procedure;
    }

    /**
     * 执行过程
     * @param ast   ast
     * @param env   环境
     * @return 结果
     */
    public Token call(AstNode ast, Env env) {
        DProcedure df = match(ast.size());
        if (df == null) throw new DevoreRuntimeException("找不到匹配条件的过程.");
        return df.procedure.apply(ast, env);
    }

    /**
     * 执行过程
     * @param args  参数
     * @param env   环境
     * @return 结果
     */
    public Token call(Token[] args, Env env) {
        DProcedure df = match(args.length);
        if (df == null) throw new DevoreRuntimeException("找不到匹配条件的过程.");
        AstNode ast = AstNode.emptyAst.copy();
        for (Token arg : args) ast.add(new AstNode(arg));
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
    public Token copy() {
        List<DProcedure> temp = new ArrayList<>();
        for (DProcedure proc : children) temp.add((DProcedure) proc.copy());
        return newProcedure(procedure, temp, argc, vararg);
    }

    @Override
    public int compareTo(Token t) {
        if (this == t) return 0;
        if (!(t instanceof DProcedure)) return -1;
        DProcedure other = (DProcedure) t;
        if (this.procedure != other.procedure) return -1;
        if (this.argc != other.argc) return -1;
        if (this.vararg != other.vararg) return -1;
        if (this.children.size() != other.children.size()) return -1;
        for (int i = 0; i < children.size(); ++i)
            if (children.get(i).compareTo(other.children.get(i)) != 0) return -1;
        return 0;
    }

    @Override
    public int hashCode() {
        int result = type().hashCode();
        result = 31 * result + System.identityHashCode(procedure);
        result = 31 * result + argc;
        result = 31 * result + Boolean.hashCode(vararg);
        result = 31 * result + children.hashCode();
        return result;
    }
}
