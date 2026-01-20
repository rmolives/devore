package org.devore;

import org.devore.lang.Env;
import org.devore.lang.Evaluator;
import org.devore.lang.token.DWord;
import org.devore.lang.token.DToken;
import org.devore.parser.Lexer;
import org.devore.parser.Parse;

import java.util.List;

public class Devore {
    // 版本
    public static final String VERSION = "0.1-alpha";
    // 版本信息
    public static final String VERSION_MESSAGE = "Devore v" + VERSION + ".\nAuthor: RMOlive (rmolives@wumoe.org)\nGitHub: https://github.com/rmolives/devore";

    /**
     * 执行代码
     * @param env  环境
     * @param code 代码
     * @return 返回值
     */
    public static DToken call(Env env, String code) {
        List<String> codes = Lexer.splitCode(code);
        DToken result = DWord.NIL;
        for (String exp : codes) result = Evaluator.eval(env, Parse.parse(Lexer.lexer(exp)));
        return result;
    }
}
