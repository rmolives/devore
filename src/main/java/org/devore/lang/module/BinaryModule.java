package org.devore.lang.module;

import org.devore.exception.DevoreCastException;
import org.devore.exception.DevoreRuntimeException;
import org.devore.lang.Env;
import org.devore.lang.token.*;
import org.devore.utils.DByteUtils;
import org.devore.utils.DIntUtils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 二进制数据
 */
public class BinaryModule extends DModule {
    public BinaryModule() {
        super("binary");
    }

    @Override
    public void init(Env dEnv) {
        dEnv.addTokenProcedure("random-binary", (args, env) -> {
            if (!(args.get(0) instanceof DInt))
                throw new DevoreCastException(args.get(0).type(), "int");
            int size = DIntUtils.toInt((DInt) args.get(0));
            if (size < 0)
                throw new DevoreRuntimeException("随机字节长度不能为负数: " + size);
            byte[] bytes = new byte[size];
            new SecureRandom().nextBytes(bytes);
            return DByteUtils.toList(bytes);
        }, 1, false);
        dEnv.addTokenProcedure("string->binary", (args, env) -> {
            if (!(args.get(0) instanceof DString))
                throw new DevoreCastException(args.get(0).type(), "string");
            return DByteUtils.toList(args.get(0).toString().getBytes(StandardCharsets.UTF_8));
        }, 1, false);
        dEnv.addTokenProcedure("string->binary", (args, env) -> {
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
            return DByteUtils.toList(args.get(0).toString().getBytes(charset));
        }, 2, false);
        dEnv.addTokenProcedure("binary->string", (args, env) -> {
            if (!(args.get(0) instanceof DList))
                throw new DevoreCastException(args.get(0).type(), "list");
            return DString.valueOf(new String(DByteUtils.toBytes((DList) args.get(0)), StandardCharsets.UTF_8));
        }, 1, false);
        dEnv.addTokenProcedure("binary->string", (args, env) -> {
            if (!(args.get(0) instanceof DList))
                throw new DevoreCastException(args.get(0).type(), "list");
            if (!(args.get(1) instanceof DString))
                throw new DevoreCastException(args.get(1).type(), "string");
            Charset charset;
            try {
                charset = Charset.forName(args.get(1).toString());
            } catch (RuntimeException e) {
                throw new DevoreRuntimeException("字符集不存在: " + args.get(1) + ".");
            }
            return DString.valueOf(new String(DByteUtils.toBytes((DList) args.get(0)), charset));
        }, 2, false);
        dEnv.addTokenProcedure("binary->hex", (args, env) -> {
            if (!(args.get(0) instanceof DList))
                throw new DevoreCastException(args.get(0).type(), "list");
            byte[] bytes = DByteUtils.toBytes((DList) args.get(0));
            return DString.valueOf(IntStream.range(0, bytes.length)
                    .mapToObj(i -> String.format("%02x", Byte.toUnsignedInt(bytes[i])))
                    .collect(Collectors.joining()));
        }, 1, false);
        dEnv.addTokenProcedure("hex->binary", (args, env) -> {
            if (!(args.get(0) instanceof DString))
                throw new DevoreCastException(args.get(0).type(), "string");
            String value = args.get(0).toString().trim().toLowerCase(Locale.ROOT);
            if (value.length() % 2 != 0)
                throw new DevoreRuntimeException("十六进制字符串长度必须为偶数.");
            byte[] result = new byte[value.length() / 2];
            for (int i = 0; i < value.length(); i += 2) {
                int high = Character.digit(value.charAt(i), 16);
                int low = Character.digit(value.charAt(i + 1), 16);
                if (high < 0 || low < 0)
                    throw new DevoreRuntimeException("十六进制字符串包含非法字符: " + args.get(0).toString());
                result[i / 2] = (byte) ((high << 4) | low);
            }
            return DByteUtils.toList(result);
        }, 1, false);
    }
}
