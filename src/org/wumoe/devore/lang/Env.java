package org.wumoe.devore.lang;

import org.wumoe.devore.lang.token.DFunction;
import org.wumoe.devore.lang.token.DWord;
import org.wumoe.devore.lang.token.Token;
import org.wumoe.devore.parse.AstNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public class Env {
    private final Map<String, Token> table;
    private final Env father;

    protected Env(Map<String, Token> table, Env father) {
        this.table = table;
        this.father = father;
    }

    public static Env newEnv(Map<String, Token> table, Env father) {
        return new Env(table, father);
    }

    public static Env newEnv(Map<String, Token> table) {
        return new Env(table, null);
    }

    public static Env newEnv(Env father) {
        return new Env(new HashMap<>(), father);
    }

    public static Env newEnv() {
        return new Env(new HashMap<>(), null);
    }

    public void put(String key, Token value) {
        table.put(key, value);
    }

    public void addFunction(String key, BiFunction<AstNode, Env, Token> function) {
        table.put(key, DFunction.newFunction(function));
    }

    public void addBuiltFunction(String key, BiFunction<List<Token>, Env, Token> function) {
        BiFunction<AstNode, Env, Token> tempF = (ast, env) -> {
            List<Token> args = new ArrayList<>();
            for (int i = 0; i < ast.size(); ++i) {
                ast.get(i).op = Evaluator.eval(env, ast.get(i).copy());
                args.add(ast.get(i).op);
            }
            return function.apply(args, env);
        };
        table.put(key, DFunction.newFunction(tempF));
    }

    public void setFunction(String key, BiFunction<AstNode, Env, Token> function) {
        Env temp = this;
        while (temp.father != null && !temp.table.containsKey(key))
            temp = temp.father;
        temp.table.put(key, DFunction.newFunction(function));
    }

    public void setBuiltFunction(String key, BiFunction<List<Token>, Env, Token> function) {
        Env temp = this;
        while (temp.father != null && !temp.table.containsKey(key))
            temp = temp.father;
        BiFunction<AstNode, Env, Token> tempF = (ast, env) -> {
            List<Token> args = new ArrayList<>();
            for (int i = 0; i < ast.size(); ++i) {
                ast.get(i).op = Evaluator.eval(env, ast.get(i).copy());
                args.add(ast.get(i).op);
            }
            return function.apply(args, env);
        };
        temp.table.put(key, DFunction.newFunction(tempF));
    }

    public void set(String key, Token value) {
        Env temp = this;
        while (temp.father != null && !temp.table.containsKey(key))
            temp = temp.father;
        temp.table.put(key, value);
    }

    public void set(String key, BiFunction<AstNode, Env, Token> function) {
        Env temp = this;
        while (temp.father != null && !temp.table.containsKey(key))
            temp = temp.father;
        temp.table.put(key, DFunction.newFunction(function));
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

    public Env createChild() {
        return newEnv(this);
    }
}
