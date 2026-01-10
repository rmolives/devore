package org.devore.lang.token;

import org.devore.exception.DevoreRuntimeException;
import org.devore.lang.Env;
import org.devore.parser.AstNode;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

/**
 * 函数
 */
public class DFunction extends Token {
    private final BiFunction<AstNode, Env, Token> function;
    private final int argSize;
    private final List<DFunction> children;
    private final boolean vararg;

    private DFunction(BiFunction<AstNode, Env, Token> function, int argSize, boolean vararg) {
        this.function = function;
        this.argSize = argSize;
        this.children = new ArrayList<>();
        this.vararg = vararg;
    }

    public static DFunction newFunction(BiFunction<AstNode, Env, Token> function, int argSize, boolean vararg) {
        return new DFunction(function, argSize, vararg);
    }

    public DFunction addFunction(DFunction function) {
        if (!isEqArgs(function.argSize))
            throw new DevoreRuntimeException("函数定义冲突");
        this.children.add(function);
        return this;
    }

    private DFunction match(int argSize) {
        DFunction function = null;
        if (this.argSize == argSize || (this.vararg && argSize >= this.argSize))
            function = this;
        else {
            for (DFunction df : children) {
                DFunction temp = df.match(argSize);
                if (temp != null)
                    function = temp;
            }
        }
        return function;
    }

    public Token call(AstNode ast, Env env) {
        DFunction df = match(ast.size());
        if (df == null)
            throw new DevoreRuntimeException("找不到匹配条件的函数.");
        return df.function.apply(ast, env);
    }

    public Token call(Token[] args, Env env) {
        DFunction df = match(args.length);
        if (df == null)
            throw new DevoreRuntimeException("找不到匹配条件的函数.");
        AstNode ast = AstNode.nullAst.copy();
        for (Token arg : args) ast.add(new AstNode(arg));
        return df.function.apply(ast, env);
    }

    private boolean isEqArgs(int argSize) {
        return match(argSize) == null;
    }

    @Override
    public String type() {
        return "function";
    }

    @Override
    public String str() {
        return "<function>";
    }

    @Override
    public Token copy() {
        return newFunction(function, argSize, vararg);
    }

    @Override
    public int compareTo(Token t) {
        return t instanceof DFunction func && func.function.equals(this.function)
                && func.argSize == this.argSize && func.children.equals(this.children)
                && func.vararg == this.vararg ? 0 : -1;
    }
}
