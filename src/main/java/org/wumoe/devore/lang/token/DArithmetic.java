package org.wumoe.devore.lang.token;

import java.math.BigDecimal;
import java.math.BigInteger;

public abstract class DArithmetic extends Token {
    public abstract DArithmetic add(DArithmetic a);

    public abstract DArithmetic sub(DArithmetic a);

    public abstract DArithmetic mul(DArithmetic a);

    public abstract DArithmetic div(DArithmetic a);

    public abstract BigInteger toBigInteger();

    public abstract BigDecimal toBigDecimal();
}
