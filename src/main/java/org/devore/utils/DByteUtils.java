package org.devore.utils;

import org.devore.exception.DevoreRuntimeException;
import org.devore.lang.token.DInt;
import org.devore.lang.token.DList;
import org.devore.lang.token.DToken;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DByteUtils {
    private static final BigInteger MIN = BigInteger.valueOf(Byte.MIN_VALUE);
    private static final BigInteger MAX = BigInteger.valueOf(Byte.MAX_VALUE);

    /**
     * 将DInt转为byte
     *
     * @param value DInt值
     * @return byte值
     */
    public static byte toByte(DInt value) {
        BigInteger integer = value.toBigInteger();
        if (outOfByteRange(integer))
            throw new DevoreRuntimeException("整数超出byte范围: " + integer);
        return (byte) integer.intValue();
    }

    /**
     * 将bytes转为DList
     *
     * @param bytes bytes
     * @return DList
     */
    public static DList toList(byte[] bytes) {
        return DList.valueOf(IntStream.range(0, bytes.length)
                .mapToObj(i -> DInt.valueOf((int) bytes[i]))
                .collect(Collectors.toList()));
    }

    /**
     * 将DList转为byte
     *
     * @param value DList值
     * @return bytes
     */
    public static byte[] toBytes(DList value) {
        byte[] bytes = new byte[value.size()];
        List<DToken> list = value.toList();
        for (int i = 0; i < list.size(); ++i)
            bytes[i] = DByteUtils.toByte((DInt) list.get(i));
        return bytes;
    }

    private static boolean outOfByteRange(BigInteger integer) {
        return integer.compareTo(MIN) < 0 || integer.compareTo(MAX) > 0;
    }
}
