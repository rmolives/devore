package org.devore.lang;

import org.devore.Devore;
import org.devore.exception.DevoreRuntimeException;
import org.devore.lang.token.DFunction;
import org.devore.lang.token.DWord;
import org.devore.lang.token.Token;
import org.devore.parser.AstNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * 环境
 */
public class Env {
    public final IOConfig io;               // IO表
    public final Map<String, Token> table;  // 环境表
    public final Env father;                // 父环境

    /**
     * 创建环境
     *
     * @param table  环境表
     * @param father 父环境
     * @param io     IO表
     */
    protected Env(Map<String, Token> table, Env father, IOConfig io) {
        this.table = table;
        this.father = father;
        this.io = io;
        for (String name : Devore.initModule)
            load(name);
    }

    /**
     * 创建环境
     *
     * @return 环境
     */
    public static Env newEnv() {
        return new Env(new HashMap<>(), null, new IOConfig());
    }

    /**
     * 创建环境
     *
     * @param father 父环境
     * @param io     IO表
     * @return 环境
     */
    public static Env newEnv(Env father, IOConfig io) {
        return new Env(new HashMap<>(), father, io);
    }

    /**
     * 创建环境
     *
     * @param io IO表
     * @return 环境
     */
    public static Env newEnv(IOConfig io) {
        return new Env(new HashMap<>(), null, io);
    }

    /**
     * 设置KY对
     *
     * @param key   key
     * @param value value
     * @return 环境
     */
    public Env put(String key, Token value) {
        if (table.containsKey(key))
            throw new DevoreRuntimeException("定义冲突: " + key);
        table.put(key, value);
        return this;
    }

    /**
     * 添加Symbol函数
     *
     * @param key      key
     * @param function 函数
     * @param argSize  参数数量
     * @param vararg   是否为可变参数
     * @return 环境
     */
    public Env addSymbolFunction(String key, BiFunction<AstNode, Env, Token> function, int argSize, boolean vararg) {
        if (table.containsKey(key)) {
            table.put(key, ((DFunction) table.get(key)).addFunction(DFunction.newFunction(function, argSize, vararg)));
            return this;
        }
        table.put(key, DFunction.newFunction(function, argSize, vararg));
        return this;
    }

    /**
     * 添加普通函数
     *
     * @param key      key
     * @param function 函数
     * @param argSize  参数数量
     * @param vararg   是否为可变参数
     * @return 环境
     */
    public Env addTokenFunction(String key, BiFunction<List<Token>, Env, Token> function, int argSize, boolean vararg) {
        BiFunction<AstNode, Env, Token> df = (ast, env) -> {
            List<Token> args = new ArrayList<>();
            for (int i = 0; i < ast.size(); ++i) {
                ast.get(i).symbol = Evaluator.eval(env, ast.get(i).copy());
                args.add(ast.get(i).symbol);
            }
            return function.apply(args, env);
        };
        if (table.containsKey(key)) {
            table.put(key, ((DFunction) table.get(key)).addFunction(DFunction.newFunction(df, argSize, vararg)));
            return this;
        }
        table.put(key, DFunction.newFunction(df, argSize, vararg));
        return this;
    }

    /**
     * 设置Symbol函数
     *
     * @param key      key
     * @param function 函数
     * @param argSize  参数数量
     * @param vararg   是否为可变参数
     * @return 环境
     */
    public Env setSymbolFunction(String key, BiFunction<AstNode, Env, Token> function, int argSize, boolean vararg) {
        Env temp = this;
        while (temp.father != null && !temp.table.containsKey(key))
            temp = temp.father;
        temp.table.put(key, DFunction.newFunction(function, argSize, vararg));
        return this;
    }

    /**
     * 设置普通函数
     *
     * @param key      key
     * @param function 函数
     * @param argSize  参数数量
     * @param vararg   是否为可变参数
     * @return 环境
     */
    public Env setTokenFunction(String key, BiFunction<List<Token>, Env, Token> function, int argSize, boolean vararg) {
        Env temp = this;
        while (temp.father != null && !temp.table.containsKey(key))
            temp = temp.father;
        BiFunction<AstNode, Env, Token> df = (ast, env) -> {
            List<Token> args = new ArrayList<>();
            for (int i = 0; i < ast.size(); ++i) {
                ast.get(i).symbol = Evaluator.eval(env, ast.get(i).copy());
                args.add(ast.get(i).symbol);
            }
            return function.apply(args, env);
        };
        temp.table.put(key, DFunction.newFunction(df, argSize, vararg));
        return this;
    }

    /**
     * 设置KY对
     *
     * @param key   key
     * @param value value
     * @return 环境
     */
    public Env set(String key, Token value) {
        Env temp = this;
        while (temp.father != null && !temp.table.containsKey(key))
            temp = temp.father;
        temp.table.put(key, value);
        return this;
    }

    /**
     * 查看是否包含特定key
     *
     * @param key key
     * @return 结果
     */
    public boolean contains(String key) {
        Env temp = this;
        while (temp.father != null && !temp.table.containsKey(key))
            temp = temp.father;
        return temp.table.containsKey(key);
    }

    /**
     * 获取key对应的value
     *
     * @param key key
     * @return value
     */
    public Token get(String key) {
        Env temp = this;
        while (temp.father != null && !temp.table.containsKey(key))
            temp = temp.father;
        return temp.contains(key) ? temp.table.get(key) : DWord.WORD_NIL;
    }

    /**
     * 删除KY对
     *
     * @param key key
     * @return 删除的value
     */
    public Token remove(String key) {
        Env temp = this;
        while (temp.father != null && !temp.table.containsKey(key))
            temp = temp.father;
        return temp.contains(key) ? temp.table.remove(key) : DWord.WORD_NIL;
    }

    /**
     * 清空环境
     */
    public void clear() {
        this.table.clear();
    }

    /**
     * 创建子环境
     *
     * @return 子环境
     */
    public Env createChild() {
        return newEnv(this, io);
    }

    /**
     * 加载对应名字的模块
     *
     * @param name 模块名
     * @return 环境
     */
    public Env load(String name) {
        Devore.moduleTable.get(name).init(this);
        return this;
    }
}
