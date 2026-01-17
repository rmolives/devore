package org.devore.lang.token;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * 数字
 */
public abstract class DNumber extends Token {
    public abstract DNumber sin();

    public abstract DNumber cos();

    public abstract DNumber tan();

    public abstract DNumber arctan();

    public abstract DNumber arcsin();

    public abstract DNumber arccos();

    public abstract DNumber ceiling();

    public abstract DNumber floor();

    public abstract DNumber truncate();

    public abstract DNumber round();

    public abstract DNumber sqrt();

    public abstract DNumber pow(DNumber n);

    public abstract DNumber abs();

    public abstract DNumber log();

    public abstract DNumber log(DNumber b);

    public abstract DNumber atan2(DNumber x);

    public abstract DNumber exp();

    public abstract DNumber add(DNumber a);

    public abstract DNumber sub(DNumber a);

    public abstract DNumber mul(DNumber a);

    public abstract DNumber div(DNumber a);

    public abstract BigInteger toBigInteger();

    public abstract BigDecimal toBigDecimal();
}
