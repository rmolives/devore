package org.devore.utils;

import org.devore.exception.DevoreRuntimeException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;

/**
 * 数学工具
 */
public class NumberUtils {
    private static final BigDecimal TWO = BigDecimal.valueOf(2);        // 2
    private static final BigDecimal PI = BigDecimal.valueOf(Math.PI);   // Pi
    private static final BigDecimal TWO_PI = PI.multiply(TWO);          // Pi * 2

    /**
     * 使用泰勒级数展开计算arctan(x)
     *
     * @param x  输入值，|x| ≤ 1时效果最好
     * @param mc 精度上下文
     * @return arctan(x)
     */
    public static BigDecimal arctan(BigDecimal x, MathContext mc) {
        if (x.compareTo(BigDecimal.ZERO) == 0)
            return BigDecimal.ZERO;
        boolean needAdjust = x.abs().compareTo(BigDecimal.ONE) > 0;
        BigDecimal workingX = needAdjust ? BigDecimal.ONE.divide(x, mc) : x;
        BigDecimal result = BigDecimal.ZERO;
        BigDecimal xSquared = workingX.multiply(workingX, mc);
        BigDecimal term = workingX;
        BigDecimal tolerance = BigDecimal.ONE.scaleByPowerOfTen(-mc.getPrecision());
        int n = 0;
        boolean add = true;
        while (term.abs().compareTo(tolerance) >= 0) {
            result = add ? result.add(term, mc) : result.subtract(term, mc);
            n++;
            term = term.multiply(xSquared, mc)
                    .multiply(BigDecimal.valueOf(2L * n - 1), mc)
                    .divide(BigDecimal.valueOf(2L * n + 1), mc);
            add = !add;
            if (n > mc.getPrecision() * 10)
                break;
        }
        if (needAdjust) {
            BigDecimal piOver2 = PI.round(mc).divide(BigDecimal.valueOf(2), mc);
            result = piOver2.subtract(result, mc);
            if (x.compareTo(BigDecimal.ZERO) < 0)
                result = result.negate();
        }
        return result.round(mc);
    }

    /**
     * 将角度缩小到范围[-π，π]
     *
     * @param angle 角度
     * @param mc    精度
     * @return [-π, π]
     */
    private static BigDecimal reduceAngle(BigDecimal angle, MathContext mc) {
        angle = angle.remainder(TWO_PI, mc);
        if (angle.compareTo(PI) > 0)
            angle = angle.subtract(TWO_PI);
        else if (angle.compareTo(PI.negate()) < 0)
            angle = angle.add(TWO_PI);
        return angle;
    }

    /**
     * 判断BigDecimal是否为整数
     *
     * @param decimal BigDecimal
     * @return 结果
     */
    public static boolean isInt(BigDecimal decimal) {
        BigDecimal stripped = decimal.stripTrailingZeros();
        return stripped.scale() <= 0;
    }

    /**
     * 使用泰勒级数展开计算sin(x)
     *
     * @param x  x
     * @param mc 精度
     * @return sin(x)
     */
    public static BigDecimal sin(BigDecimal x, MathContext mc) {
        x = reduceAngle(x, mc);
        BigDecimal result = BigDecimal.ZERO;
        BigDecimal term = x;
        BigDecimal xSquared = x.multiply(x);
        BigDecimal tolerance = BigDecimal.ONE.scaleByPowerOfTen(-mc.getPrecision());
        int i = 1;
        while (term.abs().compareTo(tolerance) > 0) {
            result = result.add(term);
            // Calculate next term: term * -x² / ((i+1)*(i+2))
            term = term.multiply(xSquared).negate()
                    .divide(BigDecimal.valueOf((long) (i + 1) * (i + 2)), mc);
            i += 2;
        }
        return result.round(mc);
    }

    /**
     * 使用泰勒级数展开计算cos(x)
     *
     * @param x  x
     * @param mc 精度
     * @return cos(x)
     */
    public static BigDecimal cos(BigDecimal x, MathContext mc) {
        x = reduceAngle(x, mc);
        BigDecimal result = BigDecimal.ZERO;
        BigDecimal term = BigDecimal.ONE;
        BigDecimal xSquared = x.multiply(x);
        BigDecimal tolerance = BigDecimal.ONE.scaleByPowerOfTen(-mc.getPrecision());
        int i = 0;
        while (term.abs().compareTo(tolerance) > 0) {
            result = result.add(term);
            // Calculate next term: term * -x² / ((i+1)*(i+2))
            term = term.multiply(xSquared).negate()
                    .divide(BigDecimal.valueOf((long) (i + 1) * (i + 2)), mc);
            i += 2;
        }
        return result.round(mc);
    }

