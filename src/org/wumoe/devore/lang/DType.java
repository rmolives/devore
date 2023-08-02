package org.wumoe.devore.lang;

import org.wumoe.devore.lang.token.*;

public class DType {
    public static boolean isOp(Token t) {
        return t instanceof DOp;
    }

    public static boolean isFunction(Token t) {
        return t instanceof DFunction;
    }

    public static boolean isInt(Token t) {
        return t instanceof DInt;
    }

    public static boolean isFloat(Token t) {
        return t instanceof DFloat;
    }
    public static boolean isBool(Token t) {
        return t instanceof DBool;
    }

    public static boolean isNumber(Token t) {
        return t instanceof DNumber;
    }

    public static boolean isArithmetic(Token t) {
        return t instanceof DArithmetic;
    }

    public static boolean isString(Token t) {
        return t instanceof DString;
    }

    public static boolean isList(Token t) {
        return t instanceof DList;
    }
}
