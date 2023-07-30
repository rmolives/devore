package org.wumoe.devore.lang;

import org.wumoe.devore.lang.token.DWord;
import org.wumoe.devore.lang.token.Token;

import java.util.HashMap;
import java.util.Map;

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

    public void set(String key, Token value) {
        Env temp = this;
        while (temp.father != null && !temp.table.containsKey(key))
            temp = temp.father;
        temp.table.put(key, value);
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
        return temp.contains(key) ? temp.get(key) : DWord.WORD_NIL;
    }

    public Env createChild() {
        return newEnv(this);
    }
}
