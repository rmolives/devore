package org.wumoe.devore.lang.token;

public class DBool extends Token {
    public final boolean bool;

    protected DBool(boolean bool) {
        this.bool = bool;
    }

    public static DBool valueOf(boolean str) {
        return new DBool(str);
    }

    @Override
    public String type() {
        return "bool";
    }

    @Override
    public String str() {
        return bool ? "true" : "false";
    }

    @Override
    public Token copy() {
        return DBool.valueOf(bool);
    }

    @Override
    public int compareTo(Token t) {
        return -1;
    }
}
