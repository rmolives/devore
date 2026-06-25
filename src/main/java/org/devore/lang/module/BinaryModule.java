package org.devore.lang.module;

import org.devore.exception.DevoreCastException;
import org.devore.exception.DevoreRuntimeException;
import org.devore.lang.Env;
import org.devore.lang.token.*;
import org.devore.utils.DNumberUtils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 二进制数据
 */
public class BinaryModule extends Module {
    public BinaryModule() {
        super("binary");
    }

    @Override
    public void init(Env dEnv) {
        dEnv.addTokenProcedure("binary", (args, env) -> {
            byte[] bytes = new byte[args.size()];
            for (int i = 0; i < args.size(); ++i) {
                if (!(args.get(i) instanceof DInt))
                    throw new DevoreCastException(args.get(i).type(), "int");
                int value = DNumberUtils.toInt((DInt) args.get(i));
                if (value < 0 || value > 255)
                    throw new DevoreRuntimeException("字节值必须在0到255之间: " + value + ".");
                bytes[i] = (byte) value;
            }
            return DBinary.valueOf(bytes);
        }, 0, true);
        dEnv.addTokenProcedure("binary-clear!", (args, env) -> {
            if (!(args.get(0) instanceof DBinary))
                throw new DevoreCastException(args.get(0).type(), "binary");
            DBinary binary = (DBinary) args.get(0);
            binary.clear();
            return binary;
        }, 1, false);
        dEnv.addTokenProcedure("binary-get", (args, env) -> {
            if (!(args.get(0) instanceof DBinary))
                throw new DevoreCastException(args.get(0).type(), "binary");
            if (!(args.get(1) instanceof DInt))
                throw new DevoreCastException(args.get(1).type(), "int");
            return DNumber.valueOf(((DBinary) args.get(0)).get(DNumberUtils.toIndex((DInt) args.get(1))));
        }, 2, false);
        dEnv.addTokenProcedure("binary-set", (args, env) -> {
            if (!(args.get(0) instanceof DBinary))
                throw new DevoreCastException(args.get(0).type(), "binary");
            if (!(args.get(1) instanceof DInt))
                throw new DevoreCastException(args.get(1).type(), "int");
            if (!(args.get(2) instanceof DInt))
                throw new DevoreCastException(args.get(2).type(), "int");
            int value = DNumberUtils.toInt((DInt) args.get(2));
            if (value < 0 || value > 255)
                throw new DevoreRuntimeException("字节值必须在0到255之间: " + value + ".");
            return ((DBinary) args.get(0)).set(DNumberUtils.toIndex((DInt) args.get(1)), value, false);
        }, 3, false);
        dEnv.addTokenProcedure("binary-set!", (args, env) -> {
            if (!(args.get(0) instanceof DBinary))
                throw new DevoreCastException(args.get(0).type(), "binary");
            if (!(args.get(1) instanceof DInt))
                throw new DevoreCastException(args.get(1).type(), "int");
            if (!(args.get(2) instanceof DInt))
                throw new DevoreCastException(args.get(2).type(), "int");
            int value = DNumberUtils.toInt((DInt) args.get(2));
            if (value < 0 || value > 255)
                throw new DevoreRuntimeException("字节值必须在0到255之间: " + value + ".");
            return ((DBinary) args.get(0)).set(DNumberUtils.toIndex((DInt) args.get(1)), value, true);
        }, 3, false);
        dEnv.addTokenProcedure("binary-add", (args, env) -> {
            if (!(args.get(0) instanceof DBinary))
                throw new DevoreCastException(args.get(0).type(), "binary");
            if (!(args.get(1) instanceof DInt))
                throw new DevoreCastException(args.get(1).type(), "int");
            int value = DNumberUtils.toInt((DInt) args.get(1));
            if (value < 0 || value > 255)
                throw new DevoreRuntimeException("字节值必须在0到255之间: " + value + ".");
            return ((DBinary) args.get(0)).add(value, false);
        }, 2, false);
        dEnv.addTokenProcedure("binary-add", (args, env) -> {
            if (!(args.get(0) instanceof DBinary))
                throw new DevoreCastException(args.get(0).type(), "binary");
            if (!(args.get(1) instanceof DInt))
                throw new DevoreCastException(args.get(1).type(), "int");
            if (!(args.get(2) instanceof DInt))
                throw new DevoreCastException(args.get(2).type(), "int");
            int value = DNumberUtils.toInt((DInt) args.get(2));
            if (value < 0 || value > 255)
                throw new DevoreRuntimeException("字节值必须在0到255之间: " + value + ".");
            return ((DBinary) args.get(0)).add(DNumberUtils.toIndex((DInt) args.get(1)), value, false);
        }, 3, false);
        dEnv.addTokenProcedure("binary-add!", (args, env) -> {
            if (!(args.get(0) instanceof DBinary))
                throw new DevoreCastException(args.get(0).type(), "binary");
            if (!(args.get(1) instanceof DInt))
                throw new DevoreCastException(args.get(1).type(), "int");
            int value = DNumberUtils.toInt((DInt) args.get(1));
            if (value < 0 || value > 255)
                throw new DevoreRuntimeException("字节值必须在0到255之间: " + value + ".");
            return ((DBinary) args.get(0)).add(value, true);
        }, 2, false);
        dEnv.addTokenProcedure("binary-add!", (args, env) -> {
            if (!(args.get(0) instanceof DBinary))
                throw new DevoreCastException(args.get(0).type(), "binary");
            if (!(args.get(1) instanceof DInt))
                throw new DevoreCastException(args.get(1).type(), "int");
            if (!(args.get(2) instanceof DInt))
                throw new DevoreCastException(args.get(2).type(), "int");
            int value = DNumberUtils.toInt((DInt) args.get(2));
            if (value < 0 || value > 255)
                throw new DevoreRuntimeException("字节值必须在0到255之间: " + value + ".");
            return ((DBinary) args.get(0)).add(DNumberUtils.toIndex((DInt) args.get(1)), value, true);
        }, 3, false);
        dEnv.addTokenProcedure("binary-sub", (args, env) -> {
            if (!(args.get(0) instanceof DBinary))
                throw new DevoreCastException(args.get(0).type(), "binary");
            if (!(args.get(1) instanceof DInt))
                throw new DevoreCastException(args.get(1).type(), "int");
            DBinary binary = (DBinary) args.get(0);
            return binary.subBinary(DNumberUtils.toIndex((DInt) args.get(1)), binary.size(), false);
        }, 2, false);
        dEnv.addTokenProcedure("binary-sub", (args, env) -> {
            if (!(args.get(0) instanceof DBinary))
                throw new DevoreCastException(args.get(0).type(), "binary");
            if (!(args.get(1) instanceof DInt))
                throw new DevoreCastException(args.get(1).type(), "int");
            if (!(args.get(2) instanceof DInt))
                throw new DevoreCastException(args.get(2).type(), "int");
            return ((DBinary) args.get(0)).subBinary(DNumberUtils.toIndex((DInt) args.get(1)),
                    DNumberUtils.toIndex((DInt) args.get(2)), false);
        }, 3, false);
        dEnv.addTokenProcedure("binary-sub!", (args, env) -> {
            if (!(args.get(0) instanceof DBinary))
                throw new DevoreCastException(args.get(0).type(), "binary");
            if (!(args.get(1) instanceof DInt))
                throw new DevoreCastException(args.get(1).type(), "int");
            DBinary binary = (DBinary) args.get(0);
            return binary.subBinary(DNumberUtils.toIndex((DInt) args.get(1)), binary.size(), true);
        }, 2, false);
        dEnv.addTokenProcedure("binary-sub!", (args, env) -> {
            if (!(args.get(0) instanceof DBinary))
                throw new DevoreCastException(args.get(0).type(), "binary");
            if (!(args.get(1) instanceof DInt))
                throw new DevoreCastException(args.get(1).type(), "int");
            if (!(args.get(2) instanceof DInt))
                throw new DevoreCastException(args.get(2).type(), "int");
            return ((DBinary) args.get(0)).subBinary(DNumberUtils.toIndex((DInt) args.get(1)),
                    DNumberUtils.toIndex((DInt) args.get(2)), true);
        }, 3, false);
        dEnv.addTokenProcedure("binary-concat", (args, env) -> {
            if (!(args.get(0) instanceof DBinary))
                throw new DevoreCastException(args.get(0).type(), "binary");
            DBinary result = (DBinary) args.get(0);
            for (int i = 1; i < args.size(); ++i) {
                if (!(args.get(i) instanceof DBinary))
                    throw new DevoreCastException(args.get(i).type(), "binary");
                result = result.concat((DBinary) args.get(i), false);
            }
            return result;
        }, 1, true);
        dEnv.addTokenProcedure("binary-concat!", (args, env) -> {
            if (!(args.get(0) instanceof DBinary))
                throw new DevoreCastException(args.get(0).type(), "binary");
            DBinary result = (DBinary) args.get(0);
            for (int i = 1; i < args.size(); ++i) {
                if (!(args.get(i) instanceof DBinary))
                    throw new DevoreCastException(args.get(i).type(), "binary");
                result = result.concat((DBinary) args.get(i), true);
            }
            return result;
        }, 1, true);
        dEnv.addTokenProcedure("binary->list", (args, env) -> {
            if (!(args.get(0) instanceof DBinary))
                throw new DevoreCastException(args.get(0).type(), "binary");
            return DList.valueOf(((DBinary) args.get(0)).toList());
        }, 1, false);
        dEnv.addTokenProcedure("list->binary", (args, env) -> {
            if (!(args.get(0) instanceof DList))
                throw new DevoreCastException(args.get(0).type(), "list");
            List<DToken> tokens = ((DList) args.get(0)).toList();
            byte[] bytes = new byte[tokens.size()];
            for (int i = 0; i < tokens.size(); ++i) {
                if (!(tokens.get(i) instanceof DInt))
                    throw new DevoreCastException(tokens.get(i).type(), "int");
                int value = DNumberUtils.toInt((DInt) tokens.get(i));
                if (value < 0 || value > 255)
                    throw new DevoreRuntimeException("字节值必须在0到255之间: " + value + ".");
                bytes[i] = (byte) value;
            }
            return DBinary.valueOf(bytes);
        }, 1, false);
        dEnv.addTokenProcedure("string->binary", (args, env) -> {
            if (!(args.get(0) instanceof DString))
                throw new DevoreCastException(args.get(0).type(), "string");
            return DBinary.valueOf(args.get(0).toString().getBytes(StandardCharsets.UTF_8));
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
                throw new DevoreRuntimeException("字符集不存在: " + args.get(1) + ".");
            }
            return DBinary.valueOf(args.get(0).toString().getBytes(charset));
        }, 2, false);
        dEnv.addTokenProcedure("binary->string", (args, env) -> {
            if (!(args.get(0) instanceof DBinary))
                throw new DevoreCastException(args.get(0).type(), "binary");
            return DString.valueOf(new String(((DBinary) args.get(0)).toByteArray(), StandardCharsets.UTF_8));
        }, 1, false);
        dEnv.addTokenProcedure("binary->string", (args, env) -> {
            if (!(args.get(0) instanceof DBinary))
                throw new DevoreCastException(args.get(0).type(), "binary");
            if (!(args.get(1) instanceof DString))
                throw new DevoreCastException(args.get(1).type(), "string");
            Charset charset;
            try {
                charset = Charset.forName(args.get(1).toString());
            } catch (RuntimeException e) {
                throw new DevoreRuntimeException("字符集不存在: " + args.get(1) + ".");
            }
            return DString.valueOf(new String(((DBinary) args.get(0)).toByteArray(), charset));
        }, 2, false);
        dEnv.addTokenProcedure("binary->hex", (args, env) -> {
            if (!(args.get(0) instanceof DBinary))
                throw new DevoreCastException(args.get(0).type(), "binary");
            return DString.valueOf(((DBinary) args.get(0)).toHex());
        }, 1, false);
        dEnv.addTokenProcedure("hex->binary", (args, env) -> {
            if (!(args.get(0) instanceof DString))
                throw new DevoreCastException(args.get(0).type(), "string");
            return DBinary.fromHex(args.get(0).toString());
        }, 1, false);
        dEnv.addTokenProcedure("binary?", (args, env) ->
                DBool.valueOf(args.get(0) instanceof DBinary), 1, false);
    }
}
