package org.wumoe.devore.lang.token;

public abstract class DNumber extends DArithmetic {
    public abstract DNumber sin();

    public abstract DNumber cos();

    public abstract DNumber tan();

    public abstract DNumber ceil();

    public abstract DNumber floor();

    public abstract DNumber sqrt();

    public abstract DNumber pow(DNumber n);

    public abstract DNumber abs();
}
