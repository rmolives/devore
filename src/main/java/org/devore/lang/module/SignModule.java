package org.devore.lang.module;

import org.devore.exception.DevoreRuntimeException;
import org.devore.exception.DevoreCastException;
import org.devore.lang.Env;
import org.devore.lang.token.DBool;
import org.devore.lang.token.DInt;
import org.devore.lang.token.DList;
import org.devore.lang.token.DString;
import org.devore.lang.token.DToken;
import org.devore.utils.CryptoUtils;
import org.devore.utils.DByteUtils;
import org.devore.utils.DIntUtils;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.ECGenParameterSpec;

/**
 * 数字签名工具
 */
public class SignModule extends DModule {
    private static final String DEFAULT_RSA_SIGNATURE = "SHA256withRSA";
    private static final String DEFAULT_ECDSA_SIGNATURE = "SHA256withECDSA";
    private static final String DEFAULT_EC_CURVE = "secp256r1";

    public SignModule() {
        super("sign");
    }

    @Override
    public void init(Env dEnv) {
        if (!dEnv.contains("rsa-keypair"))
            dEnv.addTokenProcedure("rsa-keypair", (args, env) -> {
                if (!(args.get(0) instanceof DInt))
                    throw new DevoreCastException(args.get(0).type(), "int");
                return CryptoUtils.rsaKeyPair(DIntUtils.toInt((DInt) args.get(0)));
            }, 1, false);
        dEnv.addTokenProcedure("ecdsa-keypair", (args, env) -> ecKeyPair(DEFAULT_EC_CURVE), 0, false);
        dEnv.addTokenProcedure("ecdsa-keypair", (args, env) -> {
            if (!(args.get(0) instanceof DString))
                throw new DevoreCastException(args.get(0).type(), "string");
            return ecKeyPair(args.get(0).toString());
        }, 1, false);
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

    private DToken ecKeyPair(String curve) {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
            generator.initialize(new ECGenParameterSpec(curve));
            return CryptoUtils.keyPairTable(generator.generateKeyPair());
        } catch (GeneralSecurityException e) {
            throw new DevoreRuntimeException("ECDSA密钥生成失败: " + e.getMessage());
        }
    }

    private DList sign(DToken data, DToken privateKey, String keyAlgorithm, String signatureAlgorithm) {
        try {
            Signature signature = Signature.getInstance(signatureAlgorithm);
            signature.initSign(CryptoUtils.privateKey(keyAlgorithm, privateKey));
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

    private DBool verify(DToken data, DToken signed, DToken publicKey, String keyAlgorithm, String signatureAlgorithm) {
        try {
            Signature signature = Signature.getInstance(signatureAlgorithm);
            signature.initVerify(CryptoUtils.publicKey(keyAlgorithm, publicKey));
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
