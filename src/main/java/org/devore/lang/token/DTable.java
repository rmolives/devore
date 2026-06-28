package org.devore.lang.token;

import org.devore.utils.FormatUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 表
 */
public class DTable extends DToken {
    private final Map<DToken, DToken> table;    // 表

    private DTable(Map<DToken, DToken> table) {
        this.table = table;
    }

    public static DTable valueOf(Map<DToken, DToken> table) {
        return new DTable(Collections.synchronizedMap(new HashMap<>(table)));
    }

    /**
     * 清空表
     */
    public void clear() {
        synchronized (this.table) {
            this.table.clear();
        }
    }

    /**
     * 设置KY对
     *
     * @param key   key
     * @param value value
     * @param force 是否更改原表
     * @return 结果
     */
    public DTable put(DToken key, DToken value, boolean force) {
        if (force) {
            synchronized (this.table) {
                this.table.put(key, value);
            }
            return this;
        }
        Map<DToken, DToken> newTable = snapshot();
        newTable.put(key, value);
        return DTable.valueOf(newTable);
    }

    /**
     * 是否包含特定key
     *
     * @param key key
     * @return 结果
     */
    public DBool containsKey(DToken key) {
        synchronized (this.table) {
            return DBool.valueOf(this.table.containsKey(key));
        }
    }

    /**
     * 是否包含特定value
     *
     * @param value value
     * @return 结果
     */
    public DBool containsValue(DToken value) {
        synchronized (this.table) {
            return DBool.valueOf(this.table.containsValue(value));
        }
    }

    /**
     * 获取key对应的value
     *
     * @param key key
     * @return 结果
     */
    public DToken get(DToken key) {
        synchronized (this.table) {
            return this.table.getOrDefault(key, DWord.NIL);
        }
    }

    /**
     * 删除key
     *
     * @param key   key
     * @param force 是否更改原表
     * @return 结果
     */
    public DTable remove(DToken key, boolean force) {
        if (force) {
            synchronized (this.table) {
                this.table.remove(key);
            }
            return this;
        }
        Map<DToken, DToken> newTable = snapshot();
        newTable.remove(key);
        return DTable.valueOf(newTable);
    }

    /**
     * 获取所有key
     *
     * @return 结果
     */
    public Set<DToken> keys() {
        synchronized (this.table) {
            return new HashSet<>(this.table.keySet());
        }
    }

    /**
     * 转换为Java的Map
     *
     * @return 结果
     */
    public Map<DToken, DToken> toMap() {
        return snapshot();
    }

    /**
     * 获取数量
     *
     * @return 数量
     */
    public int size() {
        synchronized (this.table) {
            return this.table.size();
        }
    }

    private Map<DToken, DToken> snapshot() {
        synchronized (this.table) {
            return new HashMap<>(this.table);
        }
    }

    @Override
    public String type() {
        return "table";
    }

    @Override
    protected String str() {
        return snapshot().entrySet().stream()
                .map(entry -> FormatUtils.formatToken(entry.getKey())
                        + "=" + FormatUtils.formatToken(entry.getValue()))
                .collect(Collectors.joining(", ", "{", "}"));
    }

    @Override
    public int compareTo(DToken t) {
        if (!(t instanceof DTable))
            return -1;
        DTable other = (DTable) t;
        Map<DToken, DToken> thisSnapshot = snapshot();
        Map<DToken, DToken> otherSnapshot = other.snapshot();
        if (thisSnapshot.size() != otherSnapshot.size())
            return -1;
        return thisSnapshot.entrySet().stream()
                .allMatch(entry -> otherSnapshot.containsKey(entry.getKey())
                        && Objects.equals(entry.getValue(), otherSnapshot.get(entry.getKey()))) ? 0 : -1;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.type(), snapshot());
    }
}
