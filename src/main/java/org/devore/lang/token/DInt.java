package org.devore.lang.token;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * 整数
 */
public class DInt extends DNumber {
    public final BigInteger num;

    protected DInt(BigInteger num) {
        this.num = num;
    }

    public DNumber mod(DInt n) {
        return DNumber.valueOf(this.toBigInteger().mod(n.toBigInteger()));
    }

    @Override
    public BigInteger toBigInteger() {
        return num;
    }

    @Override
    public BigDecimal toBigDecimal() {
        return new BigDecimal(num);
    }

    @Override
    public String type() {
        return "int";
    }

    @Override
    protected String str() {
        return num.toString();
    }

    @Override
    public Token copy() {
        return DNumber.valueOf(num);
    }

    @Override
    public int compareTo(Token t) {
        return t instanceof DNumber n ? this.toBigDecimal().compareTo(n.toBigDecimal()) : -1;
    }
}
