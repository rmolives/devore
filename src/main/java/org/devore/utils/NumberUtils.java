package org.devore.utils;

import org.devore.exception.DevoreRuntimeException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;

/**
 * 数学工具
 */
public class NumberUtils {
    private static final BigDecimal TWO = BigDecimal.valueOf(2);                                // 2
    private static final BigDecimal PI = approximatePi();                                       // Pi
    private static final BigDecimal TWO_PI = PI.multiply(TWO, MathContext.DECIMAL128);          // Pi * 2

    /**
     * gcd(a, b)
     *
     * @param a a
     * @param b b
     * @return gcd(a, b)
     */
    public static BigInteger gcd(BigInteger a, BigInteger b) {
        BigInteger r0 = a.abs();
        BigInteger r1 = b.abs();
        while (!r1.equals(BigInteger.ZERO)) {
            BigInteger r2 = r0.mod(r1);
            r0 = r1;
            r1 = r2;
        }
        return r0;
    }

    /**
     * lcm(a, b)
     *
     * @param a a
     * @param b b
     * @return lcm(a, b)
     */
    public static BigInteger lcm(BigInteger a, BigInteger b) {
        if (a.equals(BigInteger.ZERO) || b.equals(BigInteger.ZERO))
            return BigInteger.ZERO;
        BigInteger g = gcd(a, b);
        return a.divide(g).multiply(b).abs();
    }

    /**
     * 将 π 近似
     *
     * @return π 近似值
     */
    private static BigDecimal approximatePi() {
        MathContext extendedMc = new MathContext(MathContext.DECIMAL128.getPrecision() + 10,
                MathContext.DECIMAL128.getRoundingMode());
        BigDecimal term1 = arctanTaylor(BigDecimal.ONE.divide(BigDecimal.valueOf(5), extendedMc), extendedMc);
        BigDecimal term2 = arctanTaylor(BigDecimal.ONE.divide(BigDecimal.valueOf(239), extendedMc), extendedMc);
        BigDecimal pi = BigDecimal.valueOf(4).multiply(
                BigDecimal.valueOf(4).multiply(term1, extendedMc)
                        .subtract(term2, extendedMc), extendedMc);
        return pi.round(MathContext.DECIMAL128);
    }

    /**
     * arctan2(y, x)
     *
     * @param y  Y坐标
     * @param x  X坐标
     * @param mc 精度
     * @return 角度
     */
    public static BigDecimal arctan2(BigDecimal y, BigDecimal x, MathContext mc) {
        if (x.compareTo(BigDecimal.ZERO) == 0 &&
                y.compareTo(BigDecimal.ZERO) == 0)
            return BigDecimal.ZERO;
        if (x.compareTo(BigDecimal.ZERO) == 0)
            return y.compareTo(BigDecimal.ZERO) > 0
                    ? PI.divide(BigDecimal.valueOf(2), mc)
                    : PI.divide(BigDecimal.valueOf(2), mc).negate();
        if (y.compareTo(BigDecimal.ZERO) == 0)
            return x.compareTo(BigDecimal.ZERO) > 0
                    ? BigDecimal.ZERO
                    : PI;
        BigDecimal ratio = y.divide(x, mc);
        BigDecimal basicAngle = arctan(ratio.abs(), mc);
        return x.compareTo(BigDecimal.ZERO) > 0 ?
                y.compareTo(BigDecimal.ZERO) > 0
                        ? basicAngle
                        : basicAngle.negate()
                : y.compareTo(BigDecimal.ZERO) > 0
                ? PI.subtract(basicAngle, mc)
                : basicAngle.subtract(PI, mc);
    }

    /**
     * sech(x)
     *
     * @param x  x
     * @param mc 精度
     * @return sech(x)
     */
    public static BigDecimal sech(BigDecimal x, MathContext mc) {
        return BigDecimal.ONE.divide(cosh(x, mc), mc);
    }

    /**
     * csch(x)
     *
     * @param x  x
     * @param mc 精度
     * @return csch(x)
     */
    public static BigDecimal csch(BigDecimal x, MathContext mc) {
        if (x.compareTo(BigDecimal.ZERO) == 0)
            throw new ArithmeticException("csch(x)要求x不为0.");
        return BigDecimal.ONE.divide(sinh(x, mc), mc);
    }

