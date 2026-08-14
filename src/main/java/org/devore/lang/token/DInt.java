package org.devore.lang.token;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * 整数
 */
public class DInt extends DNumber {
    public final BigInteger num;    // 数字

    protected DInt(BigInteger num) {
        this.num = num;
    }

    @Override
    public BigInteger toBigInteger() {
        return this.num;
    }

    @Override
    public BigDecimal toBigDecimal() {
        return new BigDecimal(this.num);
    }

    @Override
    public String type() {
        return "int";
    }
}
