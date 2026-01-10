package org.devore.lang.token;

/**
 * 关键字
 */
public class DSymbol extends DString {
    protected DSymbol(String symbol) {
        super(symbol);
    }

    public static DSymbol valueOf(String symbol) {
        return new DSymbol(symbol);
    }

    @Override
    public String type() {
        return "symbol";
    }
}
