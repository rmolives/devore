package org.devore.lang.token;

import java.util.Objects;

/**
 * 布尔值
 */
public class DBool extends DToken {
    public static final DBool FALSE = new DBool(false);     // False
    public static final DBool TRUE = new DBool(true);       // True
    public final boolean bool;                              // 布尔值

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
        return Boolean.toString(this.bool);
    }

    @Override
    public int compareTo(DToken t) {
        return t instanceof DBool && this.bool == ((DBool) t).bool ? 0 : -1;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.type(), this.bool);
    }
}
