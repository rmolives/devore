package org.devore.exception;

/**
 * 解析错误
 */
public class DevoreParseException extends RuntimeException {
    private final int index;   // 错误位置

    public DevoreParseException(String e) {
        this(e, -1);
    }

    public DevoreParseException(String e, int index) {
        super(e);
        this.index = index;
    }

    public int index() {
        return this.index;
    }
}
