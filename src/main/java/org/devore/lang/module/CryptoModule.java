package org.devore.lang.module;

import org.devore.exception.DevoreCastException;
import org.devore.exception.DevoreRuntimeException;
import org.devore.lang.Env;
import org.devore.lang.token.*;
import org.devore.utils.DByteUtils;
import org.devore.utils.DIntUtils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 加密工具
 */
public class CryptoModule extends DModule {
    private static final String DEFAULT_AES_TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final String DEFAULT_RSA_TRANSFORMATION = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
    private static final String DEFAULT_RSA_SIGNATURE = "SHA256withRSA";
    private static final String DEFAULT_ECDSA_SIGNATURE = "SHA256withECDSA";
    private static final String DEFAULT_EC_CURVE = "secp256r1";

    /**
     * 创建Crypto模块实例
     */
    public CryptoModule() {
        super("crypto");
    }

    /**
     * 初始化加密模块，按算法注册加密解密过程
     */
    @Override
    public void init(Env dEnv) {
        initAesProcedures(dEnv);    // AES密钥生成、加密和解密
        initRsaProcedures(dEnv);    // RSA密钥生成、加密、解密、签名和验签
        initEcdsaProcedures(dEnv);  // ECDSA密钥生成、签名和验签
    }

    /**
     * 注册AES密钥生成、加密和解密过程
     */
    private void initAesProcedures(Env dEnv) {
        dEnv.addTokenProcedure("aes-key", (args, env) -> {
            if (!(args.get(0) instanceof DInt))
                throw new DevoreCastException(args.get(0).type(), "int");
            int bits = DIntUtils.toInt((DInt) args.get(0));
            if (bits != 128 && bits != 192 && bits != 256)
                throw new DevoreRuntimeException("AES密钥长度必须是128、192或256位.");
            try {
                KeyGenerator generator = KeyGenerator.getInstance("AES");
                generator.init(bits);
                SecretKey key = generator.generateKey();
                return DByteUtils.toList(key.getEncoded());
            } catch (GeneralSecurityException e) {
                throw new DevoreRuntimeException("AES密钥生成失败: " + e.getMessage());
            }
        }, 1, false);
        dEnv.addTokenProcedure("aes-encrypt", (args, env) ->
                aes(args.get(0), args.get(1), args.get(2), DEFAULT_AES_TRANSFORMATION, Cipher.ENCRYPT_MODE), 3, false);
        dEnv.addTokenProcedure("aes-encrypt", (args, env) -> {
            if (!(args.get(3) instanceof DString))
                throw new DevoreCastException(args.get(3).type(), "string");
            return aes(args.get(0), args.get(1), args.get(2), args.get(3).toString(), Cipher.ENCRYPT_MODE);
        }, 4, false);
        dEnv.addTokenProcedure("aes-decrypt", (args, env) ->
                aes(args.get(0), args.get(1), args.get(2), DEFAULT_AES_TRANSFORMATION, Cipher.DECRYPT_MODE), 3, false);
        dEnv.addTokenProcedure("aes-decrypt", (args, env) -> {
            if (!(args.get(3) instanceof DString))
                throw new DevoreCastException(args.get(3).type(), "string");
            return aes(args.get(0), args.get(1), args.get(2), args.get(3).toString(), Cipher.DECRYPT_MODE);
        }, 4, false);
    }

    /**
     * 注册RSA密钥生成、加密和解密过程
     */
    private void initRsaProcedures(Env dEnv) {
        dEnv.addTokenProcedure("rsa-keypair", (args, env) -> {
            if (!(args.get(0) instanceof DInt))
                throw new DevoreCastException(args.get(0).type(), "int");
            return rsaKeyPair(DIntUtils.toInt((DInt) args.get(0)));
        }, 1, false);
        dEnv.addTokenProcedure("rsa-encrypt", (args, env) ->
                rsa(args.get(0), args.get(1), DEFAULT_RSA_TRANSFORMATION, Cipher.ENCRYPT_MODE), 2, false);
        dEnv.addTokenProcedure("rsa-encrypt", (args, env) -> {
            if (!(args.get(2) instanceof DString))
                throw new DevoreCastException(args.get(2).type(), "string");
            return rsa(args.get(0), args.get(1), args.get(2).toString(), Cipher.ENCRYPT_MODE);
        }, 3, false);
        dEnv.addTokenProcedure("rsa-decrypt", (args, env) ->
                rsa(args.get(0), args.get(1), DEFAULT_RSA_TRANSFORMATION, Cipher.DECRYPT_MODE), 2, false);
        dEnv.addTokenProcedure("rsa-decrypt", (args, env) -> {
            if (!(args.get(2) instanceof DString))
                throw new DevoreCastException(args.get(2).type(), "string");
            return rsa(args.get(0), args.get(1), args.get(2).toString(), Cipher.DECRYPT_MODE);
        }, 3, false);
        dEnv.addTokenProcedure("rsa-sign", (args, env) ->
                sign(args.get(0), args.get(1), "RSA", DEFAULT_RSA_SIGNATURE), 2, false);
        dEnv.addTokenProcedure("rsa-sign", (args, env) -> {
            if (!(args.get(2) instanceof DString))
                throw new DevoreCastException(args.get(2).type(), "string");
            return sign(args.get(0), args.get(1), "RSA", args.get(2).toString());
        }, 3, false);
        dEnv.addTokenProcedure("rsa-verify", (args, env) ->
                verify(args.get(0), args.get(1), args.get(2), "RSA", DEFAULT_RSA_SIGNATURE), 3, false);
        dEnv.addTokenProcedure("rsa-verify", (args, env) -> {
            if (!(args.get(3) instanceof DString))
                throw new DevoreCastException(args.get(3).type(), "string");
            return verify(args.get(0), args.get(1), args.get(2), "RSA", args.get(3).toString());
        }, 4, false);
    }

