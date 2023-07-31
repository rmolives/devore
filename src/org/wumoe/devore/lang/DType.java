package org.wumoe.devore.lang;

import org.wumoe.devore.lang.token.Token;

public class DType {
    public static boolean isOp(Token t) {
        return "op".equals(t.type());
    }

    public static boolean isFunction(Token t) {
        return "function".equals(t.type());
    }

    public static boolean isInt(Token t) {
        return "int".equals(t.type());
    }

    public static boolean isFloat(Token t) {
        return "float".equals(t.type());
    }

    public static boolean isNumber(Token t) {
        return isInt(t) || isFloat(t);
    }

    public static boolean isArithmetic(Token t) {
        return isNumber(t);
    }

    public static boolean isString(Token t) {
        return "string".equals(t.type());
    }
}