    /**
     * 将tan计算为sin(x)/cos(x)
     *
     * @param x  x
     * @param mc 精度
     * @return tan(x)
     */
    public static BigDecimal tan(BigDecimal x, MathContext mc) {
        BigDecimal cos = cos(x, mc);
        if (cos.compareTo(BigDecimal.ZERO) == 0)
            throw new DevoreRuntimeException("此角度的tan未定义.");
        return sin(x, mc).divide(cos, mc);
    }

    /**
     * 使用牛顿法计算BigDecimal的平方根
     *
     * @param x  x
     * @param mc 精度
     * @return √x
     */
    public static BigDecimal sqrt(BigDecimal x, MathContext mc) {
        if (x.compareTo(BigDecimal.ZERO) < 0)
            throw new DevoreRuntimeException("负数的平方根.");
        if (x.compareTo(BigDecimal.ZERO) == 0)
            return BigDecimal.ZERO;
        if (x.compareTo(BigDecimal.ONE) == 0)
            return BigDecimal.ONE;
        BigDecimal guess = x.divide(TWO, mc);
        BigDecimal tolerance = BigDecimal.ONE.scaleByPowerOfTen(-mc.getPrecision());
        BigDecimal lastGuess;
        do {
            lastGuess = guess;
            guess = x.divide(guess, mc).add(guess).divide(TWO, mc);
        } while (guess.subtract(lastGuess).abs().compareTo(tolerance) > 0);
        return guess.round(mc);
    }

    /**
     * 计算BigDecimal的x^y
     *
     * @param x  x
     * @param y  y
     * @param mc 精度
     * @return x^y
     */
    public static BigDecimal pow(BigDecimal x, BigDecimal y, MathContext mc) {
        if (y.compareTo(BigDecimal.ZERO) == 0)
            return BigDecimal.ONE;
        if (x.compareTo(BigDecimal.ZERO) == 0) {
            if (y.compareTo(BigDecimal.ZERO) > 0)
                return BigDecimal.ZERO;
            throw new DevoreRuntimeException("零的负次方.");
        }
        if (x.compareTo(BigDecimal.ONE) == 0)
            return BigDecimal.ONE;
        if (isInt(y))
            return powInt(x, y.toBigInteger(), mc);
        if (x.compareTo(BigDecimal.ZERO) < 0)
            throw new DevoreRuntimeException("非整数指数的负数.");
        // General case: x^y = exp(y * ln(x))
        return exp(y.multiply(ln(x, mc)), mc);
    }

    /**
     * 计算BigDecimal的x^y, 其中y为整数
     *
     * @param x  x
     * @param y  y
     * @param mc 精度
     * @return x^y
     */
    private static BigDecimal powInt(BigDecimal x, BigInteger y, MathContext mc) {
        if (y.compareTo(BigInteger.ZERO) < 0)
            return BigDecimal.ONE.divide(powInt(x, y.subtract(y.multiply(BigInteger.TWO)), mc), mc);
        BigDecimal result = BigDecimal.ONE;
        BigInteger exp = y;
        while (exp.compareTo(BigInteger.ZERO) > 0) {
            result = result.multiply(x);
            exp = exp.subtract(BigInteger.ONE);
        }
        return result;
    }

    /**
     * 使用泰勒级数计算ln(x)
     *
     * @param x  x
     * @param mc 精度
     * @return ln(x)
     */
    public static BigDecimal ln(BigDecimal x, MathContext mc) {
        if (x.compareTo(BigDecimal.ZERO) <= 0)
            throw new DevoreRuntimeException("非正数的对数.");
        // Use ln(x) = 2*atanh((x-1)/(x+1)) for better convergence
        BigDecimal term = x.subtract(BigDecimal.ONE).divide(x.add(BigDecimal.ONE), mc);
        BigDecimal termSquared = term.multiply(term);
        BigDecimal result = term;
        BigDecimal currentTerm = term;
        BigDecimal tolerance = BigDecimal.ONE.scaleByPowerOfTen(-mc.getPrecision());
        for (int k = 3; ; k += 2) {
            currentTerm = currentTerm.multiply(termSquared, mc);
            BigDecimal delta = currentTerm.divide(BigDecimal.valueOf(k), mc);
            result = result.add(delta, mc);
            if (delta.abs().compareTo(tolerance) < 0)
                break;
        }
        return result.multiply(TWO, mc);
    }

    /**
     * 使用泰勒级数计算e^x
     *
     * @param x  x
     * @param mc 精度
     * @return e^x
     */
    public static BigDecimal exp(BigDecimal x, MathContext mc) {
        BigDecimal result = BigDecimal.ONE;
        BigDecimal term = BigDecimal.ONE;
        BigDecimal tolerance = BigDecimal.ONE.scaleByPowerOfTen(-mc.getPrecision());
        int n = 1;
        while (true) {
            term = term.multiply(x, mc).divide(BigDecimal.valueOf(n), mc);
            result = result.add(term, mc);
            if (term.abs().compareTo(tolerance) < 0)
                break;
            ++n;
        }
        return result.round(mc);
    }
}
