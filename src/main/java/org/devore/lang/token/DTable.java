package org.devore.lang.token;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 表
 */
public class DTable extends Token {
    private final Map<Token, Token> table;

    private DTable(Map<Token, Token> table) {
        this.table = table;
    }

    public static DTable valueOf(Map<Token, Token> table) {
        return new DTable(table);
    }

    public DTable put(Token key, Token value, boolean force) {
        if (force) {
            table.put(key, value);
            return this;
        }
        Map<Token, Token> newTable = new HashMap<>(table);
        newTable.put(key, value);
        return DTable.valueOf(newTable);
    }

    public DBool containsKey(Token key) {
        return DBool.valueOf(table.containsKey(key));
    }

    public DBool containsValue(Token key) {
        return DBool.valueOf(table.containsValue(key));
    }

    public Token get(Token key) {
        return table.get(key);
    }

    public DTable remove(Token key, boolean force) {
        if (force) {
            table.remove(key);
            return this;
        }
        Map<Token, Token> newTable = new HashMap<>(table);
        newTable.remove(key);
        return DTable.valueOf(newTable);
    }

    public Set<Token> keys() {
        return table.keySet();
    }

    public int size() {
        return table.size();
    }

    @Override
    public String type() {
        return "table";
    }

    @Override
    protected String str() {
        return table.toString();
    }

    @Override
    public Token copy() {
        return DTable.valueOf(new HashMap<>(table));
    }

    @Override
    public int compareTo(Token t) {
        if (!(t instanceof DTable))
            return -1;
        DTable map = (DTable) t;
        if (map.size() != this.size())
            return -1;
        Object[] mapKeys = map.table.keySet().toArray();
        Object[] thisKeys = this.table.keySet().toArray();
        for (int i = 0; i < this.size(); ++i)
            if (!mapKeys[i].equals(thisKeys[i]))
                return -1;
        for (Object key : mapKeys)
            if (!map.get((Token) key).equals(this.table.get((Token) key)))
                return -1;
        return 0;
    }
}
