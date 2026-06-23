package org.devore.utils;

import org.devore.lang.token.DString;
import org.devore.lang.token.DToken;

public class FormatUtils {
    public static String formatToken(DToken token) {
        if (token instanceof DString)
            return "\"" + token + "\"";
        return token.toString();
    }
}
