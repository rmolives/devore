package org.devore.lang.token;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * 四则运算
 */
public abstract class DArithmetic extends Token {
    public abstract DArithmetic add(DArithmetic a);

    public abstract DArithmetic sub(DArithmetic a);

    public abstract DArithmetic mul(DArithmetic a);

    public abstract DArithmetic div(DArithmetic a);

    public abstract BigInteger toBigInteger();

    public abstract BigDecimal toBigDecimal();
}
