package org.devore.lang.token;

import java.util.Objects;

/**
 * 字符串
 */
public class DString extends DToken {
    private final String str;    // 字符串

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
    public int compareTo(DToken t) {
        return t instanceof DString ? this.str.compareTo(((DString) t).str) : -1;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.type(), this.str);
    }
}
