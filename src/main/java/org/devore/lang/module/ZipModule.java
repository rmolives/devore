package org.devore.lang.module;

import org.devore.exception.DevoreCastException;
import org.devore.exception.DevoreRuntimeException;
import org.devore.lang.Env;
import org.devore.lang.token.*;
import org.devore.utils.DByteUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * ZIP压缩包操作
 */
public class ZipModule extends DModule {
    /**
     * 创建Zip模块实例
     */
    public ZipModule() {
        super("zip");
    }

    /**
     * 初始化压缩模块，注册ZIP和GZIP过程
     */
    @Override
    public void init(Env dEnv) {
        initZipProcedures(dEnv); // 压缩解压
    }

    /**
     * 注册ZIP和GZIP压缩解压过程
     */
    private void initZipProcedures(Env dEnv) {
        dEnv.addTokenProcedure("zip-create", (args, env) -> {
            Path zipPath = stringPath(args.get(0));
            Path sourcePath = stringPath(args.get(1));
            String entryName = defaultEntryName(sourcePath);
            createZip(zipPath, sourcePath, entryName);
            return DWord.NIL;
        }, 2, false);

        dEnv.addTokenProcedure("zip-create", (args, env) -> {
            Path zipPath = stringPath(args.get(0));
            Path sourcePath = stringPath(args.get(1));
            String entryName = stringValue(args.get(2));
            createZip(zipPath, sourcePath, normalizeEntryName(entryName));
            return DWord.NIL;
        }, 3, false);

        dEnv.addTokenProcedure("zip-list", (args, env) -> {
            Path zipPath = stringPath(args.get(0));
            try (ZipFile zipFile = new ZipFile(zipPath.toFile())) {
                List<DToken> entries = new ArrayList<>();
                zipFile.stream().forEach(entry -> entries.add(entryTable(entry)));
                return DList.valueOf(entries);
            } catch (IOException e) {
                throw new DevoreRuntimeException("读取ZIP条目失败: " + zipPath + ", " + e.getMessage());
            }
        }, 1, false);

        dEnv.addTokenProcedure("zip-entry-exists?", (args, env) -> {
            Path zipPath = stringPath(args.get(0));
            String entryName = stringValue(args.get(1));
            try (ZipFile zipFile = new ZipFile(zipPath.toFile())) {
                return DBool.valueOf(zipFile.getEntry(entryName) != null);
            } catch (IOException e) {
                throw new DevoreRuntimeException("读取ZIP条目失败: " + zipPath + ", " + e.getMessage());
            }
        }, 2, false);

        dEnv.addTokenProcedure("zip-read-entry", (args, env) -> {
            Path zipPath = stringPath(args.get(0));
            String entryName = stringValue(args.get(1));
            try (ZipFile zipFile = new ZipFile(zipPath.toFile())) {
                ZipEntry entry = zipFile.getEntry(entryName);
                if (entry == null)
                    throw new DevoreRuntimeException("ZIP条目不存在: " + entryName);
                if (entry.isDirectory())
                    throw new DevoreRuntimeException("ZIP条目是目录: " + entryName);
                try (InputStream in = zipFile.getInputStream(entry);
                     ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                    copy(in, out);
                    return DByteUtils.toList(out.toByteArray());
                }
            } catch (IOException e) {
                throw new DevoreRuntimeException("读取ZIP条目失败: " + zipPath + ", " + e.getMessage());
            }
        }, 2, false);

        dEnv.addTokenProcedure("zip-extract", (args, env) -> {
            Path zipPath = stringPath(args.get(0));
            Path targetDir = stringPath(args.get(1));
            extractZip(zipPath, targetDir);
            return DWord.NIL;
        }, 2, false);

        dEnv.addTokenProcedure("zip-extract-entry", (args, env) -> {
            Path zipPath = stringPath(args.get(0));
            String entryName = stringValue(args.get(1));
            Path targetPath = stringPath(args.get(2));
            extractEntry(zipPath, entryName, targetPath);
            return DWord.NIL;
        }, 3, false);

        dEnv.addTokenProcedure("gzip-compress", (args, env) -> {
            if (!(args.get(0) instanceof DList))
                throw new DevoreCastException(args.get(0).type(), "list");
            return DByteUtils.toList(gzipCompress(DByteUtils.toBytes((DList) args.get(0))));
        }, 1, false);

        dEnv.addTokenProcedure("gzip-decompress", (args, env) -> {
            if (!(args.get(0) instanceof DList))
                throw new DevoreCastException(args.get(0).type(), "list");
            return DByteUtils.toList(gzipDecompress(DByteUtils.toBytes((DList) args.get(0))));
        }, 1, false);

        dEnv.addTokenProcedure("gzip-compress-file", (args, env) -> {
            Path sourcePath = stringPath(args.get(0));
            Path gzipPath = stringPath(args.get(1));
            gzipCompressFile(sourcePath, gzipPath);
            return DWord.NIL;
        }, 2, false);

        dEnv.addTokenProcedure("gzip-decompress-file", (args, env) -> {
            Path gzipPath = stringPath(args.get(0));
            Path targetPath = stringPath(args.get(1));
            gzipDecompressFile(gzipPath, targetPath);
            return DWord.NIL;
        }, 2, false);
    }

