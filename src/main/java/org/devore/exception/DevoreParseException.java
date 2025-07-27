package org.devore.exception;

/**
 * 解析错误
 */
public class DevoreParseException extends RuntimeException {
    public DevoreParseException(String e) {
        super(e);
    }
}
