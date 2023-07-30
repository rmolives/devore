package org.wumoe.devore;

import org.wumoe.devore.core.Core;
import org.wumoe.devore.lang.Env;
import org.wumoe.devore.lang.Evaluator;
import org.wumoe.devore.lang.token.Token;
import org.wumoe.devore.parse.Lexer;
import org.wumoe.devore.parse.Parser;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<String> expressions = Lexer.splitCode("(+ 2 (+ 3 4))");
        Env env = Env.newEnv();
        Core.init(env);
        for (String expression : expressions) {
            List<Token> tokens = Lexer.lexer(expression);
            System.out.println(Evaluator.eval(env, Parser.parser(tokens)));
        }
    }
}
