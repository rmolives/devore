package org.wumoe.devore.lang.token;

public class DOp extends DString {
    private DOp(String op) {
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
