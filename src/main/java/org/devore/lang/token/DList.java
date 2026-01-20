package org.devore.lang.token;

import java.util.ArrayList;
import java.util.Collections;
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

    /**
     * 清空列表
     */
    public void clear() {
        this.tokens.clear();
    }

    /**
     * 排序
     * @param force 是否更改原列表
     * @return 结果
     */
    public DList sort(boolean force) {
        if (force) {
            this.tokens.sort(Token::compareTo);
            return this;
        }
        List<Token> newList = new ArrayList<>(this.tokens);
        newList.sort(Token::compareTo);
        return DList.valueOf(newList);
    }

    /**
     * 颠倒
     * @param force 是否更改原列表
     * @return 结果
     */
    public DList reverse(boolean force) {
        if (force) {
            Collections.reverse(this.tokens);
            return this;
        }
        List<Token> newList = new ArrayList<>(this.tokens);
        Collections.reverse(newList);
        return DList.valueOf(newList);
    }

    /**
     * 添加元素
     * @param t     元素
     * @param force 是否更改原列表
     * @return 结果
     */
    public DList add(Token t, boolean force) {
        if (force) {
            this.tokens.add(t);
            return this;
        }
        List<Token> newList = new ArrayList<>(this.tokens);
        newList.add(t);
        return DList.valueOf(newList);
    }

    /**
     * 添加元素
     * @param index 位置
     * @param t     元素
     * @param force 是否更改原列表
     * @return 结果
     */
    public DList add(int index, Token t, boolean force) {
        if (force) {
            this.tokens.add(index, t);
            return this;
        }
        List<Token> newList = new ArrayList<>(this.tokens);
        newList.add(index, t);
        return DList.valueOf(newList);
    }

    /**
     * 获取元素
     * @param index 位置
     * @return 元素
     */
    public Token get(int index) {
        return this.tokens.get(index);
    }

    /**
     * 设置元素
     * @param index 位置
     * @param t     元素
     * @param force 是否更改原列表
     * @return 结果
     */
    public Token set(int index, Token t, boolean force) {
        if (force) {
            this.tokens.set(index, t);
            return this;
        }
        List<Token> newList = new ArrayList<>(this.tokens);
        newList.set(index, t);
        return DList.valueOf(newList);
    }

    /**
     * 删除元素
     * @param index 位置
     * @param force 是否更改原列表
     * @return 结果
     */
    public DList remove(int index, boolean force) {
        if (force) {
            this.tokens.remove(index);
            return this;
        }
        List<Token> newList = new ArrayList<>(this.tokens);
        newList.remove(index);
        return DList.valueOf(newList);
    }

    /**
     * 获取元素首次出现的位置
     * @param t 元素
     * @return 位置
     */
    public int indexOf(Token t) {
        return this.tokens.indexOf(t);
    }

    /**
     * 获取元素最后出现的位置
     * @param t 元素
     * @return 位置
     */
    public int lastIndexOf(Token t) {
        return this.tokens.lastIndexOf(t);
    }

    /**
     * 是否包含特定元素
     * @param t 元素
     * @return 结果
     */
    public boolean contains(Token t) {
        return this.tokens.contains(t);
    }

    /**
     * 获取数量
     * @return 数量
     */
    public int size() {
        return this.tokens.size();
    }

    /**
     * 截取
     * @param fromIndex 起始位置
     * @param toIndex   结束位置
     * @param force     是否更改原列表
     * @return 截取后的列表
     */
    public DList subList(int fromIndex, int toIndex, boolean force) {
        if (force) {
            List<Token> view = this.tokens.subList(fromIndex, toIndex);
            this.tokens.clear();
            this.tokens.addAll(view);
            return this;
        }
        return DList.valueOf(new ArrayList<>(this.tokens.subList(fromIndex, toIndex)));
    }

    /**
     * 转换为Java的List
     * @return 结果
     */
    public List<Token> toList() {
        return this.tokens;
    }

    @Override
    public String type() {
        return "list";
    }

    @Override
    protected String str() {
        return this.tokens.toString();
    }

    @Override
    public Token copy() {
        return DList.valueOf(new ArrayList<>(this.tokens));
    }

    @Override
    public int compareTo(Token t) {
        if (!(t instanceof DList)) return -1;
        DList other = (DList) t;
        if (other.size() != this.size()) return -1;
        for (int i = 0; i < this.size(); ++i)
            if (!other.get(i).equals(this.get(i))) return -1;
        return 0;
    }

    @Override
    public int hashCode() {
        int result = this.type().hashCode();
        result = 31 * result + this.tokens.hashCode();
        return result;
    }
}
