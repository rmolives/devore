package org.devore.exception;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 运行错误
 */
public class DevoreRuntimeException extends RuntimeException {
    private String expression;    // 出错表达式
    private int index = -1;       // 出错位置
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

    public void setExpressionIfAbsent(String expression, int index) {
        if (this.expression == null) {
            this.expression = expression;
            this.index = index;
        }
    }

    public String expression() {
        return this.expression;
    }

    public int index() {
        return this.index;
    }

    public void addFrameIfAbsent(String expression, int index) {
        if (expression == null || expression.isEmpty())
            return;
        if (!this.trace.isEmpty()) {
            Frame last = this.trace.get(this.trace.size() - 1);
            if (last.expression.equals(expression) && last.index == index)
                return;
        }
        this.trace.add(new Frame(expression, index));
    }

    public List<Frame> trace() {
        return Collections.unmodifiableList(this.trace);
    }

    public static class Frame {
        public final String expression;
        public final int index;

        public Frame(String expression, int index) {
            this.expression = expression;
            this.index = index;
        }
    }
}
