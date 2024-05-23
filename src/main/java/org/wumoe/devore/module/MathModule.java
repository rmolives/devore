package org.wumoe.devore.module;

import org.wumoe.devore.exception.DevoreCastException;
import org.wumoe.devore.lang.Env;
import org.wumoe.devore.lang.token.DBool;
import org.wumoe.devore.lang.token.DInt;

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
    }
}
