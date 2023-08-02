package org.wumoe.devore.lang.token;

import java.util.ArrayList;
import java.util.List;

public class DVariableList extends DList {
    private final List<Token> tokens;

    private DVariableList(List<Token> tokens) {
        this.tokens = tokens;
    }

    public static DVariableList valueOf(List<Token> tokens) {
        return new DVariableList(tokens);
    }

    public static DVariableList newEmptyImmutableList() {
        return valueOf(new ArrayList<>());
    }

    @Override
    public DList add(Token t) {
        tokens.add(t);
        return this;
    }

    @Override
    public Token get(int index) {
        return tokens.get(index);
    }

    @Override
    public Token set(int index, Token t) {
        tokens.set(index, t);
        return this;
    }

    @Override
    public DList remove(int index) {
        tokens.remove(index);
        return this;
    }

    @Override
    public DList remove(Token t) {
        tokens.remove(t);
        return this;
    }

    @Override
    public int size() {
        return tokens.size();
    }

    @Override
    public DList subList(int fromIndex, int toIndex) {
        return DVariableList.valueOf(tokens.subList(fromIndex, toIndex));
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
        DVariableList newList = newEmptyImmutableList();
        for (Token t : tokens)
            newList.add(t);
        return newList;
    }

    @Override
    public int compareTo(Token t) {
        return 0;
    }
}
