package org.devore.lang.token;

/**
 * 符号
 */
public class DSymbol extends Token {
    public final String symbol;

    protected DSymbol(String symbol) {
        this.symbol = symbol;
    }

    public static DSymbol valueOf(String symbol) {
        return new DSymbol(symbol);
    }

    @Override
    public String type() {
        return "symbol";
    }

    @Override
    protected String str() {
        return symbol;
    }

    @Override
    public Token copy() {
        return DSymbol.valueOf(symbol);
    }

    @Override
    public int compareTo(Token t) {
        return symbol.compareTo(t.toString());
    }
}
