package org.wumoe.devore;

import org.wumoe.devore.lang.Env;
import org.wumoe.devore.lang.Evaluator;
import org.wumoe.devore.lang.token.DWord;
import org.wumoe.devore.lang.token.Token;
import org.wumoe.devore.parse.Lexer;
import org.wumoe.devore.parse.Parser;

import java.util.List;

public class Devore {
    public static Token call(Env env, String code) {
        List<String> codes = Lexer.splitCode(code);
        Token result = DWord.WORD_NIL;
        for (String exp : codes)
            result = Evaluator.eval(env, Parser.parser(Lexer.lexer(exp)));
        return result;
    }
}
