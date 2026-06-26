package org.devore.lang.module;

import org.devore.exception.DevoreCastException;
import org.devore.exception.DevoreRuntimeException;
import org.devore.lang.Env;
import org.devore.lang.token.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * JSON处理
 */
public class JsonModule extends DModule {
    public JsonModule() {
        super("json");
    }

    @Override
    public void init(Env dEnv) {
        dEnv.addTokenProcedure("json-read", (args, env) -> {
            if (!(args.get(0) instanceof DString))
                throw new DevoreCastException(args.get(0).type(), "string");
            return new Parser(args.get(0).toString()).parse();
        }, 1, false);
        dEnv.addTokenProcedure("json-write", (args, env) ->
                DString.valueOf(writeJson(args.get(0),
                        Collections.newSetFromMap(new IdentityHashMap<>()))), 1, false);
        dEnv.addTokenProcedure("json?", (args, env) ->
                DBool.valueOf(isJson(args.get(0),
                        Collections.newSetFromMap(new IdentityHashMap<>()))), 1, false);
    }

    private static String writeJson(DToken token, Set<DToken> parents) {
        if (token == DWord.NIL)
            return "null";
        if (token instanceof DBool)
            return token.toString();
        if (token instanceof DNumber)
            return token.toString();
        if (token instanceof DString)
            return quote(token.toString());
        if (token instanceof DList) {
            if (!parents.add(token))
                throw new DevoreRuntimeException("JSON序列化失败: 检测到循环列表.");
            List<String> items = new ArrayList<>();
            for (DToken item : ((DList) token).toList())
                items.add(writeJson(item, parents));
            parents.remove(token);
            return "[" + String.join(",", items) + "]";
        }
        if (token instanceof DTable) {
            if (!parents.add(token))
                throw new DevoreRuntimeException("JSON序列化失败: 检测到循环表.");
            List<String> items = new ArrayList<>();
            DTable table = (DTable) token;
            for (DToken key : table.keys()) {
                if (!(key instanceof DString))
                    throw new DevoreCastException(key.type(), "string");
                items.add(quote(key.toString()) + ":" + writeJson(table.get(key), parents));
            }
            parents.remove(token);
            return "{" + String.join(",", items) + "}";
        }
        throw new DevoreRuntimeException("JSON不支持类型: " + token.type());
    }

    private static boolean isJson(DToken token, Set<DToken> parents) {
        if (token == DWord.NIL || token instanceof DBool || token instanceof DNumber || token instanceof DString)
            return true;
        if (token instanceof DList) {
            if (!parents.add(token))
                return false;
            for (DToken item : ((DList) token).toList()) {
                if (!isJson(item, parents)) {
                    parents.remove(token);
                    return false;
                }
            }
            parents.remove(token);
            return true;
        }
        if (token instanceof DTable) {
            if (!parents.add(token))
                return false;
            DTable table = (DTable) token;
            for (DToken key : table.keys()) {
                if (!(key instanceof DString) || !isJson(table.get(key), parents)) {
                    parents.remove(token);
                    return false;
                }
            }
            parents.remove(token);
            return true;
        }
        return false;
    }

    private static String quote(String value) {
        StringBuilder builder = new StringBuilder("\"");
        for (int i = 0; i < value.length(); ++i) {
            char c = value.charAt(i);
            switch (c) {
                case '"':
                    builder.append("\\\"");
                    break;
                case '\\':
                    builder.append("\\\\");
                    break;
                case '\b':
                    builder.append("\\b");
                    break;
                case '\f':
                    builder.append("\\f");
                    break;
                case '\n':
                    builder.append("\\n");
                    break;
                case '\r':
                    builder.append("\\r");
                    break;
                case '\t':
                    builder.append("\\t");
                    break;
                default:
                    if (c < 0x20)
                        builder.append(String.format("\\u%04x", (int) c));
                    else
                        builder.append(c);
            }
        }
        return builder.append("\"").toString();
    }

    private static final class Parser {
        private final String json;
        private int index;

        private Parser(String json) {
            this.json = json;
            this.index = 0;
        }

        private DToken parse() {
            DToken token = parseValue();
            skipWhitespace();
            if (!end())
                error("JSON结尾后存在多余内容.");
            return token;
        }

        private DToken parseValue() {
            skipWhitespace();
            if (end())
                error("JSON意外结束.");
            char c = peek();
            if (c == '"')
                return DString.valueOf(parseString());
            if (c == '{')
                return parseObject();
            if (c == '[')
                return parseArray();
            if (c == '-' || isDigit(c))
                return parseNumber();
            if (match("true"))
                return DBool.TRUE;
            if (match("false"))
                return DBool.FALSE;
            if (match("null"))
                return DWord.NIL;
            error("JSON值格式错误");
            return DWord.NIL;
        }

