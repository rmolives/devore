package org.devore.lang;

import java.io.InputStream;
import java.io.PrintStream;

/**
 * IO表
 */
public final class IOConfig {
    public final InputStream in;    // 输入流
    public final PrintStream out;   // 输出流

    /**
     * 创建IO表
     *
     * @param out 输出流
     * @param in  输入流
     */
    public IOConfig(InputStream in, PrintStream out) {
        this.in = in;
        this.out = out;
    }

    /**
     * 创建默认IO表
     */
    public IOConfig() {
        this(System.in, System.out);
    }
}
