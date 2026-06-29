package org.devore.lang.module;

import org.devore.exception.DevoreCastException;
import org.devore.exception.DevoreRuntimeException;
import org.devore.lang.DSecurity;
import org.devore.lang.Env;
import org.devore.lang.token.DString;
import org.devore.lang.token.DTable;
import org.devore.lang.token.DToken;
import org.devore.lang.token.DWord;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Properties配置处理
 */
public class PropertiesModule extends DModule {
    /**
     * 创建Properties模块实例
     */
    public PropertiesModule() {
        super("properties");
    }

    /**
     * 初始化properties模块，注册字符串、文件和键值访问过程
     */
    @Override
    public void init(Env dEnv) {
        initPropertiesProcedures(dEnv); // Properties读写
    }

    /**
     * 注册properties字符串、文件和键值访问过程
     */
    private void initPropertiesProcedures(Env dEnv) {
        dEnv.addTokenProcedure("properties-read-string", (args, env) ->
                readString(stringArg(args.get(0))), 1, false);
        dEnv.addTokenProcedure("properties-write-string", (args, env) ->
                DString.valueOf(writeString(tableArg(args.get(0)))), 1, false);
        dEnv.addTokenProcedure("properties-get", (args, env) ->
                propertyOrNil(tableArg(args.get(0)), stringArg(args.get(1))), 2, false);
        dEnv.addTokenProcedure("properties-read-file", (args, env) -> {
            DSecurity.checkRestrictFile(env);
            return readFile(stringArg(args.get(0)), StandardCharsets.UTF_8);
        }, 1, false);
        dEnv.addTokenProcedure("properties-read-file", (args, env) -> {
            DSecurity.checkRestrictFile(env);
            return readFile(stringArg(args.get(0)), charsetArg(args.get(1)));
        }, 2, false);
        dEnv.addTokenProcedure("properties-write-file", (args, env) -> {
            DSecurity.checkRestrictFile(env);
            writeFile(stringArg(args.get(0)), tableArg(args.get(1)), StandardCharsets.UTF_8);
            return DWord.NIL;
        }, 2, false);
        dEnv.addTokenProcedure("properties-write-file", (args, env) -> {
            DSecurity.checkRestrictFile(env);
            writeFile(stringArg(args.get(0)), tableArg(args.get(1)), charsetArg(args.get(2)));
            return DWord.NIL;
        }, 3, false);
    }

    /**
     * 读取properties字符串并转换为表
     */
    private static DTable readString(String content) {
        try (Reader reader = new StringReader(content)) {
            return toTable(load(reader));
        } catch (IOException e) {
            throw new DevoreRuntimeException("读取properties字符串失败: " + e.getMessage());
        }
    }

    /**
     * 按指定字符集读取properties文件
     */
    private static DTable readFile(String file, Charset charset) {
        Path path = Paths.get(file);
        try (Reader reader = Files.newBufferedReader(path, charset)) {
            return toTable(load(reader));
        } catch (IOException e) {
            throw new DevoreRuntimeException("读取properties文件失败: " + path + ", " + e.getMessage());
        }
    }

    /**
     * 将表写成properties字符串
     */
    private static String writeString(DTable table) {
        try (StringWriter writer = new StringWriter()) {
            store(toProperties(table), writer);
            return stripStoreHeader(writer.toString());
        } catch (IOException e) {
            throw new DevoreRuntimeException("写入properties字符串失败: " + e.getMessage());
        }
    }

    /**
     * 按指定字符集写入properties文件
     */
    private static void writeFile(String file, DTable table, Charset charset) {
        Path path = Paths.get(file);
        try (Writer writer = Files.newBufferedWriter(path, charset)) {
            writer.write(writeString(table));
        } catch (IOException e) {
            throw new DevoreRuntimeException("写入properties文件失败: " + path + ", " + e.getMessage());
        }
    }

    /**
     * 从Reader加载Properties对象
     */
    private static Properties load(Reader reader) throws IOException {
        Properties properties = new Properties();
        properties.load(reader);
        return properties;
    }

    /**
     * 将Properties对象写入Writer
     */
    private static void store(Properties properties, Writer writer) throws IOException {
        properties.store(writer, null);
    }

    /**
     * 将Properties转换为Devore表
     */
    private static DTable toTable(Properties properties) {
        Map<DToken, DToken> table = new HashMap<>();
        for (String name : properties.stringPropertyNames())
            table.put(DString.valueOf(name), DString.valueOf(properties.getProperty(name)));
        return DTable.valueOf(table);
    }

    /**
     * 将Devore表转换为Properties
     */
    private static Properties toProperties(DTable table) {
        Properties properties = new Properties();
        for (DToken key : table.keys()) {
            if (!(key instanceof DString))
                throw new DevoreCastException(key.type(), "string");
            DToken value = table.get(key);
            if (!(value instanceof DString))
                throw new DevoreCastException(value.type(), "string");
            properties.setProperty(key.toString(), value.toString());
        }
        return properties;
    }

    /**
     * 按键读取属性值，不存在时返回nil
     */
    private static DToken propertyOrNil(DTable table, String key) {
        DToken value = table.get(DString.valueOf(key));
        return value instanceof DString ? value : DWord.NIL;
    }

    /**
     * 移除Properties.store生成的时间注释头
     */
    private static String stripStoreHeader(String content) {
        int index = 0;
        while (index < content.length() && content.charAt(index) == '#') {
            int nextLine = content.indexOf('\n', index);
            if (nextLine < 0)
                return "";
            index = nextLine + 1;
        }
        return content.substring(index);
    }

    /**
     * 校验并取得表参数
     */
    private static DTable tableArg(DToken token) {
        if (!(token instanceof DTable))
            throw new DevoreCastException(token.type(), "table");
        return (DTable) token;
    }

    /**
     * 校验并取得字符串参数
     */
    private static String stringArg(DToken token) {
        if (!(token instanceof DString))
            throw new DevoreCastException(token.type(), "string");
        return token.toString();
    }

    /**
     * 校验并取得字符集参数
     */
    private static Charset charsetArg(DToken token) {
        try {
            return Charset.forName(stringArg(token));
        } catch (RuntimeException e) {
            throw new DevoreRuntimeException("字符集不存在: " + token);
        }
    }
}
