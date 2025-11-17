package org.devore.lang.token;

import java.util.ArrayList;
import java.util.List;

/**
 * 列表
 */
public class DList extends Token {
    private final List<Token> tokens;

    private DList(List<Token> tokens) {
        this.tokens = tokens;
    }

    public static DList valueOf(List<Token> tokens) {
        return new DList(tokens);
    }

    public void clear() {
        tokens.clear();
    }

    public DList sort(boolean force) {
        if (force) {
            tokens.sort(Token::compareTo);
            return this;
        }
        List<Token> newList = new ArrayList<>(tokens);
        newList.sort(Token::compareTo);
        return DList.valueOf(newList);
    }

    public DList add(Token t, boolean force) {
        if (force) {
            tokens.add(t);
            return this;
        }
        List<Token> newList = new ArrayList<>(tokens);
        newList.add(t);
        return DList.valueOf(newList);
    }

    public Token get(int index) {
        return tokens.get(index);
    }

    public Token getFirst() {
        return get(0);
    }

    public Token set(int index, Token t, boolean force) {
        if (force) {
            tokens.set(index, t);
            return this;
        }
        List<Token> newList = new ArrayList<>(tokens);
        newList.set(index, t);
        return DList.valueOf(newList);
    }

    public DList remove(int index, boolean force) {
        if (force) {
            tokens.remove(index);
            return this;
        }
        List<Token> newList = new ArrayList<>(tokens);
        newList.remove(index);
        return DList.valueOf(newList);
    }

    public DList remove(Token t, boolean force) {
        if (force) {
            tokens.remove(t);
            return this;
        }
        List<Token> newList = new ArrayList<>(tokens);
        newList.add(t);
        return DList.valueOf(newList);
    }

    public boolean contains(Token t) {
        return tokens.contains(t);
    }

    public int size() {
        return tokens.size();
    }

    public DList subList(int fromIndex, int toIndex) {
        return DList.valueOf(tokens.subList(fromIndex, toIndex));
    }

    public List<Token> toList() {
        return tokens;
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
        return DList.valueOf(new ArrayList<>(tokens));
    }

    @Override
    public int compareTo(Token t) {
        if (!(t instanceof DList list) || list.size() != this.size())
            return -1;
        for (int i = 0; i < this.size(); ++i)
            if (!list.get(i).equals(this.get(i)))
                return -1;
        return 0;
    }
}