    /**
     * coth(x)
     *
     * @param x  x
     * @param mc 精度
     * @return coth(x)
     */
    public static BigDecimal coth(BigDecimal x, MathContext mc) {
        if (x.compareTo(BigDecimal.ZERO) == 0)
            throw new ArithmeticException("coth(x)要求x不为0.");
        return cosh(x, mc).divide(sinh(x, mc), mc);
    }

    /**
     * arcsech(x)
     *
     * @param x  x
     * @param mc 精度
     * @return arcsech(x)
     */
    public static BigDecimal arcsech(BigDecimal x, MathContext mc) {
        if (x.compareTo(BigDecimal.ZERO) <= 0 || x.compareTo(BigDecimal.ONE) > 0)
            throw new ArithmeticException("arcsech(x)定义域为(0, 1], x超出范围.");
        return arccosh(BigDecimal.ONE.divide(x, mc), mc);
    }

    /**
     * arccsch(x)
     *
     * @param x  x
     * @param mc 精度
     * @return arccsch(x)
     */
    public static BigDecimal arccsch(BigDecimal x, MathContext mc) {
        if (x.compareTo(BigDecimal.ZERO) == 0)
            throw new ArithmeticException("arccsch(x)要求x不为0.");
        return arcsinh(BigDecimal.ONE.divide(x, mc), mc);
    }

    /**
     * arccoth(x)
     *
     * @param x  x
     * @param mc 精度
     * @return arccoth(x)
     */
    public static BigDecimal arccoth(BigDecimal x, MathContext mc) {
        if (x.abs().compareTo(BigDecimal.ONE) <= 0)
            throw new ArithmeticException("arccoth(x)定义域为(-∞, -1)∪(1, +∞), x超出范围.");
        return arctanh(BigDecimal.ONE.divide(x, mc), mc);
    }

    /**
     * sinh(x)
     *
     * @param x  x
     * @param mc 精度
     * @return sinh(x)
     */
    public static BigDecimal sinh(BigDecimal x, MathContext mc) {
        BigDecimal ex = exp(x, mc);
        BigDecimal emx = exp(x.negate(), mc);
        return ex.subtract(emx, mc)
                .divide(BigDecimal.valueOf(2), mc)
                .round(mc);
    }

    /**
     * cosh(x)
     *
     * @param x  x
     * @param mc 精度
     * @return cosh(x)
     */
    public static BigDecimal cosh(BigDecimal x, MathContext mc) {
        BigDecimal ex = exp(x, mc);
        BigDecimal emx = exp(x.negate(), mc);
        return ex.add(emx, mc)
                .divide(BigDecimal.valueOf(2), mc)
                .round(mc);
    }

    /**
     * tanh(x)
     *
     * @param x  x
     * @param mc 精度
     * @return tanh(x)
     */
    public static BigDecimal tanh(BigDecimal x, MathContext mc) {
        if (x.compareTo(BigDecimal.ZERO) == 0)
            return BigDecimal.ZERO;
        return sinh(x, mc).divide(cosh(x, mc), mc);
    }

    /**
     * arcsinh(x)
     *
     * @param x  x
     * @param mc 精度
     * @return arcsinh(x)
     */
    public static BigDecimal arcsinh(BigDecimal x, MathContext mc) {
        BigDecimal x2 = x.multiply(x, mc);
        BigDecimal sqrt = sqrt(x2.add(BigDecimal.ONE, mc), mc);
        BigDecimal inner = x.add(sqrt, mc);
        return ln(inner, mc).round(mc);
    }

