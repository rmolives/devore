package org.wumoe.devore;

import org.wumoe.devore.lang.token.Token;
import org.wumoe.devore.parse.Lexer;
import org.wumoe.devore.parse.Parser;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<String> expressions = Lexer.splitCode("(a 123)");
        for (String expression : expressions) {
            List<Token> tokens = Lexer.lexer(expression);
            System.out.println(Parser.parser(tokens));
        }
    }
}
