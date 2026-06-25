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
        initBinaryProcedures(dEnv);
        initConversionProcedures(dEnv);
        initPredicateProcedures(dEnv);
    }

    private void initBinaryProcedures(Env dEnv) {
        dEnv.addTokenProcedure("binary", (args, env) ->
                DBinary.valueOf(args.stream()
                        .map(BinaryModule::requireByte)
                        .collect(BinaryModule::newByteArrayBuilder,
                                ByteArrayBuilder::add,
                                ByteArrayBuilder::addAll)
                        .toByteArray()), 0, true);
        dEnv.addTokenProcedure("binary-clear!", (args, env) -> {
            DBinary binary = requireBinary(args.get(0));
            binary.clear();
            return binary;
        }, 1, false);
        dEnv.addTokenProcedure("binary-get", (args, env) ->
                DNumber.valueOf(requireBinary(args.get(0)).get(requireIndex(args.get(1)))), 2, false);
        dEnv.addTokenProcedure("binary-set", (args, env) ->
                requireBinary(args.get(0)).set(requireIndex(args.get(1)), requireByte(args.get(2)), false), 3, false);
        dEnv.addTokenProcedure("binary-set!", (args, env) ->
                requireBinary(args.get(0)).set(requireIndex(args.get(1)), requireByte(args.get(2)), true), 3, false);
        dEnv.addTokenProcedure("binary-add", (args, env) ->
                requireBinary(args.get(0)).add(requireByte(args.get(1)), false), 2, false);
        dEnv.addTokenProcedure("binary-add!", (args, env) ->
                requireBinary(args.get(0)).add(requireByte(args.get(1)), true), 2, false);
        dEnv.addTokenProcedure("binary-sub", (args, env) -> {
            DBinary binary = requireBinary(args.get(0));
            return binary.subBinary(requireIndex(args.get(1)), binary.size(), false);
        }, 2, false);
        dEnv.addTokenProcedure("binary-sub", (args, env) ->
                requireBinary(args.get(0)).subBinary(requireIndex(args.get(1)), requireIndex(args.get(2)), false), 3, false);
        dEnv.addTokenProcedure("binary-sub!", (args, env) -> {
            DBinary binary = requireBinary(args.get(0));
            return binary.subBinary(requireIndex(args.get(1)), binary.size(), true);
        }, 2, false);
        dEnv.addTokenProcedure("binary-sub!", (args, env) ->
                requireBinary(args.get(0)).subBinary(requireIndex(args.get(1)), requireIndex(args.get(2)), true), 3, false);
        dEnv.addTokenProcedure("binary-concat", (args, env) -> {
            DBinary result = requireBinary(args.get(0));
            for (int i = 1; i < args.size(); ++i)
                result = result.concat(requireBinary(args.get(i)), false);
            return result;
        }, 1, true);
        dEnv.addTokenProcedure("binary-concat!", (args, env) -> {
            DBinary result = requireBinary(args.get(0));
            for (int i = 1; i < args.size(); ++i)
                result = result.concat(requireBinary(args.get(i)), true);
            return result;
        }, 1, true);
    }

    private void initConversionProcedures(Env dEnv) {
        dEnv.addTokenProcedure("binary->list", (args, env) ->
                DList.valueOf(requireBinary(args.get(0)).toList()), 1, false);
        dEnv.addTokenProcedure("list->binary", (args, env) -> {
            if (!(args.get(0) instanceof DList))
                throw new DevoreCastException(args.get(0).type(), "list");
            List<DToken> tokens = ((DList) args.get(0)).toList();
            byte[] bytes = new byte[tokens.size()];
            for (int i = 0; i < tokens.size(); ++i)
                bytes[i] = (byte) requireByte(tokens.get(i));
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
            return DBinary.valueOf(args.get(0).toString().getBytes(charset(args.get(1).toString())));
        }, 2, false);
        dEnv.addTokenProcedure("binary->string", (args, env) ->
                DString.valueOf(new String(requireBinary(args.get(0)).toByteArray(), StandardCharsets.UTF_8)), 1, false);
        dEnv.addTokenProcedure("binary->string", (args, env) -> {
            if (!(args.get(1) instanceof DString))
                throw new DevoreCastException(args.get(1).type(), "string");
            return DString.valueOf(new String(requireBinary(args.get(0)).toByteArray(), charset(args.get(1).toString())));
        }, 2, false);
        dEnv.addTokenProcedure("binary->hex", (args, env) ->
                DString.valueOf(requireBinary(args.get(0)).toHex()), 1, false);
        dEnv.addTokenProcedure("hex->binary", (args, env) -> {
            if (!(args.get(0) instanceof DString))
                throw new DevoreCastException(args.get(0).type(), "string");
            return DBinary.fromHex(args.get(0).toString());
        }, 1, false);
    }

    private void initPredicateProcedures(Env dEnv) {
        dEnv.addTokenProcedure("binary?", (args, env) ->
                DBool.valueOf(args.get(0) instanceof DBinary), 1, false);
    }

    private static DBinary requireBinary(DToken token) {
        if (!(token instanceof DBinary))
            throw new DevoreCastException(token.type(), "binary");
        return (DBinary) token;
    }

    private static int requireIndex(DToken token) {
        if (!(token instanceof DInt))
            throw new DevoreCastException(token.type(), "int");
        return DNumberUtils.toIndex((DInt) token);
    }

    private static int requireByte(DToken token) {
        if (!(token instanceof DInt))
            throw new DevoreCastException(token.type(), "int");
        int value = DNumberUtils.toInt((DInt) token);
        if (value < 0 || value > 255)
            throw new DevoreRuntimeException("字节值必须在0到255之间: " + value + ".");
        return value;
    }

    private static Charset charset(String name) {
        try {
            return Charset.forName(name);
        } catch (RuntimeException e) {
            throw new DevoreRuntimeException("字符集不存在: " + name + ".");
        }
    }

    private static ByteArrayBuilder newByteArrayBuilder() {
        return new ByteArrayBuilder();
    }

    private static class ByteArrayBuilder {
        private byte[] bytes = new byte[8];
        private int size = 0;

        private void add(int value) {
            if (this.size >= this.bytes.length) {
                byte[] newBytes = new byte[this.bytes.length * 2];
                System.arraycopy(this.bytes, 0, newBytes, 0, this.bytes.length);
                this.bytes = newBytes;
            }
            this.bytes[this.size++] = (byte) value;
        }

        private void addAll(ByteArrayBuilder other) {
            for (int i = 0; i < other.size; ++i)
                add(Byte.toUnsignedInt(other.bytes[i]));
        }

        private byte[] toByteArray() {
            byte[] result = new byte[this.size];
            System.arraycopy(this.bytes, 0, result, 0, this.size);
            return result;
        }
    }
}
