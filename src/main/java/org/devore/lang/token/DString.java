package org.devore.lang.token;

/**
 * 字符串
 */
public class DString extends Token {
    public final String str;

    protected DString(String str) {
        this.str = str;
    }

    public static DString valueOf(String str) {
        return new DString(str);
    }

    @Override
    public String type() {
        return "string";
    }

    @Override
    protected String str() {
        return str;
    }

    @Override
    public Token copy() {
        return DString.valueOf(str);
    }

    @Override
    public int compareTo(Token t) {
        return t instanceof DString ? str.compareTo(((DString) t).str) : -1;
    }
}
