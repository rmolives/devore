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

    /**
     * 清空表
     */
    public void clear() {
        this.table.clear();
    }

    /**
     * 设置KY对
     * @param key   key
     * @param value value
     * @param force 是否更改原表
     * @return 结果
     */
    public DTable put(Token key, Token value, boolean force) {
        if (force) {
            this.table.put(key, value);
            return this;
        }
        Map<Token, Token> newTable = new HashMap<>(this.table);
        newTable.put(key, value);
        return DTable.valueOf(newTable);
    }

    /**
     * 是否包含特定key
     * @param key   key
     * @return 结果
     */
    public DBool containsKey(Token key) {
        return DBool.valueOf(this.table.containsKey(key));
    }

    /**
     * 是否包含特定value
     * @param value value
     * @return 结果
     */
    public DBool containsValue(Token value) {
        return DBool.valueOf(this.table.containsValue(value));
    }

    /**
     * 获取key对应的value
     * @param key   key
     * @return 结果
     */
    public Token get(Token key) {
        return this.table.get(key);
    }

    /**
     * 删除key
     * @param key   key
     * @param force 是否更改原表
     * @return 结果
     */
    public DTable remove(Token key, boolean force) {
        if (force) {
            this.table.remove(key);
            return this;
        }
        Map<Token, Token> newTable = new HashMap<>(this.table);
        newTable.remove(key);
        return DTable.valueOf(newTable);
    }

    /**
     * 获取所有key
     * @return 结果
     */
    public Set<Token> keys() {
        return this.table.keySet();
    }

    /**
     * 获取数量
     * @return 数量
     */
    public int size() {
        return this.table.size();
    }

    @Override
    public String type() {
        return "table";
    }

    @Override
    protected String str() {
        return this.table.toString();
    }

    @Override
    public Token copy() {
        return DTable.valueOf(new HashMap<>(this.table));
    }

    @Override
    public int compareTo(Token t) {
        if (!(t instanceof DTable)) return -1;
        DTable other = (DTable) t;
        return this.table.equals(other.table) ? 0 : -1;
    }

    @Override
    public int hashCode() {
        int result = this.type().hashCode();
        result = 31 * result + this.table.hashCode();
        return result;
    }
}
