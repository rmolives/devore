package org.devore.lang;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

/**
 * IO表
 */
public class IOConfig {
    public final InputStream in;    // 输入流
    public final PrintStream out;   // 输出流
    public final PrintStream err;   // 异常流
    public final Scanner scanner;   // 输入读取器
    private final LineState lineState;

    /**
     * 创建IO表
     *
     * @param in  输入流
     * @param out 输出流
     * @param err 错误流
     */
    public IOConfig(InputStream in, PrintStream out, PrintStream err) {
        this.lineState = new LineState();
        this.in = in;
        this.out = wrapOutput(out);
        this.err = wrapOutput(err);
        this.scanner = new Scanner(in);
    }

    public void resetLineState() {
        lineState.reset();
    }

    public boolean isAtLineStart() {
        return lineState.isAtLineStart();
    }

    private PrintStream wrapOutput(PrintStream stream) {
        if (stream instanceof LineTrackingPrintStream)
            return stream;
        return new LineTrackingPrintStream(stream, lineState);
    }

    private static class LineState {
        private boolean atLineStart = true;

        private synchronized void reset() {
            this.atLineStart = true;
        }

        private synchronized boolean isAtLineStart() {
            return this.atLineStart;
        }

        private synchronized void record(int b) {
            this.atLineStart = b == '\n';
        }

        private synchronized void record(byte[] buf, int off, int len) {
            for (int index = off; index < off + len; ++index)
                this.atLineStart = buf[index] == '\n';
        }
    }

    private static class LineTrackingPrintStream extends PrintStream {
        private final LineState lineState;

        private LineTrackingPrintStream(PrintStream stream, LineState lineState) {
            super(stream, false);
            this.lineState = lineState;
        }

        @Override
        public void write(int b) {
            super.write(b);
            this.lineState.record(b);
        }

        @Override
        public void write(byte[] buf, int off, int len) {
            super.write(buf, off, len);
            this.lineState.record(buf, off, len);
        }
    }

    /**
     * 创建默认IO表
     */
    public IOConfig() {
        this(System.in, System.out, System.err);
    }
}
