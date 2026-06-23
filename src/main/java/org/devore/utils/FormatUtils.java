package org.devore.utils;

import org.devore.lang.token.DString;
import org.devore.lang.token.DToken;

import java.util.stream.Collectors;

public class FormatUtils {
    public static String formatToken(DToken token) {
        if (token instanceof DString)
            return "\"" + escapeString(token.toString()) + "\"";
        return token.toString();
    }

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
