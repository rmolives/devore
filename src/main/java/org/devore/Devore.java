package org.devore;

import org.devore.lang.Env;
import org.devore.lang.Evaluator;
import org.devore.lang.token.DWord;
import org.devore.lang.token.Token;
import org.devore.module.*;
import org.devore.module.Module;
import org.wumoe.devore.module.*;
import org.devore.parser.Lexer;
import org.devore.parser.Parse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Devore {
    // 版本
    public static final String VERSION = "0.1-alpha";
    // 版本信息
    public static final String VERSION_MESSAGE = "Devore v" + VERSION + ".\nAuthor: RMOlive (rmolives@wumoe.org)\nGithub: https://github.com/rmolives/devore";

    // 模块列表
    public static Map<String, Module> moduleTable = new HashMap<>() {
        {
            put("core", new CoreModule());
            put("math", new MathModule());
            put("table", new TableModule());
            put("thread", new ThreadModule());
        }
    };

    // 默认导入的模块
    public static List<String> initModule = new ArrayList<>() {
        {
            add("core");
        }
    };

    /**
     * 添加模块
     * @param name 名字
     * @param module 模块
     */
    public static void addModule(String name, Module module) {
        moduleTable.put(name, module);
    }

    /**
     * 添加默认导入的模块
     * @param name 名字
     */
    public static void addDefaultLoadModule(String name) {
        initModule.add(name);
    }

    /**
     * 执行代码
     * @param env 环境
     * @param code 代码
     * @return 返回值
     */
    public static Token call(Env env, String code) {
        List<String> codes = Lexer.splitCode(code);
        Token result = DWord.WORD_NIL;
        for (String exp : codes)
            result = Evaluator.eval(env, Parse.parse(Lexer.lexer(exp)));
        return result;
    }
}
