package org.devore.utils;

import org.devore.exception.DevoreCastException;
import org.devore.exception.DevoreRuntimeException;
import org.devore.lang.token.*;

import java.security.*;
import java.security.spec.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 加密和签名模块共享的工具函数
 */
public class CryptoUtils {
    /**
     * 从Base64编码的X.509公钥字符串解析公钥
     *
     * @param algorithm 密钥算法，如RSA或EC
     * @param token     Base64编码的公钥字符串
     * @return 公钥对象
     */
    public static PublicKey publicKey(String algorithm, DToken token) {
        if (!(token instanceof DString))
            throw new DevoreCastException(token.type(), "string");
        try {
            byte[] encoded = Base64.getDecoder().decode(token.toString());
            return KeyFactory.getInstance(algorithm).generatePublic(new X509EncodedKeySpec(encoded));
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            throw new DevoreRuntimeException("公钥解析失败: " + e.getMessage());
        }
    }

    /**
     * 从Base64编码的PKCS#8私钥字符串解析私钥
     *
     * @param algorithm 密钥算法，如RSA或EC
     * @param token     Base64编码的私钥字符串
     * @return 私钥对象
     */
    public static PrivateKey privateKey(String algorithm, DToken token) {
        if (!(token instanceof DString))
            throw new DevoreCastException(token.type(), "string");
        try {
            byte[] encoded = Base64.getDecoder().decode(token.toString());
            return KeyFactory.getInstance(algorithm).generatePrivate(new PKCS8EncodedKeySpec(encoded));
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            throw new DevoreRuntimeException("私钥解析失败: " + e.getMessage());
        }
    }

    /**
     * 将Java密钥对转换为Devore表
     *
     * @param keyPair Java密钥对
     * @return 包含public和private字段的表
     */
    public static DTable keyPairTable(KeyPair keyPair) {
        Map<DToken, DToken> table = new HashMap<>();
        table.put(DString.valueOf("public"), DString.valueOf(Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded())));
        table.put(DString.valueOf("private"), DString.valueOf(Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded())));
        return DTable.valueOf(table);
    }

    /**
     * 生成RSA密钥对
     *
     * @param bits 密钥位数，不能小于1024
     * @return 包含public和private字段的表
     */
    public static DTable rsaKeyPair(int bits) {
        if (bits < 1024)
            throw new DevoreRuntimeException("RSA密钥长度不能小于1024位.");
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(bits);
            return keyPairTable(generator.generateKeyPair());
        } catch (GeneralSecurityException e) {
            throw new DevoreRuntimeException("RSA密钥生成失败: " + e.getMessage());
        }
    }
}
