package org.wumoe.devore.lang.token;

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
        return DInt.valueOf(num.mod(n.toBigInteger()));
    }

    @Override
    public DArithmetic add(DArithmetic a) {
        return DFloat.valueOf(num).add(a);
    }

    @Override
    public DArithmetic sub(DArithmetic a) {
        return DFloat.valueOf(num).sub(a);
    }

    @Override
    public DArithmetic mul(DArithmetic a) {
        return DFloat.valueOf(num).mul(a);
    }

    @Override
    public DArithmetic div(DArithmetic a) {
        return DFloat.valueOf(this.toBigInteger()).div(a);
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
    public DNumber sqrt() {
        return DFloat.valueOf(num).sqrt();
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
    public DNumber ln() {
        return DFloat.valueOf(num).ln();
    }

    @Override
    public DNumber exp() {
        return DFloat.valueOf(num).exp();
    }

    @Override
    public BigInteger toBigInteger() {
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
}
