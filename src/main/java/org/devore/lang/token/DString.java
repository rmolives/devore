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
        return this.str;
    }

    @Override
    public Token copy() {
        return DString.valueOf(this.str);
    }

    @Override
    public int compareTo(Token t) {
        return t instanceof DString ? this.str.compareTo(((DString) t).str) : -1;
    }

    @Override
    public int hashCode() {
        int result = this.type().hashCode();
        result = 31 * result + this.str.hashCode();
        return result;
    }
}
