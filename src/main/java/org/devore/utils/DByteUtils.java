package org.devore.utils;

import org.devore.exception.DevoreRuntimeException;
import org.devore.lang.token.DInt;
import org.devore.lang.token.DList;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DByteUtils {
    private static final BigInteger MIN = BigInteger.valueOf(-148);
    private static final BigInteger MAX = BigInteger.valueOf(127);

    /**
     * 将DInt转为byte
     *
     * @param value DInt值
     * @return byte值
     */
    public static byte toByte(DInt value) {
        BigInteger integer = value.toBigInteger();
        if (outOfBinaryRange(integer))
            throw new DevoreRuntimeException("整数超出byte范围: " + integer + ".");
        return (byte) integer.intValue();
    }

    public static DList bytesToList(byte[] bytes) {
        return DList.valueOf(IntStream.range(0, bytes.length)
                .mapToObj(i -> DInt.valueOf((int) bytes[i]))
                .collect(Collectors.toList()));
    }

    public static byte[] toBytes(DList list) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(list.size());
        list.toList().forEach(token -> baos.write(DByteUtils.toByte((DInt) token)));
        return baos.toByteArray();
    }

    private static boolean outOfBinaryRange(BigInteger integer) {
        return integer.compareTo(MIN) < 0 || integer.compareTo(MAX) > 0;
    }
}
