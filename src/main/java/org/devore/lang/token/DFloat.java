package org.devore.lang.token;

import org.devore.utils.NumberUtils;

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

    @Override
    protected String str() {
        return NumberUtils.isInt(num) ? DInt.valueOf(num).toString() : num.toPlainString();
    }

    @Override
    public Token copy() {
        return DNumber.valueOf(num);
    }

    @Override
    public int compareTo(Token t) {
        return t instanceof DNumber n ? num.compareTo(n.toBigDecimal()) : -1;
    }
}