        private DTable parseObject() {
            expect('{');
            skipWhitespace();
            Map<DToken, DToken> table = new HashMap<>();
            if (consume('}'))
                return DTable.valueOf(table);
            while (true) {
                skipWhitespace();
                if (end() || peek() != '"')
                    error("JSON对象key必须是字符串.");
                DString key = DString.valueOf(parseString());
                skipWhitespace();
                expect(':');
                table.put(key, parseValue());
                skipWhitespace();
                if (consume('}'))
                    return DTable.valueOf(table);
                expect(',');
            }
        }

        private DList parseArray() {
            expect('[');
            skipWhitespace();
            List<DToken> list = new ArrayList<>();
            if (consume(']'))
                return DList.valueOf(list);
            while (true) {
                list.add(parseValue());
                skipWhitespace();
                if (consume(']'))
                    return DList.valueOf(list);
                expect(',');
            }
        }

        private DNumber parseNumber() {
            int start = this.index;
            consume('-');
            if (consume('0')) {
                if (!end() && isDigit(peek()))
                    error("JSON数字不能包含前导0.");
            } else {
                readDigits("JSON数字缺少整数部分.");
            }
            if (consume('.'))
                readDigits("JSON数字缺少小数部分.");
            if (!end() && (peek() == 'e' || peek() == 'E')) {
                ++this.index;
                if (!end() && (peek() == '+' || peek() == '-'))
                    ++this.index;
                readDigits("JSON数字缺少指数部分.");
            }
            try {
                return DNumber.valueOf(new BigDecimal(this.json.substring(start, this.index)));
            } catch (NumberFormatException e) {
                error("JSON数字格式错误.");
                return DNumber.valueOf(0);
            }
        }

        private String parseString() {
            expect('"');
            StringBuilder builder = new StringBuilder();
            while (!end()) {
                char c = next();
                if (c == '"')
                    return builder.toString();
                if (c < 0x20)
                    error("JSON字符串不能包含未转义控制字符.");
                if (c != '\\') {
                    builder.append(c);
                    continue;
                }
                if (end())
                    error("JSON字符串转义未完成.");
                char escaped = next();
                switch (escaped) {
                    case '"':
                    case '\\':
                    case '/':
                        builder.append(escaped);
                        break;
                    case 'b':
                        builder.append('\b');
                        break;
                    case 'f':
                        builder.append('\f');
                        break;
                    case 'n':
                        builder.append('\n');
                        break;
                    case 'r':
                        builder.append('\r');
                        break;
                    case 't':
                        builder.append('\t');
                        break;
                    case 'u':
                        builder.append(readUnicode());
                        break;
                    default:
                        error("JSON字符串转义错误.");
                }
            }
            error("JSON字符串未闭合.");
            return "";
        }

        private char readUnicode() {
            if (this.index + 4 > this.json.length())
                error("JSON unicode转义未完成.");
            int value = 0;
            for (int i = 0; i < 4; ++i) {
                int digit = Character.digit(this.json.charAt(this.index++), 16);
                if (digit < 0)
                    error("JSON unicode转义包含非法字符.");
                value = value * 16 + digit;
            }
            return (char) value;
        }

        private void readDigits(String message) {
            int start = this.index;
            while (!end() && isDigit(peek()))
                ++this.index;
            if (this.index == start)
                error(message);
        }

        private boolean match(String value) {
            if (!this.json.startsWith(value, this.index))
                return false;
            this.index += value.length();
            return true;
        }

        private void skipWhitespace() {
            while (!end()) {
                char c = peek();
                if (c != ' ' && c != '\n' && c != '\r' && c != '\t')
                    return;
                this.index++;
            }
        }

        private void expect(char c) {
            if (!consume(c))
                error("JSON期望字符: " + c);
        }

        private boolean consume(char c) {
            if (end() || peek() != c)
                return false;
            ++this.index;
            return true;
        }

        private char peek() {
            return this.json.charAt(this.index);
        }

        private char next() {
            return this.json.charAt(this.index++);
        }

        private boolean end() {
            return this.index >= this.json.length();
        }

        private boolean isDigit(char c) {
            return c >= '0' && c <= '9';
        }

        private void error(String message) {
            throw new DevoreRuntimeException(message + ", index=" + this.index);
        }
    }
}
