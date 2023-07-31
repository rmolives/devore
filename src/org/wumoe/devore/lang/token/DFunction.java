package org.wumoe.devore.lang.token;

import org.wumoe.devore.exception.DevoreRuntimeException;
import org.wumoe.devore.lang.Env;
import org.wumoe.devore.parse.AstNode;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class DFunction extends Token {
    private final BiFunction<AstNode, Env, Token> function;
    private final int argSize;
    private final List<DFunction> children;
    private final boolean mult;

    private DFunction(BiFunction<AstNode, Env, Token> function, int argSize, boolean mult) {
        this.function = function;
        this.argSize = argSize;
        this.children = new ArrayList<>();
        this.mult = mult;
    }

    public static DFunction newFunction(BiFunction<AstNode, Env, Token> function, int argSize, boolean mult) {
        return new DFunction(function, argSize, mult);
    }

    public DFunction addFunction(DFunction function) {
        this.children.add(function);
        return this;
    }

    private DFunction match(int argSize) {
        DFunction function = null;
        if (this.argSize == argSize || (this.mult && argSize >= this.argSize))
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
        return newFunction(function, argSize, mult);
    }

    @Override
    public int compareTo(Token t) {
        return -1;
    }
}
