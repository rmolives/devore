package org.devore.lang.module;

import org.devore.exception.DevoreCastException;
import org.devore.exception.DevoreRuntimeException;
import org.devore.lang.Env;
import org.devore.lang.token.DString;
import org.devore.lang.token.DToken;

/**
 * HTML工具
 */
public class HtmlModule extends DModule {
    public HtmlModule() {
        super("html");
    }

    @Override
    public void init(Env dEnv) {
        dEnv.addTokenProcedure("html-escape", (args, env) ->
                DString.valueOf(escape(stringArg(args.get(0)))), 1, false);
        dEnv.addTokenProcedure("html-unescape", (args, env) ->
                DString.valueOf(unescape(stringArg(args.get(0)))), 1, false);
    }

    private static String stringArg(DToken token) {
        if (!(token instanceof DString))
            throw new DevoreCastException(token.type(), "string");
        return token.toString();
    }

    private static String escape(String value) {
        StringBuilder builder = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); ++i) {
            char c = value.charAt(i);
            switch (c) {
                case '&':
                    builder.append("&amp;");
                    break;
                case '<':
                    builder.append("&lt;");
                    break;
                case '>':
                    builder.append("&gt;");
                    break;
                case '"':
                    builder.append("&quot;");
                    break;
                case '\'':
                    builder.append("&#39;");
                    break;
                default:
                    builder.append(c);
            }
        }
        return builder.toString();
    }

    private static String unescape(String value) {
        StringBuilder builder = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); ++i) {
            char c = value.charAt(i);
            if (c != '&') {
                builder.append(c);
                continue;
            }

            int semicolon = value.indexOf(';', i + 1);
            if (semicolon < 0) {
                builder.append(c);
                continue;
            }

            String entity = value.substring(i + 1, semicolon);
            String decoded = decodeEntity(entity);
            if (decoded == null) {
                builder.append('&');
                continue;
            }
            builder.append(decoded);
            i = semicolon;
        }
        return builder.toString();
    }

    private static String decodeEntity(String entity) {
        switch (entity) {
            case "amp":
                return "&";
            case "lt":
                return "<";
            case "gt":
                return ">";
            case "quot":
                return "\"";
            case "apos":
            case "#39":
                return "'";
            default:
                if (entity.startsWith("#x") || entity.startsWith("#X"))
                    return decodeCodePoint(entity.substring(2), 16, entity);
                if (entity.startsWith("#"))
                    return decodeCodePoint(entity.substring(1), 10, entity);
                return null;
        }
    }

    private static String decodeCodePoint(String value, int radix, String entity) {
        try {
            int codePoint = Integer.parseInt(value, radix);
            if (!Character.isValidCodePoint(codePoint))
                throw new DevoreRuntimeException("HTML实体编码点无效: &" + entity + ";");
            return new String(Character.toChars(codePoint));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
