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

    private DProcedure(BiFunction<AstNode, Env, Token> procedure, int argc, boolean vararg) {
        this.procedure = procedure;
        this.argc = argc;
        this.children = new ArrayList<>();
        this.vararg = vararg;
    }

    public static DProcedure newProcedure(BiFunction<AstNode, Env, Token> function, int argc, boolean vararg) {
        return new DProcedure(function, argc, vararg);
    }

    public DProcedure addProcedure(DProcedure procedure) {
        if (match(procedure.argc) != null) throw new DevoreRuntimeException("过程定义冲突.");
        this.children.add(procedure);
        return this;
    }

    private DProcedure match(int argc) {
        DProcedure function = null;
        if (this.argc == argc || (this.vararg && argc >= this.argc)) function = this;
        else {
            for (DProcedure df : children) {
                DProcedure temp = df.match(argc);
                if (temp != null) function = temp;
            }
        }
        return function;
    }

    public Token call(AstNode ast, Env env) {
        DProcedure df = match(ast.size());
        if (df == null) throw new DevoreRuntimeException("找不到匹配条件的过程.");
        return df.procedure.apply(ast, env);
    }

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
        return newProcedure(procedure, argc, vararg);
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
