package org.devore.lang.token;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * 整数
 */
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
    public DNumber add(DNumber a) {
        return DFloat.valueOf(num).add(a);
    }

    @Override
    public DNumber sub(DNumber a) {
        return DFloat.valueOf(num).sub(a);
    }

    @Override
    public DNumber mul(DNumber a) {
        return DFloat.valueOf(num).mul(a);
    }

    @Override
    public DNumber div(DNumber a) {
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
    public DNumber arctan() {
        return DFloat.valueOf(num).arctan();
    }

    @Override
    public DNumber arcsin() {
        return DFloat.valueOf(num).arcsin();
    }

    @Override
    public DNumber arccos() {
        return DFloat.valueOf(num).arccos();
    }

    @Override
    public DNumber ceiling() {
        return (DNumber) copy();
    }

    @Override
    public DNumber floor() {
        return (DNumber) copy();
    }

    @Override
    public DNumber truncate() {
        return (DNumber) copy();
    }

    @Override
    public DNumber round() {
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
    public DNumber log() {
        return DFloat.valueOf(num).log();
    }

    @Override
    public DNumber log(DNumber b) {
        return DFloat.valueOf(num).log(b);
    }

    @Override
    public DNumber atan2(DNumber x) {
        return DFloat.valueOf(num).atan2(x);
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
    protected String str() {
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
