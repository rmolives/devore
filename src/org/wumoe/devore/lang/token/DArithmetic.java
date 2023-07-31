package org.wumoe.devore.lang.token;

public abstract class DArithmetic extends Token {
    public abstract DArithmetic add(DArithmetic a);

    public abstract DArithmetic sub(DArithmetic a);

    public abstract DArithmetic mul(DArithmetic a);

    public abstract DArithmetic div(DArithmetic a);
}
