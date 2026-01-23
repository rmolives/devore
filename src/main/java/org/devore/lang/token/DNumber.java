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
            throw new DevoreRuntimeException("除数不能为0.");
        return DNumber.valueOf(this.toBigDecimal().divide(a.toBigDecimal(), MathContext.DECIMAL128));
    }

    /**
     * sin运算
     *
     * @return 结果
     */
    public DNumber sin() {
        return DNumber.valueOf(NumberUtils.sin(this.toBigDecimal(), MathContext.DECIMAL128));
    }

    /**
     * cos运算
     *
     * @return 结果
     */
    public DNumber cos() {
        return DNumber.valueOf(NumberUtils.cos(this.toBigDecimal(), MathContext.DECIMAL128));
    }

    /**
     * tan运算
     *
     * @return 结果
     */
    public DNumber tan() {
        return DNumber.valueOf(NumberUtils.tan(this.toBigDecimal(), MathContext.DECIMAL128));
    }

    /**
     * arctan运算
     *
     * @return 结果
     */
    public DNumber arctan() {
        return DNumber.valueOf(NumberUtils.arctan(this.toBigDecimal(), MathContext.DECIMAL128));
    }

    /**
     * arcsin运算
     *
     * @return 结果
     */
    public DNumber arcsin() {
        return DNumber.valueOf(NumberUtils.arcsin(this.toBigDecimal(), MathContext.DECIMAL128));
    }

    /**
     * arccos运算
     *
     * @return 结果
     */
    public DNumber arccos() {
        return DNumber.valueOf(NumberUtils.arccos(this.toBigDecimal(), MathContext.DECIMAL128));
    }

    /**
     * tanh运算
     *
     * @return 结果
     */
    public DNumber tanh() {
        return DNumber.valueOf(NumberUtils.tanh(this.toBigDecimal(), MathContext.DECIMAL128));
    }

    /**
     * sinh运算
     *
     * @return 结果
     */
    public DNumber sinh() {
        return DNumber.valueOf(NumberUtils.sinh(this.toBigDecimal(), MathContext.DECIMAL128));
    }

    /**
     * cosh运算
     *
     * @return 结果
     */
    public DNumber cosh() {
        return DNumber.valueOf(NumberUtils.cosh(this.toBigDecimal(), MathContext.DECIMAL128));
    }

    /**
     * arccosh运算
     *
     * @return 结果
     */
    public DNumber arccosh() {
        return DNumber.valueOf(NumberUtils.arccosh(this.toBigDecimal(), MathContext.DECIMAL128));
    }

    /**
     * arctanh运算
     *
     * @return 结果
     */
    public DNumber arctanh() {
        return DNumber.valueOf(NumberUtils.arctanh(this.toBigDecimal(), MathContext.DECIMAL128));
    }

    /**
     * arcsinh运算
     *
     * @return 结果
     */
    public DNumber arcsinh() {
        return DNumber.valueOf(NumberUtils.arcsinh(this.toBigDecimal(), MathContext.DECIMAL128));
    }

    /**
     * sech运算
     *
     * @return 结果
     */
    public DNumber sech() {
        return DNumber.valueOf(NumberUtils.sech(this.toBigDecimal(), MathContext.DECIMAL128));
    }

    /**
     * csch运算
     *
     * @return 结果
     */
    public DNumber csch() {
        return DNumber.valueOf(NumberUtils.csch(this.toBigDecimal(), MathContext.DECIMAL128));
    }

    /**
     * coth运算
     *
     * @return 结果
     */
    public DNumber coth() {
        return DNumber.valueOf(NumberUtils.coth(this.toBigDecimal(), MathContext.DECIMAL128));
    }

    /**
     * arcsech运算
     *
     * @return 结果
     */
    public DNumber arcsech() {
        return DNumber.valueOf(NumberUtils.arcsech(this.toBigDecimal(), MathContext.DECIMAL128));
    }

    /**
     * arccsch运算
     *
     * @return 结果
     */
    public DNumber arccsch() {
        return DNumber.valueOf(NumberUtils.arccsch(this.toBigDecimal(), MathContext.DECIMAL128));
    }

    /**
     * arccoth运算
     *
     * @return 结果
     */
    public DNumber arccoth() {
        return DNumber.valueOf(NumberUtils.arccoth(this.toBigDecimal(), MathContext.DECIMAL128));
    }

    /**
     * sec运算
     *
     * @return 结果
     */
    public DNumber sec() {
        return DNumber.valueOf(NumberUtils.sec(this.toBigDecimal(), MathContext.DECIMAL128));
    }

    /**
     * csc运算
     *
     * @return 结果
     */
    public DNumber csc() {
        return DNumber.valueOf(NumberUtils.csc(this.toBigDecimal(), MathContext.DECIMAL128));
    }

    /**
     * cot运算
     *
     * @return 结果
     */
    public DNumber cot() {
        return DNumber.valueOf(NumberUtils.cot(this.toBigDecimal(), MathContext.DECIMAL128));
    }

    /**
     * arcsec运算
     *
     * @return 结果
     */
    public DNumber arcsec() {
        return DNumber.valueOf(NumberUtils.arcsec(this.toBigDecimal(), MathContext.DECIMAL128));
    }

    /**
     * arccsc运算
     *
     * @return 结果
     */
    public DNumber arccsc() {
        return DNumber.valueOf(NumberUtils.arccsc(this.toBigDecimal(), MathContext.DECIMAL128));
    }

    /**
     * arccot运算
     *
     * @return 结果
     */
    public DNumber arccot() {
        return DNumber.valueOf(NumberUtils.arccot(this.toBigDecimal(), MathContext.DECIMAL128));
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
        return DNumber.valueOf(NumberUtils.sqrt(this.toBigDecimal(), MathContext.DECIMAL128));
    }

    /**
     * cbrt运算
     *
     * @return 结果
     */
    public DNumber cbrt() {
        return DNumber.valueOf(NumberUtils.cbrt(this.toBigDecimal(), MathContext.DECIMAL128));
    }

    /**
     * pow运算
     *
     * @return 结果
     */
    public DNumber pow(DNumber n) {
        return DNumber.valueOf(NumberUtils.pow(this.toBigDecimal(), n.toBigDecimal(), MathContext.DECIMAL128));
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
        return DNumber.valueOf(NumberUtils.ln(this.toBigDecimal(), MathContext.DECIMAL128));
    }

    /**
     * log运算
     *
     * @param a 数值
     * @return 结果
     */
    public DNumber log(DNumber a) {
        return DNumber.valueOf(NumberUtils.log(this.toBigDecimal(), a.toBigDecimal(), MathContext.DECIMAL128));
    }

    /**
     * arctan2运算
     *
     * @param x x
     * @return 结果
     */
    public DNumber arctan(DNumber x) {
        return DNumber.valueOf(NumberUtils.arctan2(this.toBigDecimal(), x.toBigDecimal(), MathContext.DECIMAL128));
    }

    /**
     * exp运算
     *
     * @return 结果
     */
    public DNumber exp() {
        return DNumber.valueOf(NumberUtils.exp(this.toBigDecimal(), MathContext.DECIMAL128));
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
        return NumberUtils.isInt(this.toBigDecimal()) ? this.toBigInteger().toString() : this.toBigDecimal().toPlainString();
    }

    @Override
    public int hashCode() {
        int result = this.type().hashCode();
        result = 31 * result + this.toBigDecimal().stripTrailingZeros().hashCode();
        return result;
    }
}
