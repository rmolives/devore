package org.devore.lang.module;

import org.devore.exception.DevoreCastException;
import org.devore.lang.Env;
import org.devore.lang.token.DBool;
import org.devore.lang.token.DInt;
import org.devore.lang.token.DNumber;
import org.devore.lang.token.DToken;
import org.devore.utils.DIntUtils;
import org.devore.utils.NumberUtils;

import java.math.BigInteger;
import java.util.function.Function;

/**
 * 数学
 */
public class MathModule extends DModule {
    /**
     * 创建Math模块实例
     */
    public MathModule() {
        super("math");
    }

    /**
     * 初始化Math模块，注册复杂数学过程
     */
    @Override
    public void init(Env dEnv) {
        initPowerAndRootProcedures(dEnv);   // 幂和开方
        initTrigonometricProcedures(dEnv);  // 三角、反三角、双曲和反双曲
        initNumberTheoryProcedures(dEnv);   // 素数、公约数和公倍数
        initLogarithmicProcedures(dEnv);    // 对数和指数
        initRoundingProcedures(dEnv);       // 取整
    }

    /**
     * 注册幂和开方过程
     */
    private void initPowerAndRootProcedures(Env dEnv) {
        dEnv.addTokenProcedure("pow", ((args, env) ->
                numberArg(args.get(0)).pow(numberArg(args.get(1)))), 2, false);
        unaryNumberProcedure(dEnv, "sqrt", DNumber::sqrt);
        unaryNumberProcedure(dEnv, "cbrt", DNumber::cbrt);
    }

    /**
     * 注册三角、反三角、双曲和反双曲过程
     */
    private void initTrigonometricProcedures(Env dEnv) {
        unaryNumberProcedure(dEnv, "sin", DNumber::sin);
        unaryNumberProcedure(dEnv, "sinh", DNumber::sinh);
        unaryNumberProcedure(dEnv, "cos", DNumber::cos);
        unaryNumberProcedure(dEnv, "cosh", DNumber::cosh);
        unaryNumberProcedure(dEnv, "tan", DNumber::tan);
        unaryNumberProcedure(dEnv, "tanh", DNumber::tanh);
        unaryNumberProcedure(dEnv, "atan", DNumber::arctan);
        unaryNumberProcedure(dEnv, "atanh", DNumber::arctanh);
        dEnv.addTokenProcedure("atan", ((args, env) ->
                numberArg(args.get(0)).arctan(numberArg(args.get(1)))), 2, false);
        unaryNumberProcedure(dEnv, "asin", DNumber::arcsin);
        unaryNumberProcedure(dEnv, "asinh", DNumber::arcsinh);
        unaryNumberProcedure(dEnv, "acos", DNumber::arccos);
        unaryNumberProcedure(dEnv, "acosh", DNumber::arccosh);
        unaryNumberProcedure(dEnv, "sech", DNumber::sech);
        unaryNumberProcedure(dEnv, "csch", DNumber::csch);
        unaryNumberProcedure(dEnv, "coth", DNumber::coth);
        unaryNumberProcedure(dEnv, "asech", DNumber::arcsech);
        unaryNumberProcedure(dEnv, "acsch", DNumber::arccsch);
        unaryNumberProcedure(dEnv, "acoth", DNumber::arccoth);
        unaryNumberProcedure(dEnv, "sec", DNumber::sec);
        unaryNumberProcedure(dEnv, "csc", DNumber::csc);
        unaryNumberProcedure(dEnv, "cot", DNumber::cot);
        unaryNumberProcedure(dEnv, "asec", DNumber::arcsec);
        unaryNumberProcedure(dEnv, "acsc", DNumber::arccsc);
        unaryNumberProcedure(dEnv, "acot", DNumber::arccot);
    }

    /**
     * 注册素数、公约数和公倍数过程
     */
    private void initNumberTheoryProcedures(Env dEnv) {
        dEnv.addTokenProcedure("prime?", ((args, env) ->
                DBool.valueOf(intArg(args.get(0)).toBigInteger().isProbablePrime(100))), 1, false);
        dEnv.addTokenProcedure("prime?", ((args, env) ->
                DBool.valueOf(intArg(args.get(0)).toBigInteger()
                        .isProbablePrime(DIntUtils.toInt(intArg(args.get(1)))))), 2, false);
        dEnv.addTokenProcedure("gcd", ((args, env) -> {
            BigInteger first = intArg(args.get(0)).toBigInteger();
            return DNumber.valueOf(args.stream()
                    .skip(1)
                    .map(MathModule::intArg)
                    .map(DInt::toBigInteger)
                    .reduce(first, NumberUtils::gcd));
        }), 2, true);
        dEnv.addTokenProcedure("lcm", ((args, env) -> {
            BigInteger first = intArg(args.get(0)).toBigInteger();
            return DNumber.valueOf(args.stream()
                    .skip(1)
                    .map(MathModule::intArg)
                    .map(DInt::toBigInteger)
                    .reduce(first, NumberUtils::lcm));
        }), 2, true);
    }

    /**
     * 注册对数和指数过程
     */
    private void initLogarithmicProcedures(Env dEnv) {
        unaryNumberProcedure(dEnv, "log", DNumber::ln);
        dEnv.addTokenProcedure("log", ((args, env) ->
                numberArg(args.get(0)).log(numberArg(args.get(1)))), 2, false);
        unaryNumberProcedure(dEnv, "exp", DNumber::exp);
    }

    /**
     * 注册取整过程
     */
    private void initRoundingProcedures(Env dEnv) {
        unaryNumberProcedure(dEnv, "ceiling", DNumber::ceiling);
        unaryNumberProcedure(dEnv, "floor", DNumber::floor);
        unaryNumberProcedure(dEnv, "truncate", DNumber::truncate);
        unaryNumberProcedure(dEnv, "round", DNumber::round);
    }

    /**
     * 注册一元数字过程
     */
    private static void unaryNumberProcedure(Env dEnv, String name, Function<DNumber, DNumber> fn) {
        dEnv.addTokenProcedure(name, ((args, env) ->
                fn.apply(numberArg(args.get(0)))), 1, false);
    }

    /**
     * 校验并取得数字参数
     */
    private static DNumber numberArg(DToken token) {
        if (!(token instanceof DNumber))
            throw new DevoreCastException(token.type(), "number");
        return (DNumber) token;
    }

    /**
     * 校验并取得整数参数
     */
    private static DInt intArg(DToken token) {
        if (!(token instanceof DInt))
            throw new DevoreCastException(token.type(), "int");
        return (DInt) token;
    }
}
