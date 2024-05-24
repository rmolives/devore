package org.wumoe.devore.lang;

import org.wumoe.devore.Devore;
import org.wumoe.devore.exception.DevoreRuntimeException;
import org.wumoe.devore.lang.token.DFunction;
import org.wumoe.devore.lang.token.DWord;
import org.wumoe.devore.lang.token.Token;
import org.wumoe.devore.parser.AstNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public class Env {
    public final IOConfig io;
    public final Map<String, Token> table;
    public final Env father;

    protected Env(Map<String, Token> table, Env father, IOConfig io) {
        this.table = table;
        this.father = father;
        this.io = io;
        for (String name : Devore.initModule)
            load(name);
    }

    public static Env newEnv(Map<String, Token> table, Env father) {
        return new Env(table, father, new IOConfig());
    }

    public static Env newEnv(Map<String, Token> table, IOConfig io) {
        return new Env(table, null, io);
    }

    public static Env newEnv(Map<String, Token> table, Env father, IOConfig io) {
        return new Env(table, father, io);
    }

    public static Env newEnv(Map<String, Token> table) {
        return new Env(table, null, new IOConfig());
    }

    public static Env newEnv(Env father) {
        return new Env(new HashMap<>(), father, new IOConfig());
    }

    public static Env newEnv() {
        return new Env(new HashMap<>(), null, new IOConfig());
    }

    public static Env newEnv(Env father, IOConfig io) {
        return new Env(new HashMap<>(), father, io);
    }

    public static Env newEnv(IOConfig io) {
        return new Env(new HashMap<>(), null, io);
    }

    public Env put(String key, Token value) {
        if (table.containsKey(key))
            throw new DevoreRuntimeException("定义冲突");
        table.put(key, value);
        return this;
    }

    public Env addSymbolFunction(String key, BiFunction<AstNode, Env, Token> function, int argSize, boolean vararg) {
        if (table.containsKey(key)) {
            table.put(key, ((DFunction) table.get(key)).addFunction(DFunction.newFunction(function, argSize, vararg)));
            return this;
        }
        table.put(key, DFunction.newFunction(function, argSize, vararg));
        return this;
    }

    public Env addTokenFunction(String key, BiFunction<List<Token>, Env, Token> function, int argSize, boolean vararg) {
        BiFunction<AstNode, Env, Token> df = (ast, env) -> {
            List<Token> args = new ArrayList<>();
            for (int i = 0; i < ast.size(); ++i) {
                ast.get(i).op = Evaluator.eval(env, ast.get(i).copy());
                args.add(ast.get(i).op);
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

    public Env setSymbolFunction(String key, BiFunction<AstNode, Env, Token> function, int argSize, boolean vararg) {
        Env temp = this;
        while (temp.father != null && !temp.table.containsKey(key))
            temp = temp.father;
        temp.table.put(key, DFunction.newFunction(function, argSize, vararg));
        return this;
    }

    public Env setTokenFunction(String key, BiFunction<List<Token>, Env, Token> function, int argSize, boolean vararg) {
        Env temp = this;
        while (temp.father != null && !temp.table.containsKey(key))
            temp = temp.father;
        BiFunction<AstNode, Env, Token> df = (ast, env) -> {
            List<Token> args = new ArrayList<>();
            for (int i = 0; i < ast.size(); ++i) {
                ast.get(i).op = Evaluator.eval(env, ast.get(i).copy());
                args.add(ast.get(i).op);
            }
            return function.apply(args, env);
        };
        temp.table.put(key, DFunction.newFunction(df, argSize, vararg));
        return this;
    }

    public Env set(String key, Token value) {
        Env temp = this;
        while (temp.father != null && !temp.table.containsKey(key))
            temp = temp.father;
        temp.table.put(key, value);
        return this;
    }

    public Env set(String key, BiFunction<AstNode, Env, Token> function, int argSize, boolean vararg) {
        Env temp = this;
        while (temp.father != null && !temp.table.containsKey(key))
            temp = temp.father;
        temp.table.put(key, DFunction.newFunction(function, argSize, vararg));
        return this;
    }

    public boolean contains(String key) {
        Env temp = this;
        while (temp.father != null && !temp.table.containsKey(key))
            temp = temp.father;
        return temp.table.containsKey(key);
    }

    public Token get(String key) {
        Env temp = this;
        while (temp.father != null && !temp.table.containsKey(key))
            temp = temp.father;
        return temp.contains(key) ? temp.table.get(key) : DWord.WORD_NIL;
    }

    public Token remove(String key) {
        Env temp = this;
        while (temp.father != null && !temp.table.containsKey(key))
            temp = temp.father;
        return temp.contains(key) ? temp.table.remove(key) : DWord.WORD_NIL;
    }

    public void clear() {
        this.table.clear();
    }

    public Env createChild() {
        return newEnv(this, io);
    }

    public Env load(String name) {
        Devore.moduleTable.get(name).init(this);
        return this;
    }
}
