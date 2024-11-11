package org.wumoe.devore.lang;

import java.io.InputStream;
import java.io.PrintStream;

public class IOConfig {
    public final PrintStream out;
    public final PrintStream err;
    public final InputStream in;

    public IOConfig(PrintStream out, PrintStream err, InputStream in) {
        this.out = out;
        this.err = err;
        this.in = in;
    }

    public IOConfig() {
        this.out = System.out;
        this.err = System.err;
        this.in = System.in;
    }
}
