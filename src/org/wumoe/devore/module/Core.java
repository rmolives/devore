package org.wumoe.devore.module;

import org.wumoe.devore.exception.DevoreCastException;
import org.wumoe.devore.lang.Env;
import org.wumoe.devore.lang.Evaluator;
import org.wumoe.devore.lang.token.*;
import org.wumoe.devore.lang.DType;
import org.wumoe.devore.parse.AstNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.function.BiFunction;

public class Core extends Module {
    @Override
    public void init(Env dEnv) {
        dEnv.addTokenFunction("+", ((args, env) -> {
            if (!DType.isArithmetic(args.get(0)))
                throw new DevoreCastException(args.get(0).type(), "arithmetic");
            DArithmetic arithmetic = (DArithmetic) args.get(0);
            for (int i = 1; i < args.size(); ++i) {
                if (!DType.isArithmetic(args.get(i)))
                    throw new DevoreCastException(args.get(i).type(), "arithmetic");
                arithmetic = arithmetic.add((DArithmetic) args.get(i));
            }
            return arithmetic;
        }), 1, true);
        dEnv.addTokenFunction("-", ((args, env) -> {
            if (!DType.isArithmetic(args.get(0)))
                throw new DevoreCastException(args.get(0).type(), "arithmetic");
            DArithmetic arithmetic = (DArithmetic) args.get(0);
            for (int i = 1; i < args.size(); ++i) {
                if (!DType.isArithmetic(args.get(i)))
                    throw new DevoreCastException(args.get(i).type(), "arithmetic");
                arithmetic = arithmetic.sub((DArithmetic) args.get(i));
            }
            return arithmetic;
        }), 1, true);
        dEnv.addTokenFunction("*", ((args, env) -> {
            if (!DType.isArithmetic(args.get(0)))
                throw new DevoreCastException(args.get(0).type(), "arithmetic");
            DArithmetic arithmetic = (DArithmetic) args.get(0);
            for (int i = 1; i < args.size(); ++i) {
                if (!DType.isArithmetic(args.get(i)))
                    throw new DevoreCastException(args.get(i).type(), "arithmetic");
                arithmetic = arithmetic.mul((DArithmetic) args.get(i));
            }
            return arithmetic;
        }), 1, true);
        dEnv.addTokenFunction("/", ((args, env) -> {
            if (!DType.isArithmetic(args.get(0)))
                throw new DevoreCastException(args.get(0).type(), "arithmetic");
            DArithmetic arithmetic = (DArithmetic) args.get(0);
            for (int i = 1; i < args.size(); ++i) {
                if (!DType.isArithmetic(args.get(i)))
                    throw new DevoreCastException(args.get(i).type(), "arithmetic");
                arithmetic = arithmetic.div((DArithmetic) args.get(i));
            }
            return arithmetic;
        }), 1, true);
        dEnv.addTokenFunction("sin", ((args, env) -> {
            if (!DType.isNumber(args.get(0)))
                throw new DevoreCastException(args.get(0).type(), "number");
            return ((DNumber) args.get(0)).sin();
        }), 1, false);
        dEnv.addTokenFunction("cos", ((args, env) -> {
            if (!DType.isNumber(args.get(0)))
                throw new DevoreCastException(args.get(0).type(), "number");
            return ((DNumber) args.get(0)).cos();
        }), 1, false);
        dEnv.addTokenFunction("tan", ((args, env) -> {
            if (!DType.isNumber(args.get(0)))
                throw new DevoreCastException(args.get(0).type(), "number");
            return ((DNumber) args.get(0)).tan();
        }), 1, false);
        dEnv.addTokenFunction("ceil", ((args, env) -> {
            if (!DType.isNumber(args.get(0)))
                throw new DevoreCastException(args.get(0).type(), "number");
            return ((DNumber) args.get(0)).ceil();
        }), 1, false);
        dEnv.addTokenFunction("floor", ((args, env) -> {
            if (!DType.isNumber(args.get(0)))
                throw new DevoreCastException(args.get(0).type(), "number");
            return ((DNumber) args.get(0)).floor();
        }), 1, false);
        dEnv.addTokenFunction("println", ((args, env) -> {
            StringBuilder builder = new StringBuilder();
            for (Token t : args)
                builder.append(t);
            env.io.out.println(builder);
            return DWord.WORD_NIL;
        }), 1, true);
        dEnv.addTokenFunction("print", ((args, env) -> {
            StringBuilder builder = new StringBuilder();
            for (Token t : args)
                builder.append(t);
            env.io.out.print(builder);
            return DWord.WORD_NIL;
        }), 1, true);
        dEnv.addTokenFunction("error-println", ((args, env) -> {
            StringBuilder builder = new StringBuilder();
            for (Token t : args)
                builder.append(t);
            env.io.err.println(builder);
            return DWord.WORD_NIL;
        }), 1, true);
        dEnv.addTokenFunction("error-print", ((args, env) -> {
            StringBuilder builder = new StringBuilder();
            for (Token t : args)
                builder.append(t);
            env.io.err.print(builder);
            return DWord.WORD_NIL;
        }), 1, true);
        dEnv.addSymbolFunction("def", ((ast, env) -> {
            if (ast.get(0).isEmpty() && ast.get(0).type != AstNode.AstType.FUNCTION) {
                Env newEnv = env.createChild();
                Token result = DWord.WORD_NIL;
                for (int i = 0; i < ast.size(); ++i)
                    result = Evaluator.eval(newEnv, ast.get(i).copy());
                env.put(ast.get(0).op.toString(), result);
            } else {
                List<String> parameters = new ArrayList<>();
                for (AstNode parameter : ast.get(0).children)
                    parameters.add(parameter.op.toString());
                List<AstNode> asts = new ArrayList<>();
                for (int i = 1; i < ast.size(); ++i)
                    asts.add(ast.get(i).copy());
                env.addTokenFunction(ast.get(0).op.toString(), ((cArgs, cEnv) -> {
                    Env newEnv = env.createChild();
                    for (int i = 0; i < parameters.size(); ++i)
                        newEnv.put(parameters.get(i), cArgs.get(i));
                    Token result = DWord.WORD_NIL;
                    for (AstNode astNode : asts)
                        result = Evaluator.eval(newEnv, astNode.copy());
                    return result;
                }), parameters.size(), false);
            }
            return DWord.WORD_NIL;
        }), 2, true);
        dEnv.addSymbolFunction("set", ((ast, env) -> {
            if (ast.get(0).isEmpty() && ast.get(0).type != AstNode.AstType.FUNCTION) {
                Env newEnv = env.createChild();
                Token result = DWord.WORD_NIL;
                for (int i = 0; i < ast.size(); ++i)
                    result = Evaluator.eval(newEnv, ast.get(i).copy());
                env.set(ast.get(0).op.toString(), result);
            } else {
                List<String> parameters = new ArrayList<>();
                for (AstNode parameter : ast.get(0).children)
                    parameters.add(parameter.op.toString());
                List<AstNode> asts = new ArrayList<>();
                for (int i = 1; i < ast.size(); ++i)
                    asts.add(ast.get(i).copy());
                env.setTokenFunction(ast.get(0).op.toString(), ((cArgs, cEnv) -> {
                    Env newEnv = env.createChild();
                    for (int i = 0; i < parameters.size(); ++i)
                        newEnv.put(parameters.get(i), cArgs.get(i));
                    Token result = DWord.WORD_NIL;
                    for (AstNode astNode : asts)
                        result = Evaluator.eval(newEnv, astNode.copy());
                    return result;
                }), parameters.size(), false);
            }
            return DWord.WORD_NIL;
        }), 2, true);
        dEnv.addSymbolFunction("let", ((ast, env) -> {
            Env newEnv = env.createChild();
            Token result = DWord.WORD_NIL;
            for (AstNode node : ast.get(0).children) {
                if ("apply".equals(node.op.toString())) {
                    List<String> parameters = new ArrayList<>();
                    List<AstNode> asts = new ArrayList<>();
                    for (AstNode parameterNode : node.get(0).children)
                        parameters.add(parameterNode.op.toString());
                    for (int i = 1; i < node.size(); ++i)
                        asts.add(node.get(i).copy());
                    newEnv.addTokenFunction(ast.get(0).op.toString(), ((cArgs, cEnv) -> {
                        Env newInEnv = env.createChild();
                        for (int i = 0; i < parameters.size(); ++i)
                            newInEnv.put(parameters.get(i), cArgs.get(i));
                        Token inResult = DWord.WORD_NIL;
                        for (AstNode astNode : asts)
                            inResult = Evaluator.eval(newInEnv, astNode.copy());
                        return inResult;
                    }), parameters.size(), false);
                } else {
                    Token value = DWord.WORD_NIL;
                    for (AstNode e : node.children)
                        value = Evaluator.eval(env, e.copy());
                    newEnv.put(node.op.toString(), value);
                }
            }
            for (int i = 1; i < ast.size(); ++i)
                result = Evaluator.eval(newEnv, ast.get(i).copy());
            return result;
        }), 2, true);
        dEnv.addSymbolFunction("let*", ((ast, env) -> {
            Env newEnv = env.createChild();
            Token result = DWord.WORD_NIL;
            for (AstNode node : ast.get(0).children) {
                if ("apply".equals(node.op.toString())) {
                    List<String> parameters = new ArrayList<>();
                    List<AstNode> asts = new ArrayList<>();
                    for (AstNode parameterNode : node.get(0).children)
                        parameters.add(parameterNode.op.toString());
                    for (int i = 1; i < node.size(); ++i)
                        asts.add(node.get(i).copy());
                    newEnv.addTokenFunction(ast.get(0).op.toString(), ((cArgs, cEnv) -> {
                        Env newInEnv = newEnv.createChild();
                        for (int i = 0; i < parameters.size(); ++i)
                            newInEnv.put(parameters.get(i), cArgs.get(i));
                        Token inResult = DWord.WORD_NIL;
                        for (AstNode astNode : asts)
                            inResult = Evaluator.eval(newInEnv, astNode.copy());
                        return inResult;
                    }), parameters.size(), false);
                } else {
                    Token value = DWord.WORD_NIL;
                    for (AstNode e : node.children)
                        value = Evaluator.eval(newEnv, e.copy());
                    newEnv.put(node.op.toString(), value);
                }
            }
            for (int i = 1; i < ast.size(); ++i)
                result = Evaluator.eval(newEnv, ast.get(i).copy());
            return result;
        }), 2, true);
        dEnv.addSymbolFunction("lambda", ((ast, env) -> {
            List<String> parameters = new ArrayList<>();
            if (!ast.get(0).isNull()) {
                parameters.add(ast.get(0).op.toString());
                for (AstNode parameter : ast.get(0).children)
                    parameters.add(parameter.op.toString());
            }
            List<AstNode> asts = new ArrayList<>();
            for (int i = 1; i < ast.size(); ++i)
                asts.add(ast.get(i).copy());
            BiFunction<AstNode, Env, Token> df = (inAst, inEnv) -> {
                List<Token> args = new ArrayList<>();
                for (int i = 0; i < inAst.size(); ++i) {
                    inAst.get(i).op = Evaluator.eval(inEnv, inAst.get(i).copy());
                    args.add(inAst.get(i).op);
                }
                Env newInEnv = env.createChild();
                for (int i = 0; i < parameters.size(); ++i)
                    newInEnv.put(parameters.get(i), args.get(i));
                Token inResult = DWord.WORD_NIL;
                for (AstNode astNode : asts)
                    inResult = Evaluator.eval(newInEnv, astNode.copy());
                return inResult;
            };
            return DFunction.newFunction(df, parameters.size(), false);
        }), 2, true);
        dEnv.addTokenFunction("apply", ((args, env) -> {
            if (!DType.isFunction(args.get(0)))
                throw new DevoreCastException(args.get(0).type(), "function");
            List<Token> parameters = new ArrayList<>();
            for (int i = 1; i < args.size(); ++i)
                parameters.add(args.get(i));
            AstNode asts = AstNode.nullAst.copy();
            for (Token arg : parameters)
                asts.add(new AstNode(arg));
            return ((DFunction) args.get(0)).call(asts, env.createChild());
        }), 1, true);
        dEnv.addTokenFunction(">", ((args, env) ->
                DBool.valueOf(args.get(0).compareTo(args.get(1)) > 0)), 2, false);
        dEnv.addTokenFunction("<", ((args, env) ->
                DBool.valueOf(args.get(0).compareTo(args.get(1)) < 0)), 2, false);
        dEnv.addTokenFunction("=", ((args, env) ->
                DBool.valueOf(args.get(0).compareTo(args.get(1)) == 0)), 2, false);
        dEnv.addTokenFunction("!=", ((args, env) ->
                DBool.valueOf(args.get(0).compareTo(args.get(1)) != 0)), 2, false);
        dEnv.addTokenFunction(">=", ((args, env) ->
                DBool.valueOf(args.get(0).compareTo(args.get(1)) >= 0)), 2, false);
        dEnv.addTokenFunction("<=", ((args, env) ->
                DBool.valueOf(args.get(0).compareTo(args.get(1)) <= 0)), 2, false);
        dEnv.addSymbolFunction("if", (ast, env) -> {
            Token result = DWord.WORD_NIL;
            Env newEnv = env.createChild();
            Token condition = Evaluator.eval(newEnv, ast.get(0).copy());
            if (!DType.isBool(condition))
                throw new DevoreCastException(condition.type(), "bool");
            if (((DBool) condition).bool)
                result = Evaluator.eval(newEnv, ast.get(1).copy());
            else if (ast.size() > 2)
                result = Evaluator.eval(newEnv, ast.get(2).copy());
            return result;
        }, 2, true);
        dEnv.addSymbolFunction("cond", (ast, env) -> {
            Token result = DWord.WORD_NIL;
            Env newEnv = env.createChild();
            for (AstNode node : ast.children) {
                if (DType.isOp(node.op) && "else".equals(node.op.toString()))
                    result = Evaluator.eval(newEnv, node.get(0).copy());
                else {
                    Token condition = Evaluator.eval(newEnv, node.get(0).copy());
                    if (!DType.isBool(condition))
                        throw new DevoreCastException(condition.type(), "bool");
                    if (((DBool) condition).bool) {
                        Token r = DWord.WORD_NIL;
                        for (int i = 1; i < node.size(); ++i)
                            r = Evaluator.eval(newEnv, node.get(i).copy());
                        result = r;
                    }
                }
            }
            return result;
        }, 2, true);
        dEnv.addSymbolFunction("begin", (ast, env) -> {
            Token result = DWord.WORD_NIL;
            Env newEnv = env.createChild();
            for (AstNode node : ast.children)
                result = Evaluator.eval(newEnv, node.copy());
            return result;
        }, 2, true);
        dEnv.addSymbolFunction("while", (ast, env) -> {
            Token result = DWord.WORD_NIL;
            Env newEnv = env.createChild();
            Token condition = Evaluator.eval(newEnv, ast.get(0).copy());
            if (!DType.isBool(condition))
                throw new DevoreCastException(condition.type(), "bool");
            while (((DBool) condition).bool) {
                for (int i = 1; i < ast.size(); ++i)
                    result = Evaluator.eval(newEnv, ast.get(i).copy());
                condition = Evaluator.eval(newEnv, ast.get(0).copy());
            }
            return result;
        }, 2, true);
        dEnv.addTokenFunction("read-line", ((args, env) ->
                DString.valueOf(new Scanner(env.io.in).nextLine())), 0, false);
        dEnv.addTokenFunction("read-int", ((args, env) ->
                DInt.valueOf(new Scanner(env.io.in).nextBigInteger())), 0, false);
        dEnv.addTokenFunction("read-float", ((args, env) ->
                DFloat.valueOf(new Scanner(env.io.in).nextBigDecimal())), 0, false);
        dEnv.addTokenFunction("read-bool", ((args, env) ->
                DBool.valueOf(new Scanner(env.io.in).nextBoolean())), 0, false);
        dEnv.addTokenFunction("read", ((args, env) ->
                DString.valueOf(new Scanner(env.io.in).next())), 0, false);
        dEnv.addTokenFunction("newline", ((args, env) -> {
            env.io.out.println();
            return DWord.WORD_NIL;
        }), 0, false);
        dEnv.addTokenFunction("error-newline", ((args, env) -> {
            env.io.err.println();
            return DWord.WORD_NIL;
        }), 0, false);
    }
}