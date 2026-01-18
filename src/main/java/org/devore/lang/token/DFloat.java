package org.devore.lang.token;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * 浮点数
 */
public class DFloat extends DNumber {
    public final BigDecimal num;

    protected DFloat(BigDecimal num) {
        this.num = num;
    }

    @Override
    public BigInteger toBigInteger() {
        return num.toBigInteger();
    }

    @Override
    public BigDecimal toBigDecimal() {
        return num;
    }

    @Override
    public String type() {
        return "float";
    }
}
