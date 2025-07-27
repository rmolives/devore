package org.devore.lang;

import java.io.InputStream;
import java.io.PrintStream;

/**
 * IO表
 */
public class IOConfig {
    public final PrintStream out;   // 输出流
    public final PrintStream err;   // 错误流
    public final InputStream in;    // 输入流

    /**
     * 创建IO表
     * @param out   输出流
     * @param err   错误流
     * @param in    输入流
     */
    public IOConfig(PrintStream out, PrintStream err, InputStream in) {
        this.out = out;
        this.err = err;
        this.in = in;
    }

    /**
     * 创建IO表
     */
    public IOConfig() {
        this.out = System.out;
        this.err = System.err;
        this.in = System.in;
    }
}