    /**
     * arccosh(x)
     *
     * @param x  x
     * @param mc 精度
     * @return arccosh(x)
     */
    public static BigDecimal arccosh(BigDecimal x, MathContext mc) {
        if (x.compareTo(BigDecimal.ONE) < 0)
            throw new DevoreRuntimeException("arcosh(x)的定义域为[1, +∞), x超出范围.");
        if (x.compareTo(BigDecimal.ONE) == 0)
            return BigDecimal.ZERO;
        BigDecimal xm1 = x.subtract(BigDecimal.ONE, mc);
        BigDecimal xp1 = x.add(BigDecimal.ONE, mc);
        BigDecimal sqrt = sqrt(xm1.multiply(xp1, mc), mc);
        return ln(x.add(sqrt, mc), mc).round(mc);
    }

    /**
     * arctanh(x)
     *
     * @param x  x
     * @param mc 精度
     * @return arctanh(x)
     */
    public static BigDecimal arctanh(BigDecimal x, MathContext mc) {
        if (x.compareTo(BigDecimal.ONE) >= 0 || x.compareTo(BigDecimal.ONE.negate()) <= 0)
            throw new DevoreRuntimeException("artanh(x)的定义域为(-1, 1), x超出范围.");
        if (x.compareTo(BigDecimal.ZERO) == 0)
            return BigDecimal.ZERO;
        BigDecimal onePlus = BigDecimal.ONE.add(x, mc);
        BigDecimal oneMinus = BigDecimal.ONE.subtract(x, mc);
        BigDecimal ln = ln(onePlus.divide(oneMinus, mc), mc);
        return ln.divide(BigDecimal.valueOf(2), mc).round(mc);
    }

    /**
     * arccos(x)
     *
     * @param x  x
     * @param mc 精度
     * @return arccos(x)
     */
    public static BigDecimal arccos(BigDecimal x, MathContext mc) {
        if (x.compareTo(BigDecimal.ONE) > 0 || x.compareTo(BigDecimal.ONE.negate()) < 0)
            throw new DevoreRuntimeException("arccos(x)的定义域为[-1, 1], x超出范围.");
        if (x.compareTo(BigDecimal.ONE) == 0)
            return BigDecimal.ZERO;
        if (x.compareTo(BigDecimal.ONE.negate()) == 0)
            return PI;
        if (x.compareTo(BigDecimal.ZERO) == 0)
            return PI.divide(BigDecimal.valueOf(2), mc);
        BigDecimal piOver2 = PI.divide(BigDecimal.valueOf(2), mc);
        BigDecimal arcsinX = arcsin(x, mc);
        BigDecimal result = piOver2.subtract(arcsinX, mc);
        if (result.compareTo(BigDecimal.ZERO) < 0)
            result = result.add(PI, mc);
        else if (result.compareTo(PI) > 0)
            result = result.subtract(PI, mc);
        return result.round(mc);
    }

    /**
     * arcsin(x)
     *
     * @param x  x
     * @param mc 精度
     * @return arcsin(x)
     */
    public static BigDecimal arcsin(BigDecimal x, MathContext mc) {
        if (x.compareTo(BigDecimal.ONE) > 0 || x.compareTo(BigDecimal.ONE.negate()) < 0)
            throw new DevoreRuntimeException("arcsin(x)的定义域为[-1, 1], x超出范围.");
        if (x.compareTo(BigDecimal.ONE) == 0)
            return PI.divide(BigDecimal.valueOf(2), mc);
        if (x.compareTo(BigDecimal.ONE.negate()) == 0)
            return PI.divide(BigDecimal.valueOf(2), mc).negate();
        if (x.compareTo(BigDecimal.ZERO) == 0)
            return BigDecimal.ZERO;
        BigDecimal absX = x.abs();
        BigDecimal result;
        if (absX.compareTo(new BigDecimal("0.7")) <= 0)
            result = arcsinTaylor(x, mc);
        else {
            BigDecimal transformed = sqrt(BigDecimal.ONE.subtract(absX)
                    .divide(BigDecimal.valueOf(2), mc), mc);
            result = PI.divide(BigDecimal.valueOf(2), mc)
                    .subtract(BigDecimal.valueOf(2).multiply(arcsinTaylor(transformed, mc), mc), mc);
            if (x.compareTo(BigDecimal.ZERO) < 0)
                result = result.negate();
        }
        return result.round(mc);
    }

