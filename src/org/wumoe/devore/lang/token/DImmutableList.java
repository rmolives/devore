package org.wumoe.devore.lang.token;

import java.util.ArrayList;
import java.util.List;

public class DImmutableList extends DList {
    private final List<Token> tokens;

    private DImmutableList(List<Token> tokens) {
        this.tokens = tokens;
    }

    public static DImmutableList valueOf(List<Token> tokens) {
        return new DImmutableList(tokens);
    }

    public static DImmutableList newEmptyImmutableList() {
        return valueOf(new ArrayList<>());
    }

    @Override
    public DList add(Token t) {
        DImmutableList newList = (DImmutableList) copy();
        newList.add(t);
        return newList;
    }

    @Override
    public Token get(int index) {
        return tokens.get(index);
    }

    @Override
    public Token set(int index, Token t) {
        DImmutableList newList = (DImmutableList) copy();
        newList.set(index, t);
        return newList;
    }

    @Override
    public DList remove(int index) {
        DImmutableList newList = (DImmutableList) copy();
        newList.remove(index);
        return newList;
    }

    @Override
    public DList remove(Token t) {
        DImmutableList newList = (DImmutableList) copy();
        newList.remove(t);
        return newList;
    }

    @Override
    public int size() {
        return tokens.size();
    }

    @Override
    public DList subList(int fromIndex, int toIndex) {
        return DImmutableList.valueOf(tokens.subList(fromIndex, toIndex));
    }

    @Override
    public String type() {
        return "list";
    }

    @Override
    public String str() {
        return tokens.toString();
    }

    @Override
    public Token copy() {
        DImmutableList newList = newEmptyImmutableList();
        for (Token t : tokens)
            newList.add(t);
        return newList;
    }

    @Override
    public int compareTo(Token t) {
        return 0;
    }
}
