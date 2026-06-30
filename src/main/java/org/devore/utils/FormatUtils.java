package org.devore.utils;

import org.devore.lang.token.DString;
import org.devore.lang.token.DToken;

import java.util.stream.Collectors;

/**
 * Token格式化工具
 */
public class FormatUtils {
    /**
     * 将Token格式化为可读字符串，字符串Token会保留引号并转义特殊字符
     *
     * @param token Token值
     * @return 格式化后的字符串
     */
    public static String formatToken(DToken token) {
        if (token instanceof DString)
            return "\"" + escapeString(token.toString()) + "\"";
        return token.toString();
    }

    /**
     * 转义字符串中的特殊字符
     *
     * @param str 字符串
     * @return 转义后的字符串
     */
    private static String escapeString(String str) {
        return str.chars().mapToObj(c -> {
                    switch (c) {
                        case '\\':
                            return "\\\\";
                        case '"':
                            return "\\\"";
                        case '\n':
                            return "\\n";
                        case '\r':
                            return "\\r";
                        case '\t':
                            return "\\t";
                        case '\b':
                            return "\\b";
                        case '\f':
                            return "\\f";
                        default:
                            return String.valueOf((char) c);
                    }
                }).collect(Collectors.joining());
    }
}
