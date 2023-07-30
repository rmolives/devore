package org.wumoe.devore.lang.token;

import org.wumoe.devore.exception.DevoreRuntimeException;
import org.wumoe.devore.utils.NumUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;

public class DInt extends DNumber {
    public final BigInteger num;

    private DInt(BigInteger num) {
        this.num = num;
    }

    public static DInt valueOf(long n) {
        return new DInt(BigInteger.valueOf(n));
    }

    public static DInt valueOf(double n) {
        return new DInt(BigDecimal.valueOf(n).toBigInteger());
    }

    public static DInt valueOf(BigInteger n) {
        return new DInt(n);
    }

    public static DInt valueOf(BigDecimal n) {
        return new DInt(n.toBigInteger());
    }

    @Override
    public DArithmetic add(DArithmetic a) {
        DArithmetic result;
        switch (a) {
            case DInt n ->
                result = DInt.valueOf(num.add(n.num));
            case DFloat n ->
                result = DInt.valueOf(num.add(n.num.toBigInteger()));
            default -> throw new DevoreRuntimeException("无法将类型 [" + type() + "] 与类型 [" + a.type() + "] 相加.");
        }
        return result;
    }

    @Override
    public DArithmetic sub(DArithmetic a) {
        DArithmetic result;
        switch (a) {
            case DInt n ->
                    result = DInt.valueOf(num.subtract(n.num));
            case DFloat n ->
                    result = DInt.valueOf(num.subtract(n.num.toBigInteger()));
            default -> throw new DevoreRuntimeException("无法将类型 [" + type() + "] 与类型 [" + a.type() + "] 相减.");
        }
        return result;
    }

    @Override
    public DArithmetic mul(DArithmetic a) {
        DArithmetic result;
        switch (a) {
            case DInt n ->
                    result = DInt.valueOf(num.multiply(n.num));
            case DFloat n ->
                    result = DInt.valueOf(num.multiply(n.num.toBigInteger()));
            default -> throw new DevoreRuntimeException("无法将类型 [" + type() + "] 与类型 [" + a.type() + "] 相乘.");
        }
        return result;
    }

    @Override
    public DArithmetic div(DArithmetic a) {
        DArithmetic result;
        switch (a) {
            case DInt n ->
                    result = DInt.valueOf(num.divide(n.num));
            case DFloat n ->
                    result = DInt.valueOf(new BigDecimal(num).divide(n.num, MathContext.DECIMAL128).toBigInteger());
            default -> throw new DevoreRuntimeException("无法将类型 [" + type() + "] 与类型 [" + a.type() + "] 相除.");
        }
        return result;
    }

    @Override
    public DNumber sin() {
        return DInt.valueOf(NumUtils.sin(new BigDecimal(num)));
    }

    @Override
    public DNumber cos() {
        return DInt.valueOf(NumUtils.cos(new BigDecimal(num)));
    }

    @Override
    public DNumber tan() {
        return DInt.valueOf(NumUtils.tan(new BigDecimal(num)));
    }

    @Override
    public DNumber ceil() {
        return (DNumber) copy();
    }

    @Override
    public DNumber floor() {
        return (DNumber) copy();
    }

    @Override
    public String type() {
        return "int";
    }

    @Override
    public String str() {
        return num.toString();
    }

    @Override
    public Token copy() {
        return DInt.valueOf(num);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof DInt n && num.compareTo(n.num) == 0;
    }
}
