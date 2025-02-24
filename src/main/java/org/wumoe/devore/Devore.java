package org.wumoe.devore;

import org.wumoe.devore.lang.Env;
import org.wumoe.devore.lang.Evaluator;
import org.wumoe.devore.lang.token.DWord;
import org.wumoe.devore.lang.token.Token;
import org.wumoe.devore.module.*;
import org.wumoe.devore.module.Module;
import org.wumoe.devore.parser.Lexer;
import org.wumoe.devore.parser.Parse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Devore {
    public static final String VERSION = "0.1-alpha";
    public static final String VERSION_MESSAGE = "Devore v" + VERSION + ".\nAuthor: RMOlive (rmolives@wumoe.org)\nGithub: https://github.com/rmolives/devore";
    public static Map<String, Module> moduleTable = new HashMap<>() {
        {
            put("core", new CoreModule());
            put("math", new MathModule());
            put("table", new TableModule());
            put("thread", new ThreadModule());
        }
    };

    public static List<String> initModule = new ArrayList<>() {
        {
            add("core");
        }
    };

    public static void addModule(String name, Module module) {
        moduleTable.put(name, module);
    }

    public static void addDefaultLoadModule(String name) {
        initModule.add(name);
    }

    public static Token call(Env env, String code) {
        List<String> codes = Lexer.splitCode(code);
        Token result = DWord.WORD_NIL;
        for (String exp : codes)
            result = Evaluator.eval(env, Parse.parse(Lexer.lexer(exp)));
        return result;
    }
}
