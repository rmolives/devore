package org.devore.exception;

/**
 * 转换错误
 */
public class DevoreCastException extends DevoreRuntimeException {
    public DevoreCastException(String t, String o) {
        super("无法将类型 [" + t + "] 转换为 [" + o + "].");
    }
}
