package org.devore.lang.token;

/**
 * Token
 */
public abstract class Token {
    /**
     * 获取当前token类型
     *
     * @return 类型
     */
    public abstract String type();

    /**
     * 将当前token转为字符串
     *
     * @return 字符串
     */
    protected abstract String str();

    /**
     * 复制当前token
     *
     * @return token
     */
    public abstract Token copy();

    /**
     * token比较
     *
     * @param t token
     * @return 结果
     */
    public abstract int compareTo(Token t);

    /**
     * 判断token是否相等
     *
     * @param obj token
     * @return 结果
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof Token && this.compareTo((Token) obj) == 0;
    }

    /**
     * 将当前token转为字符串
     *
     * @return 字符串
     */
    @Override
    public String toString() {
        return str();
    }
}
