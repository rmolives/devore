package org.devore.lang.token;

import org.devore.exception.DevoreRuntimeException;
import org.devore.utils.NumberUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * 数字
 */
public abstract class DNumber extends Token {
    public static DNumber valueOf(long n) {
        return DNumber.valueOf(BigInteger.valueOf(n));
    }

    public static DNumber valueOf(double n) {
        return DNumber.valueOf(BigDecimal.valueOf(n));
    }

    public static DNumber valueOf(BigInteger n) {
        return new DInt(n);
    }

    public static DNumber valueOf(BigDecimal n) {
        return NumberUtils.isInt(n) ? new DInt(n.toBigInteger()) : new DFloat(n.stripTrailingZeros());
    }

    public DNumber add(DNumber a) {
        return DNumber.valueOf(this.toBigDecimal().add(a.toBigDecimal()));
    }

    public DNumber sub(DNumber a) {
        return DNumber.valueOf(this.toBigDecimal().subtract(a.toBigDecimal()));
    }

    public DNumber mul(DNumber a) {
        return DNumber.valueOf(this.toBigDecimal().multiply(a.toBigDecimal()));
    }

    public DNumber div(DNumber a) {
        if (a.toBigDecimal().compareTo(BigDecimal.ZERO) == 0) throw new DevoreRuntimeException("除数不能为0.");
        return DNumber.valueOf(this.toBigDecimal().divide(a.toBigDecimal(), MathContext.DECIMAL128));
    }

    public DNumber sin() {
        return DNumber.valueOf(NumberUtils.sin(this.toBigDecimal(), MathContext.DECIMAL128));
    }

    public DNumber cos() {
        return DNumber.valueOf(NumberUtils.cos(this.toBigDecimal(), MathContext.DECIMAL128));
    }

    public DNumber tan() {
        return DNumber.valueOf(NumberUtils.tan(this.toBigDecimal(), MathContext.DECIMAL128));
    }

    public DNumber arctan() {
        return DNumber.valueOf(NumberUtils.arctan(this.toBigDecimal(), MathContext.DECIMAL128));
    }

    public DNumber arcsin() {
        return DNumber.valueOf(NumberUtils.arcsin(this.toBigDecimal(), MathContext.DECIMAL128));
    }

    public DNumber arccos() {
        return DNumber.valueOf(NumberUtils.arccos(this.toBigDecimal(), MathContext.DECIMAL128));
    }

    public DNumber ceiling() {
        return DNumber.valueOf(this.toBigDecimal().setScale(0, RoundingMode.CEILING).toBigInteger());
    }

    public DNumber floor() {
        return DNumber.valueOf(this.toBigDecimal().setScale(0, RoundingMode.FLOOR).toBigInteger());
    }

    public DNumber truncate() {
        return DNumber.valueOf((this.toBigDecimal().signum() >= 0
                ? this.toBigDecimal().setScale(0, RoundingMode.FLOOR)
                : this.toBigDecimal().setScale(0, RoundingMode.CEILING)).toBigInteger());
    }

    public DNumber round() {
        return DNumber.valueOf(this.toBigDecimal().setScale(0, RoundingMode.HALF_UP).toBigInteger());
    }

    public DNumber sqrt() {
        return DNumber.valueOf(NumberUtils.sqrt(this.toBigDecimal(), MathContext.DECIMAL128));
    }

    public DNumber pow(DNumber n) {
        return DNumber.valueOf(NumberUtils.pow(this.toBigDecimal(), n.toBigDecimal(), MathContext.DECIMAL128));
    }

    public DNumber abs() {
        return DNumber.valueOf(this.toBigDecimal().abs());
    }

    public DNumber log() {
        return DNumber.valueOf(NumberUtils.log(this.toBigDecimal(), MathContext.DECIMAL128));
    }

    public DNumber log(DNumber b) {
        return DNumber.valueOf(NumberUtils.log(this.toBigDecimal(), b.toBigDecimal(), MathContext.DECIMAL128));
    }

    public DNumber arctan2(DNumber x) {
        return DNumber.valueOf(NumberUtils.arctan2(this.toBigDecimal(), x.toBigDecimal(), MathContext.DECIMAL128));
    }

    public DNumber exp() {
        return DNumber.valueOf(NumberUtils.exp(this.toBigDecimal(), MathContext.DECIMAL128));
    }

    public abstract BigInteger toBigInteger();

    public abstract BigDecimal toBigDecimal();

    @Override
    public int compareTo(Token t) {
        return t instanceof DNumber ? this.toBigDecimal().compareTo(((DNumber) t).toBigDecimal()) : -1;
    }

    @Override
    public Token copy() {
        return DNumber.valueOf(this.toBigDecimal());
    }

    @Override
    protected String str() {
        return NumberUtils.isInt(this.toBigDecimal()) ? this.toBigInteger().toString() : this.toBigDecimal().toPlainString();
    }
}
