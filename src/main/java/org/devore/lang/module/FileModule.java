package org.devore.lang.module;

import org.devore.exception.DevoreCastException;
import org.devore.exception.DevoreRuntimeException;
import org.devore.lang.Env;
import org.devore.lang.token.*;
import org.devore.utils.DByteUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 文件操作
 */
public class FileModule extends DModule {
    /**
     * 创建File模块实例
     */
    public FileModule() {
        super("file");
    }

    /**
     * 初始化文件模块，注册二进制、文本和路径操作过程
     */
    @Override
    public void init(Env dEnv) {
        initFileProcedures(dEnv); // 文件操作
    }

    /**
     * 注册二进制、文本和路径操作过程
     */
    private void initFileProcedures(Env dEnv) {
        dEnv.addTokenProcedure("file-read-binary", (args, env) -> {
            if (!(args.get(0) instanceof DString))
                throw new DevoreCastException(args.get(0).type(), "string");
            Path path = Paths.get(args.get(0).toString());
            try {
                return DByteUtils.toList(Files.readAllBytes(path));
            } catch (IOException e) {
                throw new DevoreRuntimeException("读取二进制文件失败: " + path + ", " + e.getMessage());
            }
        }, 1, false);
        dEnv.addTokenProcedure("file-write-binary", (args, env) -> {
            if (!(args.get(0) instanceof DString))
                throw new DevoreCastException(args.get(0).type(), "string");
            if (!(args.get(1) instanceof DList))
                throw new DevoreCastException(args.get(1).type(), "list");
            Path path = Paths.get(args.get(0).toString());
            DList binary = (DList) args.get(1);
            try {
                Files.write(path, DByteUtils.toBytes(binary),
                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                return DWord.NIL;
            } catch (IOException e) {
                throw new DevoreRuntimeException("写入二进制文件失败: " + path + ", " + e.getMessage());
            }
        }, 2, false);
        dEnv.addTokenProcedure("file-append-binary", (args, env) -> {
            if (!(args.get(0) instanceof DString))
                throw new DevoreCastException(args.get(0).type(), "string");
            if (!(args.get(1) instanceof DList))
                throw new DevoreCastException(args.get(1).type(), "list");
            Path path = Paths.get(args.get(0).toString());
            DList binary = (DList) args.get(1);
            try {
                Files.write(path, DByteUtils.toBytes(binary), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                return DWord.NIL;
            } catch (IOException e) {
                throw new DevoreRuntimeException("追加二进制文件失败: " + path + ", " + e.getMessage());
            }
        }, 2, false);
        dEnv.addTokenProcedure("file-read-string", (args, env) -> {
            if (!(args.get(0) instanceof DString))
                throw new DevoreCastException(args.get(0).type(), "string");
            Path path = Paths.get(args.get(0).toString());
            try {
                return DString.valueOf(new String(Files.readAllBytes(path), StandardCharsets.UTF_8));
            } catch (IOException e) {
                throw new DevoreRuntimeException("读取文本文件失败: " + path + ", " + e.getMessage());
            }
        }, 1, false);
        dEnv.addTokenProcedure("file-read-string", (args, env) -> {
            if (!(args.get(0) instanceof DString))
                throw new DevoreCastException(args.get(0).type(), "string");
            if (!(args.get(1) instanceof DString))
                throw new DevoreCastException(args.get(1).type(), "string");
            Path path = Paths.get(args.get(0).toString());
            Charset charset;
            try {
                charset = Charset.forName(args.get(1).toString());
            } catch (RuntimeException e) {
                throw new DevoreRuntimeException("字符集不存在: " + args.get(1));
            }
            try {
                return DString.valueOf(new String(Files.readAllBytes(path), charset));
            } catch (IOException e) {
                throw new DevoreRuntimeException("读取文本文件失败: " + path + ", " + e.getMessage());
            }
        }, 2, false);
        dEnv.addTokenProcedure("file-write-string", (args, env) -> {
            if (!(args.get(0) instanceof DString))
                throw new DevoreCastException(args.get(0).type(), "string");
            if (!(args.get(1) instanceof DString))
                throw new DevoreCastException(args.get(1).type(), "string");
            Path path = Paths.get(args.get(0).toString());
            String content = args.get(1).toString();
            try {
                Files.write(path, content.getBytes(StandardCharsets.UTF_8),
                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                return DWord.NIL;
            } catch (IOException e) {
                throw new DevoreRuntimeException("写入文本文件失败: " + path + ", " + e.getMessage());
            }
        }, 2, false);
        dEnv.addTokenProcedure("file-write-string", (args, env) -> {
            if (!(args.get(0) instanceof DString))
                throw new DevoreCastException(args.get(0).type(), "string");
            if (!(args.get(1) instanceof DString))
                throw new DevoreCastException(args.get(1).type(), "string");
            if (!(args.get(2) instanceof DString))
                throw new DevoreCastException(args.get(2).type(), "string");
            Path path = Paths.get(args.get(0).toString());
            String content = args.get(1).toString();
            Charset charset;
            try {
                charset = Charset.forName(args.get(2).toString());
            } catch (RuntimeException e) {
                throw new DevoreRuntimeException("字符集不存在: " + args.get(2));
            }
            try {
                Files.write(path, content.getBytes(charset),
                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                return DWord.NIL;
            } catch (IOException e) {
                throw new DevoreRuntimeException("写入文本文件失败: " + path + ", " + e.getMessage());
            }
        }, 3, false);
        dEnv.addTokenProcedure("file-append-string", (args, env) -> {
            if (!(args.get(0) instanceof DString))
                throw new DevoreCastException(args.get(0).type(), "string");
            if (!(args.get(1) instanceof DString))
                throw new DevoreCastException(args.get(1).type(), "string");
            Path path = Paths.get(args.get(0).toString());
            String content = args.get(1).toString();
            try {
                Files.write(path, content.getBytes(StandardCharsets.UTF_8),
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                return DWord.NIL;
            } catch (IOException e) {
                throw new DevoreRuntimeException("追加文本文件失败: " + path + ", " + e.getMessage());
            }
        }, 2, false);
        dEnv.addTokenProcedure("file-append-string", (args, env) -> {
            if (!(args.get(0) instanceof DString))
                throw new DevoreCastException(args.get(0).type(), "string");
            if (!(args.get(1) instanceof DString))
                throw new DevoreCastException(args.get(1).type(), "string");
            if (!(args.get(2) instanceof DString))
                throw new DevoreCastException(args.get(2).type(), "string");
            Path path = Paths.get(args.get(0).toString());
            String content = args.get(1).toString();
            Charset charset;
            try {
                charset = Charset.forName(args.get(2).toString());
            } catch (RuntimeException e) {
                throw new DevoreRuntimeException("字符集不存在: " + args.get(2));
            }
            try {
                Files.write(path, content.getBytes(charset),
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                return DWord.NIL;
            } catch (IOException e) {
                throw new DevoreRuntimeException("追加文本文件失败: " + path + ", " + e.getMessage());
            }
        }, 3, false);

        dEnv.addTokenProcedure("file-exists?", (args, env) -> {
            if (!(args.get(0) instanceof DString))
                throw new DevoreCastException(args.get(0).type(), "string");
            return DBool.valueOf(Files.exists(Paths.get(args.get(0).toString())));
        }, 1, false);
        dEnv.addTokenProcedure("file-regular?", (args, env) -> {
            if (!(args.get(0) instanceof DString))
                throw new DevoreCastException(args.get(0).type(), "string");
            return DBool.valueOf(Files.isRegularFile(Paths.get(args.get(0).toString())));
        }, 1, false);
        dEnv.addTokenProcedure("file-directory?", (args, env) -> {
            if (!(args.get(0) instanceof DString))
                throw new DevoreCastException(args.get(0).type(), "string");
            return DBool.valueOf(Files.isDirectory(Paths.get(args.get(0).toString())));
        }, 1, false);
        dEnv.addTokenProcedure("file-absolute-path", (args, env) -> {
            if (!(args.get(0) instanceof DString))
                throw new DevoreCastException(args.get(0).type(), "string");
            return DString.valueOf(Paths.get(args.get(0).toString()).toAbsolutePath().toString());
        }, 1, false);
        dEnv.addTokenProcedure("file-list", (args, env) -> {
            if (!(args.get(0) instanceof DString))
                throw new DevoreCastException(args.get(0).type(), "string");
            Path path = Paths.get(args.get(0).toString());
            if (!Files.exists(path))
                throw new DevoreRuntimeException("目录不存在: " + path);
            if (!Files.isDirectory(path))
                throw new DevoreRuntimeException("不是目录: " + path);
            try (Stream<Path> stream = Files.list(path)) {
                return DList.valueOf(
                        stream.map(p -> DString.valueOf(p.getFileName().toString()))
                                .collect(Collectors.toList())
                );
            } catch (IOException e) {
                throw new DevoreRuntimeException("读取文件列表失败: " + path + ", " + e.getMessage());
            }
        }, 1, false);
        dEnv.addTokenProcedure("file-size", (args, env) -> {
            if (!(args.get(0) instanceof DString))
                throw new DevoreCastException(args.get(0).type(), "string");
            Path path = Paths.get(args.get(0).toString());
            try {
                return DNumber.valueOf(Files.size(path));
            } catch (IOException e) {
                throw new DevoreRuntimeException("读取文件大小失败: " + path + ", " + e.getMessage());
            }
        }, 1, false);
        dEnv.addTokenProcedure("file-create-dirs", (args, env) -> {
            if (!(args.get(0) instanceof DString))
                throw new DevoreCastException(args.get(0).type(), "string");
            Path path = Paths.get(args.get(0).toString());
            try {
                Files.createDirectories(path);
                return DWord.NIL;
            } catch (IOException e) {
                throw new DevoreRuntimeException("创建目录失败: " + path + ", " + e.getMessage());
            }
        }, 1, false);
        dEnv.addTokenProcedure("file-delete!", (args, env) -> {
            if (!(args.get(0) instanceof DString))
                throw new DevoreCastException(args.get(0).type(), "string");
            Path path = Paths.get(args.get(0).toString());
            try {
                return DBool.valueOf(Files.deleteIfExists(path));
            } catch (IOException e) {
                throw new DevoreRuntimeException("删除文件失败: " + path + ", " + e.getMessage());
            }
        }, 1, false);
    }
}
