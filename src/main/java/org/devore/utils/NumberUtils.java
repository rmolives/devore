package org.devore.utils;

import org.devore.exception.DevoreRuntimeException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * 数学工具
 */
public class NumberUtils {
    public static final MathContext DEFAULT_CONTEXT = new MathContext(
            MathContext.DECIMAL128.getPrecision() * 2,
            MathContext.DECIMAL128.getRoundingMode());                              // 默认计算精度
    private static final BigDecimal TWO = BigDecimal.valueOf(2);                    // 2
    private static final BigDecimal PI = approximatePi();                           // Pi
    private static final BigDecimal TWO_PI = PI.multiply(TWO, DEFAULT_CONTEXT);     // Pi * 2

    public static String plain(BigDecimal value) {
        return value.stripTrailingZeros().toPlainString();
    }

    /**
     * a mod m
     *
     * @param a a
     * @param m m
     * @return a mod m
     */
    public static BigDecimal mod(BigDecimal a, BigDecimal m) {
        return a.subtract(m.multiply(a.divide(m, 0, RoundingMode.FLOOR)));
    }

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
     * 精确除法, 仅在结果为无限小数时使用指定精度近似
     *
     * @param a  被除数
     * @param b  除数
     * @return a / b
     */
    public static BigDecimal divide(BigDecimal a, BigDecimal b) {
        try {
            return a.divide(b);
        } catch (ArithmeticException e) {
            return a.divide(b, DEFAULT_CONTEXT);
        }
    }

    /**
     * 将 π 近似
     *
     * @return π 近似值
     */
    private static BigDecimal approximatePi() {
        MathContext extendedMc = new MathContext(DEFAULT_CONTEXT.getPrecision() + 10,
                DEFAULT_CONTEXT.getRoundingMode());
        BigDecimal term1 = arctanTaylor(BigDecimal.ONE.divide(BigDecimal.valueOf(5), extendedMc));
        BigDecimal term2 = arctanTaylor(BigDecimal.ONE.divide(BigDecimal.valueOf(239), extendedMc));
        BigDecimal pi = BigDecimal.valueOf(4).multiply(
                BigDecimal.valueOf(4).multiply(term1, extendedMc)
                        .subtract(term2, extendedMc), extendedMc);
        return pi.round(DEFAULT_CONTEXT);
    }

    /**
     * arctan2(y, x)
     *
     * @param y  Y坐标
     * @param x  X坐标
     * @return 角度
     */
    public static BigDecimal arctan2(BigDecimal y, BigDecimal x) {
        if (x.compareTo(BigDecimal.ZERO) == 0 && y.compareTo(BigDecimal.ZERO) == 0)
            return BigDecimal.ZERO;
        if (x.compareTo(BigDecimal.ZERO) == 0)
            return y.compareTo(BigDecimal.ZERO) > 0
                    ? PI.divide(BigDecimal.valueOf(2), DEFAULT_CONTEXT)
                    : PI.divide(BigDecimal.valueOf(2), DEFAULT_CONTEXT).negate();
        if (y.compareTo(BigDecimal.ZERO) == 0)
            return x.compareTo(BigDecimal.ZERO) > 0
                    ? BigDecimal.ZERO
                    : PI;
        BigDecimal ratio = y.divide(x, DEFAULT_CONTEXT);
        BigDecimal basicAngle = arctan(ratio.abs());
        return x.compareTo(BigDecimal.ZERO) > 0 ?
                y.compareTo(BigDecimal.ZERO) > 0
                        ? basicAngle
                        : basicAngle.negate()
                : y.compareTo(BigDecimal.ZERO) > 0
                ? PI.subtract(basicAngle, DEFAULT_CONTEXT)
                : basicAngle.subtract(PI, DEFAULT_CONTEXT);
    }

    /**
     * sech(x)
     *
     * @param x  x
     * @return sech(x)
     */
    public static BigDecimal sech(BigDecimal x) {
        return BigDecimal.ONE.divide(cosh(x), DEFAULT_CONTEXT);
    }

    /**
     * csch(x)
     *
     * @param x  x
     * @return csch(x)
     */
    public static BigDecimal csch(BigDecimal x) {
        if (x.compareTo(BigDecimal.ZERO) == 0)
            throw new DevoreRuntimeException("csch(x)要求x不为0, 实际x=" + plain(x));
        return BigDecimal.ONE.divide(sinh(x), DEFAULT_CONTEXT);
    }

    /**
     * coth(x)
     *
     * @param x  x
     * @return coth(x)
     */
    public static BigDecimal coth(BigDecimal x) {
        if (x.compareTo(BigDecimal.ZERO) == 0)
            throw new DevoreRuntimeException("coth(x)要求x不为0, 实际x=" + plain(x));
        return cosh(x).divide(sinh(x), DEFAULT_CONTEXT);
    }

