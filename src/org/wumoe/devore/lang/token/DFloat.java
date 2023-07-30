package org.wumoe.devore.lang.token;

import org.wumoe.devore.exception.DevoreRuntimeException;
import org.wumoe.devore.utils.NumUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;

public class DFloat extends DNumber {
    public final BigDecimal num;

    protected DFloat(BigDecimal num) {
        this.num = num;
    }

    public static DFloat valueOf(long n) {
        return new DFloat(BigDecimal.valueOf(n));
    }

    public static DFloat valueOf(double n) {
        return new DFloat(BigDecimal.valueOf(n));
    }

    public static DFloat valueOf(BigInteger n) {
        return new DFloat(new BigDecimal(n));
    }

    public static DFloat valueOf(BigDecimal n) {
        return new DFloat(n);
    }
    @Override
    public DArithmetic add(DArithmetic a) {
        DFloat result;
        switch (a) {
            case DInt n ->
                    result = DFloat.valueOf(num.add(new BigDecimal(n.num)));
            case DFloat n ->
                    result = DFloat.valueOf(num.add(n.num));
            default -> throw new DevoreRuntimeException("无法将类型 [" + type() + "] 与类型 [" + a.type() + "] 相加.");
        }
        return NumUtils.isInt(num)? DInt.valueOf(num): result;
    }

    @Override
    public DArithmetic sub(DArithmetic a) {
        DFloat result;
        switch (a) {
            case DInt n ->
                    result = DFloat.valueOf(num.subtract(new BigDecimal(n.num)));
            case DFloat n ->
                    result = DFloat.valueOf(num.subtract(n.num));
            default -> throw new DevoreRuntimeException("无法将类型 [" + type() + "] 与类型 [" + a.type() + "] 相减.");
        }
        return NumUtils.isInt(num)? DInt.valueOf(num): result;
    }

    @Override
    public DArithmetic mul(DArithmetic a) {
        DFloat result;
        switch (a) {
            case DInt n ->
                    result = DFloat.valueOf(num.multiply(new BigDecimal(n.num)));
            case DFloat n ->
                    result = DFloat.valueOf(num.multiply(n.num));
            default -> throw new DevoreRuntimeException("无法将类型 [" + type() + "] 与类型 [" + a.type() + "] 相乘.");
        }
        return NumUtils.isInt(num)? DInt.valueOf(num): result;
    }

    @Override
    public DArithmetic div(DArithmetic a) {
        DFloat result;
        switch (a) {
            case DInt n ->
                    result = DFloat.valueOf(num.divide(new BigDecimal(n.num), MathContext.DECIMAL128));
            case DFloat n ->
                    result = DFloat.valueOf(num.divide(n.num, MathContext.DECIMAL128));
            default -> throw new DevoreRuntimeException("无法将类型 [" + type() + "] 与类型 [" + a.type() + "] 相除.");
        }
        return NumUtils.isInt(num)? DInt.valueOf(num): result;
    }

    @Override
    public DNumber sin() {
        DFloat result = DFloat.valueOf(NumUtils.sin(num));
        return NumUtils.isInt(num)? DInt.valueOf(num): result;
    }

    @Override
    public DNumber cos() {
        DFloat result = DFloat.valueOf(NumUtils.cos(num));
        return NumUtils.isInt(num)? DInt.valueOf(num): result;
    }

    @Override
    public DNumber tan() {
        DFloat result = DFloat.valueOf(NumUtils.tan(num));
        return NumUtils.isInt(num)? DInt.valueOf(num): result;
    }

    @Override
    public DNumber ceil() {
        return DInt.valueOf(num.setScale(0, RoundingMode.CEILING));
    }

    @Override
    public DNumber floor() {
        return DInt.valueOf(num.setScale(0, RoundingMode.FLOOR));
    }

    @Override
    public String type() {
        return "float";
    }

    @Override
    public String str() {
        return NumUtils.isInt(num)? DInt.valueOf(num).str(): num.toPlainString();
    }

    @Override
    public Token copy() {
        return DFloat.valueOf(num);
    }

    @Override
    public int compareTo(Token t) {
        return t instanceof DFloat n ? num.compareTo(n.num): -1;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof DFloat n && num.compareTo(n.num) == 0;
    }
}
