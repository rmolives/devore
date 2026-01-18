package org.devore.lang;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Objects;


public final class IOConfig {
    private final PrintStream out;
    private final InputStream in;

    /**
     * IO表
     *
     * @param out 输出流
     * @param in  输入流
     */
    public IOConfig(PrintStream out, InputStream in) {
        this.out = out;
        this.in = in;
    }

    /**
     * 创建默认IO表（等价于 record 的紧凑构造器）
     */
    public IOConfig() {
        this(System.out, System.in);
    }

    public PrintStream out() {
        return out;
    }

    public InputStream in() {
        return in;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IOConfig)) return false;
        IOConfig ioConfig = (IOConfig) o;
        return Objects.equals(out, ioConfig.out) &&
                Objects.equals(in, ioConfig.in);
    }

    @Override
    public int hashCode() {
        return Objects.hash(out, in);
    }

    @Override
    public String toString() {
        return "IOConfig[out=" + out + ", in=" + in + "]";
    }
}