    /**
     * arcsech(x)
     *
     * @param x  x
     * @return arcsech(x)
     */
    public static BigDecimal arcsech(BigDecimal x) {
        if (x.compareTo(BigDecimal.ZERO) <= 0 || x.compareTo(BigDecimal.ONE) > 0)
            throw new DevoreRuntimeException("arcsech(x)定义域为(0, 1], x超出范围, 实际x=" + plain(x));
        return arccosh(BigDecimal.ONE.divide(x, DEFAULT_CONTEXT));
    }

    /**
     * arccsch(x)
     *
     * @param x  x
     * @return arccsch(x)
     */
    public static BigDecimal arccsch(BigDecimal x) {
        if (x.compareTo(BigDecimal.ZERO) == 0)
            throw new DevoreRuntimeException("arccsch(x)要求x不为0, 实际x=" + plain(x));
        return arcsinh(BigDecimal.ONE.divide(x, DEFAULT_CONTEXT));
    }

    /**
     * arccoth(x)
     *
     * @param x  x
     * @return arccoth(x)
     */
    public static BigDecimal arccoth(BigDecimal x) {
        if (x.abs().compareTo(BigDecimal.ONE) <= 0)
            throw new DevoreRuntimeException("arccoth(x)定义域为(-∞, -1)∪(1, +∞), x超出范围, 实际x="
                    + plain(x));
        return arctanh(BigDecimal.ONE.divide(x, DEFAULT_CONTEXT));
    }

    /**
     * sec(x)
     *
     * @param x  x
     * @return sec(x)
     */
    public static BigDecimal sec(BigDecimal x) {
        BigDecimal c = cos(x);
        if (c.compareTo(BigDecimal.ZERO) == 0)
            throw new DevoreRuntimeException("sec(x)在cos(x)=0时未定义, 实际x=" + plain(x));
        return BigDecimal.ONE.divide(c, DEFAULT_CONTEXT);
    }

    /**
     * csc(x)
     *
     * @param x  x
     * @return csch(x)
     */
    public static BigDecimal csc(BigDecimal x) {
        BigDecimal s = sin(x);
        if (s.compareTo(BigDecimal.ZERO) == 0)
            throw new DevoreRuntimeException("csc(x)在sin(x)=0时未定义, 实际x=" + plain(x));
        return BigDecimal.ONE.divide(s, DEFAULT_CONTEXT);
    }

    /**
     * cot(x)
     *
     * @param x  x
     * @return cot(x)
     */
    public static BigDecimal cot(BigDecimal x) {
        BigDecimal s = sin(x);
        if (s.compareTo(BigDecimal.ZERO) == 0)
            throw new DevoreRuntimeException("cot(x)在sin(x)=0时未定义, 实际x=" + plain(x));
        return cos(x).divide(s, DEFAULT_CONTEXT);
    }

    /**
     * arcsec(x)
     *
     * @param x  x
     * @return arcsech(x)
     */
    public static BigDecimal arcsec(BigDecimal x) {
        if (x.abs().compareTo(BigDecimal.ONE) < 0)
            throw new DevoreRuntimeException("arcsec(x)定义域为(-∞, -1]∪[1, +∞), x超出范围, 实际x="
                    + plain(x));
        return arccos(BigDecimal.ONE.divide(x, DEFAULT_CONTEXT));
    }

    /**
     * arccsc(x)
     *
     * @param x  x
     * @return arccsc(x)
     */
    public static BigDecimal arccsc(BigDecimal x) {
        if (x.compareTo(BigDecimal.ZERO) == 0 || x.abs().compareTo(BigDecimal.ONE) < 0)
            throw new DevoreRuntimeException("arccsc(x)定义域为(-∞, -1]∪[1, +∞), x超出范围, 实际x="
                    + plain(x));
        return arcsin(BigDecimal.ONE.divide(x, DEFAULT_CONTEXT));
    }

    /**
     * arccot(x)
     *
     * @param x  x
     * @return arccot(x)
     */
    public static BigDecimal arccot(BigDecimal x) {
        if (x.compareTo(BigDecimal.ZERO) == 0)
            return new BigDecimal(Math.PI / 2, DEFAULT_CONTEXT);
        return arctan(BigDecimal.ONE.divide(x, DEFAULT_CONTEXT));
    }

    /**
     * sinh(x)
     *
     * @param x  x
     * @return sinh(x)
     */
    public static BigDecimal sinh(BigDecimal x) {
        BigDecimal ex = exp(x);
        BigDecimal emx = exp(x.negate());
        return ex.subtract(emx, DEFAULT_CONTEXT)
                .divide(BigDecimal.valueOf(2), DEFAULT_CONTEXT)
                .round(DEFAULT_CONTEXT);
    }

