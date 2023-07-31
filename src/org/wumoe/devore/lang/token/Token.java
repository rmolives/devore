package org.wumoe.devore.lang.token;

public abstract class Token {
    public abstract String type();

    public abstract String str();

    public abstract Token copy();

    public abstract int compareTo(Token t);

    @Override
    public String toString() {
        return str();
    }
}
