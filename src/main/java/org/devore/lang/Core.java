package org.devore.lang;

import org.devore.exception.DevoreCastException;
import org.devore.exception.DevoreRuntimeException;
import org.devore.lang.token.*;
import org.devore.parser.AstNode;
import org.devore.utils.NumberUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.function.BiFunction;

/**
 * 核心
 */
public class Core {
    public static void init(Env dEnv) {
        dEnv.put("nil", DWord.NIL);
        dEnv.put("true", DBool.TRUE);
        dEnv.put("false", DBool.FALSE);
        dEnv.addTokenProcedure("+", ((args, env) -> {
            if (!(args.get(0) instanceof DNumber)) throw new DevoreCastException(args.get(0).type(), "number");
            DNumber number = (DNumber) args.get(0);
            for (int i = 1; i < args.size(); ++i) {
                if (!(args.get(i) instanceof DNumber)) throw new DevoreCastException(args.get(i).type(), "number");
                number = number.add((DNumber) args.get(i));
            }
            return number;
        }), 1, true);
        dEnv.addTokenProcedure("-", ((args, env) -> {
            if (!(args.get(0) instanceof DNumber)) throw new DevoreCastException(args.get(0).type(), "number");
            DNumber number = (DNumber) args.get(0);
            if (args.size() == 1) return DNumber.valueOf(number.toBigDecimal().negate());
            for (int i = 1; i < args.size(); ++i) {
                if (!(args.get(i) instanceof DNumber)) throw new DevoreCastException(args.get(i).type(), "number");
                number = number.sub((DNumber) args.get(i));
            }
            return number;
        }), 1, true);
        dEnv.addTokenProcedure("*", ((args, env) -> {
            if (!(args.get(0) instanceof DNumber)) throw new DevoreCastException(args.get(0).type(), "number");
            DNumber number = (DNumber) args.get(0);
            for (int i = 1; i < args.size(); ++i) {
                if (!(args.get(i) instanceof DNumber)) throw new DevoreCastException(args.get(i).type(), "number");
                number = number.mul((DNumber) args.get(i));
            }
            return number;
        }), 1, true);
        dEnv.addTokenProcedure("/", ((args, env) -> {
            if (!(args.get(0) instanceof DNumber)) throw new DevoreCastException(args.get(0).type(), "number");
            DNumber number = (DNumber) args.get(0);
            for (int i = 1; i < args.size(); ++i) {
                if (!(args.get(i) instanceof DNumber)) throw new DevoreCastException(args.get(i).type(), "number");
                number = number.div((DNumber) args.get(i));
            }
            return number;
        }), 1, true);
        dEnv.addTokenProcedure("pow", ((args, env) -> {
            if (!(args.get(0) instanceof DNumber)) throw new DevoreCastException(args.get(0).type(), "number");
            if (!(args.get(1) instanceof DNumber)) throw new DevoreCastException(args.get(1).type(), "number");
            return ((DNumber) args.get(0)).pow(((DNumber) args.get(1)));
        }), 2, false);
        dEnv.addTokenProcedure("average", ((args, env) -> {
            DNumber num = DNumber.valueOf(0);
            for (Token arg : args) {
                if (!(arg instanceof DNumber)) throw new DevoreCastException(arg.type(), "number");
                num = DNumber.valueOf(num.add((DNumber) arg).toBigDecimal());
            }
            return num.div(DNumber.valueOf(args.size()));
        }), 1, true);
        dEnv.addTokenProcedure("mod", ((args, env) -> {
            if (!(args.get(0) instanceof DInt)) throw new DevoreCastException(args.get(0).type(), "int");
            if (!(args.get(1) instanceof DInt)) throw new DevoreCastException(args.get(1).type(), "int");
            return DNumber.valueOf(((DInt) args.get(0)).toBigInteger().mod(((DInt) args.get(1)).toBigInteger()));
        }), 2, false);
        dEnv.addTokenProcedure("abs", ((args, env) -> {
            if (!(args.get(0) instanceof DNumber)) throw new DevoreCastException(args.get(0).type(), "number");
            return ((DNumber) args.get(0)).abs();
        }), 1, false);
        dEnv.addTokenProcedure("sqrt", ((args, env) -> {
            if (!(args.get(0) instanceof DNumber)) throw new DevoreCastException(args.get(0).type(), "number");
            return ((DNumber) args.get(0)).sqrt();
        }), 1, false);
        dEnv.addTokenProcedure("sin", ((args, env) -> {
            if (!(args.get(0) instanceof DNumber)) throw new DevoreCastException(args.get(0).type(), "number");
            return ((DNumber) args.get(0)).sin();
        }), 1, false);
        dEnv.addTokenProcedure("cos", ((args, env) -> {
            if (!(args.get(0) instanceof DNumber)) throw new DevoreCastException(args.get(0).type(), "number");
            return ((DNumber) args.get(0)).cos();
        }), 1, false);
        dEnv.addTokenProcedure("tan", ((args, env) -> {
            if (!(args.get(0) instanceof DNumber)) throw new DevoreCastException(args.get(0).type(), "number");
            return ((DNumber) args.get(0)).tan();
        }), 1, false);
        dEnv.addTokenProcedure("atan", ((args, env) -> {
            if (!(args.get(0) instanceof DNumber)) throw new DevoreCastException(args.get(0).type(), "number");
            return ((DNumber) args.get(0)).arctan();
        }), 1, false);
        dEnv.addTokenProcedure("atan", ((args, env) -> {
            if (!(args.get(0) instanceof DNumber)) throw new DevoreCastException(args.get(0).type(), "number");
            if (!(args.get(1) instanceof DNumber)) throw new DevoreCastException(args.get(1).type(), "number");
            return ((DNumber) args.get(0)).arctan((DNumber) args.get(1));
        }), 2, false);
        dEnv.addTokenProcedure("asin", ((args, env) -> {
            if (!(args.get(0) instanceof DNumber)) throw new DevoreCastException(args.get(0).type(), "number");
            return ((DNumber) args.get(0)).arcsin();
        }), 1, false);
        dEnv.addTokenProcedure("acos", ((args, env) -> {
            if (!(args.get(0) instanceof DNumber)) throw new DevoreCastException(args.get(0).type(), "number");
            return ((DNumber) args.get(0)).arccos();
        }), 1, false);
        dEnv.addTokenProcedure("prime?", ((args, env) -> {
            if (!(args.get(0) instanceof DInt)) throw new DevoreCastException(args.get(0).type(), "int");
            return DBool.valueOf(((DInt) args.get(0)).toBigInteger().isProbablePrime(100));
        }), 1, false);
        dEnv.addTokenProcedure("prime?", ((args, env) -> {
            if (!(args.get(0) instanceof DInt)) throw new DevoreCastException(args.get(0).type(), "int");
            if (!(args.get(1) instanceof DInt)) throw new DevoreCastException(args.get(1).type(), "int");
            return DBool.valueOf(((DInt) args.get(0)).toBigInteger().isProbablePrime(((DInt) args.get(1)).toBigInteger().intValue()));
        }), 2, false);
        dEnv.addTokenProcedure("gcd", ((args, env) -> {
            for (Token arg : args)
                if (!(arg instanceof DInt)) throw new DevoreCastException(arg.type(), "int");
            BigInteger result = ((DInt) args.get(0)).toBigInteger();
            for (int i = 1; i < args.size(); ++i)
                result = NumberUtils.gcd(result, ((DInt) args.get(i)).toBigInteger());
            return DNumber.valueOf(result);
        }), 2, true);
        dEnv.addTokenProcedure("lcm", ((args, env) -> {
            for (Token arg : args)
                if (!(arg instanceof DInt)) throw new DevoreCastException(arg.type(), "int");
            BigInteger result = ((DInt) args.get(0)).toBigInteger();
            for (int i = 1; i < args.size(); ++i)
                result = NumberUtils.lcm(result, ((DInt) args.get(i)).toBigInteger());
            return DNumber.valueOf(result);
        }), 2, true);
        dEnv.addTokenProcedure("log", ((args, env) -> {
            if (!(args.get(0) instanceof DNumber)) throw new DevoreCastException(args.get(0).type(), "number");
            return ((DNumber) args.get(0)).log();
        }), 1, false);
        dEnv.addTokenProcedure("log", ((args, env) -> {
            if (!(args.get(0) instanceof DNumber)) throw new DevoreCastException(args.get(0).type(), "number");
            if (!(args.get(1) instanceof DNumber)) throw new DevoreCastException(args.get(1).type(), "number");
            return ((DNumber) args.get(0)).log((DNumber) args.get(1));
        }), 2, false);
        dEnv.addTokenProcedure("exp", ((args, env) -> {
            if (!(args.get(0) instanceof DNumber)) throw new DevoreCastException(args.get(0).type(), "number");
            return ((DNumber) args.get(0)).exp();
        }), 1, false);
        dEnv.addTokenProcedure("ceiling", ((args, env) -> {
            if (!(args.get(0) instanceof DNumber)) throw new DevoreCastException(args.get(0).type(), "number");
            return ((DNumber) args.get(0)).ceiling();
        }), 1, false);
        dEnv.addTokenProcedure("floor", ((args, env) -> {
            if (!(args.get(0) instanceof DNumber)) throw new DevoreCastException(args.get(0).type(), "number");
            return ((DNumber) args.get(0)).floor();
        }), 1, false);
        dEnv.addTokenProcedure("truncate", ((args, env) -> {
            if (!(args.get(0) instanceof DNumber)) throw new DevoreCastException(args.get(0).type(), "number");
            return ((DNumber) args.get(0)).truncate();
        }), 1, false);
        dEnv.addTokenProcedure("round", ((args, env) -> {
            if (!(args.get(0) instanceof DNumber)) throw new DevoreCastException(args.get(0).type(), "number");
            return ((DNumber) args.get(0)).round();
        }), 1, false);
        dEnv.addTokenProcedure("println", ((args, env) -> {
            StringBuilder builder = new StringBuilder();
            for (Token t : args) builder.append(t);
            env.io.out.println(builder);
            return DWord.NIL;
        }), 1, true);
        dEnv.addTokenProcedure("print", ((args, env) -> {
            StringBuilder builder = new StringBuilder();
            for (Token t : args) builder.append(t);
            env.io.out.print(builder);
            return DWord.NIL;
        }), 1, true);
        dEnv.addAstProcedure("undef", ((ast, env) -> {
            for (AstNode child : ast.children) {
                if (!(child.symbol instanceof DSymbol)) throw new DevoreCastException(child.symbol.type(), "symbol");
                env.remove(child.symbol.toString());
            }
            return DWord.NIL;
        }), 1, true);
        dEnv.addAstProcedure("def-macro", ((ast, env) -> {
            if (!(ast.get(0).symbol instanceof DSymbol))
                throw new DevoreCastException(ast.get(0).symbol.type(), "symbol");
            List<String> params = new ArrayList<>();
            for (AstNode param : ast.get(0).children) {
                if (!(param.symbol instanceof DSymbol)) throw new DevoreCastException(param.symbol.type(), "symbol");
                params.add(param.symbol.toString());
            }
            List<AstNode> asts = new ArrayList<>();
            for (int i = 1; i < ast.size(); ++i) asts.add(ast.get(i).copy());
            env.addMacro(ast.get(0).symbol.toString(), DMacro.newMacro(params, asts));
            return DWord.NIL;
        }), 2, true);
        dEnv.addAstProcedure("def", ((ast, env) -> {
            if (!(ast.get(0).symbol instanceof DSymbol))
                throw new DevoreCastException(ast.get(0).symbol.type(), "symbol");
            if (ast.get(0).isEmpty() && ast.get(0).type != AstNode.AstType.PROCEDURE) {
                Env newEnv = env.createChild();
                Token result = DWord.NIL;
                for (int i = 1; i < ast.size(); ++i) result = Evaluator.eval(newEnv, ast.get(i).copy());
                env.put(ast.get(0).symbol.toString(), result);
            } else {
                List<String> params = new ArrayList<>();
                for (AstNode param : ast.get(0).children) {
                    if (!(param.symbol instanceof DSymbol)) throw new DevoreCastException(param.symbol.type(), "symbol");
                    params.add(param.symbol.toString());
                }
                List<AstNode> asts = new ArrayList<>();
                for (int i = 1; i < ast.size(); ++i) asts.add(ast.get(i).copy());
                env.addTokenProcedure(ast.get(0).symbol.toString(), ((cArgs, cEnv) -> {
                    Env newEnv = env.createChild();
                    for (int i = 0; i < params.size(); ++i) newEnv.put(params.get(i), cArgs.get(i));
                    Token result = DWord.NIL;
                    for (AstNode astNode : asts) result = Evaluator.eval(newEnv, astNode.copy());
                    return result;
                }), params.size(), false);
            }
            return DWord.NIL;
        }), 2, true);
        dEnv.addAstProcedure("set!", ((ast, env) -> {
            if (!(ast.get(0).symbol instanceof DSymbol))
                throw new DevoreCastException(ast.get(0).symbol.type(), "symbol");
            if (ast.get(0).isEmpty() && ast.get(0).type != AstNode.AstType.PROCEDURE) {
                Env newEnv = env.createChild();
                Token result = DWord.NIL;
                for (int i = 1; i < ast.size(); ++i) result = Evaluator.eval(newEnv, ast.get(i).copy());
                env.set(ast.get(0).symbol.toString(), result);
            } else {
                List<String> params = new ArrayList<>();
                for (AstNode param : ast.get(0).children) {
                    if (!(param.symbol instanceof DSymbol)) throw new DevoreCastException(param.symbol.type(), "symbol");
                    params.add(param.symbol.toString());
                }
                List<AstNode> asts = new ArrayList<>();
                for (int i = 1; i < ast.size(); ++i) asts.add(ast.get(i).copy());
                env.setTokenProcedure(ast.get(0).symbol.toString(), ((cArgs, cEnv) -> {
                    Env newEnv = env.createChild();
                    for (int i = 0; i < params.size(); ++i) newEnv.put(params.get(i), cArgs.get(i));
                    Token result = DWord.NIL;
                    for (AstNode astNode : asts) result = Evaluator.eval(newEnv, astNode.copy());
                    return result;
                }), params.size(), false);
            }
            return DWord.NIL;
        }), 2, true);
        dEnv.addAstProcedure("let", ((ast, env) -> {
            Env newEnv = env.createChild();
            Token result = DWord.NIL;
            for (AstNode node : ast.get(0).children) {
                if ("apply".equals(node.symbol.toString())) {
                    List<String> params = new ArrayList<>();
                    List<AstNode> asts = new ArrayList<>();
                    for (AstNode paramNode : node.get(0).children) {
                        if (!(paramNode.symbol instanceof DSymbol)) throw new DevoreCastException(paramNode.symbol.type(), "symbol");
                        params.add(paramNode.symbol.toString());
                    }
                    for (int i = 1; i < node.size(); ++i) asts.add(node.get(i).copy());
                    if (!(ast.get(0).symbol instanceof DSymbol)) throw new DevoreCastException(ast.get(0).symbol.type(), "symbol");
                    newEnv.addTokenProcedure(ast.get(0).symbol.toString(), ((cArgs, cEnv) -> {
                        Env newInEnv = env.createChild();
                        for (int i = 0; i < params.size(); ++i) newInEnv.put(params.get(i), cArgs.get(i));
                        Token inResult = DWord.NIL;
                        for (AstNode astNode : asts) inResult = Evaluator.eval(newInEnv, astNode.copy());
                        return inResult;
                    }), params.size(), false);
                } else {
                    if (!(node.symbol instanceof DSymbol)) throw new DevoreCastException(node.symbol.type(), "symbol");
                    Token value = DWord.NIL;
                    for (AstNode e : node.children) value = Evaluator.eval(env, e.copy());
                    newEnv.put(node.symbol.toString(), value);
                }
            }
            for (int i = 1; i < ast.size(); ++i) result = Evaluator.eval(newEnv, ast.get(i).copy());
            return result;
        }), 2, true);
        dEnv.addAstProcedure("let*", ((ast, env) -> {
            Env newEnv = env.createChild();
            Token result = DWord.NIL;
            for (AstNode node : ast.get(0).children) {
                if ("apply".equals(node.symbol.toString())) {
                    List<String> params = new ArrayList<>();
                    List<AstNode> asts = new ArrayList<>();
                    for (AstNode paramNode : node.get(0).children) {
                        if (!(paramNode.symbol instanceof DSymbol)) throw new DevoreCastException(paramNode.symbol.type(), "symbol");
                        params.add(paramNode.symbol.toString());
                    }
                    for (int i = 1; i < node.size(); ++i) asts.add(node.get(i).copy());
                    if (!(ast.get(0).symbol instanceof DSymbol)) throw new DevoreCastException(ast.get(0).symbol.type(), "symbol");
                    newEnv.addTokenProcedure(ast.get(0).symbol.toString(), ((cArgs, cEnv) -> {
                        Env newInEnv = newEnv.createChild();
                        for (int i = 0; i < params.size(); ++i) newInEnv.put(params.get(i), cArgs.get(i));
                        Token inResult = DWord.NIL;
                        for (AstNode astNode : asts) inResult = Evaluator.eval(newInEnv, astNode.copy());
                        return inResult;
                    }), params.size(), false);
                } else {
                    if (!(node.symbol instanceof DSymbol)) throw new DevoreCastException(node.symbol.type(), "symbol");
                    Token value = DWord.NIL;
                    for (AstNode e : node.children) value = Evaluator.eval(newEnv, e.copy());
                    newEnv.put(node.symbol.toString(), value);
                }
            }
            for (int i = 1; i < ast.size(); ++i) result = Evaluator.eval(newEnv, ast.get(i).copy());
            return result;
        }), 2, true);
        dEnv.addAstProcedure("lambda", ((ast, env) -> {
            List<String> params = new ArrayList<>();
            if (ast.get(0).isNotNil()) {
                if (!(ast.get(0).symbol instanceof DSymbol)) throw new DevoreCastException(ast.get(0).symbol.type(), "symbol");
                params.add(ast.get(0).symbol.toString());
                for (AstNode param : ast.get(0).children) {
                    if (!(param.symbol instanceof DSymbol)) throw new DevoreCastException(param.symbol.type(), "symbol");
                    params.add(param.symbol.toString());
                }
            }
            List<AstNode> asts = new ArrayList<>();
            for (int i = 1; i < ast.size(); ++i) asts.add(ast.get(i).copy());
            BiFunction<AstNode, Env, Token> df = (inAst, inEnv) -> {
                List<Token> args = new ArrayList<>();
                for (int i = 0; i < inAst.size(); ++i) {
                    inAst.get(i).symbol = Evaluator.eval(inEnv, inAst.get(i).copy());
                    args.add(inAst.get(i).symbol);
                }
                Env newInEnv = env.createChild();
                for (int i = 0; i < params.size(); ++i) newInEnv.put(params.get(i), args.get(i));
                Token inResult = DWord.NIL;
                for (AstNode astNode : asts) inResult = Evaluator.eval(newInEnv, astNode.copy());
                return inResult;
            };
            return DProcedure.newProcedure(df, params.size(), false);
        }), 2, true);
        dEnv.addTokenProcedure("apply", ((args, env) -> {
            if (!(args.get(0) instanceof DProcedure)) throw new DevoreCastException(args.get(0).type(), "procedure");
            List<Token> params = new ArrayList<>();
            for (int i = 1; i < args.size(); ++i) params.add(args.get(i));
            AstNode asts = AstNode.emptyAst.copy();
            for (Token arg : params) asts.add(new AstNode(arg));
            return ((DProcedure) args.get(0)).call(asts, env);
        }), 1, true);
        dEnv.addTokenProcedure("act", ((args, env) -> {
            if (!(args.get(0) instanceof DProcedure)) throw new DevoreCastException(args.get(0).type(), "procedure");
            if (!(args.get(1) instanceof DList)) throw new DevoreCastException(args.get(0).type(), "list");
            List<Token> params = ((DList) args.get(1)).toList();
            AstNode asts = AstNode.emptyAst.copy();
            for (Token arg : params) asts.add(new AstNode(arg));
            return ((DProcedure) args.get(0)).call(asts, env);
        }), 2, false);
        dEnv.addTokenProcedure(">", ((args, env) ->
                DBool.valueOf(args.get(0).compareTo(args.get(1)) > 0)), 2, false);
        dEnv.addTokenProcedure("<", ((args, env) ->
                DBool.valueOf(args.get(0).compareTo(args.get(1)) < 0)), 2, false);
        dEnv.addTokenProcedure("=", ((args, env) ->
                DBool.valueOf(args.get(0).equals(args.get(1)))), 2, false);
        dEnv.addTokenProcedure("/=", ((args, env) ->
                DBool.valueOf(args.get(0).compareTo(args.get(1)) != 0)), 2, false);
        dEnv.addTokenProcedure(">=", ((args, env) ->
                DBool.valueOf(args.get(0).compareTo(args.get(1)) >= 0)), 2, false);
        dEnv.addTokenProcedure("<=", ((args, env) ->
                DBool.valueOf(args.get(0).compareTo(args.get(1)) <= 0)), 2, false);
        dEnv.addAstProcedure("unless", (ast, env) -> {
            Token result = DWord.NIL;
            Token condition = Evaluator.eval(env, ast.get(0).copy());
            if (!(condition instanceof DBool)) throw new DevoreCastException(condition.type(), "bool");
            if (!((DBool) condition).bool)
                for (int i = 1; i < ast.size(); ++i) result = Evaluator.eval(env, ast.get(i).copy());
            return result;
        }, 2, true);
        dEnv.addAstProcedure("when", (ast, env) -> {
            Token result = DWord.NIL;
            Token condition = Evaluator.eval(env, ast.get(0).copy());
            if (!(condition instanceof DBool)) throw new DevoreCastException(condition.type(), "bool");
            if (((DBool) condition).bool) {
                for (int i = 1; i < ast.size(); ++i) result = Evaluator.eval(env, ast.get(i).copy());
            }
            return result;
        }, 2, true);
        dEnv.addAstProcedure("if", (ast, env) -> {
            Token result = DWord.NIL;
            Token condition = Evaluator.eval(env, ast.get(0).copy());
            if (!(condition instanceof DBool)) throw new DevoreCastException(condition.type(), "bool");
            if (((DBool) condition).bool) result = Evaluator.eval(env, ast.get(1).copy());
            return result;
        }, 2, false);
        dEnv.addAstProcedure("if", (ast, env) -> {
            Token result;
            Token condition = Evaluator.eval(env, ast.get(0).copy());
            if (!(condition instanceof DBool)) throw new DevoreCastException(condition.type(), "bool");
            if (((DBool) condition).bool) result = Evaluator.eval(env, ast.get(1).copy());
            else result = Evaluator.eval(env, ast.get(2).copy());
            return result;
        }, 3, false);
        dEnv.addAstProcedure("cond", (ast, env) -> {
            Token result = DWord.NIL;
            for (AstNode node : ast.children) {
                if (node.symbol instanceof DSymbol && "else".equals(node.symbol.toString())) {
                    result = Evaluator.eval(env, node.get(0).copy());
                    break;
                } else {
                    Token condition = Evaluator.eval(env, node.get(0).copy());
                    if (!(condition instanceof DBool)) throw new DevoreCastException(condition.type(), "bool");
                    if (((DBool) condition).bool) {
                        Token r = DWord.NIL;
                        for (int i = 1; i < node.size(); ++i) r = Evaluator.eval(env, node.get(i).copy());
                        result = r;
                        break;
                    }
                }
            }
            return result;
        }, 2, true);
        dEnv.addAstProcedure("begin", (ast, env) -> {
            Token result = DWord.NIL;
            for (AstNode node : ast.children) result = Evaluator.eval(env, node.copy());
            return result;
        }, 1, true);
        dEnv.addAstProcedure("while", (ast, env) -> {
            Token result = DWord.NIL;
            Token condition = Evaluator.eval(env, ast.get(0).copy());
            if (!(condition instanceof DBool)) throw new DevoreCastException(condition.type(), "bool");
            while (((DBool) condition).bool) {
                for (int i = 1; i < ast.size(); ++i) result = Evaluator.eval(env, ast.get(i).copy());
                condition = Evaluator.eval(env, ast.get(0).copy());
                if (!(condition instanceof DBool)) throw new DevoreCastException(condition.type(), "bool");
            }
            return result;
        }, 2, true);
        dEnv.addTokenProcedure("read-line", ((args, env) ->
                DString.valueOf(new Scanner(env.io.in).nextLine())), 0, false);
        dEnv.addTokenProcedure("read-int", ((args, env) ->
                DNumber.valueOf(new Scanner(env.io.in).nextBigInteger())), 0, false);
        dEnv.addTokenProcedure("read-float", ((args, env) ->
                DNumber.valueOf(new Scanner(env.io.in).nextBigDecimal())), 0, false);
        dEnv.addTokenProcedure("read-bool", ((args, env) ->
                DBool.valueOf(new Scanner(env.io.in).nextBoolean())), 0, false);
        dEnv.addTokenProcedure("read", ((args, env) ->
                DString.valueOf(new Scanner(env.io.in).next())), 0, false);
        dEnv.addTokenProcedure("newline", ((args, env) -> {
            env.io.out.println();
            return DWord.NIL;
        }), 0, false);
        dEnv.addTokenProcedure("and", ((args, env) -> {
            for (Token arg : args) {
                if (!(arg instanceof DBool)) throw new DevoreCastException(arg.type(), "bool");
                if (!((DBool) arg).bool) return DBool.FALSE;
            }
            return DBool.TRUE;
        }), 1, true);
        dEnv.addTokenProcedure("or", ((args, env) -> {
            for (Token arg : args) {
                if (!(arg instanceof DBool)) throw new DevoreCastException(arg.type(), "bool");
                if (((DBool) arg).bool) return DBool.TRUE;
            }
            return DBool.FALSE;
        }), 1, true);
        dEnv.addTokenProcedure("not", ((args, env) -> {
            if (!(args.get(0) instanceof DBool)) throw new DevoreCastException(args.get(0).type(), "bool");
            if (((DBool) args.get(0)).bool) return DBool.FALSE;
            return DBool.TRUE;
        }), 1, false);
        dEnv.addTokenProcedure("random", ((args, env) -> {
            if (!(args.get(0) instanceof DInt)) throw new DevoreCastException(args.get(0).type(), "int");
            return DNumber.valueOf(random(BigInteger.ZERO, ((DInt) args.get(0)).toBigInteger().subtract(BigInteger.ONE)));
        }), 1, false);
        dEnv.addTokenProcedure("random", ((args, env) -> {
            if (!(args.get(0) instanceof DInt)) throw new DevoreCastException(args.get(0).type(), "int");
            if (!(args.get(1) instanceof DInt)) throw new DevoreCastException(args.get(1).type(), "int");
            return DNumber.valueOf(random(((DInt) args.get(0)).toBigInteger(),
                    ((DInt) args.get(1)).toBigInteger().subtract(BigInteger.ONE)));
        }), 2, false);
        dEnv.addTokenProcedure("list", ((args, env) -> DList.valueOf(new ArrayList<>(args))), 0, true);
        dEnv.addTokenProcedure("list-contains", ((args, env) -> {
            if (!(args.get(0) instanceof DList)) throw new DevoreCastException(args.get(0).type(), "list");
            return DBool.valueOf(((DList) args.get(0)).contains(args.get(1)));
        }), 2, false);
        dEnv.addTokenProcedure("list-index", ((args, env) -> {
            if (!(args.get(0) instanceof DList)) throw new DevoreCastException(args.get(0).type(), "list");
            return DNumber.valueOf(((DList) args.get(0)).indexOf(args.get(1)));
        }), 2, false);
        dEnv.addTokenProcedure("list-index-last", ((args, env) -> {
            if (!(args.get(0) instanceof DList)) throw new DevoreCastException(args.get(0).type(), "list");
            return DNumber.valueOf(((DList) args.get(0)).lastIndexOf(args.get(1)));
        }), 2, false);
        dEnv.addTokenProcedure("list-get", ((args, env) -> {
            if (!(args.get(0) instanceof DList)) throw new DevoreCastException(args.get(0).type(), "list");
            if (!(args.get(1) instanceof DInt)) throw new DevoreCastException(args.get(1).type(), "int");
            DList list = (DList) args.get(0);
            if (list.size() == 0) return DWord.NIL;
            return list.get(((DInt) args.get(1)).toBigInteger().intValue());
        }), 2, false);
        dEnv.addTokenProcedure("list-set", ((args, env) -> {
            if (!(args.get(0) instanceof DList)) throw new DevoreCastException(args.get(0).type(), "list");
            if (!(args.get(1) instanceof DInt)) throw new DevoreCastException(args.get(1).type(), "int");
            DList list = (DList) args.get(0);
            if (list.size() == 0) return DWord.NIL;
            return list.set(((DInt) args.get(1)).toBigInteger().intValue(), args.get(2), false);
        }), 3, false);
        dEnv.addTokenProcedure("list-remove", ((args, env) -> {
            if (!(args.get(0) instanceof DList)) throw new DevoreCastException(args.get(0).type(), "list");
            if (!(args.get(1) instanceof DInt)) throw new DevoreCastException(args.get(1).type(), "int");
            DList list = (DList) args.get(0);
            if (list.size() == 0) return DWord.NIL;
            return list.remove(((DInt) args.get(1)).toBigInteger().intValue(), false);
        }), 2, false);
        dEnv.addTokenProcedure("list-add", ((args, env) -> {
            if (!(args.get(0) instanceof DList)) throw new DevoreCastException(args.get(0).type(), "list");
            return ((DList) args.get(0)).add(args.get(1), false);
        }), 2, false);
        dEnv.addTokenProcedure("list-add", ((args, env) -> {
            if (!(args.get(0) instanceof DList)) throw new DevoreCastException(args.get(0).type(), "list");
            if (!(args.get(1) instanceof DInt)) throw new DevoreCastException(args.get(0).type(), "int");
            DList list = (DList) args.get(0);
            return list.add(((DInt) args.get(1)).toBigInteger().intValue(), args.get(2), false);
        }), 3, false);
        dEnv.addTokenProcedure("list-set!", ((args, env) -> {
            if (!(args.get(0) instanceof DList)) throw new DevoreCastException(args.get(0).type(), "list");
            if (!(args.get(1) instanceof DInt)) throw new DevoreCastException(args.get(1).type(), "int");
            DList list = (DList) args.get(0);
            if (list.size() == 0) return DWord.NIL;
            return list.set(((DInt) args.get(1)).toBigInteger().intValue(), args.get(2), true);
        }), 3, false);
        dEnv.addTokenProcedure("list-remove!", ((args, env) -> {
            if (!(args.get(0) instanceof DList)) throw new DevoreCastException(args.get(0).type(), "list");
            if (!(args.get(1) instanceof DInt)) throw new DevoreCastException(args.get(1).type(), "int");
            DList list = (DList) args.get(0);
            if (list.size() == 0) return DWord.NIL;
            return list.remove(((DInt) args.get(1)).toBigInteger().intValue(), true);
        }), 2, false);
        dEnv.addTokenProcedure("list-add!", ((args, env) -> {
            if (!(args.get(0) instanceof DList)) throw new DevoreCastException(args.get(0).type(), "list");
            return ((DList) args.get(0)).add(args.get(1), true);
        }), 2, false);
        dEnv.addTokenProcedure("list-add!", ((args, env) -> {
            if (!(args.get(0) instanceof DList)) throw new DevoreCastException(args.get(0).type(), "list");
            if (!(args.get(1) instanceof DInt)) throw new DevoreCastException(args.get(0).type(), "int");
            return ((DList) args.get(0)).add(((DInt) args.get(1)).toBigInteger().intValue(), args.get(2), true);
        }), 3, false);
        dEnv.addTokenProcedure("head", ((args, env) -> {
            if (!(args.get(0) instanceof DList)) throw new DevoreCastException(args.get(0).type(), "list");
            DList list = (DList) args.get(0);
            if (list.size() == 0) return DWord.NIL;
            return list.get(0);
        }), 1, false);
        dEnv.addTokenProcedure("last", ((args, env) -> {
            if (!(args.get(0) instanceof DList)) throw new DevoreCastException(args.get(0).type(), "list");
            DList list = (DList) args.get(0);
            if (list.size() == 0) return DWord.NIL;
            return list.get(list.size() - 1);
        }), 1, false);
        dEnv.addTokenProcedure("tail", ((args, env) -> {
            if (!(args.get(0) instanceof DList)) throw new DevoreCastException(args.get(0).type(), "list");
            DList list = (DList) args.get(0);
            return list.subList(1, list.size());
        }), 1, false);
        dEnv.addTokenProcedure("init", ((args, env) -> {
            if (!(args.get(0) instanceof DList)) throw new DevoreCastException(args.get(0).type(), "list");
            DList list = (DList) args.get(0);
            return list.subList(0, list.size() - 1);
        }), 1, false);
        dEnv.addTokenProcedure("length", ((args, env) -> {
            if (!(args.get(0) instanceof DList)) return DNumber.valueOf(args.get(0).toString().length());
            return DNumber.valueOf(((DList) args.get(0)).size());
        }), 1, false);
        dEnv.addTokenProcedure("list-sub", ((args, env) -> {
            if (!(args.get(0) instanceof DList)) throw new DevoreCastException(args.get(0).type(), "list");
            if (!(args.get(1) instanceof DInt)) throw new DevoreCastException(args.get(0).type(), "int");
            if (!(args.get(2) instanceof DInt)) throw new DevoreCastException(args.get(0).type(), "int");
            return ((DList) args.get(0)).subList(((DInt) args.get(1)).toBigInteger().intValue(),
                    ((DInt) args.get(2)).toBigInteger().intValue());
        }), 3, false);
        dEnv.addTokenProcedure("reverse", ((args, env) -> {
            if (!(args.get(0) instanceof DList)) throw new DevoreCastException(args.get(0).type(), "list");
            DList list = (DList) args.get(0);
            List<Token> temp = new ArrayList<>();
            for (int i = list.size() - 1; i >= 0; --i) temp.add(list.get(i));
            return DList.valueOf(new ArrayList<>(temp));
        }), 1, false);
        dEnv.addTokenProcedure("reverse!", ((args, env) -> {
            if (!(args.get(0) instanceof DList)) throw new DevoreCastException(args.get(0).type(), "list");
            DList list = (DList) args.get(0);
            List<Token> temp = new ArrayList<>();
            for (int i = list.size() - 1; i >= 0; --i) temp.add(list.get(i));
            list.clear();
            for (Token t : temp) list.add(t, true);
            return list;
        }), 1, false);
        dEnv.addTokenProcedure("sort", ((args, env) -> {
            if (!(args.get(0) instanceof DList)) throw new DevoreCastException(args.get(0).type(), "list");
            return ((DList) args.get(0)).sort(false);
        }), 1, false);
        dEnv.addTokenProcedure("sort!", ((args, env) -> {
            if (!(args.get(0) instanceof DList)) throw new DevoreCastException(args.get(0).type(), "list");
            return ((DList) args.get(0)).sort(true);
        }), 1, true);
        dEnv.addTokenProcedure("++", ((args, env) -> {
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
                    if (arg instanceof DList) list.addAll(((DList) arg).toList());
                    else list.add(arg);
                }
                result = DList.valueOf(list);
            } else {
                StringBuilder builder = new StringBuilder();
                for (Token arg : args) builder.append(arg.toString());
                result = DString.valueOf(builder.toString());
            }
            return result;
        }), 1, true);
        dEnv.addTokenProcedure("map", ((args, env) -> {
            if (!(args.get(0) instanceof DProcedure))
                throw new DevoreCastException(args.get(0).type(), "procedure");
            if (!(args.get(1) instanceof DList)) throw new DevoreCastException(args.get(1).type(), "list");
            List<Token> result = new ArrayList<>();
            List<Token> tokens = ((DList) args.get(1)).toList();
            for (int i = 0; i < tokens.size(); ++i) {
                List<Token> params = new ArrayList<>();
                params.add(tokens.get(i));
                for (int j = 1; j < args.size() - 1; ++j) {
                    if (!(args.get(j + 1) instanceof DList)) throw new DevoreCastException(args.get(j + 1).type(), "list");
                    params.add(((DList) args.get(j + 1)).get(i));
                }
                result.add(((DProcedure) args.get(0)).call(params.toArray(new Token[0]), env.createChild()));
            }
            return DList.valueOf(result);
        }), 2, true);
        dEnv.addTokenProcedure("for-each", ((args, env) -> {
            if (!(args.get(0) instanceof DProcedure)) throw new DevoreCastException(args.get(0).type(), "procedure");
            if (!(args.get(1) instanceof DList)) throw new DevoreCastException(args.get(1).type(), "list");
            List<Token> tokens = ((DList) args.get(1)).toList();
            for (int i = 0; i < tokens.size(); ++i) {
                List<Token> params = new ArrayList<>();
                params.add(tokens.get(i));
                for (int j = 1; j < args.size() - 1; ++j) {
                    if (!(args.get(j + 1) instanceof DList)) throw new DevoreCastException(args.get(j + 1).type(), "list");
                    params.add(((DList) args.get(j + 1)).get(i));
                }
                ((DProcedure) args.get(0)).call(params.toArray(new Token[0]), env.createChild());
            }
            return DWord.NIL;
        }), 2, true);
        dEnv.addTokenProcedure("foldr", ((args, env) -> {
            if (!(args.get(0) instanceof DProcedure)) throw new DevoreCastException(args.get(0).type(), "procedure");
            if (!(args.get(2) instanceof DList)) throw new DevoreCastException(args.get(2).type(), "list");
            Token result = args.get(1);
            List<Token> tokens = ((DList) args.get(2)).toList();
            for (int i = tokens.size() - 1; i >= 0; --i)
                result = ((DProcedure) args.get(0)).call(new Token[]{tokens.get(i), result}, env.createChild());
            return result;
        }), 3, false);
        dEnv.addTokenProcedure("foldl", ((args, env) -> {
            if (!(args.get(0) instanceof DProcedure)) throw new DevoreCastException(args.get(0).type(), "procedure");
            if (!(args.get(2) instanceof DList)) throw new DevoreCastException(args.get(2).type(), "list");
            Token result = args.get(1);
            List<Token> tokens = ((DList) args.get(2)).toList();
            for (int i = tokens.size() - 1; i >= 0; --i)
                result = ((DProcedure) args.get(0)).call(new Token[]{result, tokens.get(i)}, env.createChild());
            return result;
        }), 3, false);
        dEnv.addTokenProcedure("filter", ((args, env) -> {
            if (!(args.get(0) instanceof DProcedure)) throw new DevoreCastException(args.get(0).type(), "procedure");
            if (!(args.get(1) instanceof DList)) throw new DevoreCastException(args.get(1).type(), "list");
            List<Token> result = new ArrayList<>();
            List<Token> tokens = ((DList) args.get(1)).toList();
            for (Token token : tokens) {
                AstNode asts = AstNode.emptyAst.copy();
                asts.add(new AstNode(token));
                Token condition = ((DProcedure) args.get(0)).call(asts, env.createChild());
                if (!(condition instanceof DBool)) throw new DevoreCastException(condition.type(), "list");
                if (((DBool) condition).bool) result.add(token);
            }
            return DList.valueOf(result);
        }), 2, false);
        dEnv.addTokenProcedure("range", ((args, env) -> {
            if (!(args.get(0) instanceof DNumber)) throw new DevoreCastException(args.get(0).type(), "number");
            return DList.valueOf(range(BigDecimal.ZERO,
                    ((DNumber) args.get(0)).toBigDecimal().subtract(BigDecimal.ONE), BigDecimal.ONE));
        }), 1, false);
        dEnv.addTokenProcedure("range", ((args, env) -> {
            if (!(args.get(0) instanceof DNumber)) throw new DevoreCastException(args.get(0).type(), "number");
            if (!(args.get(1) instanceof DNumber)) throw new DevoreCastException(args.get(1).type(), "number");
            return DList.valueOf(range(((DNumber) args.get(0)).toBigDecimal(),
                    ((DNumber) args.get(1)).toBigDecimal().subtract(BigDecimal.ONE), BigDecimal.ONE));
        }), 2, false);
        dEnv.addTokenProcedure("range", ((args, env) -> {
            if (!(args.get(0) instanceof DNumber)) throw new DevoreCastException(args.get(0).type(), "number");
            if (!(args.get(1) instanceof DNumber)) throw new DevoreCastException(args.get(1).type(), "number");
            if (!(args.get(2) instanceof DNumber)) throw new DevoreCastException(args.get(2).type(), "number");
            return DList.valueOf(range(((DNumber) args.get(0)).toBigDecimal(),
                    ((DNumber) args.get(1)).toBigDecimal().subtract(((DNumber) args.get(2)).toBigDecimal()),
                    ((DNumber) args.get(2)).toBigDecimal()));
        }), 3, false);
        dEnv.addTokenProcedure("string->symbol", ((args, env) -> {
            if (!(args.get(0) instanceof DString)) throw new DevoreCastException(args.get(0).type(), "string");
            return DSymbol.valueOf(args.get(0).toString());
        }), 1, false);
        dEnv.addTokenProcedure("string->number", ((args, env) -> {
            if (!(args.get(0) instanceof DString)) throw new DevoreCastException(args.get(0).type(), "string");
            return DNumber.valueOf(new BigDecimal(args.get(0).toString()));
        }), 1, false);
        dEnv.addTokenProcedure("string->bool", ((args, env) -> {
            if (!(args.get(0) instanceof DString)) throw new DevoreCastException(args.get(0).type(), "string");
            return "true".equals(args.get(0).toString()) ? DBool.TRUE : DBool.FALSE;
        }), 1, false);
        dEnv.addTokenProcedure("->string", ((args, env) -> DString.valueOf(args.get(0).toString())), 1, false);
        dEnv.addTokenProcedure("string->list", ((args, env) -> {
            if (!(args.get(0) instanceof DString)) throw new DevoreCastException(args.get(0).type(), "string");
            char[] chars = args.get(0).toString().toCharArray();
            List<Token> tokens = new ArrayList<>();
            for (char c : chars) tokens.add(DString.valueOf(String.valueOf(c)));
            return DList.valueOf(tokens);
        }), 1, false);
        dEnv.addTokenProcedure("char->unicode", ((args, env) -> {
            if (!(args.get(0) instanceof DString)) throw new DevoreCastException(args.get(0).type(), "string");
            return DNumber.valueOf((int) args.get(0).toString().charAt(0));
        }), 1, false);
        dEnv.addTokenProcedure("unicode->char", ((args, env) -> {
            if (!(args.get(0) instanceof DInt)) throw new DevoreCastException(args.get(0).type(), "int");
            return DString.valueOf(String.valueOf((char) ((DInt) args.get(0)).toBigInteger().intValue()));
        }), 1, false);
        dEnv.addTokenProcedure("exit", ((args, env) -> {
            if (!(args.get(0) instanceof DInt)) throw new DevoreCastException(args.get(0).type(), "int");
            System.exit(((DInt) args.get(0)).toBigInteger().intValue());
            return DWord.NIL;
        }), 1, false);
        dEnv.addTokenProcedure("sleep", ((args, env) -> {
            if (!(args.get(0) instanceof DInt)) throw new DevoreCastException(args.get(0).type(), "int");
            try {
                Thread.sleep(((DInt) args.get(0)).toBigInteger().longValue());
            } catch (InterruptedException e) {
                return DWord.NIL;
            }
            return DWord.NIL;
        }), 1, false);
        dEnv.addTokenProcedure("type", ((args, env) -> DString.valueOf(args.get(0).type())), 1, false);
        dEnv.addTokenProcedure("time", ((args, env) -> DNumber.valueOf(System.currentTimeMillis())), 0, false);
        dEnv.addTokenProcedure("table", ((args, env) -> DTable.valueOf(new HashMap<>())), 0, false);
        dEnv.addTokenProcedure("table-get", ((args, env) -> {
            if (!(args.get(0) instanceof DTable)) throw new DevoreCastException(args.get(0).type(), "table");
            return ((DTable) args.get(0)).get(args.get(1));
        }), 2, false);
        dEnv.addTokenProcedure("table-contains-key", ((args, env) -> {
            if (!(args.get(0) instanceof DTable)) throw new DevoreCastException(args.get(0).type(), "table");
            return ((DTable) args.get(0)).containsKey(args.get(1));
        }), 2, false);
        dEnv.addTokenProcedure("table-contains-value", ((args, env) -> {
            if (!(args.get(0) instanceof DTable)) throw new DevoreCastException(args.get(0).type(), "table");
            return ((DTable) args.get(0)).containsValue(args.get(1));
        }), 2, false);
        dEnv.addTokenProcedure("table-size", ((args, env) -> {
            if (!(args.get(0) instanceof DTable)) throw new DevoreCastException(args.get(0).type(), "table");
            return DNumber.valueOf(((DTable) args.get(0)).size());
        }), 1, false);
        dEnv.addTokenProcedure("table-put", ((args, env) -> {
            if (!(args.get(0) instanceof DTable)) throw new DevoreCastException(args.get(0).type(), "table");
            return ((DTable) args.get(0)).put(args.get(1), args.get(2), false);
        }), 3, false);
        dEnv.addTokenProcedure("table-put!", ((args, env) -> {
            if (!(args.get(0) instanceof DTable)) throw new DevoreCastException(args.get(0).type(), "table");
            return ((DTable) args.get(0)).put(args.get(1), args.get(2), true);
        }), 3, false);
        dEnv.addTokenProcedure("table-remove", ((args, env) -> {
            if (!(args.get(0) instanceof DTable)) throw new DevoreCastException(args.get(0).type(), "table");
            return ((DTable) args.get(0)).remove(args.get(1), false);
        }), 2, false);
        dEnv.addTokenProcedure("table-remove!", ((args, env) -> {
            if (!(args.get(0) instanceof DTable)) throw new DevoreCastException(args.get(0).type(), "table");
            return ((DTable) args.get(0)).remove(args.get(1), true);
        }), 2, false);
        dEnv.addTokenProcedure("table-keys", ((args, env) -> {
            if (!(args.get(0) instanceof DTable)) throw new DevoreCastException(args.get(0).type(), "table");
            return DList.valueOf(new ArrayList<>(((DTable) args.get(0)).keys()));
        }), 1, false);
        dEnv.addTokenProcedure("max", ((args, env) -> {
            Token t = args.get(0);
            for (int i = 1; i < args.size(); ++i)
                if (args.get(i).compareTo(t) > 0) t = args.get(i);
            return t;
        }), 1, true);
        dEnv.addTokenProcedure("min", ((args, env) -> {
            Token t = args.get(0);
            for (int i = 1; i < args.size(); ++i)
                if (args.get(i).compareTo(t) < 0) t = args.get(i);
            return t;
        }), 1, true);
        dEnv.addTokenProcedure("bool?", ((args, env) ->
                DBool.valueOf(args.get(0) instanceof DBool)), 1, false);
        dEnv.addTokenProcedure("float?", ((args, env) ->
                DBool.valueOf(args.get(0) instanceof DFloat)), 1, false);
        dEnv.addTokenProcedure("int?", ((args, env) ->
                DBool.valueOf(args.get(0) instanceof DInt)), 1, false);
        dEnv.addTokenProcedure("list?", ((args, env) ->
                DBool.valueOf(args.get(0) instanceof DList)), 1, false);
        dEnv.addTokenProcedure("macro?", ((args, env) ->
                DBool.valueOf(args.get(0) instanceof DMacro)), 1, false);
        dEnv.addTokenProcedure("number?", ((args, env) ->
                DBool.valueOf(args.get(0) instanceof DNumber)), 1, false);
        dEnv.addTokenProcedure("procedure?", ((args, env) ->
                DBool.valueOf(args.get(0) instanceof DProcedure)), 1, false);
        dEnv.addTokenProcedure("string?", ((args, env) ->
                DBool.valueOf(args.get(0) instanceof DString)), 1, false);
        dEnv.addTokenProcedure("symbol?", ((args, env) ->
                DBool.valueOf(args.get(0) instanceof DSymbol)), 1, false);
        dEnv.addTokenProcedure("table?", ((args, env) ->
                DBool.valueOf(args.get(0) instanceof DTable)), 1, false);
        dEnv.addTokenProcedure("word?", ((args, env) ->
                DBool.valueOf(args.get(0) instanceof DWord)), 1, false);
        dEnv.addTokenProcedure("nil?", ((args, env) ->
                DBool.valueOf(args.get(0).equals(DWord.NIL))), 1, false);
        dEnv.addTokenProcedure("zero?", ((args, env) ->
                DBool.valueOf(args.get(0).equals(DNumber.valueOf(0)))), 1, false);
    }

    private static BigInteger random(BigInteger start, BigInteger end) {
        Random rand = new Random();
        BigInteger range = end.subtract(start).add(BigInteger.ONE);
        BigInteger randomValue;
        do {
            randomValue = new BigInteger(range.bitLength(), rand);
        } while (randomValue.compareTo(range) >= 0);
        return randomValue.add(start);
    }

    private static List<Token> range(BigDecimal start, BigDecimal end, BigDecimal step) {
        if (step.compareTo(BigDecimal.ZERO) == 0) throw new DevoreRuntimeException("步长不能为零.");
        List<Token> list = new ArrayList<>();
        if (step.compareTo(BigDecimal.ZERO) > 0)
            for (BigDecimal current = start; current.compareTo(end) <= 0; current = current.add(step))
                list.add(DNumber.valueOf(current));
        else
            for (BigDecimal current = start; current.compareTo(end) >= 0; current = current.add(step))
                list.add(DNumber.valueOf(current));
        return list;
    }
}
