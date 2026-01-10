package org.devore.lang.token;

/**
 * 关键字
 */
public class DSymbol extends DString {
    protected DSymbol(String op) {
        super(op);
    }

    public static DSymbol valueOf(String op) {
        return new DSymbol(op);
    }

    @Override
    public String type() {
        return "symbol";
    }
}
