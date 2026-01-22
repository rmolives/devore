package org.devore.lang.token;

/**
 * Token
 */
public abstract class DToken {
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
    public abstract DToken copy();

    /**
     * token比较
     *
     * @param t token
     * @return 结果
     */
    public abstract int compareTo(DToken t);

    /**
     * 获取当前token的hash
     *
     * @return hash
     */
    @Override
    public abstract int hashCode();

    /**
     * 判断token是否相等
     *
     * @param obj token
     * @return 结果
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof DToken && this.compareTo((DToken) obj) == 0;
    }

    /**
     * 将当前token转为字符串
     *
     * @return 字符串
     */
    @Override
    public String toString() {
        return this.str();
    }
}
