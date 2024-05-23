package org.wumoe.devore.module;

import org.wumoe.devore.exception.DevoreCastException;
import org.wumoe.devore.exception.DevoreRuntimeException;
import org.wumoe.devore.lang.Env;
import org.wumoe.devore.lang.token.DBool;
import org.wumoe.devore.lang.token.DInt;

import java.math.BigInteger;

public class MathModule extends Module {
    @Override
    public void init(Env dEnv) {
        dEnv.addTokenFunction("prime?", ((args, env) -> {
            if (!(args.get(0) instanceof DInt num))
                throw new DevoreCastException(args.get(0).type(), "int");
            return DBool.valueOf(num.toBigIntger().isProbablePrime(100));
        }), 1, false);
        dEnv.addTokenFunction("prime?", ((args, env) -> {
            if (!(args.get(0) instanceof DInt num))
                throw new DevoreCastException(args.get(0).type(), "int");
            if (!(args.get(1) instanceof DInt certainty))
                throw new DevoreCastException(args.get(1).type(), "int");
            return DBool.valueOf(num.toBigIntger().isProbablePrime(certainty.toBigIntger().intValue()));
        }), 2, false);
        dEnv.addTokenFunction("legendre", ((args, env) -> {
            if (!(args.get(0) instanceof DInt n1))
                throw new DevoreCastException(args.get(0).type(), "int");
            if (!(args.get(1) instanceof DInt n2))
                throw new DevoreCastException(args.get(1).type(), "int");
            BigInteger a = n1.toBigIntger();
            BigInteger p = n2.toBigIntger();
            if (!p.isProbablePrime(100))
                throw new DevoreRuntimeException(p + "不是素数。");
            if (a.mod(p).equals(BigInteger.ZERO))
                return DInt.valueOf(0);
            BigInteger exponent = p.subtract(BigInteger.ONE).divide(BigInteger.TWO);
            BigInteger result = a.modPow(exponent, p);
            if (result.equals(p.subtract(BigInteger.ONE)))
                return DInt.valueOf(-1);
            return DInt.valueOf(1);
        }), 2, false);
    }
}
