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
    private final int argSize;
    private final List<DProcedure> children;
    private final boolean vararg;

    private DProcedure(BiFunction<AstNode, Env, Token> procedure, int argSize, boolean vararg) {
        this.procedure = procedure;
        this.argSize = argSize;
        this.children = new ArrayList<>();
        this.vararg = vararg;
    }

    public static DProcedure newProcedure(BiFunction<AstNode, Env, Token> function, int argSize, boolean vararg) {
        return new DProcedure(function, argSize, vararg);
    }

    public DProcedure addProcedure(DProcedure procedure) {
        if (match(procedure.argSize) != null) throw new DevoreRuntimeException("过程定义冲突.");
        this.children.add(procedure);
        return this;
    }

    private DProcedure match(int argSize) {
        DProcedure function = null;
        if (this.argSize == argSize || (this.vararg && argSize >= this.argSize)) function = this;
        else {
            for (DProcedure df : children) {
                DProcedure temp = df.match(argSize);
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
        return newProcedure(procedure, argSize, vararg);
    }

    @Override
    public int compareTo(Token t) {
        return t instanceof DProcedure proc && proc.procedure.equals(this.procedure) && proc.argSize == this.argSize && proc.children.equals(this.children) && proc.vararg == this.vararg ? 0 : -1;
    }
}
