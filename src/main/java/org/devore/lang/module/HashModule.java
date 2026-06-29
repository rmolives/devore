package org.devore.lang.module;

import org.devore.exception.DevoreCastException;
import org.devore.exception.DevoreRuntimeException;
import org.devore.lang.Env;
import org.devore.lang.token.DList;
import org.devore.lang.token.DString;
import org.devore.utils.DByteUtils;
import org.devore.utils.HashUtils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class HashModule extends DModule {
    /**
     * 创建Hash模块实例
     */
    public HashModule() {
        super("hash");
    }

    /**
     * 初始化哈希模块，注册MD5、SHA-1、SHA-256和SHA-512过程
     */
    @Override
    public void init(Env dEnv) {
        initHashProcedures(dEnv); // 哈希摘要
    }

    /**
     * 注册MD5、SHA-1、SHA-256和SHA-512过程
     */
    private void initHashProcedures(Env dEnv) {
        dEnv.addTokenProcedure("md5", ((args, env) -> {
            if (args.get(0) instanceof DString)
                return DString.valueOf(HashUtils.hash(args.get(0).toString()
                        .getBytes(StandardCharsets.UTF_8), "MD5"));
            else if (args.get(0) instanceof DList)
                return DString.valueOf(HashUtils.hash(DByteUtils.toBytes((DList) args.get(0)), "MD5"));
            throw new DevoreCastException(args.get(0).type(), "string|list");
        }), 1, false);
        dEnv.addTokenProcedure("sha1", ((args, env) -> {
            if (args.get(0) instanceof DString)
                return DString.valueOf(HashUtils.hash(args.get(0).toString()
                        .getBytes(StandardCharsets.UTF_8), "SHA-1"));
            else if (args.get(0) instanceof DList)
                return DString.valueOf(HashUtils.hash(DByteUtils.toBytes((DList) args.get(0)), "SHA-1"));
            throw new DevoreCastException(args.get(0).type(), "string|list");
        }), 1, false);
        dEnv.addTokenProcedure("sha256", ((args, env) -> {
            if (args.get(0) instanceof DString)
                return DString.valueOf(HashUtils.hash(args.get(0).toString()
                        .getBytes(StandardCharsets.UTF_8), "SHA-256"));
            else if (args.get(0) instanceof DList)
                return DString.valueOf(HashUtils.hash(DByteUtils.toBytes((DList) args.get(0)), "SHA-256"));
            throw new DevoreCastException(args.get(0).type(), "string|list");
        }), 1, false);
        dEnv.addTokenProcedure("sha512", ((args, env) -> {
            if (args.get(0) instanceof DString)
                return DString.valueOf(HashUtils.hash(args.get(0).toString()
                        .getBytes(StandardCharsets.UTF_8), "SHA-512"));
            else if (args.get(0) instanceof DList)
                return DString.valueOf(HashUtils.hash(DByteUtils.toBytes((DList) args.get(0)), "SHA-512"));
            throw new DevoreCastException(args.get(0).type(), "string|list");
        }), 1, false);
        dEnv.addTokenProcedure("md5", ((args, env) -> {
            if (!(args.get(0) instanceof DString))
                throw new DevoreCastException(args.get(0).type(), "string");
            if (!(args.get(1) instanceof DString))
                throw new DevoreCastException(args.get(1).type(), "string");
            Charset charset;
            try {
                charset = Charset.forName(args.get(1).toString());
            } catch (RuntimeException e) {
                throw new DevoreRuntimeException("字符集不存在: " + args.get(1));
            }
            return DString.valueOf(HashUtils.hash(args.get(0).toString().getBytes(charset), "MD5"));
        }), 2, false);
        dEnv.addTokenProcedure("sha1", ((args, env) -> {
            if (!(args.get(0) instanceof DString))
                throw new DevoreCastException(args.get(0).type(), "string");
            if (!(args.get(1) instanceof DString))
                throw new DevoreCastException(args.get(1).type(), "string");
            Charset charset;
            try {
                charset = Charset.forName(args.get(1).toString());
            } catch (RuntimeException e) {
                throw new DevoreRuntimeException("字符集不存在: " + args.get(1));
            }
            return DString.valueOf(HashUtils.hash(args.get(0).toString().getBytes(charset), "SHA-1"));
        }), 2, false);
        dEnv.addTokenProcedure("sha256", ((args, env) -> {
            if (!(args.get(0) instanceof DString))
                throw new DevoreCastException(args.get(0).type(), "string");
            if (!(args.get(1) instanceof DString))
                throw new DevoreCastException(args.get(1).type(), "string");
            Charset charset;
            try {
                charset = Charset.forName(args.get(1).toString());
            } catch (RuntimeException e) {
                throw new DevoreRuntimeException("字符集不存在: " + args.get(1));
            }
            return DString.valueOf(HashUtils.hash(args.get(0).toString().getBytes(charset), "SHA-256"));
        }), 2, false);
        dEnv.addTokenProcedure("sha512", ((args, env) -> {
            if (!(args.get(0) instanceof DString))
                throw new DevoreCastException(args.get(0).type(), "string");
            if (!(args.get(1) instanceof DString))
                throw new DevoreCastException(args.get(1).type(), "string");
            Charset charset;
            try {
                charset = Charset.forName(args.get(1).toString());
            } catch (RuntimeException e) {
                throw new DevoreRuntimeException("字符集不存在: " + args.get(1));
            }
            return DString.valueOf(HashUtils.hash(args.get(0).toString().getBytes(charset), "SHA-512"));
        }), 2, false);
    }
}
