package org.wumoe.devore.module;

import org.wumoe.devore.exception.DevoreCastException;
import org.wumoe.devore.lang.Env;
import org.wumoe.devore.lang.Evaluator;
import org.wumoe.devore.lang.token.DArithmetic;
import org.wumoe.devore.lang.token.DNumber;
import org.wumoe.devore.lang.token.DWord;
import org.wumoe.devore.lang.token.Token;
import org.wumoe.devore.lang.DType;
import org.wumoe.devore.parse.AstNode;

import java.util.ArrayList;
import java.util.List;

public class Core {
    public static void init(Env dEnv) {
        dEnv.addTokenFunction("+", ((tokens, env) -> {
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
        dEnv.addTokenFunction("-", ((tokens, env) -> {
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
        dEnv.addTokenFunction("*", ((tokens, env) -> {
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
        dEnv.addTokenFunction("/", ((tokens, env) -> {
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
        dEnv.addTokenFunction("sin", ((tokens, env) -> {
            if (!DType.isNumber(tokens.get(0)))
                throw new DevoreCastException(tokens.get(0).type(), "number");
            return ((DNumber) tokens.get(0)).sin();
        }), 1, false);
        dEnv.addTokenFunction("cos", ((tokens, env) -> {
            if (!DType.isNumber(tokens.get(0)))
                throw new DevoreCastException(tokens.get(0).type(), "number");
            return ((DNumber) tokens.get(0)).cos();
        }), 1, false);
        dEnv.addTokenFunction("tan", ((tokens, env) -> {
            if (!DType.isNumber(tokens.get(0)))
                throw new DevoreCastException(tokens.get(0).type(), "number");
            return ((DNumber) tokens.get(0)).tan();
        }), 1, false);
        dEnv.addTokenFunction("println", ((tokens, env) -> {
            StringBuilder builder = new StringBuilder();
            for (Token t : tokens)
                builder.append(t);
            env.io.out.println(builder);
            return DWord.WORD_NIL;
        }), 1, true);
        dEnv.addTokenFunction("print", ((tokens, env) -> {
            StringBuilder builder = new StringBuilder();
            for (Token t : tokens)
                builder.append(t);
            env.io.out.print(builder);
            return DWord.WORD_NIL;
        }), 1, true);
        dEnv.addTokenFunction("error-println", ((tokens, env) -> {
            StringBuilder builder = new StringBuilder();
            for (Token t : tokens)
                builder.append(t);
            env.io.err.println(builder);
            return DWord.WORD_NIL;
        }), 1, true);
        dEnv.addTokenFunction("error-print", ((tokens, env) -> {
            StringBuilder builder = new StringBuilder();
            for (Token t : tokens)
                builder.append(t);
            env.io.err.print(builder);
            return DWord.WORD_NIL;
        }), 1, true);
        dEnv.addSymbolFunction("def", ((ast, env) -> {
            if (ast.get(0).isEmpty() && ast.get(0).type != AstNode.AstType.FUNCTION) {
                Token result = DWord.WORD_NIL;
                for (int i = 0; i < ast.size(); ++i)
                    result = Evaluator.eval(env.createChild(), ast.get(i).copy());
                env.put(ast.get(0).op.toString(), result);
            } else {
                List<String> parameters = new ArrayList<>();
                for (int i = 0; i < ast.get(0).size(); ++i)
                    parameters.add(ast.get(0).get(i).op.toString());
                List<AstNode> asts = new ArrayList<>();
                for (int i = 1; i < ast.size(); ++i)
                    asts.add(ast.get(i).copy());
                env.addTokenFunction(ast.get(0).op.toString(), ((cTokens, cEnv) -> {
                    Env newEnv = env.createChild();
                    for (int i = 0; i < parameters.size(); ++i)
                        newEnv.put(parameters.get(i), cTokens.get(i));
                    Token result = DWord.WORD_NIL;
                    for (AstNode astNode : asts)
                        result = Evaluator.eval(newEnv, astNode.copy());
                    return result;
                }), parameters.size(), false);
            }
            return DWord.WORD_NIL;
        }), 2, true);
    }
}