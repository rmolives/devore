package org.wumoe.devore.utils;

import java.math.BigDecimal;
import java.math.MathContext;

public class NumUtils {
    public static BigDecimal sin(BigDecimal angle) {
        BigDecimal result = BigDecimal.ZERO;
        BigDecimal term = angle;
        BigDecimal factorial = BigDecimal.ONE;
        BigDecimal sign = BigDecimal.ONE;
        int maxIterations = 50;
        for (int i = 1; i <= maxIterations; ++i) {
            result = result.add(term);
            term = term.multiply(angle).multiply(angle).divide(factorial, MathContext.DECIMAL128);
            factorial = factorial.multiply(BigDecimal.valueOf((2L * i) * (2L * i + 1)));
            sign = sign.negate();
        }
        return result;
    }

    public static BigDecimal cos(BigDecimal angle) {
        BigDecimal result = BigDecimal.ONE;
        BigDecimal term = BigDecimal.ONE;
        BigDecimal factorial = BigDecimal.ONE;
        BigDecimal sign = BigDecimal.ONE;
        int maxIterations = 50;
        for (int i = 1; i <= maxIterations; ++i) {
            term = term.multiply(angle).multiply(angle).divide(factorial, MathContext.DECIMAL128);
            factorial = factorial.multiply(BigDecimal.valueOf((2L * i) * (2L * i - 1)));
            sign = sign.negate();
            result = result.add(term.multiply(sign));
        }
        return result;
    }

    public static BigDecimal tan(BigDecimal angle) {
        BigDecimal result = BigDecimal.ZERO;
        BigDecimal term = angle;
        BigDecimal factorial = BigDecimal.ONE;
        BigDecimal sign = BigDecimal.ONE;
        int maxIterations = 50;
        for (int i = 1; i <= maxIterations; ++i) {
            result = result.add(term);
            term = term.multiply(angle).multiply(angle).divide(factorial, MathContext.DECIMAL128);
            factorial = factorial.multiply(BigDecimal.valueOf((2L * i) + 1));
            sign = sign.negate();
        }
        return result;
    }

    public static boolean isInt(BigDecimal decimal) {
        BigDecimal stripped = decimal.stripTrailingZeros();
        return stripped.scale() <= 0;
    }
}
