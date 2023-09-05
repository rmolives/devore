package org.wumoe.devore.lang.token;

public abstract class Token {
    public abstract String type();

    public abstract String str();

    public abstract Token copy();

    public abstract int compareTo(Token t);

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Token t && this.compareTo(t) == 0;
    }

    @Override
    public String toString() {
        return str();
    }

    @Override
    public int hashCode() {
        return (type() + str()).hashCode();
    }
}
