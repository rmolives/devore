package org.devore.utils;

import org.devore.exception.DevoreRuntimeException;
import org.devore.lang.token.DInt;

import java.math.BigInteger;

/**
 * DInt工具
 */
public class DIntUtils {
    private static final BigInteger MIN_INT = BigInteger.valueOf(Integer.MIN_VALUE);
    private static final BigInteger MAX_INT = BigInteger.valueOf(Integer.MAX_VALUE);

    /**
     * 将DInt转为int
     *
     * @param value DInt值
     * @return int值
     */
    public static int toInt(DInt value) {
        BigInteger integer = value.toBigInteger();
        if (outOfIntRange(integer))
            throw new DevoreRuntimeException("整数超出int范围: " + integer + ".");
        return integer.intValue();
    }

    /**
     * 将DInt下标转为int
     *
     * @param value DInt下标
     * @return int下标
     */
    public static int toIndex(DInt value) {
        BigInteger integer = value.toBigInteger();
        if (outOfIntRange(integer))
            throw new DevoreRuntimeException("下标超出int范围: " + integer + ".");
        return integer.intValue();
    }

    private static boolean outOfIntRange(BigInteger integer) {
        return integer.compareTo(MIN_INT) < 0 || integer.compareTo(MAX_INT) > 0;
    }
}
