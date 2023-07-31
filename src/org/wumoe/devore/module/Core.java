package org.wumoe.devore.module;

import org.wumoe.devore.exception.DevoreCastException;
import org.wumoe.devore.lang.Env;
import org.wumoe.devore.lang.token.DArithmetic;
import org.wumoe.devore.lang.token.DWord;
import org.wumoe.devore.lang.token.Token;
import org.wumoe.devore.lang.type.DType;

public class Core {
    public static void init(Env dEnv) {
        dEnv.addBuiltFunction("+", ((tokens, env) -> {
            if (!DType.isArithmetic(tokens.get(0)))
                throw new DevoreCastException(tokens.get(0).type(), "arithmetic");
            DArithmetic arithmetic = (DArithmetic) tokens.get(0);
            for (int i = 1; i < tokens.size(); ++i) {
                if (!DType.isArithmetic(tokens.get(i)))
                    throw new DevoreCastException(tokens.get(i).type(), "arithmetic");
                arithmetic = arithmetic.add((DArithmetic) tokens.get(i));
            }
            return arithmetic;
        }), 1, true);
        dEnv.addBuiltFunction("-", ((tokens, env) -> {
            if (!DType.isArithmetic(tokens.get(0)))
                throw new DevoreCastException(tokens.get(0).type(), "arithmetic");
            DArithmetic arithmetic = (DArithmetic) tokens.get(0);
            for (int i = 1; i < tokens.size(); ++i) {
                if (!DType.isArithmetic(tokens.get(i)))
                    throw new DevoreCastException(tokens.get(i).type(), "arithmetic");
                arithmetic = arithmetic.sub((DArithmetic) tokens.get(i));
            }
            return arithmetic;
        }), 1, true);
        dEnv.addBuiltFunction("*", ((tokens, env) -> {
            if (!DType.isArithmetic(tokens.get(0)))
                throw new DevoreCastException(tokens.get(0).type(), "arithmetic");
            DArithmetic arithmetic = (DArithmetic) tokens.get(0);
            for (int i = 1; i < tokens.size(); ++i) {
                if (!DType.isArithmetic(tokens.get(i)))
                    throw new DevoreCastException(tokens.get(i).type(), "arithmetic");
                arithmetic = arithmetic.mul((DArithmetic) tokens.get(i));
            }
            return arithmetic;
        }), 1, true);
        dEnv.addBuiltFunction("/", ((tokens, env) -> {
            if (!DType.isArithmetic(tokens.get(0)))
                throw new DevoreCastException(tokens.get(0).type(), "arithmetic");
            DArithmetic arithmetic = (DArithmetic) tokens.get(0);
            for (int i = 1; i < tokens.size(); ++i) {
                if (!DType.isArithmetic(tokens.get(i)))
                    throw new DevoreCastException(tokens.get(i).type(), "arithmetic");
                arithmetic = arithmetic.div((DArithmetic) tokens.get(i));
            }
            return arithmetic;
        }), 1, true);
        dEnv.addBuiltFunction("println", ((tokens, env) -> {
            StringBuilder builder = new StringBuilder();
            for (Token t : tokens)
                builder.append(t);
            env.io.out.println(builder);
            return DWord.WORD_NIL;
        }), 1, true);
        dEnv.addBuiltFunction("print", ((tokens, env) -> {
            StringBuilder builder = new StringBuilder();
            for (Token t : tokens)
                builder.append(t);
            env.io.out.print(builder);
            return DWord.WORD_NIL;
        }), 1, true);
        dEnv.addBuiltFunction("err-println", ((tokens, env) -> {
            StringBuilder builder = new StringBuilder();
            for (Token t : tokens)
                builder.append(t);
            env.io.err.println(builder);
            return DWord.WORD_NIL;
        }), 1, true);
        dEnv.addBuiltFunction("err-print", ((tokens, env) -> {
            StringBuilder builder = new StringBuilder();
            for (Token t : tokens)
                builder.append(t);
            env.io.err.print(builder);
            return DWord.WORD_NIL;
        }), 1, true);
    }
}
