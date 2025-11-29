package org.devore.lang.token;

import org.devore.exception.DevoreRuntimeException;
import org.devore.utils.NumberUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * 浮点数
 */
public class DFloat extends DNumber {
    public final BigDecimal num;

    protected DFloat(BigDecimal num) {
        this.num = num;
    }

    public static DFloat valueOf(long n) {
        return new DFloat(BigDecimal.valueOf(n));
    }

    public static DFloat valueOf(double n) {
        return new DFloat(BigDecimal.valueOf(n));
    }

    public static DFloat valueOf(BigInteger n) {
        return new DFloat(new BigDecimal(n));
    }

    public static DFloat valueOf(BigDecimal n) {
        return new DFloat(n);
    }

    @Override
    public DArithmetic add(DArithmetic a) {
        DFloat result;
        if (a instanceof DInt n)
            result = DFloat.valueOf(num.add(new BigDecimal(n.num)));
        else if (a instanceof DFloat n)
            result = DFloat.valueOf(num.add(n.num));
        else
            throw new DevoreRuntimeException("无法将类型 [" + type() + "] 与类型 [" + a.type() + "] 相加.");
        return NumberUtils.isInt(result.num) ? DInt.valueOf(result.num) : result;
    }

    @Override
    public DArithmetic sub(DArithmetic a) {
        DFloat result;
        if (a instanceof DInt n)
            result = DFloat.valueOf(num.subtract(new BigDecimal(n.num)));
        else if (a instanceof DFloat n)
            result = DFloat.valueOf(num.subtract(n.num));
        else
            throw new DevoreRuntimeException("无法将类型 [" + type() + "] 与类型 [" + a.type() + "] 相减.");
        return NumberUtils.isInt(result.num) ? DInt.valueOf(result.num) : result;
    }

    @Override
    public DArithmetic mul(DArithmetic a) {
        DFloat result;
        if (a instanceof DInt n)
            result = DFloat.valueOf(num.multiply(new BigDecimal(n.num)));
        else if (a instanceof DFloat n)
            result = DFloat.valueOf(num.multiply(n.num));
        else
            throw new DevoreRuntimeException("无法将类型 [" + type() + "] 与类型 [" + a.type() + "] 相乘.");
        return NumberUtils.isInt(result.num) ? DInt.valueOf(result.num) : result;
    }

    @Override
    public DArithmetic div(DArithmetic a) {
        DFloat result;
        if (a instanceof DInt n)
            result = DFloat.valueOf(num.divide(new BigDecimal(n.num), MathContext.DECIMAL128));
        else if (a instanceof DFloat n)
            result = DFloat.valueOf(num.divide(n.num, MathContext.DECIMAL128));
        else
            throw new DevoreRuntimeException("无法将类型 [" + type() + "] 与类型 [" + a.type() + "] 相除.");
        return NumberUtils.isInt(result.num) ? DInt.valueOf(result.num) : result;
    }

    @Override
    public DNumber sin() {
        DFloat result = DFloat.valueOf(NumberUtils.sin(num, MathContext.DECIMAL128));
        return NumberUtils.isInt(result.num) ? DInt.valueOf(result.num) : result;
    }

    @Override
    public DNumber cos() {
        DFloat result = DFloat.valueOf(NumberUtils.cos(num, MathContext.DECIMAL128));
        return NumberUtils.isInt(result.num) ? DInt.valueOf(result.num) : result;
    }

    @Override
    public DNumber tan() {
        DFloat result = DFloat.valueOf(NumberUtils.tan(num, MathContext.DECIMAL128));
        return NumberUtils.isInt(result.num) ? DInt.valueOf(result.num) : result;
    }

    @Override
    public DNumber arctan() {
        DFloat result = DFloat.valueOf(NumberUtils.arctan(num, MathContext.DECIMAL128));
        return NumberUtils.isInt(result.num) ? DInt.valueOf(result.num) : result;
    }

    @Override
    public DNumber ceil() {
        return DInt.valueOf(num.setScale(0, RoundingMode.CEILING));
    }

    @Override
    public DNumber floor() {
        return DInt.valueOf(num.setScale(0, RoundingMode.FLOOR));
    }

    @Override
    public DNumber sqrt() {
        DFloat result = DFloat.valueOf(NumberUtils.sqrt(num, MathContext.DECIMAL128));
        return NumberUtils.isInt(result.num) ? DInt.valueOf(result.num) : result;
    }

    @Override
    public DNumber pow(DNumber n) {
        DFloat result = DFloat.valueOf(NumberUtils.pow(this.toBigDecimal(), n.toBigDecimal(), MathContext.DECIMAL128));
        return NumberUtils.isInt(result.num) ? DInt.valueOf(result.num) : result;
    }

    @Override
    public DNumber abs() {
        return DFloat.valueOf(num.abs());
    }

    @Override
    public DNumber ln() {
        DFloat result = DFloat.valueOf(NumberUtils.ln(this.toBigDecimal(), MathContext.DECIMAL128));
        return NumberUtils.isInt(result.num) ? DInt.valueOf(result.num) : result;
    }

    @Override
    public DNumber exp() {
        DFloat result = DFloat.valueOf(NumberUtils.exp(this.toBigDecimal(), MathContext.DECIMAL128));
        return NumberUtils.isInt(result.num) ? DInt.valueOf(result.num) : result;
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
    public String str() {
        return NumberUtils.isInt(num) ? DInt.valueOf(num).str() : num.toPlainString();
    }

    @Override
    public Token copy() {
        return DFloat.valueOf(num);
    }

    @Override
    public int compareTo(Token t) {
        return t instanceof DNumber n ? num.compareTo(n.toBigDecimal()) : -1;
    }
}