    /**
     * cosh(x)
     *
     * @param x  x
     * @return cosh(x)
     */
    public static BigDecimal cosh(BigDecimal x) {
        BigDecimal ex = exp(x);
        BigDecimal emx = exp(x.negate());
        return ex.add(emx, DEFAULT_CONTEXT)
                .divide(BigDecimal.valueOf(2), DEFAULT_CONTEXT)
                .round(DEFAULT_CONTEXT);
    }

    /**
     * tanh(x)
     *
     * @param x  x
     * @return tanh(x)
     */
    public static BigDecimal tanh(BigDecimal x) {
        if (x.compareTo(BigDecimal.ZERO) == 0)
            return BigDecimal.ZERO;
        return sinh(x).divide(cosh(x), DEFAULT_CONTEXT);
    }

    /**
     * arcsinh(x)
     *
     * @param x  x
     * @return arcsinh(x)
     */
    public static BigDecimal arcsinh(BigDecimal x) {
        BigDecimal x2 = x.multiply(x, DEFAULT_CONTEXT);
        BigDecimal sqrt = sqrt(x2.add(BigDecimal.ONE, DEFAULT_CONTEXT));
        BigDecimal inner = x.add(sqrt, DEFAULT_CONTEXT);
        return ln(inner).round(DEFAULT_CONTEXT);
    }

    /**
     * arccosh(x)
     *
     * @param x  x
     * @return arccosh(x)
     */
    public static BigDecimal arccosh(BigDecimal x) {
        if (x.compareTo(BigDecimal.ONE) < 0)
            throw new DevoreRuntimeException("arcosh(x)的定义域为[1, +∞), x超出范围, 实际x="
                    + plain(x));
        if (x.compareTo(BigDecimal.ONE) == 0)
            return BigDecimal.ZERO;
        BigDecimal xm1 = x.subtract(BigDecimal.ONE, DEFAULT_CONTEXT);
        BigDecimal xp1 = x.add(BigDecimal.ONE, DEFAULT_CONTEXT);
        BigDecimal sqrt = sqrt(xm1.multiply(xp1, DEFAULT_CONTEXT));
        return ln(x.add(sqrt, DEFAULT_CONTEXT)).round(DEFAULT_CONTEXT);
    }

    /**
     * arctanh(x)
     *
     * @param x  x
     * @return arctanh(x)
     */
    public static BigDecimal arctanh(BigDecimal x) {
        if (x.compareTo(BigDecimal.ONE) >= 0 || x.compareTo(BigDecimal.ONE.negate()) <= 0)
            throw new DevoreRuntimeException("artanh(x)的定义域为(-1, 1), x超出范围, 实际x="
                    + plain(x));
        if (x.compareTo(BigDecimal.ZERO) == 0)
            return BigDecimal.ZERO;
        BigDecimal onePlus = BigDecimal.ONE.add(x, DEFAULT_CONTEXT);
        BigDecimal oneMinus = BigDecimal.ONE.subtract(x, DEFAULT_CONTEXT);
        BigDecimal ln = ln(onePlus.divide(oneMinus, DEFAULT_CONTEXT));
        return ln.divide(BigDecimal.valueOf(2), DEFAULT_CONTEXT).round(DEFAULT_CONTEXT);
    }

    /**
     * arccos(x)
     *
     * @param x  x
     * @return arccos(x)
     */
    public static BigDecimal arccos(BigDecimal x) {
        if (x.compareTo(BigDecimal.ONE) > 0 || x.compareTo(BigDecimal.ONE.negate()) < 0)
            throw new DevoreRuntimeException("arccos(x)的定义域为[-1, 1], x超出范围, 实际x="
                    + plain(x));
        if (x.compareTo(BigDecimal.ONE) == 0)
            return BigDecimal.ZERO;
        if (x.compareTo(BigDecimal.ONE.negate()) == 0)
            return PI;
        if (x.compareTo(BigDecimal.ZERO) == 0)
            return PI.divide(BigDecimal.valueOf(2), DEFAULT_CONTEXT);
        BigDecimal piOver2 = PI.divide(BigDecimal.valueOf(2), DEFAULT_CONTEXT);
        BigDecimal arcsinX = arcsin(x);
        BigDecimal result = piOver2.subtract(arcsinX, DEFAULT_CONTEXT);
        if (result.compareTo(BigDecimal.ZERO) < 0)
            result = result.add(PI, DEFAULT_CONTEXT);
        else if (result.compareTo(PI) > 0)
            result = result.subtract(PI, DEFAULT_CONTEXT);
        return result.round(DEFAULT_CONTEXT);
    }

