package org.wumoe.devore.lang.token;

import org.wumoe.devore.exception.DevoreRuntimeException;

import java.math.BigDecimal;
import java.math.BigInteger;

public abstract class Token {
    public abstract String type();

    public abstract String str();

    public abstract Token copy();

    public abstract int compareTo(Token t);

    public static Token cast(Object object) {
        Token token;
        switch (object) {
            case String s ->
                    token = DString.valueOf(s);
            case Integer i ->
                    token = DInt.valueOf(i);
            case Long l ->
                    token = DInt.valueOf(l);
            case BigInteger b ->
                    token = DInt.valueOf(b);
            case Float f ->
                    token = DFloat.valueOf(f);
            case Double d ->
                    token = DFloat.valueOf(d);
            case BigDecimal b ->
                    token = DFloat.valueOf(b);
            case Boolean b ->
                    token = DBool.valueOf(b);
            default ->
                    throw new DevoreRuntimeException("Devore未支持该类型.");
        }
        return token;
    }

    @Override
    public String toString() {
        return str();
    }
}
