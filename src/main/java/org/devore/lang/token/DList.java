package org.devore.lang.token;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 列表
 */
public class DList extends DToken {
    private final List<DToken> list;    // 列表

    private DList(List<DToken> list) {
        this.list = list;
    }

    public static DList valueOf(List<DToken> list) {
        return new DList(list);
    }

    /**
     * 清空列表
     */
    public void clear() {
        this.list.clear();
    }

    /**
     * 排序
     *
     * @param force 是否更改原列表
     * @return 结果
     */
    public DList sort(boolean force) {
        if (force) {
            this.list.sort(DToken::compareTo);
            return this;
        }
        List<DToken> newList = new ArrayList<>(this.list);
        newList.sort(DToken::compareTo);
        return DList.valueOf(newList);
    }

    /**
     * 颠倒
     *
     * @param force 是否更改原列表
     * @return 结果
     */
    public DList reverse(boolean force) {
        if (force) {
            Collections.reverse(this.list);
            return this;
        }
        List<DToken> newList = new ArrayList<>(this.list);
        Collections.reverse(newList);
        return DList.valueOf(newList);
    }

    /**
     * 添加元素
     *
     * @param t     元素
     * @param force 是否更改原列表
     * @return 结果
     */
    public DList add(DToken t, boolean force) {
        if (force) {
            this.list.add(t);
            return this;
        }
        List<DToken> newList = new ArrayList<>(this.list);
        newList.add(t);
        return DList.valueOf(newList);
    }

    /**
     * 添加元素
     *
     * @param index 位置
     * @param t     元素
     * @param force 是否更改原列表
     * @return 结果
     */
    public DList add(int index, DToken t, boolean force) {
        if (force) {
            this.list.add(index, t);
            return this;
        }
        List<DToken> newList = new ArrayList<>(this.list);
        newList.add(index, t);
        return DList.valueOf(newList);
    }

    /**
     * 获取元素
     *
     * @param index 位置
     * @return 元素
     */
    public DToken get(int index) {
        return this.list.get(index);
    }

    /**
     * 设置元素
     *
     * @param index 位置
     * @param t     元素
     * @param force 是否更改原列表
     * @return 结果
     */
    public DToken set(int index, DToken t, boolean force) {
        if (force) {
            this.list.set(index, t);
            return this;
        }
        List<DToken> newList = new ArrayList<>(this.list);
        newList.set(index, t);
        return DList.valueOf(newList);
    }

    /**
     * 删除元素
     *
     * @param index 位置
     * @param force 是否更改原列表
     * @return 结果
     */
    public DList remove(int index, boolean force) {
        if (force) {
            this.list.remove(index);
            return this;
        }
        List<DToken> newList = new ArrayList<>(this.list);
        newList.remove(index);
        return DList.valueOf(newList);
    }

    /**
     * 获取元素首次出现的位置
     *
     * @param t 元素
     * @return 位置
     */
    public int indexOf(DToken t) {
        return this.list.indexOf(t);
    }

    /**
     * 获取元素最后出现的位置
     *
     * @param t 元素
     * @return 位置
     */
    public int lastIndexOf(DToken t) {
        return this.list.lastIndexOf(t);
    }

    /**
     * 是否包含特定元素
     *
     * @param t 元素
     * @return 结果
     */
    public boolean contains(DToken t) {
        return this.list.contains(t);
    }

    /**
     * 获取数量
     *
     * @return 数量
     */
    public int size() {
        return this.list.size();
    }

    /**
     * 截取
     *
     * @param fromIndex 起始位置
     * @param toIndex   结束位置
     * @param force     是否更改原列表
     * @return 截取后的列表
     */
    public DList subList(int fromIndex, int toIndex, boolean force) {
        if (force) {
            List<DToken> view = this.list.subList(fromIndex, toIndex);
            this.list.clear();
            this.list.addAll(view);
            return this;
        }
        return DList.valueOf(new ArrayList<>(this.list.subList(fromIndex, toIndex)));
    }

    /**
     * 转换为Java的List
     *
     * @return 结果
     */
    public List<DToken> toList() {
        return this.list;
    }

    @Override
    public String type() {
        return "list";
    }

    @Override
    protected String str() {
        return this.list.toString();
    }

    @Override
    public int compareTo(DToken t) {
        if (!(t instanceof DList))
            return -1;
        DList other = (DList) t;
        if (other.size() != this.size())
            return -1;
        for (int i = 0; i < this.size(); ++i)
            if (!other.get(i).equals(this.get(i)))
                return -1;
        return 0;
    }

    @Override
    public int hashCode() {
        int result = this.type().hashCode();
        result = 31 * result + this.list.hashCode();
        return result;
    }
}
