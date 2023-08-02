package org.wumoe.devore.lang.token;

public abstract class DList extends Token {
    public abstract DList add(Token t);
    public abstract Token get(int index);
    public abstract Token set(int index, Token t);
    public abstract DList remove(int index);
    public abstract DList remove(Token t);

    public abstract int size();

    public abstract DList subList(int fromIndex, int toIndex);
}
