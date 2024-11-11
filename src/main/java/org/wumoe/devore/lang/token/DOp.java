package org.wumoe.devore.lang.token;

public class DOp extends DString {
    protected DOp(String op) {
        super(op);
    }

    public static DOp valueOf(String op) {
        return new DOp(op);
    }

    @Override
    public String type() {
        return "op";
    }
}
