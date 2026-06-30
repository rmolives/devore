package org.devore.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 哈希摘要工具
 */
public class HashUtils {
    /**
     * 使用指定算法计算字节数组的哈希摘要，并返回十六进制字符串
     *
     * @param bytes     字节数组
     * @param algorithm 哈希算法
     * @return 十六进制哈希摘要
     */
    public static String hash(byte[] bytes, String algorithm) {
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            byte[] result = digest.digest(bytes);
            return toHex(result);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("不支持的哈希算法：" + algorithm, e);
        }
    }

    /**
     * 将字节数组转换为小写十六进制字符串
     *
     * @param bytes 字节数组
     * @return 小写十六进制字符串
     */
    private static String toHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte b : bytes)
            builder.append(String.format("%02x", b & 0xff));
        return builder.toString();
    }
}
