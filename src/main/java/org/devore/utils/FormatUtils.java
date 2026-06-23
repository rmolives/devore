package org.devore.utils;

import org.devore.lang.token.DString;
import org.devore.lang.token.DToken;

public class FormatUtils {
    public static String formatToken(DToken token) {
        if (token instanceof DString)
            return "\"" + escapeString(token.toString()) + "\"";
        return token.toString();
    }

    private static String escapeString(String str) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < str.length(); ++i) {
            switch (str.charAt(i)) {
                case '\\':
                    builder.append("\\\\");
                    break;
                case '"':
                    builder.append("\\\"");
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
                case '\b':
                    builder.append("\\b");
                    break;
                case '\f':
                    builder.append("\\f");
                    break;
                default:
                    builder.append(str.charAt(i));
                    break;
            }
        }
        return builder.toString();
    }
}
