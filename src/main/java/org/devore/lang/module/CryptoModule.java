package org.devore.lang.module;

import org.devore.exception.DevoreCastException;
import org.devore.exception.DevoreRuntimeException;
import org.devore.lang.Env;
import org.devore.lang.token.DInt;
import org.devore.lang.token.DList;
import org.devore.lang.token.DString;
import org.devore.lang.token.DToken;
import org.devore.utils.CryptoUtils;
import org.devore.utils.DByteUtils;
import org.devore.utils.DNumberUtils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;

/**
 * 加密工具
 */
public class CryptoModule extends DModule {
    private static final String DEFAULT_AES_TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final String DEFAULT_RSA_TRANSFORMATION = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";

    public CryptoModule() {
        super("crypto");
    }

    @Override
    public void init(Env dEnv) {
        dEnv.addTokenProcedure("aes-key", (args, env) -> {
            if (!(args.get(0) instanceof DInt))
                throw new DevoreCastException(args.get(0).type(), "int");
            int bits = DNumberUtils.toInt((DInt) args.get(0));
            if (bits != 128 && bits != 192 && bits != 256)
                throw new DevoreRuntimeException("AES密钥长度必须是128、192或256位.");
            try {
                KeyGenerator generator = KeyGenerator.getInstance("AES");
                generator.init(bits);
                SecretKey key = generator.generateKey();
                return DByteUtils.toList(key.getEncoded());
            } catch (GeneralSecurityException e) {
                throw CryptoUtils.cryptoError("AES密钥生成", e);
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
        if (!dEnv.contains("rsa-keypair"))
            dEnv.addTokenProcedure("rsa-keypair", (args, env) -> {
                if (!(args.get(0) instanceof DInt))
                    throw new DevoreCastException(args.get(0).type(), "int");
                return CryptoUtils.rsaKeyPair(DNumberUtils.toInt((DInt) args.get(0)));
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
    }

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
            throw CryptoUtils.cryptoError(mode == Cipher.ENCRYPT_MODE ? "AES加密" : "AES解密", e);
        }
    }

    private DList rsa(DToken data, DToken key, String transformation, int mode) {
        try {
            Cipher cipher = Cipher.getInstance(transformation);
            if (mode == Cipher.ENCRYPT_MODE)
                cipher.init(mode, CryptoUtils.publicKey("RSA", key));
            else
                cipher.init(mode, CryptoUtils.privateKey("RSA", key));
            byte[] dataBytes;
            if (data instanceof DList)
                dataBytes = DByteUtils.toBytes((DList) data);
            else if (data instanceof DString)
                dataBytes = data.toString().getBytes(StandardCharsets.UTF_8);
            else
                throw new DevoreCastException(data.type(), "list|string");
            return DByteUtils.toList(cipher.doFinal(dataBytes));
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            throw CryptoUtils.cryptoError(mode == Cipher.ENCRYPT_MODE ? "RSA加密" : "RSA解密", e);
        }
    }
}
