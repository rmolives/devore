package org.devore.lang.module;

import org.devore.exception.DevoreCastException;
import org.devore.exception.DevoreRuntimeException;
import org.devore.lang.Env;
import org.devore.lang.token.DList;
import org.devore.lang.token.DString;
import org.devore.lang.token.DToken;
import org.devore.utils.DByteUtils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Base64编码和解码工具
 */
public class Base64Module extends DModule {
    /**
     * 创建Base64模块实例
     */
    public Base64Module() {
        super("base64");
    }

    /**
     * 初始化Base64模块，注册编码和解码过程
     */
    @Override
    public void init(Env dEnv) {
        initCodecProcedures(dEnv); // Base64编解码
    }

    /**
     * 注册Base64编码和解码过程
     */
    private void initCodecProcedures(Env dEnv) {
        dEnv.addTokenProcedure("base64-encode", (args, env) -> {
            DToken token = args.get(0);
            byte[] bytes;
            if (token instanceof DList)
                bytes = DByteUtils.toBytes((DList) token);
            else if (token instanceof DString)
                bytes = token.toString().getBytes(StandardCharsets.UTF_8);
            else
                throw new DevoreCastException(token.type(), "list|string");
            return DString.valueOf(Base64.getEncoder().encodeToString(bytes));
        }, 1, false);
        dEnv.addTokenProcedure("base64-encode", (args, env) -> {
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
            return DString.valueOf(Base64.getEncoder().encodeToString(args.get(0).toString().getBytes(charset)));
        }, 2, false);
        dEnv.addTokenProcedure("base64-decode", (args, env) -> {
            if (!(args.get(0) instanceof DString))
                throw new DevoreCastException(args.get(0).type(), "string");
            try {
                return DByteUtils.toList(Base64.getDecoder().decode(args.get(0).toString()));
            } catch (IllegalArgumentException e) {
                throw new DevoreRuntimeException("Base64解码失败: " + e.getMessage());
            }
        }, 1, false);
    }
}
