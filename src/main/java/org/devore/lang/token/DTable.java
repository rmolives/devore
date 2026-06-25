package org.devore.lang.token;

import org.devore.utils.FormatUtils;

import java.util.HashMap;
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
     *
     * @param key   key
     * @param value value
     * @param force 是否更改原表
     * @return 结果
     */
    public DTable put(DToken key, DToken value, boolean force) {
        if (force) {
            this.table.put(key, value);
            return this;
        }
        Map<DToken, DToken> newTable = new HashMap<>(this.table);
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
        return DBool.valueOf(this.table.containsKey(key));
    }

    /**
     * 是否包含特定value
     *
     * @param value value
     * @return 结果
     */
    public DBool containsValue(DToken value) {
        return DBool.valueOf(this.table.containsValue(value));
    }

    /**
     * 获取key对应的value
     *
     * @param key key
     * @return 结果
     */
    public DToken get(DToken key) {
        return this.table.getOrDefault(key, DWord.NIL);
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
            this.table.remove(key);
            return this;
        }
        Map<DToken, DToken> newTable = new HashMap<>(this.table);
        newTable.remove(key);
        return DTable.valueOf(newTable);
    }

    /**
     * 获取所有key
     *
     * @return 结果
     */
    public Set<DToken> keys() {
        return this.table.keySet();
    }

    /**
     * 获取数量
     *
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
        return this.table.entrySet().stream()
                .map(entry -> FormatUtils.formatToken(entry.getKey())
                        + "=" + FormatUtils.formatToken(entry.getValue()))
                .collect(Collectors.joining(", ", "{", "}"));
    }

    @Override
    public int compareTo(DToken t) {
        if (!(t instanceof DTable))
            return -1;
        DTable other = (DTable) t;
        if (this.table.size() != other.table.size())
            return -1;
        return this.table.entrySet().stream()
                .allMatch(entry -> other.table.containsKey(entry.getKey())
                        && Objects.equals(entry.getValue(), other.table.get(entry.getKey()))) ? 0 : -1;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.type(), this.table);
    }
}
