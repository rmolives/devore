package org.devore.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtils {
    public static String hash(byte[] bytes, String algorithm) {
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            byte[] result = digest.digest(bytes);
            return toHex(result);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("不支持的哈希算法：" + algorithm, e);
        }
    }

    private static String toHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte b : bytes)
            builder.append(String.format("%02x", b & 0xff));
        return builder.toString();
    }
}