    /**
     * arcsin(x)
     *
     * @param x  x
     * @param mc 精度
     * @return arcsin(x)
     */
    private static BigDecimal arcsinTaylor(BigDecimal x, MathContext mc) {
        if (x.compareTo(BigDecimal.ZERO) == 0)
            return BigDecimal.ZERO;
        BigDecimal result = x;
        BigDecimal xSquared = x.multiply(x, mc);
        BigDecimal term = x;
        BigDecimal tolerance = BigDecimal.ONE.scaleByPowerOfTen(-mc.getPrecision());
        int n = 1;
        BigDecimal numerator = BigDecimal.ONE;
        BigDecimal denominator = BigDecimal.ONE;
        while (term.abs().compareTo(tolerance) >= 0) {
            numerator = numerator.multiply(BigDecimal.valueOf(2L * n - 1), mc);
            denominator = denominator.multiply(BigDecimal.valueOf(2L * n), mc);
            BigDecimal coefficient = numerator.divide(denominator, mc)
                    .divide(BigDecimal.valueOf(2L * n + 1), mc);
            term = term.multiply(xSquared, mc);
            BigDecimal currentTerm = coefficient.multiply(term, mc);
            result = result.add(currentTerm, mc);
            ++n;
            if (n > mc.getPrecision() * 10)
                break;
        }
        return result;
    }

    /**
     * arctan(x)
     *
     * @param x  x
     * @param mc 精度
     * @return arctan(x)
     */
    public static BigDecimal arctan(BigDecimal x, MathContext mc) {
        if (x.compareTo(BigDecimal.ZERO) == 0)
            return BigDecimal.ZERO;
        if (x.compareTo(BigDecimal.ONE) == 0)
            return PI.divide(BigDecimal.valueOf(4), mc);
        if (x.compareTo(BigDecimal.ONE.negate()) == 0)
            return PI.divide(BigDecimal.valueOf(4), mc).negate();
        BigDecimal absX = x.abs();
        BigDecimal result;
        if (absX.compareTo(BigDecimal.ONE) <= 0)
            result = arctanTaylor(absX, mc);
        else {
            BigDecimal reciprocal = BigDecimal.ONE.divide(absX, mc);
            BigDecimal piOver2 = PI.divide(BigDecimal.valueOf(2), mc);
            result = piOver2.subtract(arctanTaylor(reciprocal, mc), mc);
        }
        if (x.compareTo(BigDecimal.ZERO) < 0)
            result = result.negate();
        return result.round(mc);
    }

    /**
     * arctan(x)
     *
     * @param x  x
     * @param mc 精度
     * @return arctan(x)
     */
    private static BigDecimal arctanTaylor(BigDecimal x, MathContext mc) {
        if (x.compareTo(BigDecimal.ZERO) == 0)
            return BigDecimal.ZERO;
        BigDecimal result = BigDecimal.ZERO;
        BigDecimal xSquared = x.multiply(x, mc);
        BigDecimal term = x;
        BigDecimal tolerance = BigDecimal.ONE.scaleByPowerOfTen(-mc.getPrecision());
        int n = 0;
        boolean add = true;
        while (term.abs().compareTo(tolerance) >= 0) {
            BigDecimal denominator = BigDecimal.valueOf(2L * n + 1);
            BigDecimal currentTerm = term.divide(denominator, mc);
            result = add ? result.add(currentTerm, mc) : result.subtract(currentTerm, mc);
            term = term.multiply(xSquared, mc);
            ++n;
            add = !add;
            if (n > mc.getPrecision() * 20)
                break;
        }
        return result;
    }

    /**
     * 将角度缩小到范围 [-π, π]
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
        return decimal.stripTrailingZeros().scale() <= 0;
    }

    /**
     * sin(x)
     *
     * @param x  x
     * @param mc 精度
     * @return sin(x)
     */
    public static BigDecimal sin(BigDecimal x, MathContext mc) {
        x = reduceAngle(x, mc);
        BigDecimal result = BigDecimal.ZERO;
        BigDecimal term = x;
        BigDecimal xSquared = x.multiply(x, mc);
        BigDecimal tolerance = BigDecimal.ONE.scaleByPowerOfTen(-mc.getPrecision());
        int i = 1;
        while (term.abs().compareTo(tolerance) > 0) {
            result = result.add(term);
            term = term.multiply(xSquared, mc).negate()
                    .divide(BigDecimal.valueOf((long) (i + 1) * (i + 2)), mc);
            i += 2;
        }
        return result.round(mc);
    }

