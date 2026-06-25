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

    /**
     * 创建IO表
     *
     * @param in  输入流
     * @param out 输出流
     * @param err 错误流
     */
    public IOConfig(InputStream in, PrintStream out, PrintStream err) {
        this.in = in;
        this.out = out;
        this.err = err;
        this.scanner = new Scanner(in);
    }

    /**
     * 创建默认IO表
     */
    public IOConfig() {
        this(System.in, System.out, System.err);
    }
}
