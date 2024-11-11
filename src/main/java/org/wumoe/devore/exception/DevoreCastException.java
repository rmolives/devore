package org.wumoe.devore.exception;

public class DevoreCastException extends DevoreRuntimeException {
    public DevoreCastException(String t, String o) {
        super("无法将类型 [" + t + "] 转换为 [" + o + "].");
    }
}
