package org.devore.lang;

import java.io.InputStream;
import java.io.PrintStream;

/**
 * IO表
 *
 * @param out 输出流
 * @param in  输入流
 */
public record IOConfig(PrintStream out, InputStream in) {
    /**
     * 创建IO表
     */
    public IOConfig() {
        this(System.out, System.in);
    }
}
