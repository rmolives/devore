package org.wumoe.devore.module;

import org.wumoe.devore.exception.DevoreCastException;
import org.wumoe.devore.exception.DevoreRuntimeException;
import org.wumoe.devore.lang.Env;
import org.wumoe.devore.lang.token.DArithmetic;
import org.wumoe.devore.lang.type.DType;

public class Core {
    public static void init(Env dEnv) {
        dEnv.addBuiltFunction("+", ((tokens, env) -> {
            if (tokens.isEmpty())
                throw new DevoreRuntimeException("调用函数 [+] 传参数量错误.");
            if (!DType.isArithmetic(tokens.get(0)))
                throw new DevoreCastException(tokens.get(0).type(), "arithmetic");
            DArithmetic arithmetic = (DArithmetic) tokens.get(0);
            for (int i = 1; i < tokens.size(); ++i) {
                if (!DType.isArithmetic(tokens.get(i)))
                    throw new DevoreCastException(tokens.get(i).type(), "arithmetic");
                arithmetic = arithmetic.add((DArithmetic) tokens.get(i));
            }
            return arithmetic;
        }));
        dEnv.addBuiltFunction("-", ((tokens, env) -> {
            if (tokens.isEmpty())
                throw new DevoreRuntimeException("调用函数 [-] 传参数量错误.");
            if (!DType.isArithmetic(tokens.get(0)))
                throw new DevoreCastException(tokens.get(0).type(), "arithmetic");
            DArithmetic arithmetic = (DArithmetic) tokens.get(0);
            for (int i = 1; i < tokens.size(); ++i) {
                if (!DType.isArithmetic(tokens.get(i)))
                    throw new DevoreCastException(tokens.get(i).type(), "arithmetic");
                arithmetic = arithmetic.sub((DArithmetic) tokens.get(i));
            }
            return arithmetic;
        }));
        dEnv.addBuiltFunction("*", ((tokens, env) -> {
            if (tokens.isEmpty())
                throw new DevoreRuntimeException("调用函数 [*] 传参数量错误.");
            if (!DType.isArithmetic(tokens.get(0)))
                throw new DevoreCastException(tokens.get(0).type(), "arithmetic");
            DArithmetic arithmetic = (DArithmetic) tokens.get(0);
            for (int i = 1; i < tokens.size(); ++i) {
                if (!DType.isArithmetic(tokens.get(i)))
                    throw new DevoreCastException(tokens.get(i).type(), "arithmetic");
                arithmetic = arithmetic.mul((DArithmetic) tokens.get(i));
            }
            return arithmetic;
        }));
        dEnv.addBuiltFunction("/", ((tokens, env) -> {
            if (tokens.isEmpty())
                throw new DevoreRuntimeException("调用函数 [/] 传参数量错误.");
            if (!DType.isArithmetic(tokens.get(0)))
                throw new DevoreCastException(tokens.get(0).type(), "arithmetic");
            DArithmetic arithmetic = (DArithmetic) tokens.get(0);
            for (int i = 1; i < tokens.size(); ++i) {
                if (!DType.isArithmetic(tokens.get(i)))
                    throw new DevoreCastException(tokens.get(i).type(), "arithmetic");
                arithmetic = arithmetic.div((DArithmetic) tokens.get(i));
            }
            return arithmetic;
        }));

    }
}
