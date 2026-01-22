package org.devore.lang.token;

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
        return this.bool ? "true" : "false";
    }

    @Override
    public int compareTo(DToken t) {
        return t instanceof DBool && this.bool == ((DBool) t).bool ? 0 : -1;
    }

    @Override
    public int hashCode() {
        int result = this.type().hashCode();
        result = 31 * result + Boolean.hashCode(this.bool);
        return result;
    }
}
