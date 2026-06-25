package org.devore.lang.token;

import org.devore.exception.DevoreRuntimeException;
import org.devore.utils.NumberUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
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
     *
     * @param a 数值
     * @return 结果
     */
    public DNumber add(DNumber a) {
        return DNumber.valueOf(this.toBigDecimal().add(a.toBigDecimal()));
    }

    /**
     * 减法运算
     *
     * @param a 数值
     * @return 结果
     */
    public DNumber sub(DNumber a) {
        return DNumber.valueOf(this.toBigDecimal().subtract(a.toBigDecimal()));
    }

    /**
     * 乘法运算
     *
     * @param a 数值
     * @return 结果
     */
    public DNumber mul(DNumber a) {
        return DNumber.valueOf(this.toBigDecimal().multiply(a.toBigDecimal()));
    }

    /**
     * 除法运算
     *
     * @param a 数值
     * @return 结果
     */
    public DNumber div(DNumber a) {
        if (a.toBigDecimal().compareTo(BigDecimal.ZERO) == 0)
            throw new DevoreRuntimeException("除数不能为0, 被除数=" + this + ", 除数=" + a + ".");
        return DNumber.valueOf(NumberUtils.divide(this.toBigDecimal(), a.toBigDecimal(), NumberUtils.DEFAULT_CONTEXT));
    }

    /**
     * sin运算
     *
     * @return 结果
     */
    public DNumber sin() {
        return DNumber.valueOf(NumberUtils.sin(this.toBigDecimal(), NumberUtils.DEFAULT_CONTEXT));
    }

    /**
     * cos运算
     *
     * @return 结果
     */
    public DNumber cos() {
        return DNumber.valueOf(NumberUtils.cos(this.toBigDecimal(), NumberUtils.DEFAULT_CONTEXT));
    }

    /**
     * tan运算
     *
     * @return 结果
     */
    public DNumber tan() {
        return DNumber.valueOf(NumberUtils.tan(this.toBigDecimal(), NumberUtils.DEFAULT_CONTEXT));
    }

    /**
     * arctan运算
     *
     * @return 结果
     */
    public DNumber arctan() {
        return DNumber.valueOf(NumberUtils.arctan(this.toBigDecimal(), NumberUtils.DEFAULT_CONTEXT));
    }

    /**
     * arcsin运算
     *
     * @return 结果
     */
    public DNumber arcsin() {
        return DNumber.valueOf(NumberUtils.arcsin(this.toBigDecimal(), NumberUtils.DEFAULT_CONTEXT));
    }

    /**
     * arccos运算
     *
     * @return 结果
     */
    public DNumber arccos() {
        return DNumber.valueOf(NumberUtils.arccos(this.toBigDecimal(), NumberUtils.DEFAULT_CONTEXT));
    }

    /**
     * tanh运算
     *
     * @return 结果
     */
    public DNumber tanh() {
        return DNumber.valueOf(NumberUtils.tanh(this.toBigDecimal(), NumberUtils.DEFAULT_CONTEXT));
    }

    /**
     * sinh运算
     *
     * @return 结果
     */
    public DNumber sinh() {
        return DNumber.valueOf(NumberUtils.sinh(this.toBigDecimal(), NumberUtils.DEFAULT_CONTEXT));
    }

    /**
     * cosh运算
     *
     * @return 结果
     */
    public DNumber cosh() {
        return DNumber.valueOf(NumberUtils.cosh(this.toBigDecimal(), NumberUtils.DEFAULT_CONTEXT));
    }

    /**
     * arccosh运算
     *
     * @return 结果
     */
    public DNumber arccosh() {
        return DNumber.valueOf(NumberUtils.arccosh(this.toBigDecimal(), NumberUtils.DEFAULT_CONTEXT));
    }

    /**
     * arctanh运算
     *
     * @return 结果
     */
    public DNumber arctanh() {
        return DNumber.valueOf(NumberUtils.arctanh(this.toBigDecimal(), NumberUtils.DEFAULT_CONTEXT));
    }

    /**
     * arcsinh运算
     *
     * @return 结果
     */
    public DNumber arcsinh() {
        return DNumber.valueOf(NumberUtils.arcsinh(this.toBigDecimal(), NumberUtils.DEFAULT_CONTEXT));
    }

    /**
     * sech运算
     *
     * @return 结果
     */
    public DNumber sech() {
        return DNumber.valueOf(NumberUtils.sech(this.toBigDecimal(), NumberUtils.DEFAULT_CONTEXT));
    }

    /**
     * csch运算
     *
     * @return 结果
     */
    public DNumber csch() {
        return DNumber.valueOf(NumberUtils.csch(this.toBigDecimal(), NumberUtils.DEFAULT_CONTEXT));
    }

    /**
     * coth运算
     *
     * @return 结果
     */
    public DNumber coth() {
        return DNumber.valueOf(NumberUtils.coth(this.toBigDecimal(), NumberUtils.DEFAULT_CONTEXT));
    }

    /**
     * arcsech运算
     *
     * @return 结果
     */
    public DNumber arcsech() {
        return DNumber.valueOf(NumberUtils.arcsech(this.toBigDecimal(), NumberUtils.DEFAULT_CONTEXT));
    }

    /**
     * arccsch运算
     *
     * @return 结果
     */
    public DNumber arccsch() {
        return DNumber.valueOf(NumberUtils.arccsch(this.toBigDecimal(), NumberUtils.DEFAULT_CONTEXT));
    }

    /**
     * arccoth运算
     *
     * @return 结果
     */
    public DNumber arccoth() {
        return DNumber.valueOf(NumberUtils.arccoth(this.toBigDecimal(), NumberUtils.DEFAULT_CONTEXT));
    }

    /**
     * sec运算
     *
     * @return 结果
     */
    public DNumber sec() {
        return DNumber.valueOf(NumberUtils.sec(this.toBigDecimal(), NumberUtils.DEFAULT_CONTEXT));
    }

    /**
     * csc运算
     *
     * @return 结果
     */
    public DNumber csc() {
        return DNumber.valueOf(NumberUtils.csc(this.toBigDecimal(), NumberUtils.DEFAULT_CONTEXT));
    }

    /**
     * cot运算
     *
     * @return 结果
     */
    public DNumber cot() {
        return DNumber.valueOf(NumberUtils.cot(this.toBigDecimal(), NumberUtils.DEFAULT_CONTEXT));
    }

    /**
     * arcsec运算
     *
     * @return 结果
     */
    public DNumber arcsec() {
        return DNumber.valueOf(NumberUtils.arcsec(this.toBigDecimal(), NumberUtils.DEFAULT_CONTEXT));
    }

    /**
     * arccsc运算
     *
     * @return 结果
     */
    public DNumber arccsc() {
        return DNumber.valueOf(NumberUtils.arccsc(this.toBigDecimal(), NumberUtils.DEFAULT_CONTEXT));
    }

    /**
     * arccot运算
     *
     * @return 结果
     */
    public DNumber arccot() {
        return DNumber.valueOf(NumberUtils.arccot(this.toBigDecimal(), NumberUtils.DEFAULT_CONTEXT));
    }

    /**
     * 向上取整
     *
     * @return 结果
     */
    public DNumber ceiling() {
        return DNumber.valueOf(this.toBigDecimal().setScale(0, RoundingMode.CEILING).toBigInteger());
    }

    /**
     * 向下取整
     *
     * @return 结果
     */
    public DNumber floor() {
        return DNumber.valueOf(this.toBigDecimal().setScale(0, RoundingMode.FLOOR).toBigInteger());
    }

    /**
     * 向0取整
     *
     * @return 结果
     */
    public DNumber truncate() {
        return DNumber.valueOf((this.toBigDecimal().signum() >= 0
                ? this.toBigDecimal().setScale(0, RoundingMode.FLOOR)
                : this.toBigDecimal().setScale(0, RoundingMode.CEILING)).toBigInteger());
    }

    /**
     * 四舍五入
     *
     * @return 结果
     */
    public DNumber round() {
        return DNumber.valueOf(this.toBigDecimal().setScale(0, RoundingMode.HALF_UP).toBigInteger());
    }

    /**
     * sqrt运算
     *
     * @return 结果
     */
    public DNumber sqrt() {
        return DNumber.valueOf(NumberUtils.sqrt(this.toBigDecimal(), NumberUtils.DEFAULT_CONTEXT));
    }

    /**
     * cbrt运算
     *
     * @return 结果
     */
    public DNumber cbrt() {
        return DNumber.valueOf(NumberUtils.cbrt(this.toBigDecimal(), NumberUtils.DEFAULT_CONTEXT));
    }

    /**
     * pow运算
     *
     * @return 结果
     */
    public DNumber pow(DNumber n) {
        return DNumber.valueOf(NumberUtils.pow(this.toBigDecimal(), n.toBigDecimal(), NumberUtils.DEFAULT_CONTEXT));
    }

    /**
     * 绝对值
     *
     * @return 结果
     */
    public DNumber abs() {
        return DNumber.valueOf(this.toBigDecimal().abs());
    }

    /**
     * ln运算
     *
     * @return 结果
     */
    public DNumber ln() {
        return DNumber.valueOf(NumberUtils.ln(this.toBigDecimal(), NumberUtils.DEFAULT_CONTEXT));
    }

    /**
     * log运算
     *
     * @param a 数值
     * @return 结果
     */
    public DNumber log(DNumber a) {
        return DNumber.valueOf(NumberUtils.log(this.toBigDecimal(), a.toBigDecimal(), NumberUtils.DEFAULT_CONTEXT));
    }

    /**
     * arctan2运算
     *
     * @param x x
     * @return 结果
     */
    public DNumber arctan(DNumber x) {
        return DNumber.valueOf(NumberUtils.arctan2(this.toBigDecimal(), x.toBigDecimal(), NumberUtils.DEFAULT_CONTEXT));
    }

    /**
     * exp运算
     *
     * @return 结果
     */
    public DNumber exp() {
        return DNumber.valueOf(NumberUtils.exp(this.toBigDecimal(), NumberUtils.DEFAULT_CONTEXT));
    }

    /**
     * 转换为BigInteger
     *
     * @return 结果
     */
    public abstract BigInteger toBigInteger();

    /**
     * 转换为BigDecimal
     *
     * @return 结果
     */
    public abstract BigDecimal toBigDecimal();

    @Override
    public int compareTo(DToken t) {
        return t instanceof DNumber ? this.toBigDecimal().compareTo(((DNumber) t).toBigDecimal()) : -1;
    }

    @Override
    protected String str() {
        return NumberUtils.plain(this.toBigDecimal());
    }

    @Override
    public int hashCode() {
        return this.toBigDecimal().stripTrailingZeros().hashCode();
    }
}