    /**
     * cos(x)
     *
     * @param x  x
     * @param mc 精度
     * @return cos(x)
     */
    public static BigDecimal cos(BigDecimal x, MathContext mc) {
        x = reduceAngle(x, mc);
        BigDecimal result = BigDecimal.ZERO;
        BigDecimal term = BigDecimal.ONE;
        BigDecimal xSquared = x.multiply(x, mc);
        BigDecimal tolerance = BigDecimal.ONE.scaleByPowerOfTen(-mc.getPrecision());
        int i = 0;
        while (term.abs().compareTo(tolerance) > 0) {
            result = result.add(term);
            term = term.multiply(xSquared, mc).negate()
                    .divide(BigDecimal.valueOf((long) (i + 1) * (i + 2)), mc);
            i += 2;
        }
        return result.round(mc);
    }

    /**
     * tan(x)
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
     * sqrt(x)
     *
     * @param x  x
     * @param mc 精度
     * @return sqrt(x)
     */
    public static BigDecimal sqrt(BigDecimal x, MathContext mc) {
        if (x.compareTo(BigDecimal.ZERO) < 0)
            throw new DevoreRuntimeException("sqrt(x)要求x不为负数.");
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
     * x^y
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
            throw new DevoreRuntimeException("x^y要求但x为0时, y不能为负数.");
        }
        if (x.compareTo(BigDecimal.ONE) == 0)
            return BigDecimal.ONE;
        if (isInt(y))
            return powInt(x, y.toBigInteger(), mc);
        if (x.compareTo(BigDecimal.ZERO) < 0)
            throw new DevoreRuntimeException("x^y要求但y为非整数时, x不能为负数.");
        return exp(y.multiply(ln(x, mc), mc), mc);
    }

    /**
     * x^y, 其中 y 为整数
     *
     * @param x  x
     * @param y  y
     * @param mc 精度
     * @return x^y
     */
    private static BigDecimal powInt(BigDecimal x, BigInteger y, MathContext mc) {
        if (y.signum() == 0)
            return BigDecimal.ONE;
        if (y.signum() < 0)
            return BigDecimal.ONE.divide(powInt(x, y.negate(), mc), mc);
        BigDecimal result = BigDecimal.ONE;
        BigDecimal base = x;
        BigInteger exp = y;
        while (exp.signum() > 0) {
            if (exp.testBit(0))
                result = result.multiply(base, mc);
            base = base.multiply(base, mc);
            exp = exp.shiftRight(1);
        }
        return result;
    }

    /**
     * ln(x)
     *
     * @param x  x
     * @param mc 精度
     * @return ln(x)
     */
    public static BigDecimal ln(BigDecimal x, MathContext mc) {
        if (x.compareTo(BigDecimal.ZERO) <= 0)
            throw new DevoreRuntimeException("ln(x)要求x为正数.");
        BigDecimal term = x.subtract(BigDecimal.ONE).divide(x.add(BigDecimal.ONE), mc);
        BigDecimal termSquared = term.multiply(term, mc);
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
     * log_b(a)
     *
     * @param a  a
     * @param b  b
     * @param mc 精度
     * @return log_b(a)
     */
    public static BigDecimal log(BigDecimal a, BigDecimal b, MathContext mc) {
        if (a.compareTo(BigDecimal.ZERO) <= 0)
            throw new DevoreRuntimeException("log_b(a)要求a必须大于0.");
        if (b.compareTo(BigDecimal.ZERO) <= 0)
            throw new DevoreRuntimeException("log_b(a)要求b必须大于0.");
        if (b.compareTo(BigDecimal.ONE) == 0)
            throw new DevoreRuntimeException("log_b(a)要求b不能为1.");
        return ln(a, mc).divide(ln(b, mc), mc);
    }

    /**
     * e^x
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
