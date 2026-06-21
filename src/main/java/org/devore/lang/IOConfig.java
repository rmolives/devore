package org.devore.lang;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.NoSuchElementException;

/**
 * IO表
 */
public class IOConfig {
    public final InputStream in;    // 输入流
    public final PrintStream out;   // 输出流
    public final PrintStream err;   // 异常流
    public final BufferedReader reader;   // 输入读取器
    private final Deque<String> tokens;   // 输入token缓存

    /**
     * 创建IO表
     *
     * @param out 输出流
     * @param in  输入流
     */
    public IOConfig(InputStream in, PrintStream out, PrintStream err) {
        this.in = in;
        this.out = out;
        this.err = err;
        this.reader = new BufferedReader(new InputStreamReader(in));
        this.tokens = new ArrayDeque<>();
    }

    /**
     * 创建默认IO表
     */
    public IOConfig() {
        this(System.in, System.out, System.err);
    }

    public String readLine() {
        if (!this.tokens.isEmpty()) {
            StringBuilder builder = new StringBuilder();
            while (!this.tokens.isEmpty()) {
                if (builder.length() > 0)
                    builder.append(" ");
                builder.append(this.tokens.removeFirst());
            }
            return builder.toString();
        }
        try {
            String line = this.reader.readLine();
            if (line == null)
                throw new NoSuchElementException();
            return line;
        } catch (IOException e) {
            throw new NoSuchElementException(e.getMessage());
        }
    }

    public String read() {
        while (this.tokens.isEmpty()) {
            try {
                String line = this.reader.readLine();
                if (line == null)
                    throw new NoSuchElementException();
                for (String token : line.trim().split("\\s+"))
                    if (!token.isEmpty())
                        this.tokens.addLast(token);
            } catch (IOException e) {
                throw new NoSuchElementException(e.getMessage());
            }
        }
        return this.tokens.removeFirst();
    }

    public BigInteger readBigInteger() {
        return new BigInteger(read());
    }

    public BigDecimal readBigDecimal() {
        return new BigDecimal(read());
    }

    public boolean readBoolean() {
        return Boolean.parseBoolean(read());
    }
}
