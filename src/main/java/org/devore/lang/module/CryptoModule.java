package org.devore.lang.module;

import org.devore.exception.DevoreCastException;
import org.devore.exception.DevoreRuntimeException;
import org.devore.lang.Env;
import org.devore.lang.token.*;
import org.devore.utils.DCryptoUtils;
import org.devore.utils.DByteUtils;
import org.devore.utils.DIntUtils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.ECGenParameterSpec;

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
            return DCryptoUtils.rsaKeyPair(DIntUtils.toInt((DInt) args.get(0)));
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
            byte[] keyBytes;
            if (key instanceof DList)
                keyBytes = DByteUtils.toBytes((DList) key);
            else if (key instanceof DString)
                keyBytes = key.toString().getBytes(StandardCharsets.UTF_8);
            else
                throw new DevoreCastException(key.type(), "list|string");
            byte[] ivBytes;
            if (iv instanceof DList)
                ivBytes = DByteUtils.toBytes((DList) iv);
            else if (iv instanceof DString)
                ivBytes = iv.toString().getBytes(StandardCharsets.UTF_8);
            else
                throw new DevoreCastException(iv.type(), "list|string");
            byte[] dataBytes;
            if (data instanceof DList)
                dataBytes = DByteUtils.toBytes((DList) data);
            else if (data instanceof DString)
                dataBytes = data.toString().getBytes(StandardCharsets.UTF_8);
            else
                throw new DevoreCastException(data.type(), "list|string");
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
                cipher.init(mode, DCryptoUtils.publicKey("RSA", key));
            else
                cipher.init(mode, DCryptoUtils.privateKey("RSA", key));
            byte[] dataBytes;
            if (data instanceof DList)
                dataBytes = DByteUtils.toBytes((DList) data);
            else if (data instanceof DString)
                dataBytes = data.toString().getBytes(StandardCharsets.UTF_8);
            else
                throw new DevoreCastException(data.type(), "list|string");
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
            return DCryptoUtils.keyPairTable(generator.generateKeyPair());
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
            signature.initSign(DCryptoUtils.privateKey(keyAlgorithm, privateKey));
            byte[] dataBytes;
            if (data instanceof DList)
                dataBytes = DByteUtils.toBytes((DList) data);
            else if (data instanceof DString)
                dataBytes = data.toString().getBytes(StandardCharsets.UTF_8);
            else
                throw new DevoreCastException(data.type(), "list|string");
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
            signature.initVerify(DCryptoUtils.publicKey(keyAlgorithm, publicKey));
            byte[] dataBytes;
            if (data instanceof DList)
                dataBytes = DByteUtils.toBytes((DList) data);
            else if (data instanceof DString)
                dataBytes = data.toString().getBytes(StandardCharsets.UTF_8);
            else
                throw new DevoreCastException(data.type(), "list|string");
            byte[] signedBytes;
            if (signed instanceof DList)
                signedBytes = DByteUtils.toBytes((DList) signed);
            else if (signed instanceof DString)
                signedBytes = signed.toString().getBytes(StandardCharsets.UTF_8);
            else
                throw new DevoreCastException(signed.type(), "list|string");
            signature.update(dataBytes);
            return DBool.valueOf(signature.verify(signedBytes));
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            throw new DevoreRuntimeException("验签失败: " + e.getMessage());
        }
    }
}