    private void initEcdsaProcedures(Env dEnv) {
        dEnv.addTokenProcedure("ecdsa-keypair", (args, env) -> ecKeyPair(DEFAULT_EC_CURVE), 0, false);
        dEnv.addTokenProcedure("ecdsa-keypair", (args, env) -> {
            if (!(args.get(0) instanceof DString))
                throw new DevoreCastException(args.get(0).type(), "string");
            return ecKeyPair(args.get(0).toString());
        }, 1, false);
        dEnv.addTokenProcedure("ecdsa-sign", (args, env) ->
                sign(args.get(0), args.get(1), "EC", DEFAULT_ECDSA_SIGNATURE), 2, false);
        dEnv.addTokenProcedure("ecdsa-sign", (args, env) -> {
            if (!(args.get(2) instanceof DString))
                throw new DevoreCastException(args.get(2).type(), "string");
            return sign(args.get(0), args.get(1), "EC", args.get(2).toString());
        }, 3, false);
        dEnv.addTokenProcedure("ecdsa-verify", (args, env) ->
                verify(args.get(0), args.get(1), args.get(2), "EC", DEFAULT_ECDSA_SIGNATURE), 3, false);
        dEnv.addTokenProcedure("ecdsa-verify", (args, env) -> {
            if (!(args.get(3) instanceof DString))
                throw new DevoreCastException(args.get(3).type(), "string");
            return verify(args.get(0), args.get(1), args.get(2), "EC", args.get(3).toString());
        }, 4, false);
    }

    /**
     * 执行AES加密或解密
     */
    private DList aes(DToken data, DToken key, DToken iv, String transformation, int mode) {
        try {
            byte[] keyBytes = binary(key);
            byte[] ivBytes = binary(iv);
            byte[] dataBytes = binary(data);
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
            Cipher cipher = Cipher.getInstance(transformation);
            cipher.init(mode, keySpec, new IvParameterSpec(ivBytes));
            return DByteUtils.toList(cipher.doFinal(dataBytes));
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            String action = mode == Cipher.ENCRYPT_MODE ? "AES加密" : "AES解密";
            throw new DevoreRuntimeException(action + "失败: " + e.getMessage());
        }
    }

    /**
     * 执行RSA加密或解密
     */
    private DList rsa(DToken data, DToken key, String transformation, int mode) {
        try {
            Cipher cipher = Cipher.getInstance(transformation);
            if (mode == Cipher.ENCRYPT_MODE)
                cipher.init(mode, publicKey("RSA", key));
            else
                cipher.init(mode, privateKey("RSA", key));
            byte[] dataBytes = binary(data);
            return DByteUtils.toList(cipher.doFinal(dataBytes));
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            String action = mode == Cipher.ENCRYPT_MODE ? "RSA加密" : "RSA解密";
            throw new DevoreRuntimeException(action + "失败: " + e.getMessage());
        }
    }

    /**
     * 生成指定曲线的EC密钥对
     */
    private DToken ecKeyPair(String curve) {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
            generator.initialize(new ECGenParameterSpec(curve));
            return keyPairTable(generator.generateKeyPair());
        } catch (GeneralSecurityException e) {
            throw new DevoreRuntimeException("ECDSA密钥生成失败: " + e.getMessage());
        }
    }

    /**
     * 使用指定算法生成数字签名
     */
    private DList sign(DToken data, DToken privateKey, String keyAlgorithm, String signatureAlgorithm) {
        try {
            Signature signature = Signature.getInstance(signatureAlgorithm);
            signature.initSign(privateKey(keyAlgorithm, privateKey));
            byte[] dataBytes = binary(data);
            signature.update(dataBytes);
            return DByteUtils.toList(signature.sign());
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            throw new DevoreRuntimeException("签名失败: " + e.getMessage());
        }
    }

    /**
     * 使用指定算法验证数字签名
     */
    private DBool verify(DToken data, DToken signed, DToken publicKey, String keyAlgorithm, String signatureAlgorithm) {
        try {
            Signature signature = Signature.getInstance(signatureAlgorithm);
            signature.initVerify(publicKey(keyAlgorithm, publicKey));
            byte[] dataBytes = binary(data);
            byte[] signedBytes = binary(signed);
            signature.update(dataBytes);
            return DBool.valueOf(signature.verify(signedBytes));
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            throw new DevoreRuntimeException("验签失败: " + e.getMessage());
        }
    }

    /**
     * 将Devore binary(list)转换为字节数组。
     */
    private static byte[] binary(DToken token) {
        if (!(token instanceof DList))
            throw new DevoreCastException(token.type(), "list");
        return DByteUtils.toBytes((DList) token);
    }

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
     * 将Java密钥对转换为DTable
     *
     * @param keyPair Java密钥对
     * @return 包含public和private字段的表
     */
    public static DTable keyPairTable(KeyPair keyPair) {
        Map<DToken, DToken> table = new HashMap<>();
        table.put(DString.valueOf("public"),
                DString.valueOf(Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded())));
        table.put(DString.valueOf("private"),
                DString.valueOf(Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded())));
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
