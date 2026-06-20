package org.devore.exception;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 运行错误
 */
public class DevoreRuntimeException extends RuntimeException {
    private String expression;    // 出错表达式
    private int index = -1;       // 出错位置
    private String source;        // 代码来源
    private String code;          // 完整源码
    private final List<Frame> trace = new ArrayList<>();  // 调用链

    public DevoreRuntimeException(String e) {
        super(e);
    }

    public DevoreRuntimeException(String e, String expression) {
        super(e);
        this.expression = expression;
    }

    public DevoreRuntimeException(String e, String expression, int index) {
        super(e);
        this.expression = expression;
        this.index = index;
        addFrameIfAbsent(expression, index);
    }

    public DevoreRuntimeException(String e, String expression, int index, String source, String code) {
        super(e);
        this.expression = expression;
        this.index = index;
        this.source = source;
        this.code = code;
        addFrameIfAbsent(expression, index, source, code);
    }

    public void setExpressionIfAbsent(String expression, int index, String source, String code) {
        if (this.expression == null) {
            this.expression = expression;
            this.index = index;
            this.source = source;
            this.code = code;
        }
    }

    public String expression() {
        return this.expression;
    }

    public int index() {
        return this.index;
    }

    public String source() {
        return this.source;
    }

    public String code() {
        return this.code;
    }

    public void addFrameIfAbsent(String expression, int index) {
        addFrameIfAbsent(expression, index, null, null);
    }

    public void addFrameIfAbsent(String expression, int index, String source, String code) {
        if (expression == null || expression.isEmpty())
            return;
        if (!this.trace.isEmpty()) {
            Frame last = this.trace.get(this.trace.size() - 1);
            if (last.expression.equals(expression) && last.index == index && equals(last.source, source))
                return;
        }
        this.trace.add(new Frame(expression, index, source, code));
    }

    public List<Frame> trace() {
        return Collections.unmodifiableList(this.trace);
    }

    public static class Frame {
        public final String expression;
        public final int index;
        public final String source;
        public final String code;

        public Frame(String expression, int index, String source, String code) {
            this.expression = expression;
            this.index = index;
            this.source = source;
            this.code = code;
        }
    }

    private static boolean equals(String left, String right) {
        return Objects.equals(left, right);
    }
}
