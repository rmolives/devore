package org.wumoe.devore.lang.token;

import org.wumoe.devore.lang.Env;
import org.wumoe.devore.parse.AstNode;

import java.util.function.BiFunction;

public class DFunction extends Token {
    private final BiFunction<AstNode, Env, Token> function;

    private DFunction(BiFunction<AstNode, Env, Token> function) {
        this.function = function;
    }

    public static DFunction newFunction(BiFunction<AstNode, Env, Token> function) {
        return new DFunction(function);
    }

    public Token call(AstNode ast, Env env) {
        return function.apply(ast, env);
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
        return newFunction(function);
    }

    @Override
    public int compareTo(Token t) {
        return -1;
    }
}