    /**
     * arcsin(x)
     *
     * @param x  x
     * @return arcsin(x)
     */
    public static BigDecimal arcsin(BigDecimal x) {
        if (x.compareTo(BigDecimal.ONE) > 0 || x.compareTo(BigDecimal.ONE.negate()) < 0)
            throw new DevoreRuntimeException("arcsin(x)的定义域为[-1, 1], x超出范围, 实际x="
                    + plain(x));
        if (x.compareTo(BigDecimal.ONE) == 0)
            return PI.divide(BigDecimal.valueOf(2), DEFAULT_CONTEXT);
        if (x.compareTo(BigDecimal.ONE.negate()) == 0)
            return PI.divide(BigDecimal.valueOf(2), DEFAULT_CONTEXT).negate();
        if (x.compareTo(BigDecimal.ZERO) == 0)
            return BigDecimal.ZERO;
        BigDecimal absX = x.abs();
        BigDecimal result;
        if (absX.compareTo(new BigDecimal("0.7")) <= 0)
            result = arcsinTaylor(x);
        else {
            BigDecimal transformed = sqrt(BigDecimal.ONE.subtract(absX)
                    .divide(BigDecimal.valueOf(2), DEFAULT_CONTEXT));
            result = PI.divide(BigDecimal.valueOf(2), DEFAULT_CONTEXT)
                    .subtract(BigDecimal.valueOf(2).multiply(arcsinTaylor(transformed), DEFAULT_CONTEXT), DEFAULT_CONTEXT);
            if (x.compareTo(BigDecimal.ZERO) < 0)
                result = result.negate();
        }
        return result.round(DEFAULT_CONTEXT);
    }

    /**
     * arcsin(x)
     *
     * @param x  x
     * @return arcsin(x)
     */
    private static BigDecimal arcsinTaylor(BigDecimal x) {
        if (x.compareTo(BigDecimal.ZERO) == 0)
            return BigDecimal.ZERO;
        BigDecimal result = x;
        BigDecimal xSquared = x.multiply(x, DEFAULT_CONTEXT);
        BigDecimal term = x;
        BigDecimal tolerance = BigDecimal.ONE.scaleByPowerOfTen(-DEFAULT_CONTEXT.getPrecision());
        int n = 1;
        BigDecimal numerator = BigDecimal.ONE;
        BigDecimal denominator = BigDecimal.ONE;
        while (term.abs().compareTo(tolerance) >= 0) {
            numerator = numerator.multiply(BigDecimal.valueOf(2L * n - 1), DEFAULT_CONTEXT);
            denominator = denominator.multiply(BigDecimal.valueOf(2L * n), DEFAULT_CONTEXT);
            BigDecimal coefficient = numerator.divide(denominator, DEFAULT_CONTEXT)
                    .divide(BigDecimal.valueOf(2L * n + 1), DEFAULT_CONTEXT);
            term = term.multiply(xSquared, DEFAULT_CONTEXT);
            BigDecimal currentTerm = coefficient.multiply(term, DEFAULT_CONTEXT);
            result = result.add(currentTerm, DEFAULT_CONTEXT);
            ++n;
            if (n > DEFAULT_CONTEXT.getPrecision() * 10)
                break;
        }
        return result;
    }

    /**
     * arctan(x)
     *
     * @param x  x
     * @return arctan(x)
     */
    public static BigDecimal arctan(BigDecimal x) {
        if (x.compareTo(BigDecimal.ZERO) == 0)
            return BigDecimal.ZERO;
        if (x.compareTo(BigDecimal.ONE) == 0)
            return PI.divide(BigDecimal.valueOf(4), DEFAULT_CONTEXT);
        if (x.compareTo(BigDecimal.ONE.negate()) == 0)
            return PI.divide(BigDecimal.valueOf(4), DEFAULT_CONTEXT).negate();
        BigDecimal absX = x.abs();
        BigDecimal result;
        if (absX.compareTo(BigDecimal.ONE) <= 0)
            result = arctanTaylor(absX);
        else {
            BigDecimal reciprocal = BigDecimal.ONE.divide(absX, DEFAULT_CONTEXT);
            BigDecimal piOver2 = PI.divide(BigDecimal.valueOf(2), DEFAULT_CONTEXT);
            result = piOver2.subtract(arctanTaylor(reciprocal), DEFAULT_CONTEXT);
        }
        if (x.compareTo(BigDecimal.ZERO) < 0)
            result = result.negate();
        return result.round(DEFAULT_CONTEXT);
    }