    /**
     * 校验字符串参数并转换为Path
     */
    private static Path stringPath(DToken token) {
        if (!(token instanceof DString))
            throw new DevoreCastException(token.type(), "string");
        return Paths.get(token.toString());
    }

    /**
     * 校验并取得字符串参数
     */
    private static String stringValue(DToken token) {
        if (!(token instanceof DString))
            throw new DevoreCastException(token.type(), "string");
        return token.toString();
    }

    /**
     * 从文件或目录创建ZIP文件
     */
    private static void createZip(Path zipPath, Path sourcePath, String entryName) {
        if (!Files.exists(sourcePath))
            throw new DevoreRuntimeException("源路径不存在: " + sourcePath);
        try {
            Path parent = zipPath.getParent();
            if (parent != null)
                Files.createDirectories(parent);
            try (ZipOutputStream zipOut = new ZipOutputStream(Files.newOutputStream(zipPath))) {
                if (Files.isDirectory(sourcePath))
                    addDirectory(zipOut, sourcePath, entryName);
                else
                    addFile(zipOut, sourcePath, entryName);
            }
        } catch (IOException e) {
            throw new DevoreRuntimeException("创建ZIP失败: " + zipPath + ", " + e.getMessage());
        }
    }

    /**
     * 递归添加目录到ZIP输出流
     */
    private static void addDirectory(ZipOutputStream zipOut, Path sourcePath, String entryName) throws IOException {
        String baseName = directoryEntryName(entryName);
        addDirectoryEntry(zipOut, baseName);
        try (java.util.stream.Stream<Path> paths = Files.walk(sourcePath)) {
            java.util.Iterator<Path> iterator = paths.iterator();
            while (iterator.hasNext()) {
                Path path = iterator.next();
                if (path.equals(sourcePath))
                    continue;
                Path relativePath = sourcePath.relativize(path);
                String childName = baseName + toZipName(relativePath);
                if (Files.isDirectory(path))
                    addDirectoryEntry(zipOut, childName);
                else
                    addFile(zipOut, path, childName);
            }
        }
    }

    /**
     * 添加文件到ZIP输出流
     */
    private static void addFile(ZipOutputStream zipOut, Path sourcePath, String entryName) throws IOException {
        ZipEntry entry = new ZipEntry(normalizeEntryName(entryName));
        zipOut.putNextEntry(entry);
        Files.copy(sourcePath, zipOut);
        zipOut.closeEntry();
    }

    /**
     * 添加目录条目到ZIP输出流
     */
    private static void addDirectoryEntry(ZipOutputStream zipOut, String entryName) throws IOException {
        ZipEntry entry = new ZipEntry(directoryEntryName(entryName));
        zipOut.putNextEntry(entry);
        zipOut.closeEntry();
    }

