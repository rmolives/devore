package org.wumoe.devore.lang.token;

import java.math.BigDecimal;
import java.math.BigInteger;

public abstract class DNumber extends DArithmetic {
    public abstract DNumber sin();

    public abstract DNumber cos();

    public abstract DNumber tan();

    public abstract DNumber ceil();

    public abstract DNumber floor();

    public abstract DNumber pow(DNumber n);

    public abstract DNumber abs();
}