    /**
     * arctan(x)
     *
     * @param x  x
     * @return arctan(x)
     */
    private static BigDecimal arctanTaylor(BigDecimal x) {
        if (x.compareTo(BigDecimal.ZERO) == 0)
            return BigDecimal.ZERO;
        BigDecimal result = BigDecimal.ZERO;
        BigDecimal xSquared = x.multiply(x, DEFAULT_CONTEXT);
        BigDecimal term = x;
        BigDecimal tolerance = BigDecimal.ONE.scaleByPowerOfTen(-DEFAULT_CONTEXT.getPrecision());
        int n = 0;
        boolean add = true;
        while (term.abs().compareTo(tolerance) >= 0) {
            BigDecimal denominator = BigDecimal.valueOf(2L * n + 1);
            BigDecimal currentTerm = term.divide(denominator, DEFAULT_CONTEXT);
            result = add ? result.add(currentTerm, DEFAULT_CONTEXT) : result.subtract(currentTerm, DEFAULT_CONTEXT);
            term = term.multiply(xSquared, DEFAULT_CONTEXT);
            ++n;
            add = !add;
            if (n > DEFAULT_CONTEXT.getPrecision() * 20)
                break;
        }
        return result;
    }

    /**
     * 将角度缩小到范围 [-π, π]
     *
     * @param angle 角度
     * @return [-π, π]
     */
    private static BigDecimal reduceAngle(BigDecimal angle) {
        angle = angle.remainder(TWO_PI, DEFAULT_CONTEXT);
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
     * @return sin(x)
     */
    public static BigDecimal sin(BigDecimal x) {
        x = reduceAngle(x);
        BigDecimal result = BigDecimal.ZERO;
        BigDecimal term = x;
        BigDecimal xSquared = x.multiply(x, DEFAULT_CONTEXT);
        BigDecimal tolerance = BigDecimal.ONE.scaleByPowerOfTen(-DEFAULT_CONTEXT.getPrecision());
        int i = 1;
        while (term.abs().compareTo(tolerance) > 0) {
            result = result.add(term);
            term = term.multiply(xSquared, DEFAULT_CONTEXT).negate()
                    .divide(BigDecimal.valueOf((long) (i + 1) * (i + 2)), DEFAULT_CONTEXT);
            i += 2;
        }
        return result.round(DEFAULT_CONTEXT);
    }

    /**
     * cos(x)
     *
     * @param x  x
     * @return cos(x)
     */
    public static BigDecimal cos(BigDecimal x) {
        x = reduceAngle(x);
        BigDecimal result = BigDecimal.ZERO;
        BigDecimal term = BigDecimal.ONE;
        BigDecimal xSquared = x.multiply(x, DEFAULT_CONTEXT);
        BigDecimal tolerance = BigDecimal.ONE.scaleByPowerOfTen(-DEFAULT_CONTEXT.getPrecision());
        int i = 0;
        while (term.abs().compareTo(tolerance) > 0) {
            result = result.add(term);
            term = term.multiply(xSquared, DEFAULT_CONTEXT).negate()
                    .divide(BigDecimal.valueOf((long) (i + 1) * (i + 2)), DEFAULT_CONTEXT);
            i += 2;
        }
        return result.round(DEFAULT_CONTEXT);
    }

    /**
     * tan(x)
     *
     * @param x  x
     * @return tan(x)
     */
    public static BigDecimal tan(BigDecimal x) {
        BigDecimal cos = cos(x);
        if (cos.compareTo(BigDecimal.ZERO) == 0)
            throw new DevoreRuntimeException("tan(x)在cos(x)=0时未定义, 实际x=" + plain(x));
        return sin(x).divide(cos, DEFAULT_CONTEXT);
    }

    /**
     * sqrt(x)
     *
     * @param x  x
     * @return sqrt(x)
     */
    public static BigDecimal sqrt(BigDecimal x) {
        if (x.compareTo(BigDecimal.ZERO) < 0)
            throw new DevoreRuntimeException("sqrt(x)要求x不为负数, 实际x=" + plain(x));
        if (x.compareTo(BigDecimal.ZERO) == 0)
            return BigDecimal.ZERO;
        if (x.compareTo(BigDecimal.ONE) == 0)
            return BigDecimal.ONE;
        BigDecimal exact = exactNthRoot(x, 2);
        if (exact != null)
            return exact;
        BigDecimal guess = x.divide(TWO, DEFAULT_CONTEXT);
        BigDecimal tolerance = BigDecimal.ONE.scaleByPowerOfTen(-DEFAULT_CONTEXT.getPrecision());
        BigDecimal lastGuess;
        do {
            lastGuess = guess;
            guess = x.divide(guess, DEFAULT_CONTEXT).add(guess).divide(TWO, DEFAULT_CONTEXT);
        } while (guess.subtract(lastGuess).abs().compareTo(tolerance) > 0);
        return guess.round(DEFAULT_CONTEXT);
    }

    /**
     * cbrt(x)
     *
     * @param x  x
     * @return cbrt(x)
     */
    public static BigDecimal cbrt(BigDecimal x) {
        if (x.compareTo(BigDecimal.ZERO) == 0)
            return BigDecimal.ZERO;
        BigDecimal exact = exactNthRoot(x, 3);
        if (exact != null)
            return exact;
        boolean negative = x.compareTo(BigDecimal.ZERO) < 0;
        BigDecimal absX = negative ? x.negate() : x;
        BigDecimal guess = absX.divide(BigDecimal.valueOf(3), DEFAULT_CONTEXT);
        BigDecimal tolerance = BigDecimal.ONE.scaleByPowerOfTen(-DEFAULT_CONTEXT.getPrecision());
        BigDecimal lastGuess;
        do {
            lastGuess = guess;
            guess = guess.multiply(BigDecimal.valueOf(2), DEFAULT_CONTEXT)
                    .add(absX.divide(guess.multiply(guess, DEFAULT_CONTEXT), DEFAULT_CONTEXT))
                    .divide(BigDecimal.valueOf(3), DEFAULT_CONTEXT);
        } while (guess.subtract(lastGuess).abs().compareTo(tolerance) > 0);
        guess = guess.round(DEFAULT_CONTEXT);
        return negative ? guess.negate() : guess;
    }

    /**
     * x^y
     *
     * @param x  x
     * @param y  y
     * @return x^y
     */
    public static BigDecimal pow(BigDecimal x, BigDecimal y) {
        if (y.compareTo(BigDecimal.ZERO) == 0)
            return BigDecimal.ONE;
        if (x.compareTo(BigDecimal.ZERO) == 0) {
            if (y.compareTo(BigDecimal.ZERO) > 0)
                return BigDecimal.ZERO;
            throw new DevoreRuntimeException("x^y要求但x为0时, y不能为负数, 实际x="
                    + plain(x) + ", y=" + plain(y));
        }
        if (x.compareTo(BigDecimal.ONE) == 0)
            return BigDecimal.ONE;
        if (isInt(y))
            return powInt(x, y.toBigInteger());
        Fraction exponent = approximateFraction(y.abs());
        if (x.compareTo(BigDecimal.ZERO) < 0
                && !exponent.denominator.mod(BigInteger.valueOf(2)).equals(BigInteger.ONE))
            throw new DevoreRuntimeException("x^y要求但y为非整数且分母为偶数时, x不能为负数, 实际x="
                    + plain(x) + ", y=" + plain(y) + ", 近似指数分母=" + exponent.denominator);
        BigDecimal exact = powRationalExact(x.abs(), exponent, y.signum() < 0);
        if (exact != null) {
            if (x.compareTo(BigDecimal.ZERO) < 0 && exponent.numerator.testBit(0))
                exact = exact.negate();
            return exact;
        }
        BigDecimal exponentValue = divide(new BigDecimal(exponent.numerator), new BigDecimal(exponent.denominator));
        if (x.compareTo(BigDecimal.ZERO) > 0)
            return powPositiveBase(x, exponentValue, y.signum() < 0);
        BigDecimal result = powPositiveBase(x.abs(), exponentValue, y.signum() < 0);
        if (exponent.numerator.testBit(0))
            result = result.negate();
        return result;
    }

    /**
     * 将有限小数近似为分母较小的分数
     *
     * @param value 非负数
     * @return 分数
     */
    private static Fraction approximateFraction(BigDecimal value) {
        value = value.stripTrailingZeros();
        if (isInt(value))
            return new Fraction(value.toBigInteger(), BigInteger.ONE);
        int scale = Math.max(1, value.scale());
        BigDecimal tolerance = BigDecimal.valueOf(5).scaleByPowerOfTen(-scale - 1);
        BigInteger maxDenominator = BigInteger.TEN.pow(Math.max(1, DEFAULT_CONTEXT.getPrecision() / 2));
        MathContext workMc = new MathContext(DEFAULT_CONTEXT.getPrecision() + 5, DEFAULT_CONTEXT.getRoundingMode());
        BigInteger h0 = BigInteger.ZERO;
        BigInteger h1 = BigInteger.ONE;
        BigInteger k0 = BigInteger.ONE;
        BigInteger k1 = BigInteger.ZERO;
        BigDecimal current = value;
        Fraction best;
        while (true) {
            BigInteger a = current.toBigInteger();
            BigInteger h2 = a.multiply(h1).add(h0);
            BigInteger k2 = a.multiply(k1).add(k0);
            if (k2.compareTo(maxDenominator) > 0)
                break;
            best = new Fraction(h2, k2);
            BigDecimal approximation = new BigDecimal(h2).divide(new BigDecimal(k2), workMc);
            if (approximation.subtract(value).abs().compareTo(tolerance) <= 0)
                return best;
            BigDecimal remainder = current.subtract(new BigDecimal(a));
            if (remainder.compareTo(BigDecimal.ZERO) == 0)
                return best;
            h0 = h1;
            h1 = h2;
            k0 = k1;
            k1 = k2;
            current = BigDecimal.ONE.divide(remainder, workMc);
        }
        BigInteger denominator = BigInteger.TEN.pow(scale);
        BigInteger numerator = value.movePointRight(scale).toBigInteger();
        BigInteger gcd = gcd(numerator, denominator);
        return new Fraction(numerator.divide(gcd), denominator.divide(gcd));
    }

    /**
     * 正底数幂
     *
     * @param x        正底数
     * @param exponent 正指数
     * @param inverse  是否取倒数
     * @return x^exponent 或其倒数
     */
    private static BigDecimal powPositiveBase(BigDecimal x, BigDecimal exponent, boolean inverse) {
        BigDecimal result = roundIfCloseToInteger(exp(exponent.multiply(ln(x), DEFAULT_CONTEXT)));
        return inverse ? divide(BigDecimal.ONE, result) : result;
    }

    /**
     * 有理数指数幂的精确路径
     *
     * @param x        非负底数
     * @param exponent 指数的绝对值
     * @param inverse  是否取倒数
     * @return 可精确表示时返回精确结果, 否则返回null
     */
    private static BigDecimal powRationalExact(BigDecimal x, Fraction exponent, boolean inverse) {
        if (exponent.denominator.equals(BigInteger.ONE)) {
            BigDecimal result = powInt(x, exponent.numerator);
            return inverse ? divide(BigDecimal.ONE, result) : result;
        }
        if (exponent.denominator.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0)
            return null;
        BigDecimal root = exactNthRoot(x, exponent.denominator.intValue());
        if (root == null)
            return null;
        BigDecimal result = powInt(root, exponent.numerator);
        return inverse ? divide(BigDecimal.ONE, result) : result;
    }

    /**
     * 将非常接近整数的结果修正为整数
     *
     * @param value 值
     * @return 修正后的值
     */
    private static BigDecimal roundIfCloseToInteger(BigDecimal value) {
        BigDecimal tolerance = BigDecimal.ONE.scaleByPowerOfTen(-Math.max(1, DEFAULT_CONTEXT.getPrecision() - 4));
        BigDecimal rounded = value.setScale(0, RoundingMode.HALF_UP);
        return rounded.subtract(value).abs().compareTo(tolerance) <= 0 ? rounded : value;
    }

    /**
     * 分数
     */
    private static class Fraction {
        private final BigInteger numerator;     // 分子
        private final BigInteger denominator;   // 分母

        private Fraction(BigInteger numerator, BigInteger denominator) {
            this.numerator = numerator;
            this.denominator = denominator;
        }
    }

    /**
     * x^y, 其中 y 为整数
     *
     * @param x  x
     * @param y  y
     * @return x^y
     */
    private static BigDecimal powInt(BigDecimal x, BigInteger y) {
        if (y.signum() == 0)
            return BigDecimal.ONE;
        if (y.signum() < 0)
            return divide(BigDecimal.ONE, powInt(x, y.negate()));
        BigDecimal result = BigDecimal.ONE;
        BigDecimal base = x;
        BigInteger exp = y;
        while (exp.signum() > 0) {
            if (exp.testBit(0))
                result = result.multiply(base);
            base = base.multiply(base);
            exp = exp.shiftRight(1);
        }
        return result;
    }

    /**
     * 有限小数的精确n次根
     *
     * @param value 值
     * @param n     根次数
     * @return 存在有限小数精确结果时返回结果, 否则返回null
     */
    private static BigDecimal exactNthRoot(BigDecimal value, int n) {
        if (n <= 0)
            throw new DevoreRuntimeException("n次根要求n为正数, 实际n=" + n + ", value=" + plain(value));
        if (n > 10000)
            return null;
        if (value.compareTo(BigDecimal.ZERO) == 0)
            return BigDecimal.ZERO;
        boolean negative = value.compareTo(BigDecimal.ZERO) < 0;
        if (negative && n % 2 == 0)
            return null;
        BigDecimal normalized = value.abs().stripTrailingZeros();
        BigInteger unscaled = normalized.unscaledValue();
        int scale = normalized.scale();
        if (scale < 0) {
            unscaled = unscaled.multiply(BigInteger.TEN.pow(-scale));
            scale = 0;
        }
        int padding = Math.floorMod(-scale, n);
        if (padding > 0) {
            unscaled = unscaled.multiply(BigInteger.TEN.pow(padding));
            scale += padding;
        }
        BigInteger root = integerNthRootExact(unscaled, n);
        if (root == null)
            return null;
        BigDecimal result = new BigDecimal(root, scale / n).stripTrailingZeros();
        return negative ? result.negate() : result;
    }

    /**
     * 整数精确n次根
     *
     * @param value 非负整数
     * @param n     根次数
     * @return 存在整数精确结果时返回结果, 否则返回null
     */
    private static BigInteger integerNthRootExact(BigInteger value, int n) {
        if (value.signum() < 0)
            return null;
        if (value.equals(BigInteger.ZERO) || value.equals(BigInteger.ONE))
            return value;
        BigInteger low = BigInteger.ONE;
        BigInteger high = BigInteger.ONE.shiftLeft((value.bitLength() + n - 1) / n);
        while (low.compareTo(high) <= 0) {
            BigInteger mid = low.add(high).shiftRight(1);
            int compare = mid.pow(n).compareTo(value);
            if (compare == 0)
                return mid;
            if (compare < 0)
                low = mid.add(BigInteger.ONE);
            else
                high = mid.subtract(BigInteger.ONE);
        }
        return null;
    }

    /**
     * ln(x)
     *
     * @param x  x
     * @return ln(x)
     */
    public static BigDecimal ln(BigDecimal x) {
        if (x.compareTo(BigDecimal.ZERO) <= 0)
            throw new DevoreRuntimeException("ln(x)要求x为正数, 实际x=" + plain(x));
        BigDecimal term = x.subtract(BigDecimal.ONE).divide(x.add(BigDecimal.ONE), DEFAULT_CONTEXT);
        BigDecimal termSquared = term.multiply(term, DEFAULT_CONTEXT);
        BigDecimal result = term;
        BigDecimal currentTerm = term;
        BigDecimal tolerance = BigDecimal.ONE.scaleByPowerOfTen(-DEFAULT_CONTEXT.getPrecision());
        for (int k = 3; ; k += 2) {
            currentTerm = currentTerm.multiply(termSquared, DEFAULT_CONTEXT);
            BigDecimal delta = currentTerm.divide(BigDecimal.valueOf(k), DEFAULT_CONTEXT);
            result = result.add(delta, DEFAULT_CONTEXT);
            if (delta.abs().compareTo(tolerance) < 0)
                break;
        }
        return result.multiply(TWO, DEFAULT_CONTEXT);
    }

    /**
     * log_b(a)
     *
     * @param a  a
     * @param b  b
     * @return log_b(a)
     */
    public static BigDecimal log(BigDecimal a, BigDecimal b) {
        if (a.compareTo(BigDecimal.ZERO) <= 0)
            throw new DevoreRuntimeException("log_b(a)要求a为正数, 实际a=" + plain(a) + ", b=" + plain(b));
        if (b.compareTo(BigDecimal.ZERO) <= 0)
            throw new DevoreRuntimeException("log_b(a)要求b为正数, 实际a=" + plain(a) + ", b=" + plain(b));
        if (b.compareTo(BigDecimal.ONE) == 0)
            throw new DevoreRuntimeException("log_b(a)要求b不能为1, 实际a=" + plain(a) + ", b=" + plain(b));
        BigDecimal exact = exactIntegerLog(a, b);
        if (exact != null)
            return exact;
        return roundIfCloseToInteger(divide(ln(a), ln(b)));
    }

    /**
     * 正整数之间的精确对数
     *
     * @param a 真数
     * @param b 底数
     * @return a为b的整数次幂时返回指数, 否则返回null
     */
    private static BigDecimal exactIntegerLog(BigDecimal a, BigDecimal b) {
        BigDecimal normalizedA = a.stripTrailingZeros();
        BigDecimal normalizedB = b.stripTrailingZeros();
        if (!isInt(normalizedA) || !isInt(normalizedB))
            return null;
        BigInteger value = normalizedA.toBigInteger();
        BigInteger base = normalizedB.toBigInteger();
        if (value.signum() <= 0 || base.compareTo(BigInteger.ONE) <= 0)
            return null;
        if (value.equals(BigInteger.ONE))
            return BigDecimal.ZERO;
        int exponent = 0;
        while (value.compareTo(BigInteger.ONE) > 0) {
            BigInteger[] divRem = value.divideAndRemainder(base);
            if (divRem[1].signum() != 0)
                return null;
            value = divRem[0];
            ++exponent;
        }
        return BigDecimal.valueOf(exponent);
    }

    /**
     * e^x
     *
     * @param x  x
     * @return e^x
     */
    public static BigDecimal exp(BigDecimal x) {
        BigDecimal result = BigDecimal.ONE;
        BigDecimal term = BigDecimal.ONE;
        BigDecimal tolerance = BigDecimal.ONE.scaleByPowerOfTen(-DEFAULT_CONTEXT.getPrecision());
        int n = 1;
        while (true) {
            term = term.multiply(x, DEFAULT_CONTEXT).divide(BigDecimal.valueOf(n), DEFAULT_CONTEXT);
            result = result.add(term, DEFAULT_CONTEXT);
            if (term.abs().compareTo(tolerance) < 0)
                break;
            ++n;
        }
        return result.round(DEFAULT_CONTEXT);
    }
}
