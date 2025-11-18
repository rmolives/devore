package org.devore.module;

import org.devore.Devore;
import org.devore.exception.DevoreCastException;
import org.devore.exception.DevoreRuntimeException;
import org.devore.lang.Env;
import org.devore.lang.Evaluator;
import org.devore.lang.token.*;
import org.devore.parser.AstNode;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.BiFunction;

/**
 * 核心模块
 */
public class CoreModule extends Module {
    @Override
    public void init(Env dEnv) {
        dEnv.put("nil", DWord.WORD_NIL);
        dEnv.put("true", DBool.TRUE);
        dEnv.put("false", DBool.FALSE);
        dEnv.addTokenFunction("+", ((args, env) -> {
            if (!(args.getFirst() instanceof DArithmetic arithmetic))
                throw new DevoreCastException(args.getFirst().type(), "arithmetic");
            for (int i = 1; i < args.size(); ++i) {
                if (!(args.get(i) instanceof DArithmetic))
                    throw new DevoreCastException(args.get(i).type(), "arithmetic");
                arithmetic = arithmetic.add((DArithmetic) args.get(i));
            }
            return arithmetic;
        }), 1, true);
        dEnv.addTokenFunction("-", ((args, env) -> {
            if (!(args.getFirst() instanceof DArithmetic arithmetic))
                throw new DevoreCastException(args.getFirst().type(), "arithmetic");
            if (args.size() == 1)
                return DInt.valueOf(0).sub(arithmetic);
            for (int i = 1; i < args.size(); ++i) {
                if (!(args.get(i) instanceof DArithmetic))
                    throw new DevoreCastException(args.get(i).type(), "arithmetic");
                arithmetic = arithmetic.sub((DArithmetic) args.get(i));
            }
            return arithmetic;
        }), 1, true);
        dEnv.addTokenFunction("*", ((args, env) -> {
            if (!(args.getFirst() instanceof DArithmetic arithmetic))
                throw new DevoreCastException(args.getFirst().type(), "arithmetic");
            for (int i = 1; i < args.size(); ++i) {
                if (!(args.get(i) instanceof DArithmetic))
                    throw new DevoreCastException(args.get(i).type(), "arithmetic");
                arithmetic = arithmetic.mul((DArithmetic) args.get(i));
            }
            return arithmetic;
        }), 1, true);
        dEnv.addTokenFunction("/", ((args, env) -> {
            if (!(args.getFirst() instanceof DArithmetic arithmetic))
                throw new DevoreCastException(args.getFirst().type(), "arithmetic");
            for (int i = 1; i < args.size(); ++i) {
                if (!(args.get(i) instanceof DArithmetic))
                    throw new DevoreCastException(args.get(i).type(), "arithmetic");
                arithmetic = arithmetic.div((DArithmetic) args.get(i));
            }
            return arithmetic;
        }), 1, true);
        dEnv.addTokenFunction("pow", ((args, env) -> {
            if (!(args.getFirst() instanceof DNumber))
                throw new DevoreCastException(args.getFirst().type(), "number");
            if (!(args.get(1) instanceof DNumber))
                throw new DevoreCastException(args.get(1).type(), "number");
            return ((DNumber) args.getFirst()).pow(((DNumber) args.get(1)));
        }), 2, false);
        dEnv.addTokenFunction("average", ((args, env) -> {
            DFloat num = DFloat.valueOf(0);
            for (Token arg : args) {
                if (!(arg instanceof DNumber))
                    throw new DevoreCastException(arg.type(), "number");
                num = DFloat.valueOf(num.add((DNumber) arg).toBigDecimal());
            }
            return num.div(DFloat.valueOf(args.size()));
        }), 1, true);
        dEnv.addTokenFunction("mod", ((args, env) -> {
            if (!(args.getFirst() instanceof DInt))
                throw new DevoreCastException(args.getFirst().type(), "int");
            if (!(args.get(1) instanceof DInt))
                throw new DevoreCastException(args.get(1).type(), "int");
            return ((DInt) args.getFirst()).mod(((DInt) args.get(1)));
        }), 2, false);
        dEnv.addTokenFunction("abs", ((args, env) -> {
            if (!(args.getFirst() instanceof DNumber))
                throw new DevoreCastException(args.getFirst().type(), "number");
            return ((DNumber) args.getFirst()).abs();
        }), 1, false);
        dEnv.addTokenFunction("sqrt", ((args, env) -> {
            if (!(args.getFirst() instanceof DNumber))
                throw new DevoreCastException(args.getFirst().type(), "number");
            return ((DNumber) args.getFirst()).sqrt();
        }), 1, false);
        dEnv.addTokenFunction("sin", ((args, env) -> {
            if (!(args.getFirst() instanceof DNumber))
                throw new DevoreCastException(args.getFirst().type(), "number");
            return ((DNumber) args.getFirst()).sin();
        }), 1, false);
        dEnv.addTokenFunction("cos", ((args, env) -> {
            if (!(args.getFirst() instanceof DNumber))
                throw new DevoreCastException(args.getFirst().type(), "number");
            return ((DNumber) args.getFirst()).cos();
        }), 1, false);
        dEnv.addTokenFunction("tan", ((args, env) -> {
            if (!(args.getFirst() instanceof DNumber))
                throw new DevoreCastException(args.getFirst().type(), "number");
            return ((DNumber) args.getFirst()).tan();
        }), 1, false);
        dEnv.addTokenFunction("ceil", ((args, env) -> {
            if (!(args.getFirst() instanceof DNumber))
                throw new DevoreCastException(args.getFirst().type(), "number");
            return ((DNumber) args.getFirst()).ceil();
        }), 1, false);
        dEnv.addTokenFunction("floor", ((args, env) -> {
            if (!(args.getFirst() instanceof DNumber))
                throw new DevoreCastException(args.getFirst().type(), "number");
            return ((DNumber) args.getFirst()).floor();
        }), 1, false);
        dEnv.addTokenFunction("require", ((args, env) -> {
            for (Token t : args) {
                if (Devore.moduleTable.containsKey(t.toString()))
                    env.load(t.toString());
                else {
                    File file = new File(System.getProperty("user.dir") + File.separator + t.toString().replaceAll("\\.", File.separator) + ".devore");
                    if (file.exists()) {
                        try {
                            Devore.call(env, Files.readString(Path.of(file.toURI())));
                        } catch (IOException e) {
                            throw new DevoreRuntimeException("加载模块[" + e + "]失败.");
                        }
                    }
                }
            }
            return DWord.WORD_NIL;
        }), 1, true);
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
            for (Token arg : args)
                builder.append(arg);
            env.io.err.print(builder);
            return DWord.WORD_NIL;
        }), 1, true);
        dEnv.addTokenFunction("undef", ((args, env) -> {
            for (Token arg : args)
                env.remove(arg.toString());
            return DWord.WORD_NIL;
        }), 1, true);
        dEnv.addSymbolFunction("def", ((ast, env) -> {
            if (ast.getFirst().isEmpty() && ast.getFirst().type != AstNode.AstType.FUNCTION) {
                Env newEnv = env.createChild();
                Token result = DWord.WORD_NIL;
                for (int i = 1; i < ast.size(); ++i)
                    result = Evaluator.eval(newEnv, ast.get(i).copy());
                env.put(ast.getFirst().op.toString(), result);
            } else {
                List<String> parameters = new ArrayList<>();
                for (AstNode parameter : ast.getFirst().children)
                    parameters.add(parameter.op.toString());
                List<AstNode> asts = new ArrayList<>();
                for (int i = 1; i < ast.size(); ++i)
                    asts.add(ast.get(i).copy());
                env.addTokenFunction(ast.getFirst().op.toString(), ((cArgs, cEnv) -> {
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
        dEnv.addSymbolFunction("set!", ((ast, env) -> {
            if (ast.getFirst().isEmpty() && ast.getFirst().type != AstNode.AstType.FUNCTION) {
                Env newEnv = env.createChild();
                Token result = DWord.WORD_NIL;
                for (int i = 1; i < ast.size(); ++i)
                    result = Evaluator.eval(newEnv, ast.get(i).copy());
                env.set(ast.getFirst().op.toString(), result);
            } else {
                List<String> parameters = new ArrayList<>();
                for (AstNode parameter : ast.getFirst().children)
                    parameters.add(parameter.op.toString());
                List<AstNode> asts = new ArrayList<>();
                for (int i = 1; i < ast.size(); ++i)
                    asts.add(ast.get(i).copy());
                env.setTokenFunction(ast.getFirst().op.toString(), ((cArgs, cEnv) -> {
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
            for (AstNode node : ast.getFirst().children) {
                if ("apply".equals(node.op.toString())) {
                    List<String> parameters = new ArrayList<>();
                    List<AstNode> asts = new ArrayList<>();
                    for (AstNode parameterNode : node.getFirst().children)
                        parameters.add(parameterNode.op.toString());
                    for (int i = 1; i < node.size(); ++i)
                        asts.add(node.get(i).copy());
                    newEnv.addTokenFunction(ast.getFirst().op.toString(), ((cArgs, cEnv) -> {
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
            for (AstNode node : ast.getFirst().children) {
                if ("apply".equals(node.op.toString())) {
                    List<String> parameters = new ArrayList<>();
                    List<AstNode> asts = new ArrayList<>();
                    for (AstNode parameterNode : node.getFirst().children)
                        parameters.add(parameterNode.op.toString());
                    for (int i = 1; i < node.size(); ++i)
                        asts.add(node.get(i).copy());
                    newEnv.addTokenFunction(ast.getFirst().op.toString(), ((cArgs, cEnv) -> {
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
            if (!ast.getFirst().isNull()) {
                parameters.add(ast.getFirst().op.toString());
                for (AstNode parameter : ast.getFirst().children)
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
            if (!(args.getFirst() instanceof DFunction))
                throw new DevoreCastException(args.getFirst().type(), "function");
            List<Token> parameters = new ArrayList<>();
            for (int i = 1; i < args.size(); ++i)
                parameters.add(args.get(i));
            AstNode asts = AstNode.nullAst.copy();
            for (Token arg : parameters)
                asts.add(new AstNode(arg));
            return ((DFunction) args.getFirst()).call(asts, env.createChild());
        }), 1, true);
        dEnv.addTokenFunction("act", ((args, env) -> {
            if (!(args.getFirst() instanceof DFunction))
                throw new DevoreCastException(args.getFirst().type(), "function");
            if (!(args.get(1) instanceof DList temp))
                throw new DevoreCastException(args.getFirst().type(), "list");
            List<Token> parameters = temp.toList();
            AstNode asts = AstNode.nullAst.copy();
            for (Token arg : parameters)
                asts.add(new AstNode(arg));
            return ((DFunction) args.getFirst()).call(asts, env.createChild());
        }), 2, false);
        dEnv.addTokenFunction(">", ((args, env) ->
                DBool.valueOf(args.getFirst().compareTo(args.get(1)) > 0)), 2, false);
        dEnv.addTokenFunction("<", ((args, env) ->
                DBool.valueOf(args.getFirst().compareTo(args.get(1)) < 0)), 2, false);
        dEnv.addTokenFunction("=", ((args, env) ->
                DBool.valueOf(args.getFirst().compareTo(args.get(1)) == 0)), 2, false);
        dEnv.addTokenFunction("/=", ((args, env) ->
                DBool.valueOf(args.getFirst().compareTo(args.get(1)) != 0)), 2, false);
        dEnv.addTokenFunction(">=", ((args, env) ->
                DBool.valueOf(args.getFirst().compareTo(args.get(1)) >= 0)), 2, false);
        dEnv.addTokenFunction("<=", ((args, env) ->
                DBool.valueOf(args.getFirst().compareTo(args.get(1)) <= 0)), 2, false);
        dEnv.addSymbolFunction("if", (ast, env) -> {
            Token result = DWord.WORD_NIL;
            Env newEnv = env.createChild();
            Token condition = Evaluator.eval(newEnv, ast.getFirst().copy());
            if (!(condition instanceof DBool))
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
                if (node.op instanceof DOp && "else".equals(node.op.toString())) {
                    result = Evaluator.eval(newEnv, node.getFirst().copy());
                    break;
                } else {
                    Token condition = Evaluator.eval(newEnv, node.getFirst().copy());
                    if (!(condition instanceof DBool))
                        throw new DevoreCastException(condition.type(), "bool");
                    if (((DBool) condition).bool) {
                        Token r = DWord.WORD_NIL;
                        for (int i = 1; i < node.size(); ++i)
                            r = Evaluator.eval(newEnv, node.get(i).copy());
                        result = r;
                        break;
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
            Token condition = Evaluator.eval(newEnv, ast.getFirst().copy());
            if (!(condition instanceof DBool))
                throw new DevoreCastException(condition.type(), "bool");
            while (((DBool) condition).bool) {
                for (int i = 1; i < ast.size(); ++i)
                    result = Evaluator.eval(newEnv, ast.get(i).copy());
                condition = Evaluator.eval(newEnv, ast.getFirst().copy());
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
        dEnv.addTokenFunction("and", ((args, env) -> {
            for (Token arg : args) {
                if (!(arg instanceof DBool))
                    throw new DevoreCastException(arg.type(), "bool");
                if (!((DBool) arg).bool)
                    return DBool.FALSE;
            }
            return DBool.TRUE;
        }), 1, true);
        dEnv.addTokenFunction("or", ((args, env) -> {
            for (Token arg : args) {
                if (!(arg instanceof DBool))
                    throw new DevoreCastException(arg.type(), "bool");
                if (((DBool) arg).bool)
                    return DBool.TRUE;
            }
            return DBool.FALSE;
        }), 1, true);
        dEnv.addTokenFunction("not", ((args, env) -> {
            if (!(args.getFirst() instanceof DBool))
                throw new DevoreCastException(args.getFirst().type(), "bool");
            if (((DBool) args.getFirst()).bool)
                return DBool.FALSE;
            return DBool.TRUE;
        }), 1, false);
        dEnv.addTokenFunction("random", ((args, env) -> {
            if (!(args.getFirst() instanceof DInt))
                throw new DevoreCastException(args.getFirst().type(), "int");
            if (args.size() > 1)
                if (!(args.get(1) instanceof DInt))
                    throw new DevoreCastException(args.get(1).type(), "int");
            BigInteger start = args.size() == 1 ? BigInteger.ZERO : ((DInt) args.getFirst()).toBigInteger();
            BigInteger end = args.size() == 1 ? ((DInt) args.getFirst()).toBigInteger().subtract(BigInteger.ONE) : ((DInt) args.get(1)).toBigInteger().subtract(BigInteger.ONE);
            Random rand = new Random();
            int scale = end.toString().length();
            StringBuilder generated = new StringBuilder();
            for (int i = 0; i < scale; ++i)
                generated.append(rand.nextInt(10));
            BigDecimal inputRangeStart = new BigDecimal("0").setScale(scale, RoundingMode.FLOOR);
            BigDecimal inputRangeEnd =
                    new BigDecimal(String.format("%0" + end.toString().length() + "d", 0).replace('0', '9')).setScale(
                            scale,
                            RoundingMode.FLOOR
                    );
            BigDecimal outputRangeStart = new BigDecimal(start).setScale(scale, RoundingMode.FLOOR);
            BigDecimal outputRangeEnd = new BigDecimal(end).add(new BigDecimal("1")).setScale(scale, RoundingMode.FLOOR);
            BigInteger returnInteger = new BigDecimal(new BigInteger(generated.toString())).setScale(scale, RoundingMode.FLOOR)
                    .subtract(inputRangeStart)
                    .divide(inputRangeEnd.subtract(inputRangeStart), RoundingMode.FLOOR)
                    .multiply(outputRangeEnd.subtract(outputRangeStart))
                    .add(outputRangeStart).setScale(0, RoundingMode.FLOOR).toBigInteger();
            returnInteger = returnInteger.compareTo(end) > 0 ? end : returnInteger;
            return DInt.valueOf(returnInteger);
        }), 1, true);
        dEnv.addTokenFunction("list", ((args, env) -> DList.valueOf(new ArrayList<>(args))), 0, true);
        dEnv.addTokenFunction("list-contains", ((args, env) -> {
            if (!(args.getFirst() instanceof DList))
                throw new DevoreCastException(args.getFirst().type(), "list");
            return DBool.valueOf(((DList) args.getFirst()).contains(args.get(1)));
        }), 2, false);
        dEnv.addTokenFunction("list-get", ((args, env) -> {
            if (!(args.getFirst() instanceof DList list))
                throw new DevoreCastException(args.getFirst().type(), "list");
            if (!(args.get(1) instanceof DInt))
                throw new DevoreCastException(args.get(1).type(), "int");
            if (list.size() == 0)
                return DWord.WORD_NIL;
            return list.get(((DInt) args.get(1)).toBigInteger().intValue());
        }), 2, false);
        dEnv.addTokenFunction("list-set", ((args, env) -> {
            if (!(args.getFirst() instanceof DList list))
                throw new DevoreCastException(args.getFirst().type(), "list");
            if (!(args.get(1) instanceof DInt))
                throw new DevoreCastException(args.get(1).type(), "int");
            if (list.size() == 0)
                return DWord.WORD_NIL;
            return list.set(((DInt) args.get(1)).toBigInteger().intValue(), args.get(2), false);
        }), 3, false);
        dEnv.addTokenFunction("list-remove", ((args, env) -> {
            if (!(args.getFirst() instanceof DList list))
                throw new DevoreCastException(args.getFirst().type(), "list");
            if (list.size() == 0)
                return DWord.WORD_NIL;
            if (!(args.get(1) instanceof DInt))
                return list.remove(((DInt) args.get(1)).toBigInteger().intValue(), false);
            return list.remove(args.get(1), false);
        }), 2, false);
        dEnv.addTokenFunction("list-add", ((args, env) -> {
            if (!(args.getFirst() instanceof DList list))
                throw new DevoreCastException(args.getFirst().type(), "list");
            return list.add(args.get(1), false);
        }), 2, false);
        dEnv.addTokenFunction("list-insert", ((args, env) -> {
            if (!(args.getFirst() instanceof DList list))
                throw new DevoreCastException(args.getFirst().type(), "list");
            if (!(args.get(1) instanceof DInt index))
                throw new DevoreCastException(args.getFirst().type(), "int");
            return list.insert(index.toBigInteger().intValue(), args.get(2), false);
        }), 3, false);
        dEnv.addTokenFunction("list-set!", ((args, env) -> {
            if (!(args.getFirst() instanceof DList list))
                throw new DevoreCastException(args.getFirst().type(), "list");
            if (!(args.get(1) instanceof DInt))
                throw new DevoreCastException(args.get(1).type(), "int");
            if (list.size() == 0)
                return DWord.WORD_NIL;
            return list.set(((DInt) args.get(1)).toBigInteger().intValue(), args.get(2), true);
        }), 3, false);
        dEnv.addTokenFunction("list-remove!", ((args, env) -> {
            if (!(args.getFirst() instanceof DList list))
                throw new DevoreCastException(args.getFirst().type(), "list");
            if (list.size() == 0)
                return DWord.WORD_NIL;
            if (!(args.get(1) instanceof DInt))
                return list.remove(((DInt) args.get(1)).toBigInteger().intValue(), true);
            return list.remove(args.get(1), false);
        }), 2, false);
        dEnv.addTokenFunction("list-add!", ((args, env) -> {
            if (!(args.getFirst() instanceof DList list))
                throw new DevoreCastException(args.getFirst().type(), "list");
            return list.add(args.get(1), true);
        }), 2, false);
        dEnv.addTokenFunction("list-insert!", ((args, env) -> {
            if (!(args.getFirst() instanceof DList list))
                throw new DevoreCastException(args.getFirst().type(), "list");
            if (!(args.get(1) instanceof DInt index))
                throw new DevoreCastException(args.getFirst().type(), "int");
            return list.insert(index.toBigInteger().intValue(), args.get(2), true);
        }), 3, false);
        dEnv.addTokenFunction("head", ((args, env) -> {
            if (!(args.getFirst() instanceof DList list))
                throw new DevoreCastException(args.getFirst().type(), "list");
            if (list.size() == 0)
                return DWord.WORD_NIL;
            return list.getFirst();
        }), 1, false);
        dEnv.addTokenFunction("last", ((args, env) -> {
            if (!(args.getFirst() instanceof DList list))
                throw new DevoreCastException(args.getFirst().type(), "list");
            if (list.size() == 0)
                return DWord.WORD_NIL;
            return list.get(list.size() - 1);
        }), 1, false);
        dEnv.addTokenFunction("tail", ((args, env) -> {
            if (!(args.getFirst() instanceof DList list))
                throw new DevoreCastException(args.getFirst().type(), "list");
            return list.subList(1, list.size());
        }), 1, false);
        dEnv.addTokenFunction("init", ((args, env) -> {
            if (!(args.getFirst() instanceof DList list))
                throw new DevoreCastException(args.getFirst().type(), "list");
            return list.subList(0, list.size() - 1);
        }), 1, false);
        dEnv.addTokenFunction("length", ((args, env) -> {
            if (!(args.getFirst() instanceof DList))
                return DInt.valueOf(args.getFirst().toString().length());
            return DInt.valueOf(((DList) args.getFirst()).size());
        }), 1, false);
        dEnv.addTokenFunction("reverse", ((args, env) -> {
            if (!(args.getFirst() instanceof DList tokens))
                throw new DevoreCastException(args.getFirst().type(), "list");
            List<Token> temp = new ArrayList<>();
            for (int i = tokens.size() - 1; i >= 0; --i)
                temp.add(tokens.get(i));
            return DList.valueOf(new ArrayList<>(temp));
        }), 1, false);
        dEnv.addTokenFunction("reverse!", ((args, env) -> {
            if (!(args.getFirst() instanceof DList tokens))
                throw new DevoreCastException(args.getFirst().type(), "list");
            List<Token> temp = new ArrayList<>();
            for (int i = tokens.size() - 1; i >= 0; --i)
                temp.add(tokens.get(i));
            tokens.clear();
            for (Token t : temp)
                tokens.add(t, true);
            return tokens;
        }), 1, false);
        dEnv.addTokenFunction("sort", ((args, env) -> {
            if (!(args.getFirst() instanceof DList list))
                throw new DevoreCastException(args.getFirst().type(), "list");
            return list.sort(false);
        }), 1, false);
        dEnv.addTokenFunction("sort!", ((args, env) -> {
            if (!(args.getFirst() instanceof DList list))
                throw new DevoreCastException(args.getFirst().type(), "list");
            return list.sort(true);
        }), 1, true);
        dEnv.addTokenFunction("++", ((args, env) -> {
            boolean flag = false;
            for (Token arg : args)
                if (arg instanceof DList) {
                    flag = true;
                    break;
                }
            Token result;
            if (flag) {
                List<Token> list = new ArrayList<>();
                for (Token arg : args) {
                    if (arg instanceof DList)
                        list.addAll(((DList) arg).toList());
                    else
                        list.add(arg);
                }
                result = DList.valueOf(list);
            } else {
                StringBuilder builder = new StringBuilder();
                for (Token arg : args)
                    builder.append(arg.toString());
                result = DString.valueOf(builder.toString());
            }
            return result;
        }), 1, true);
        dEnv.addTokenFunction("map", ((args, env) -> {
            if (!(args.getFirst() instanceof DFunction))
                throw new DevoreCastException(args.getFirst().type(), "function");
            if (!(args.get(1) instanceof DList))
                throw new DevoreCastException(args.get(1).type(), "list");
            List<Token> result = new ArrayList<>();
            List<Token> tokens = ((DList) args.get(1)).toList();
            for (int i = 0; i < tokens.size(); ++i) {
                List<Token> parameters = new ArrayList<>();
                parameters.add(tokens.get(i));
                for (int j = 1; j < args.size() - 1; ++j) {
                    if (!(args.get(j + 1) instanceof DList))
                        throw new DevoreCastException(args.get(j + 1).type(), "list");
                    parameters.add(((DList) args.get(j + 1)).get(i));
                }
                ((DFunction) args.getFirst()).call(parameters.toArray(Token[]::new), env.createChild());
            }
            return DList.valueOf(result);
        }), 2, true);
        dEnv.addTokenFunction("for-each", ((args, env) -> {
            if (!(args.getFirst() instanceof DFunction))
                throw new DevoreCastException(args.getFirst().type(), "function");
            if (!(args.get(1) instanceof DList))
                throw new DevoreCastException(args.get(1).type(), "list");
            List<Token> tokens = ((DList) args.get(1)).toList();
            for (int i = 0; i < tokens.size(); ++i) {
                List<Token> parameters = new ArrayList<>();
                parameters.add(tokens.get(i));
                for (int j = 1; j < args.size() - 1; ++j) {
                    if (!(args.get(j + 1) instanceof DList))
                        throw new DevoreCastException(args.get(j + 1).type(), "list");
                    parameters.add(((DList) args.get(j + 1)).get(i));
                }
                ((DFunction) args.getFirst()).call(parameters.toArray(Token[]::new), env.createChild());
            }
            return DWord.WORD_NIL;
        }), 2, true);
        dEnv.addTokenFunction("foldr", ((args, env) -> {
            if (!(args.getFirst() instanceof DFunction))
                throw new DevoreCastException(args.getFirst().type(), "function");
            if (!(args.get(2) instanceof DList))
                throw new DevoreCastException(args.get(2).type(), "list");
            var result = args.get(1);
            List<Token> tokens = ((DList) args.get(2)).toList();
            for (int i = tokens.size() - 1; i >= 0; --i)
                result = ((DFunction) args.getFirst()).call(new Token[]{tokens.get(i), result}, env.createChild());
            return result;
        }), 3, false);
        dEnv.addTokenFunction("foldl", ((args, env) -> {
            if (!(args.getFirst() instanceof DFunction))
                throw new DevoreCastException(args.getFirst().type(), "function");
            if (!(args.get(2) instanceof DList))
                throw new DevoreCastException(args.get(2).type(), "list");
            var result = args.get(1);
            List<Token> tokens = ((DList) args.get(2)).toList();
            for (int i = tokens.size() - 1; i >= 0; --i)
                result = ((DFunction) args.getFirst()).call(new Token[]{result, tokens.get(i)}, env.createChild());
            return result;
        }), 3, false);
        dEnv.addTokenFunction("filter", ((args, env) -> {
            if (!(args.getFirst() instanceof DFunction))
                throw new DevoreCastException(args.getFirst().type(), "function");
            if (!(args.get(1) instanceof DList))
                throw new DevoreCastException(args.get(1).type(), "list");
            List<Token> result = new ArrayList<>();
            List<Token> tokens = ((DList) args.get(1)).toList();
            for (Token token : tokens) {
                AstNode asts = AstNode.nullAst.copy();
                asts.add(new AstNode(token));
                Token condition = ((DFunction) args.getFirst()).call(asts, env.createChild());
                if (!(condition instanceof DBool))
                    throw new DevoreCastException(condition.type(), "list");
                if (((DBool) condition).bool)
                    result.add(token);
            }
            return DList.valueOf(result);
        }), 2, false);
        dEnv.addTokenFunction("range", ((args, env) -> {
            if (!(args.getFirst() instanceof DInt))
                throw new DevoreCastException(args.getFirst().type(), "int");
            if (args.size() > 1 && !(args.get(1) instanceof DInt))
                throw new DevoreCastException(args.get(1).type(), "int");
            if (args.size() > 2 && !(args.get(2) instanceof DInt))
                throw new DevoreCastException(args.get(2).type(), "int");
            BigInteger start = args.size() > 1 ? ((DInt) args.getFirst()).toBigInteger() : BigInteger.ZERO;
            BigInteger end = args.size() > 1 ? ((DInt) args.get(1)).toBigInteger().subtract(BigInteger.ONE)
                    : ((DInt) args.getFirst()).toBigInteger().subtract(BigInteger.ONE);
            BigInteger step = args.size() > 2 ? ((DInt) args.get(2)).toBigInteger() : BigInteger.ONE;
            BigInteger size = end.subtract(start).divide(step);
            List<Token> list = new ArrayList<>();
            BigInteger i = BigInteger.ZERO;
            while (i.compareTo(size) < 1) {
                list.add(DInt.valueOf(start.add(i.multiply(step))));
                i = i.add(BigInteger.ONE);
            }
            return DList.valueOf(list);
        }), 1, true);
        dEnv.addTokenFunction("string->int", ((args, env) -> {
            if (!(args.getFirst() instanceof DString))
                throw new DevoreCastException(args.getFirst().type(), "string");
            return DInt.valueOf(new BigInteger(args.getFirst().toString()));
        }), 1, false);
        dEnv.addTokenFunction("string->float", ((args, env) -> {
            if (!(args.getFirst() instanceof DString))
                throw new DevoreCastException(args.getFirst().type(), "string");
            return DFloat.valueOf(new BigDecimal(args.getFirst().toString()));
        }), 1, false);
        dEnv.addTokenFunction("string->bool", ((args, env) -> {
            if (!(args.getFirst() instanceof DString))
                throw new DevoreCastException(args.getFirst().type(), "string");
            return "true".equals(args.getFirst().toString()) ? DBool.TRUE : DBool.FALSE;
        }), 1, false);
        dEnv.addTokenFunction("->string", ((args, env) -> DString.valueOf(args.getFirst().toString())), 1, false);
        dEnv.addTokenFunction("string->chars", ((args, env) -> {
            if (!(args.getFirst() instanceof DString))
                throw new DevoreCastException(args.getFirst().type(), "string");
            char[] chars = args.getFirst().toString().toCharArray();
            List<Token> tokens = new ArrayList<>();
            for (char c : chars)
                tokens.add(DString.valueOf(String.valueOf(c)));
            return DList.valueOf(tokens);
        }), 1, false);
        dEnv.addTokenFunction("char->ascii", ((args, env) -> {
            if (!(args.getFirst() instanceof DString))
                throw new DevoreCastException(args.getFirst().type(), "string");
            return DInt.valueOf((int) args.getFirst().toString().charAt(0));
        }), 1, false);
        dEnv.addTokenFunction("ascii->char", ((args, env) -> {
            if (!(args.getFirst() instanceof DInt))
                throw new DevoreCastException(args.getFirst().type(), "int");
            return DString.valueOf(String.valueOf((char) ((DInt) args.getFirst()).toBigInteger().intValue()));
        }), 1, false);
        dEnv.addTokenFunction("exit", ((args, env) -> {
            if (!(args.getFirst() instanceof DInt))
                throw new DevoreCastException(args.getFirst().type(), "int");
            System.exit(((DInt) args.getFirst()).toBigInteger().intValue());
            return args.getFirst();
        }), 1, false);
        dEnv.addTokenFunction("sleep", ((args, env) -> {
            if (!(args.getFirst() instanceof DInt))
                throw new DevoreCastException(args.getFirst().type(), "int");
            try {
                Thread.sleep(((DInt) args.getFirst()).toBigInteger().longValue());
            } catch (InterruptedException e) {
                return args.getFirst();
            }
            return args.getFirst();
        }), 1, false);
        dEnv.addTokenFunction("type", ((args, env) -> DString.valueOf(args.getFirst().type())), 1, false);
        dEnv.addTokenFunction("time", ((args, env) -> DInt.valueOf(System.currentTimeMillis())), 0, false);
    }
}
