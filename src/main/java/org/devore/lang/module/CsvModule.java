package org.devore.lang.module;

import org.devore.exception.DevoreCastException;
import org.devore.exception.DevoreRuntimeException;
import org.devore.lang.Env;
import org.devore.lang.token.DList;
import org.devore.lang.token.DString;
import org.devore.lang.token.DToken;
import org.devore.lang.token.DWord;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * RFC 4180 CSV处理
 */
public class CsvModule extends DModule {
    public CsvModule() {
        super("csv");
    }

    @Override
    public void init(Env dEnv) {
        dEnv.addTokenProcedure("csv-read-string", (args, env) ->
                readString(stringArg(args.get(0))), 1, false);
        dEnv.addTokenProcedure("csv-write-string", (args, env) ->
                DString.valueOf(writeString(listArg(args.get(0)))), 1, false);
        dEnv.addTokenProcedure("csv-read-file", (args, env) ->
                readFile(stringArg(args.get(0)), StandardCharsets.UTF_8), 1, false);
        dEnv.addTokenProcedure("csv-read-file", (args, env) ->
                readFile(stringArg(args.get(0)), charsetArg(args.get(1))), 2, false);
        dEnv.addTokenProcedure("csv-write-file", (args, env) -> {
            writeFile(stringArg(args.get(0)), listArg(args.get(1)), StandardCharsets.UTF_8);
            return DWord.NIL;
        }, 2, false);
        dEnv.addTokenProcedure("csv-write-file", (args, env) -> {
            writeFile(stringArg(args.get(0)), listArg(args.get(1)), charsetArg(args.get(2)));
            return DWord.NIL;
        }, 3, false);
    }

    private static DList readString(String content) {
        return toRows(new Parser(content).parse());
    }

    private static DList readFile(String file, Charset charset) {
        Path path = Paths.get(file);
        try {
            return readString(new String(Files.readAllBytes(path), charset));
        } catch (IOException e) {
            throw new DevoreRuntimeException("读取CSV文件失败: " + path + ", " + e.getMessage());
        }
    }

    private static String writeString(DList rows) {
        List<String> records = new ArrayList<>();
        for (DToken row : rows.toList())
            records.add(writeRecord(rowList(row)));
        return String.join("\r\n", records);
    }

    private static void writeFile(String file, DList rows, Charset charset) {
        Path path = Paths.get(file);
        try {
            Files.write(path, writeString(rows).getBytes(charset));
        } catch (IOException e) {
            throw new DevoreRuntimeException("写入CSV文件失败: " + path + ", " + e.getMessage());
        }
    }

    private static String writeRecord(DList row) {
        List<String> fields = new ArrayList<>();
        for (DToken field : row.toList())
            fields.add(writeField(stringArg(field)));
        return String.join(",", fields);
    }

    private static String writeField(String field) {
        boolean quote = field.isEmpty();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < field.length(); ++i) {
            char c = field.charAt(i);
            if (c == '"' || c == ',' || c == '\r' || c == '\n')
                quote = true;
            if (c == '"')
                builder.append("\"\"");
            else
                builder.append(c);
        }
        return quote ? "\"" + builder + "\"" : builder.toString();
    }

    private static DList toRows(List<List<String>> rows) {
        List<DToken> result = new ArrayList<>();
        for (List<String> row : rows) {
            List<DToken> fields = new ArrayList<>();
            for (String field : row)
                fields.add(DString.valueOf(field));
            result.add(DList.valueOf(fields));
        }
        return DList.valueOf(result);
    }

    private static DList rowList(DToken token) {
        if (!(token instanceof DList))
            throw new DevoreCastException(token.type(), "list");
        return (DList) token;
    }

    private static DList listArg(DToken token) {
        if (!(token instanceof DList))
            throw new DevoreCastException(token.type(), "list");
        return (DList) token;
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

    private static final class Parser {
        private final String content;
        private int index;

        private Parser(String content) {
            this.content = content;
            this.index = 0;
        }

        private List<List<String>> parse() {
            List<List<String>> rows = new ArrayList<>();
            if (end())
                return rows;
            while (true) {
                rows.add(parseRecord());
                if (end())
                    return rows;
                expectRecordSeparator();
                if (end())
                    return rows;
            }
        }

        private List<String> parseRecord() {
            List<String> fields = new ArrayList<>();
            fields.add(parseField());
            while (!end() && peek() == ',') {
                ++this.index;
                fields.add(parseField());
            }
            if (!end() && peek() != '\r')
                error("CSV记录分隔符必须是CRLF.");
            return fields;
        }

        private String parseField() {
            if (!end() && peek() == '"')
                return parseEscapedField();
            return parsePlainField();
        }

        private String parseEscapedField() {
            expect('"');
            StringBuilder builder = new StringBuilder();
            while (!end()) {
                char c = next();
                if (c != '"') {
                    builder.append(c);
                    continue;
                }
                if (!end() && peek() == '"') {
                    ++this.index;
                    builder.append('"');
                    continue;
                }
                if (end() || peek() == ',' || peek() == '\r')
                    return builder.toString();
                error("CSV引号字段结束后只能跟随逗号、CRLF或文件结尾.");
            }
            error("CSV引号字段未闭合.");
            return "";
        }

        private String parsePlainField() {
            StringBuilder builder = new StringBuilder();
            while (!end()) {
                char c = peek();
                if (c == ',' || c == '\r')
                    return builder.toString();
                if (c == '"')
                    error("CSV非引号字段不能包含双引号.");
                if (c == '\n')
                    error("CSV记录分隔符必须是CRLF.");
                builder.append(c);
                ++this.index;
            }
            return builder.toString();
        }

        private void expectRecordSeparator() {
            expect('\r');
            if (end() || next() != '\n')
                error("CSV记录分隔符必须是CRLF.");
        }

        private void expect(char expected) {
            if (end() || next() != expected)
                error("CSV格式错误，期望: " + readable(expected));
        }

        private char next() {
            return this.content.charAt(this.index++);
        }

        private char peek() {
            return this.content.charAt(this.index);
        }

        private boolean end() {
            return this.index >= this.content.length();
        }

        private void error(String message) {
            throw new DevoreRuntimeException(message + " index=" + this.index);
        }

        private String readable(char c) {
            if (c == '\r')
                return "CR";
            if (c == '\n')
                return "LF";
            return Character.toString(c);
        }
    }
}