    /**
     * 解压整个ZIP文件并防止路径穿越
     */
    private static void extractZip(Path zipPath, Path targetDir) {
        Path normalizedTargetDir = targetDir.toAbsolutePath().normalize();
        try (ZipFile zipFile = new ZipFile(zipPath.toFile())) {
            Files.createDirectories(normalizedTargetDir);
            java.util.Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                Path targetPath = normalizedTargetDir.resolve(entry.getName()).normalize();
                if (!targetPath.startsWith(normalizedTargetDir))
                    throw new DevoreRuntimeException("ZIP条目路径非法: " + entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(targetPath);
                } else {
                    Path parent = targetPath.getParent();
                    if (parent != null)
                        Files.createDirectories(parent);
                    try (InputStream in = zipFile.getInputStream(entry);
                         OutputStream out = Files.newOutputStream(targetPath)) {
                        copy(in, out);
                    }
                }
            }
        } catch (IOException e) {
            throw new DevoreRuntimeException("解压ZIP失败: " + zipPath + ", " + e.getMessage());
        }
    }

    /**
     * 解压单个ZIP条目
     */
    private static void extractEntry(Path zipPath, String entryName, Path targetPath) {
        try (ZipFile zipFile = new ZipFile(zipPath.toFile())) {
            ZipEntry entry = zipFile.getEntry(entryName);
            if (entry == null)
                throw new DevoreRuntimeException("ZIP条目不存在: " + entryName);
            if (entry.isDirectory()) {
                Files.createDirectories(targetPath);
                return;
            }
            Path parent = targetPath.getParent();
            if (parent != null)
                Files.createDirectories(parent);
            try (InputStream in = zipFile.getInputStream(entry);
                 OutputStream out = Files.newOutputStream(targetPath)) {
                copy(in, out);
            }
        } catch (IOException e) {
            throw new DevoreRuntimeException("解压ZIP条目失败: " + zipPath + ", " + e.getMessage());
        }
    }

    /**
     * 将ZIP条目信息转换为Devore表
     */
    private static DTable entryTable(ZipEntry entry) {
        Map<DToken, DToken> table = new HashMap<>();
        table.put(DString.valueOf("name"), DString.valueOf(entry.getName()));
        table.put(DString.valueOf("directory?"), DBool.valueOf(entry.isDirectory()));
        table.put(DString.valueOf("size"), DNumber.valueOf(entry.getSize()));
        table.put(DString.valueOf("compressed-size"), DNumber.valueOf(entry.getCompressedSize()));
        table.put(DString.valueOf("crc"), DNumber.valueOf(entry.getCrc()));
        table.put(DString.valueOf("time"), DNumber.valueOf(entry.getTime()));
        return DTable.valueOf(table);
    }

    /**
     * 压缩字节数组为GZIP数据
     */
    private static byte[] gzipCompress(byte[] bytes) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             GZIPOutputStream gzipOut = new GZIPOutputStream(out)) {
            gzipOut.write(bytes);
            gzipOut.finish();
            return out.toByteArray();
        } catch (IOException e) {
            throw new DevoreRuntimeException("GZIP压缩失败: " + e.getMessage());
        }
    }

    /**
     * 解压GZIP字节数组
     */
    private static byte[] gzipDecompress(byte[] bytes) {
        try (GZIPInputStream gzipIn = new GZIPInputStream(new java.io.ByteArrayInputStream(bytes));
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            copy(gzipIn, out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new DevoreRuntimeException("GZIP解压失败: " + e.getMessage());
        }
    }

    /**
     * 将单个文件压缩为GZIP文件
     */
    private static void gzipCompressFile(Path sourcePath, Path gzipPath) {
        if (!Files.exists(sourcePath))
            throw new DevoreRuntimeException("源路径不存在: " + sourcePath);
        if (Files.isDirectory(sourcePath))
            throw new DevoreRuntimeException("GZIP只能压缩单个文件: " + sourcePath);
        try {
            Path parent = gzipPath.getParent();
            if (parent != null)
                Files.createDirectories(parent);
            try (InputStream in = Files.newInputStream(sourcePath);
                 GZIPOutputStream out = new GZIPOutputStream(Files.newOutputStream(gzipPath))) {
                copy(in, out);
            }
        } catch (IOException e) {
            throw new DevoreRuntimeException("GZIP压缩文件失败: " + sourcePath + ", " + e.getMessage());
        }
    }

    /**
     * 将GZIP文件解压到目标文件
     */
    private static void gzipDecompressFile(Path gzipPath, Path targetPath) {
        try {
            Path parent = targetPath.getParent();
            if (parent != null)
                Files.createDirectories(parent);
            try (GZIPInputStream in = new GZIPInputStream(Files.newInputStream(gzipPath));
                 OutputStream out = Files.newOutputStream(targetPath)) {
                copy(in, out);
            }
        } catch (IOException e) {
            throw new DevoreRuntimeException("GZIP解压文件失败: " + gzipPath + ", " + e.getMessage());
        }
    }

    /**
     * 根据源路径生成默认ZIP条目名
     */
    private static String defaultEntryName(Path sourcePath) {
        Path fileName = sourcePath.getFileName();
        if (fileName == null)
            throw new DevoreRuntimeException("无法生成ZIP条目名: " + sourcePath);
        return normalizeEntryName(fileName.toString());
    }

    /**
     * 规范化并校验ZIP条目名
     */
    private static String normalizeEntryName(String entryName) {
        String normalized = entryName.replace('\\', '/');
        while (normalized.startsWith("/"))
            normalized = normalized.substring(1);
        if (normalized.isEmpty())
            throw new DevoreRuntimeException("ZIP条目名不能为空");
        for (String name : normalized.split("/")) {
            if (name.equals(".") || name.equals(".."))
                throw new DevoreRuntimeException("ZIP条目名非法: " + entryName);
        }
        return normalized;
    }

    /**
     * 生成目录形式ZIP条目名
     */
    private static String directoryEntryName(String entryName) {
        String normalized = normalizeEntryName(entryName);
        return normalized.endsWith("/") ? normalized : normalized + "/";
    }

    /**
     * 将路径转换为ZIP内部路径名
     */
    private static String toZipName(Path path) {
        return path.toString().replace('\\', '/');
    }

    /**
     * 复制输入流到输出流
     */
    private static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[8192];
        int length;
        while ((length = in.read(buffer)) != -1)
            out.write(buffer, 0, length);
    }
}
