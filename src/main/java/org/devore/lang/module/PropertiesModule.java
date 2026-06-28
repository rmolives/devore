package org.devore.lang.module;

import org.devore.exception.DevoreCastException;
import org.devore.exception.DevoreRuntimeException;
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
    public PropertiesModule() {
        super("properties");
    }

    @Override
    public void init(Env dEnv) {
        dEnv.addTokenProcedure("properties-read-string", (args, env) ->
                readString(stringArg(args.get(0))), 1, false);
        dEnv.addTokenProcedure("properties-write-string", (args, env) ->
                DString.valueOf(writeString(tableArg(args.get(0)))), 1, false);
        dEnv.addTokenProcedure("properties-get", (args, env) ->
                propertyOrNil(tableArg(args.get(0)), stringArg(args.get(1))), 2, false);
        dEnv.addTokenProcedure("properties-read-file", (args, env) ->
                readFile(stringArg(args.get(0)), StandardCharsets.UTF_8), 1, false);
        dEnv.addTokenProcedure("properties-read-file", (args, env) ->
                readFile(stringArg(args.get(0)), charsetArg(args.get(1))), 2, false);
        dEnv.addTokenProcedure("properties-write-file", (args, env) -> {
            writeFile(stringArg(args.get(0)), tableArg(args.get(1)), StandardCharsets.UTF_8);
            return DWord.NIL;
        }, 2, false);
        dEnv.addTokenProcedure("properties-write-file", (args, env) -> {
            writeFile(stringArg(args.get(0)), tableArg(args.get(1)), charsetArg(args.get(2)));
            return DWord.NIL;
        }, 3, false);
    }

    private static DTable readString(String content) {
        try (Reader reader = new StringReader(content)) {
            return toTable(load(reader));
        } catch (IOException e) {
            throw new DevoreRuntimeException("读取properties字符串失败: " + e.getMessage());
        }
    }

    private static DTable readFile(String file, Charset charset) {
        Path path = Paths.get(file);
        try (Reader reader = Files.newBufferedReader(path, charset)) {
            return toTable(load(reader));
        } catch (IOException e) {
            throw new DevoreRuntimeException("读取properties文件失败: " + path + ", " + e.getMessage());
        }
    }

    private static String writeString(DTable table) {
        try (StringWriter writer = new StringWriter()) {
            store(toProperties(table), writer);
            return stripStoreHeader(writer.toString());
        } catch (IOException e) {
            throw new DevoreRuntimeException("写入properties字符串失败: " + e.getMessage());
        }
    }

    private static void writeFile(String file, DTable table, Charset charset) {
        Path path = Paths.get(file);
        try (Writer writer = Files.newBufferedWriter(path, charset)) {
            writer.write(writeString(table));
        } catch (IOException e) {
            throw new DevoreRuntimeException("写入properties文件失败: " + path + ", " + e.getMessage());
        }
    }

    private static Properties load(Reader reader) throws IOException {
        Properties properties = new Properties();
        properties.load(reader);
        return properties;
    }

    private static void store(Properties properties, Writer writer) throws IOException {
        properties.store(writer, null);
    }

    private static DTable toTable(Properties properties) {
        Map<DToken, DToken> table = new HashMap<>();
        for (String name : properties.stringPropertyNames())
            table.put(DString.valueOf(name), DString.valueOf(properties.getProperty(name)));
        return DTable.valueOf(table);
    }

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

    private static DToken propertyOrNil(DTable table, String key) {
        DToken value = table.get(DString.valueOf(key));
        return value instanceof DString ? value : DWord.NIL;
    }

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

    private static DTable tableArg(DToken token) {
        if (!(token instanceof DTable))
            throw new DevoreCastException(token.type(), "table");
        return (DTable) token;
    }

    private static String stringArg(DToken token) {
        if (!(token instanceof DString))
            throw new DevoreCastException(token.type(), "string");
        return token.toString();
    }

    private static Charset charsetArg(DToken token) {
        try {
            return Charset.forName(stringArg(token));
        } catch (RuntimeException e) {
            throw new DevoreRuntimeException("字符集不存在: " + token);
        }
    }
}
