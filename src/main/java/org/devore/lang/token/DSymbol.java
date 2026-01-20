package org.devore.lang.token;

/**
 * 符号
 */
public class DSymbol extends DToken {
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
        return this.symbol;
    }

    @Override
    public DToken copy() {
        return DSymbol.valueOf(this.symbol);
    }

    @Override
    public int compareTo(DToken t) {
        return t instanceof DSymbol ? this.symbol.compareTo(((DSymbol) t).symbol) : -1;
    }

    @Override
    public int hashCode() {
        int result = this.type().hashCode();
        result = 31 * result + this.symbol.hashCode();
        return result;
    }
}
