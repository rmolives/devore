package org.devore.lang.token;

/**
 * 布尔值
 */
public class DBool extends Token {
    public static final DBool FALSE = new DBool(false);
    public static final DBool TRUE = new DBool(true);
    public final boolean bool;

    protected DBool(boolean bool) {
        this.bool = bool;
    }

    public static DBool valueOf(boolean bool) {
        return bool ? TRUE : FALSE;
    }

    @Override
    public String type() {
        return "bool";
    }

    @Override
    protected String str() {
        return bool ? "true" : "false";
    }

    @Override
    public Token copy() {
        return DBool.valueOf(bool);
    }

    @Override
    public int compareTo(Token t) {
        return t instanceof DBool b && b.bool == this.bool ? 0 : -1;
    }
}
