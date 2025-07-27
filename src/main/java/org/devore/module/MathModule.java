package org.devore.module;

import org.devore.exception.DevoreCastException;
import org.devore.lang.Env;
import org.devore.lang.token.DBool;
import org.devore.lang.token.DInt;
import org.devore.lang.token.DNumber;

import java.math.BigInteger;

/**
 * 数学相关模块
 */
public class MathModule extends Module {
    @Override
    public void init(Env dEnv) {
        dEnv.addTokenFunction("prime?", ((args, env) -> {
            if (!(args.get(0) instanceof DInt num))
                throw new DevoreCastException(args.get(0).type(), "int");
            return DBool.valueOf(num.toBigInteger().isProbablePrime(100));
        }), 1, false);
        dEnv.addTokenFunction("prime?", ((args, env) -> {
            if (!(args.get(0) instanceof DInt num))
                throw new DevoreCastException(args.get(0).type(), "int");
            if (!(args.get(1) instanceof DInt certainty))
                throw new DevoreCastException(args.get(1).type(), "int");
            return DBool.valueOf(num.toBigInteger().isProbablePrime(certainty.toBigInteger().intValue()));
        }), 2, false);
        dEnv.addTokenFunction("gcd", ((args, env) -> {
            if (!(args.get(0) instanceof DInt n1))
                throw new DevoreCastException(args.get(0).type(), "int");
            if (!(args.get(1) instanceof DInt n2))
                throw new DevoreCastException(args.get(1).type(), "int");
            for (BigInteger i = (n1.toBigInteger().compareTo(n2.toBigInteger()) < 0? n1 : n2).toBigInteger();
                 i.compareTo(BigInteger.ONE) > 0; i = i.subtract(BigInteger.ONE))
                if (n1.toBigInteger().mod(i).equals(BigInteger.ZERO) && n2.toBigInteger().mod(i).equals(BigInteger.ZERO))
                    return DInt.valueOf(i);
            return DInt.valueOf(1);
        }), 2, false);
        dEnv.addTokenFunction("lcm", ((args, env) -> {
            if (!(args.get(0) instanceof DInt n1))
                throw new DevoreCastException(args.get(0).type(), "int");
            if (!(args.get(1) instanceof DInt n2))
                throw new DevoreCastException(args.get(1).type(), "int");
            BigInteger gcd = BigInteger.ONE;
            for (BigInteger i = (n1.toBigInteger().compareTo(n2.toBigInteger()) < 0? n1 : n2).toBigInteger();
                 i.compareTo(BigInteger.ONE) > 0; i = i.subtract(BigInteger.ONE))
                if (n1.toBigInteger().mod(i).equals(BigInteger.ZERO) && n2.toBigInteger().mod(i).equals(BigInteger.ZERO)) {
                    gcd = i;
                    break;
                }
            if (n1.toBigInteger().compareTo(BigInteger.ZERO) == 0 || n2.toBigInteger().compareTo(BigInteger.ZERO) == 0)
                return DInt.valueOf(0);
            return DInt.valueOf(n1.toBigInteger().multiply(n2.toBigInteger()).divide(gcd));
        }), 2, false);
        dEnv.addTokenFunction("ln", ((args, env) -> {
            if (!(args.get(0) instanceof DNumber))
                throw new DevoreCastException(args.get(0).type(), "number");
            return ((DNumber) args.get(0)).ln();
        }), 1, false);
        dEnv.addTokenFunction("exp", ((args, env) -> {
            if (!(args.get(0) instanceof DNumber))
                throw new DevoreCastException(args.get(0).type(), "number");
            return ((DNumber) args.get(0)).exp();
        }), 1, false);
    }
}
