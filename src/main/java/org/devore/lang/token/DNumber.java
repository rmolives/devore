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
public abstract class DNumber extends DToken {
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

    /**
     * 加法运算
     * @param a 数值
     * @return 结果
     */
    public DNumber add(DNumber a) {
        return DNumber.valueOf(this.toBigDecimal().add(a.toBigDecimal()));
    }

    /**
     * 减法运算
     * @param a 数值
     * @return 结果
     */
    public DNumber sub(DNumber a) {
        return DNumber.valueOf(this.toBigDecimal().subtract(a.toBigDecimal()));
    }

    /**
     * 乘法运算
     * @param a 数值
     * @return 结果
     */
    public DNumber mul(DNumber a) {
        return DNumber.valueOf(this.toBigDecimal().multiply(a.toBigDecimal()));
    }

    /**
     * 除法运算
     * @param a 数值
     * @return 结果
     */
    public DNumber div(DNumber a) {
        if (a.toBigDecimal().compareTo(BigDecimal.ZERO) == 0) throw new DevoreRuntimeException("除数不能为0.");
        return DNumber.valueOf(this.toBigDecimal().divide(a.toBigDecimal(), MathContext.DECIMAL128));
    }

    /**
     * sin运算
     * @return 结果
     */
    public DNumber sin() {
        return DNumber.valueOf(NumberUtils.sin(this.toBigDecimal(), MathContext.DECIMAL128));
    }

    /**
     * cos运算
     * @return 结果
     */
    public DNumber cos() {
        return DNumber.valueOf(NumberUtils.cos(this.toBigDecimal(), MathContext.DECIMAL128));
    }

    /**
     * tan运算
     * @return 结果
     */
    public DNumber tan() {
        return DNumber.valueOf(NumberUtils.tan(this.toBigDecimal(), MathContext.DECIMAL128));
    }

    /**
     * arctan运算
     * @return 结果
     */
    public DNumber arctan() {
        return DNumber.valueOf(NumberUtils.arctan(this.toBigDecimal(), MathContext.DECIMAL128));
    }

    /**
     * arcsin运算
     * @return 结果
     */
    public DNumber arcsin() {
        return DNumber.valueOf(NumberUtils.arcsin(this.toBigDecimal(), MathContext.DECIMAL128));
    }

    /**
     * arccos运算
     * @return 结果
     */
    public DNumber arccos() {
        return DNumber.valueOf(NumberUtils.arccos(this.toBigDecimal(), MathContext.DECIMAL128));
    }

    /**
     * 向上取整
     * @return 结果
     */
    public DNumber ceiling() {
        return DNumber.valueOf(this.toBigDecimal().setScale(0, RoundingMode.CEILING).toBigInteger());
    }

    /**
     * 向下取整
     * @return 结果
     */
    public DNumber floor() {
        return DNumber.valueOf(this.toBigDecimal().setScale(0, RoundingMode.FLOOR).toBigInteger());
    }

    /**
     * 向0取整
     * @return 结果
     */
    public DNumber truncate() {
        return DNumber.valueOf((this.toBigDecimal().signum() >= 0
                ? this.toBigDecimal().setScale(0, RoundingMode.FLOOR)
                : this.toBigDecimal().setScale(0, RoundingMode.CEILING)).toBigInteger());
    }

    /**
     * 四舍五入
     * @return 结果
     */
    public DNumber round() {
        return DNumber.valueOf(this.toBigDecimal().setScale(0, RoundingMode.HALF_UP).toBigInteger());
    }

    /**
     * sqrt运算
     * @return 结果
     */
    public DNumber sqrt() {
        return DNumber.valueOf(NumberUtils.sqrt(this.toBigDecimal(), MathContext.DECIMAL128));
    }

    /**
     * pow运算
     * @return 结果
     */
    public DNumber pow(DNumber n) {
        return DNumber.valueOf(NumberUtils.pow(this.toBigDecimal(), n.toBigDecimal(), MathContext.DECIMAL128));
    }

    /**
     * 绝对值
     * @return 结果
     */
    public DNumber abs() {
        return DNumber.valueOf(this.toBigDecimal().abs());
    }

    /**
     * ln运算
     * @return 结果
     */
    public DNumber log() {
        return DNumber.valueOf(NumberUtils.log(this.toBigDecimal(), MathContext.DECIMAL128));
    }

    /**
     * log运算
     * @param a 数值
     * @return 结果
     */
    public DNumber log(DNumber a) {
        return DNumber.valueOf(NumberUtils.log(this.toBigDecimal(), a.toBigDecimal(), MathContext.DECIMAL128));
    }

    /**
     * arctan2运算
     * @param x x
     * @return 结果
     */
    public DNumber arctan(DNumber x) {
        return DNumber.valueOf(NumberUtils.arctan2(this.toBigDecimal(), x.toBigDecimal(), MathContext.DECIMAL128));
    }

    /**
     * exp运算
     * @return 结果
     */
    public DNumber exp() {
        return DNumber.valueOf(NumberUtils.exp(this.toBigDecimal(), MathContext.DECIMAL128));
    }

    /**
     * 转换为BigInteger
     * @return 结果
     */
    public abstract BigInteger toBigInteger();

    /**
     * 转换为BigDecimal
     * @return 结果
     */
    public abstract BigDecimal toBigDecimal();

    @Override
    public int compareTo(DToken t) {
        return t instanceof DNumber ? this.toBigDecimal().compareTo(((DNumber) t).toBigDecimal()) : -1;
    }

    @Override
    public DToken copy() {
        return DNumber.valueOf(this.toBigDecimal());
    }

    @Override
    protected String str() {
        return NumberUtils.isInt(this.toBigDecimal()) ? this.toBigInteger().toString() : this.toBigDecimal().toPlainString();
    }

    @Override
    public int hashCode() {
        int result = this.type().hashCode();
        result = 31 * result + this.toBigDecimal().stripTrailingZeros().hashCode();
        return result;
    }
}
