package org.wumoe.devore.lang.token;

public abstract class Token {
    public abstract String type();
    public abstract String str();
    public abstract Token copy();

    @Override
    public String toString() {
        return str();
    }
}
