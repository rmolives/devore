package org.wumoe.devore.utils;

import org.wumoe.devore.exception.DevoreRuntimeException;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class NumberUtils {
    private static final BigDecimal TWO = BigDecimal.valueOf(2);
    private static final BigDecimal PI = approximatePi();
    private static final BigDecimal TWO_PI = PI.multiply(TWO);

    private static BigDecimal approximatePi() {
        MathContext mc = new MathContext(100 + 2, RoundingMode.HALF_EVEN);
        BigDecimal term1 = arctan(BigDecimal.ONE.divide(BigDecimal.valueOf(5), MathContext.DECIMAL128), mc);
        BigDecimal term2 = arctan(BigDecimal.ONE.divide(BigDecimal.valueOf(239), MathContext.DECIMAL128), mc);
        BigDecimal pi = BigDecimal.valueOf(4).multiply(
                BigDecimal.valueOf(4).multiply(term1).subtract(term2)
        );
        return pi.round(new MathContext(100, RoundingMode.HALF_EVEN));
    }

    private static BigDecimal arctan(BigDecimal x, MathContext mc) {
        BigDecimal result = BigDecimal.ZERO;
        BigDecimal xSquared = x.multiply(x);
        BigDecimal term = x;

        boolean add = true;
        BigDecimal tolerance = BigDecimal.ONE.scaleByPowerOfTen(-mc.getPrecision());

        for (int i = 1; term.abs().compareTo(tolerance) > 0; i += 2) {
            if (add)
                result = result.add(term);
            else
                result = result.subtract(term);
            term = term.multiply(xSquared).divide(
                    BigDecimal.valueOf(i + 2), mc);
            add = !add;
        }

        return result;
    }

    private static BigDecimal reduceAngle(BigDecimal angle, MathContext mc) {
        angle = angle.remainder(TWO_PI, mc);
        if (angle.compareTo(PI) > 0)
            angle = angle.subtract(TWO_PI);
        else if (angle.compareTo(PI.negate()) < 0)
            angle = angle.add(TWO_PI);
        return angle;
    }

    public static BigDecimal sin(BigDecimal x, MathContext mc) {
        x = reduceAngle(x, mc);
        BigDecimal result = BigDecimal.ZERO;
        BigDecimal term = x;
        BigDecimal xSquared = x.multiply(x);
        BigDecimal tolerance = BigDecimal.ONE.scaleByPowerOfTen(-mc.getPrecision());
        int i = 1;
        while (term.abs().compareTo(tolerance) > 0) {
            result = result.add(term);
            term = term.multiply(xSquared).negate()
                    .divide(BigDecimal.valueOf((long) (i + 1) * (i + 2)), mc);
            i += 2;
        }
        return result.round(mc);
    }

    public static BigDecimal cos(BigDecimal x, MathContext mc) {
        x = reduceAngle(x, mc);

        BigDecimal result = BigDecimal.ZERO;
        BigDecimal term = BigDecimal.ONE;
        BigDecimal xSquared = x.multiply(x);
        BigDecimal tolerance = BigDecimal.ONE.scaleByPowerOfTen(-mc.getPrecision());
        int i = 0;

        while (term.abs().compareTo(tolerance) > 0) {
            result = result.add(term);
            term = term.multiply(xSquared).negate()
                    .divide(BigDecimal.valueOf((long) (i + 1) * (i + 2)), mc);
            i += 2;
        }
        return result.round(mc);
    }

    public static BigDecimal tan(BigDecimal x, MathContext mc) {
        BigDecimal cos = cos(x, mc);
        if (cos.compareTo(BigDecimal.ZERO) == 0)
            throw new DevoreRuntimeException("此角度的切线未定义.");
        return sin(x, mc).divide(cos, mc);
    }

    public static BigDecimal sqrt(BigDecimal x, MathContext mc) {
        if (x.compareTo(BigDecimal.ZERO) < 0) {
            throw new DevoreRuntimeException("负数的平方根.");
        }

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
        try {
            return powInt(x, y.intValueExact(), mc);
        } catch (ArithmeticException ignored) {
        }

        if (x.compareTo(BigDecimal.ZERO) < 0)
            throw new DevoreRuntimeException("非整数指数的负数.");
        return exp(y.multiply(ln(x, mc)), mc);
    }

    private static BigDecimal powInt(BigDecimal x, int y, MathContext mc) {
        if (y < 0)
            return BigDecimal.ONE.divide(powInt(x, -y, mc), mc);
        BigDecimal result = BigDecimal.ONE;
        BigDecimal base = x;
        int exp = y;
        while (exp > 0) {
            if (exp % 2 == 1)
                result = result.multiply(base, mc);
            base = base.multiply(base, mc);
            exp >>= 1;
        }

        return result;
    }

    public static BigDecimal ln(BigDecimal x, MathContext mc) {
        if (x.compareTo(BigDecimal.ZERO) <= 0)
            throw new DevoreRuntimeException("非正数的对数.");
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
