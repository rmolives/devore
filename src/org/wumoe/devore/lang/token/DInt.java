package org.wumoe.devore.lang.token;

import org.wumoe.devore.exception.DevoreRuntimeException;

import java.math.BigDecimal;
import java.math.BigInteger;

public class DInt extends DNumber {
    public final BigInteger num;

    protected DInt(BigInteger num) {
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

    public DInt mod(DInt n) {
        return DInt.valueOf(num.mod(n.toBigIntger()));
    }

    @Override
    public DArithmetic add(DArithmetic a) {
        DArithmetic result;
        switch (a) {
            case DInt n -> result = DInt.valueOf(num.add(n.num));
            case DFloat n -> result = DFloat.valueOf(new BigDecimal(num).add(n.num));
            default -> throw new DevoreRuntimeException("无法将类型 [" + type() + "] 与类型 [" + a.type() + "] 相加.");
        }
        return result;
    }

    @Override
    public DArithmetic sub(DArithmetic a) {
        DArithmetic result;
        switch (a) {
            case DInt n -> result = DInt.valueOf(num.subtract(n.num));
            case DFloat n -> result = DFloat.valueOf(new BigDecimal(num).subtract(n.num));
            default -> throw new DevoreRuntimeException("无法将类型 [" + type() + "] 与类型 [" + a.type() + "] 相减.");
        }
        return result;
    }

    @Override
    public DArithmetic mul(DArithmetic a) {
        DArithmetic result;
        switch (a) {
            case DInt n -> result = DInt.valueOf(num.multiply(n.num));
            case DFloat n -> result = DFloat.valueOf(new BigDecimal(num).multiply(n.num));
            default -> throw new DevoreRuntimeException("无法将类型 [" + type() + "] 与类型 [" + a.type() + "] 相乘.");
        }
        return result;
    }

    @Override
    public DArithmetic div(DArithmetic a) {
        return DFloat.valueOf(this.toBigIntger()).div(a);
    }

    @Override
    public DNumber sin() {
        return DFloat.valueOf(num).sin();
    }

    @Override
    public DNumber cos() {
        return DFloat.valueOf(num).cos();
    }

    @Override
    public DNumber tan() {
        return DFloat.valueOf(num).tan();
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
    public DNumber pow(DNumber n) {
        return DFloat.valueOf(this.num).pow(n);
    }

    @Override
    public DNumber abs() {
        return DInt.valueOf(num.abs());
    }

    @Override
    public BigInteger toBigIntger() {
        return num;
    }

    @Override
    public BigDecimal toBigDecimal() {
        return new BigDecimal(num);
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
    public int compareTo(Token t) {
        return DFloat.valueOf(this.num).compareTo(t);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof DInt n && num.compareTo(n.num) == 0;
    }
}
