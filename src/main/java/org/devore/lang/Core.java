package org.devore.lang;

import org.devore.exception.DevoreCastException;
import org.devore.exception.DevoreRuntimeException;
import org.devore.lang.token.*;
import org.devore.parser.AstNode;

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
            if (!(args.getFirst() instanceof DNumber number))
                throw new DevoreCastException(args.getFirst().type(), "number");
            for (int i = 1; i < args.size(); ++i) {
                if (!(args.get(i) instanceof DNumber)) throw new DevoreCastException(args.get(i).type(), "number");
                number = number.add((DNumber) args.get(i));
            }
            return number;
        }), 1, true);
        dEnv.addTokenProcedure("-", ((args, env) -> {
            if (!(args.getFirst() instanceof DNumber number))
                throw new DevoreCastException(args.getFirst().type(), "number");
            if (args.size() == 1) return DNumber.valueOf(0).sub(number);
            for (int i = 1; i < args.size(); ++i) {
                if (!(args.get(i) instanceof DNumber)) throw new DevoreCastException(args.get(i).type(), "number");
                number = number.sub((DNumber) args.get(i));
            }
            return number;
        }), 1, true);
        dEnv.addTokenProcedure("*", ((args, env) -> {
            if (!(args.getFirst() instanceof DNumber number))
                throw new DevoreCastException(args.getFirst().type(), "number");
            for (int i = 1; i < args.size(); ++i) {
                if (!(args.get(i) instanceof DNumber)) throw new DevoreCastException(args.get(i).type(), "number");
                number = number.mul((DNumber) args.get(i));
            }
            return number;
        }), 1, true);
        dEnv.addTokenProcedure("/", ((args, env) -> {
            if (!(args.getFirst() instanceof DNumber number))
                throw new DevoreCastException(args.getFirst().type(), "number");
            for (int i = 1; i < args.size(); ++i) {
                if (!(args.get(i) instanceof DNumber)) throw new DevoreCastException(args.get(i).type(), "number");
                number = number.div((DNumber) args.get(i));
            }
            return number;
        }), 1, true);
        dEnv.addTokenProcedure("pow", ((args, env) -> {
            if (!(args.getFirst() instanceof DNumber)) throw new DevoreCastException(args.getFirst().type(), "number");
            if (!(args.get(1) instanceof DNumber)) throw new DevoreCastException(args.get(1).type(), "number");
            return ((DNumber) args.getFirst()).pow(((DNumber) args.get(1)));
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
            if (!(args.getFirst() instanceof DInt)) throw new DevoreCastException(args.getFirst().type(), "int");
            if (!(args.get(1) instanceof DInt)) throw new DevoreCastException(args.get(1).type(), "int");
            return ((DInt) args.getFirst()).mod(((DInt) args.get(1)));
        }), 2, false);
        dEnv.addTokenProcedure("abs", ((args, env) -> {
            if (!(args.getFirst() instanceof DNumber)) throw new DevoreCastException(args.getFirst().type(), "number");
            return ((DNumber) args.getFirst()).abs();
        }), 1, false);
        dEnv.addTokenProcedure("sqrt", ((args, env) -> {
            if (!(args.getFirst() instanceof DNumber)) throw new DevoreCastException(args.getFirst().type(), "number");
            return ((DNumber) args.getFirst()).sqrt();
        }), 1, false);
        dEnv.addTokenProcedure("sin", ((args, env) -> {
            if (!(args.getFirst() instanceof DNumber)) throw new DevoreCastException(args.getFirst().type(), "number");
            return ((DNumber) args.getFirst()).sin();
        }), 1, false);
        dEnv.addTokenProcedure("cos", ((args, env) -> {
            if (!(args.getFirst() instanceof DNumber)) throw new DevoreCastException(args.getFirst().type(), "number");
            return ((DNumber) args.getFirst()).cos();
        }), 1, false);
        dEnv.addTokenProcedure("tan", ((args, env) -> {
            if (!(args.getFirst() instanceof DNumber)) throw new DevoreCastException(args.getFirst().type(), "number");
            return ((DNumber) args.getFirst()).tan();
        }), 1, false);
        dEnv.addTokenProcedure("arctan", ((args, env) -> {
            if (!(args.getFirst() instanceof DNumber)) throw new DevoreCastException(args.getFirst().type(), "number");
            return ((DNumber) args.getFirst()).arctan();
        }), 1, false);
        dEnv.addTokenProcedure("arcsin", ((args, env) -> {
            if (!(args.getFirst() instanceof DNumber)) throw new DevoreCastException(args.getFirst().type(), "number");
            return ((DNumber) args.getFirst()).arcsin();
        }), 1, false);
        dEnv.addTokenProcedure("arccos", ((args, env) -> {
            if (!(args.getFirst() instanceof DNumber)) throw new DevoreCastException(args.getFirst().type(), "number");
            return ((DNumber) args.getFirst()).arccos();
        }), 1, false);
        dEnv.addTokenProcedure("atan2", ((args, env) -> {
            if (!(args.getFirst() instanceof DNumber)) throw new DevoreCastException(args.getFirst().type(), "number");
            if (!(args.get(1) instanceof DNumber)) throw new DevoreCastException(args.get(1).type(), "number");
            return ((DNumber) args.getFirst()).atan2((DNumber) args.get(1));
        }), 2, false);
        dEnv.addTokenProcedure("prime?", ((args, env) -> {
            if (!(args.getFirst() instanceof DInt)) throw new DevoreCastException(args.getFirst().type(), "int");
            return DBool.valueOf(((DInt) args.getFirst()).toBigInteger().isProbablePrime(100));
        }), 1, false);
        dEnv.addTokenProcedure("prime?", ((args, env) -> {
            if (!(args.getFirst() instanceof DInt)) throw new DevoreCastException(args.getFirst().type(), "int");
            if (!(args.get(1) instanceof DInt)) throw new DevoreCastException(args.get(1).type(), "int");
            return DBool.valueOf(((DInt) args.getFirst()).toBigInteger().isProbablePrime(((DInt) args.get(1)).toBigInteger().intValue()));
        }), 2, false);
        dEnv.addTokenProcedure("gcd", ((args, env) -> {
            if (!(args.getFirst() instanceof DInt n1)) throw new DevoreCastException(args.getFirst().type(), "int");
            if (!(args.get(1) instanceof DInt n2)) throw new DevoreCastException(args.get(1).type(), "int");
            for (BigInteger i = (n1.toBigInteger().compareTo(n2.toBigInteger()) < 0 ? n1 : n2).toBigInteger(); i.compareTo(BigInteger.ONE) > 0; i = i.subtract(BigInteger.ONE))
                if (n1.toBigInteger().mod(i).equals(BigInteger.ZERO) && n2.toBigInteger().mod(i).equals(BigInteger.ZERO))
                    return DNumber.valueOf(i);
            return DNumber.valueOf(1);
        }), 2, false);
        dEnv.addTokenProcedure("lcm", ((args, env) -> {
            if (!(args.getFirst() instanceof DInt n1)) throw new DevoreCastException(args.getFirst().type(), "int");
            if (!(args.get(1) instanceof DInt n2)) throw new DevoreCastException(args.get(1).type(), "int");
            BigInteger gcd = BigInteger.ONE;
            for (BigInteger i = (n1.toBigInteger().compareTo(n2.toBigInteger()) < 0 ? n1 : n2).toBigInteger(); i.compareTo(BigInteger.ONE) > 0; i = i.subtract(BigInteger.ONE))
                if (n1.toBigInteger().mod(i).equals(BigInteger.ZERO) && n2.toBigInteger().mod(i).equals(BigInteger.ZERO)) {
                    gcd = i;
                    break;
                }
            if (n1.toBigInteger().compareTo(BigInteger.ZERO) == 0 || n2.toBigInteger().compareTo(BigInteger.ZERO) == 0)
                return DNumber.valueOf(0);
            return DNumber.valueOf(n1.toBigInteger().multiply(n2.toBigInteger()).divide(gcd));
        }), 2, false);
        dEnv.addTokenProcedure("log", ((args, env) -> {
            if (!(args.getFirst() instanceof DNumber)) throw new DevoreCastException(args.getFirst().type(), "number");
            return ((DNumber) args.getFirst()).log();
        }), 1, false);
        dEnv.addTokenProcedure("log", ((args, env) -> {
            if (!(args.getFirst() instanceof DNumber)) throw new DevoreCastException(args.getFirst().type(), "number");
            if (!(args.get(1) instanceof DNumber)) throw new DevoreCastException(args.get(1).type(), "number");
            return ((DNumber) args.getFirst()).log((DNumber) args.get(1));
        }), 2, false);
        dEnv.addTokenProcedure("exp", ((args, env) -> {
            if (!(args.getFirst() instanceof DNumber)) throw new DevoreCastException(args.getFirst().type(), "number");
            return ((DNumber) args.getFirst()).exp();
        }), 1, false);
        dEnv.addTokenProcedure("ceiling", ((args, env) -> {
            if (!(args.getFirst() instanceof DNumber)) throw new DevoreCastException(args.getFirst().type(), "number");
            return ((DNumber) args.getFirst()).ceiling();
        }), 1, false);
        dEnv.addTokenProcedure("floor", ((args, env) -> {
            if (!(args.getFirst() instanceof DNumber)) throw new DevoreCastException(args.getFirst().type(), "number");
            return ((DNumber) args.getFirst()).floor();
        }), 1, false);
        dEnv.addTokenProcedure("truncate", ((args, env) -> {
            if (!(args.getFirst() instanceof DNumber)) throw new DevoreCastException(args.getFirst().type(), "number");
            return ((DNumber) args.getFirst()).truncate();
        }), 1, false);
        dEnv.addTokenProcedure("round", ((args, env) -> {
            if (!(args.getFirst() instanceof DNumber)) throw new DevoreCastException(args.getFirst().type(), "number");
            return ((DNumber) args.getFirst()).round();
        }), 1, false);
        dEnv.addTokenProcedure("println", ((args, env) -> {
            StringBuilder builder = new StringBuilder();
            for (Token t : args)
                builder.append(t);
            env.io().out().println(builder);
            return DWord.NIL;
        }), 1, true);
        dEnv.addTokenProcedure("print", ((args, env) -> {
            StringBuilder builder = new StringBuilder();
            for (Token t : args)
                builder.append(t);
            env.io().out().print(builder);
            return DWord.NIL;
        }), 1, true);
        dEnv.addSymbolProcedure("undef", ((ast, env) -> {
            for (AstNode child : ast.children) {
                if (!(child.symbol instanceof DSymbol)) throw new DevoreCastException(child.symbol.type(), "symbol");
                env.remove(child.symbol.toString());
            }
            return DWord.NIL;
        }), 1, true);
        dEnv.addSymbolProcedure("def-macro", ((ast, env) -> {
            if (!(ast.getFirst().symbol instanceof DSymbol))
                throw new DevoreCastException(ast.getFirst().symbol.type(), "symbol");
            List<String> params = new ArrayList<>();
            for (AstNode param : ast.getFirst().children) {
                if (!(param.symbol instanceof DSymbol)) throw new DevoreCastException(param.symbol.type(), "symbol");
                params.add(param.symbol.toString());
            }
            List<AstNode> asts = new ArrayList<>();
            for (int i = 1; i < ast.size(); ++i)
                asts.add(ast.get(i).copy());
            env.addMacro(ast.getFirst().symbol.toString(), DMacro.newMacro(params, asts));
            return DWord.NIL;
        }), 2, true);
        dEnv.addSymbolProcedure("def", ((ast, env) -> {
            if (!(ast.getFirst().symbol instanceof DSymbol))
                throw new DevoreCastException(ast.getFirst().symbol.type(), "symbol");
            if (ast.getFirst().isEmpty() && ast.getFirst().type != AstNode.AstType.PROCEDURE) {
                Env newEnv = env.createChild();
                Token result = DWord.NIL;
                for (int i = 1; i < ast.size(); ++i)
                    result = Evaluator.eval(newEnv, ast.get(i).copy());
                env.put(ast.getFirst().symbol.toString(), result);
            } else {
                List<String> params = new ArrayList<>();
                for (AstNode param : ast.getFirst().children) {
                    if (!(param.symbol instanceof DSymbol))
                        throw new DevoreCastException(param.symbol.type(), "symbol");
                    params.add(param.symbol.toString());
                }
                List<AstNode> asts = new ArrayList<>();
                for (int i = 1; i < ast.size(); ++i)
                    asts.add(ast.get(i).copy());
                env.addTokenProcedure(ast.getFirst().symbol.toString(), ((cArgs, cEnv) -> {
                    Env newEnv = env.createChild();
                    for (int i = 0; i < params.size(); ++i)
                        newEnv.put(params.get(i), cArgs.get(i));
                    Token result = DWord.NIL;
                    for (AstNode astNode : asts)
                        result = Evaluator.eval(newEnv, astNode.copy());
                    return result;
                }), params.size(), false);
            }
            return DWord.NIL;
        }), 2, true);
        dEnv.addSymbolProcedure("set!", ((ast, env) -> {
            if (!(ast.getFirst().symbol instanceof DSymbol))
                throw new DevoreCastException(ast.getFirst().symbol.type(), "symbol");
            if (ast.getFirst().isEmpty() && ast.getFirst().type != AstNode.AstType.PROCEDURE) {
                Env newEnv = env.createChild();
                Token result = DWord.NIL;
                for (int i = 1; i < ast.size(); ++i)
                    result = Evaluator.eval(newEnv, ast.get(i).copy());
                env.set(ast.getFirst().symbol.toString(), result);
            } else {
                List<String> params = new ArrayList<>();
                for (AstNode param : ast.getFirst().children) {
                    if (!(param.symbol instanceof DSymbol))
                        throw new DevoreCastException(param.symbol.type(), "symbol");
                    params.add(param.symbol.toString());
                }
                List<AstNode> asts = new ArrayList<>();
                for (int i = 1; i < ast.size(); ++i)
                    asts.add(ast.get(i).copy());
                env.setTokenProcedure(ast.getFirst().symbol.toString(), ((cArgs, cEnv) -> {
                    Env newEnv = env.createChild();
                    for (int i = 0; i < params.size(); ++i)
                        newEnv.put(params.get(i), cArgs.get(i));
                    Token result = DWord.NIL;
                    for (AstNode astNode : asts)
                        result = Evaluator.eval(newEnv, astNode.copy());
                    return result;
                }), params.size(), false);
            }
            return DWord.NIL;
        }), 2, true);
        dEnv.addSymbolProcedure("let", ((ast, env) -> {
            Env newEnv = env.createChild();
            Token result = DWord.NIL;
            for (AstNode node : ast.getFirst().children) {
                if ("apply".equals(node.symbol.toString())) {
                    List<String> params = new ArrayList<>();
                    List<AstNode> asts = new ArrayList<>();
                    for (AstNode paramNode : node.getFirst().children) {
                        if (!(paramNode.symbol instanceof DSymbol))
                            throw new DevoreCastException(paramNode.symbol.type(), "symbol");
                        params.add(paramNode.symbol.toString());
                    }
                    for (int i = 1; i < node.size(); ++i)
                        asts.add(node.get(i).copy());
                    if (!(ast.getFirst().symbol instanceof DSymbol))
                        throw new DevoreCastException(ast.getFirst().symbol.type(), "symbol");
                    newEnv.addTokenProcedure(ast.getFirst().symbol.toString(), ((cArgs, cEnv) -> {
                        Env newInEnv = env.createChild();
                        for (int i = 0; i < params.size(); ++i)
                            newInEnv.put(params.get(i), cArgs.get(i));
                        Token inResult = DWord.NIL;
                        for (AstNode astNode : asts)
                            inResult = Evaluator.eval(newInEnv, astNode.copy());
                        return inResult;
                    }), params.size(), false);
                } else {
                    if (!(node.symbol instanceof DSymbol)) throw new DevoreCastException(node.symbol.type(), "symbol");
                    Token value = DWord.NIL;
                    for (AstNode e : node.children)
                        value = Evaluator.eval(env, e.copy());
                    newEnv.put(node.symbol.toString(), value);
                }
            }
            for (int i = 1; i < ast.size(); ++i)
                result = Evaluator.eval(newEnv, ast.get(i).copy());
            return result;
        }), 2, true);
        dEnv.addSymbolProcedure("let*", ((ast, env) -> {
            Env newEnv = env.createChild();
            Token result = DWord.NIL;
            for (AstNode node : ast.getFirst().children) {
                if ("apply".equals(node.symbol.toString())) {
                    List<String> params = new ArrayList<>();
                    List<AstNode> asts = new ArrayList<>();
                    for (AstNode paramNode : node.getFirst().children) {
                        if (!(paramNode.symbol instanceof DSymbol))
                            throw new DevoreCastException(paramNode.symbol.type(), "symbol");
                        params.add(paramNode.symbol.toString());
                    }
                    for (int i = 1; i < node.size(); ++i)
                        asts.add(node.get(i).copy());
                    if (!(ast.getFirst().symbol instanceof DSymbol))
                        throw new DevoreCastException(ast.getFirst().symbol.type(), "symbol");
                    newEnv.addTokenProcedure(ast.getFirst().symbol.toString(), ((cArgs, cEnv) -> {
                        Env newInEnv = newEnv.createChild();
                        for (int i = 0; i < params.size(); ++i)
                            newInEnv.put(params.get(i), cArgs.get(i));
                        Token inResult = DWord.NIL;
                        for (AstNode astNode : asts)
                            inResult = Evaluator.eval(newInEnv, astNode.copy());
                        return inResult;
                    }), params.size(), false);
                } else {
                    if (!(node.symbol instanceof DSymbol)) throw new DevoreCastException(node.symbol.type(), "symbol");
                    Token value = DWord.NIL;
                    for (AstNode e : node.children)
                        value = Evaluator.eval(newEnv, e.copy());
                    newEnv.put(node.symbol.toString(), value);
                }
            }
            for (int i = 1; i < ast.size(); ++i)
                result = Evaluator.eval(newEnv, ast.get(i).copy());
            return result;
        }), 2, true);
        dEnv.addSymbolProcedure("lambda", ((ast, env) -> {
            List<String> params = new ArrayList<>();
            if (ast.getFirst().isNotNil()) {
                if (!(ast.getFirst().symbol instanceof DSymbol))
                    throw new DevoreCastException(ast.getFirst().symbol.type(), "symbol");
                params.add(ast.getFirst().symbol.toString());
                for (AstNode param : ast.getFirst().children) {
                    if (!(param.symbol instanceof DSymbol))
                        throw new DevoreCastException(param.symbol.type(), "symbol");
                    params.add(param.symbol.toString());
                }
            }
            List<AstNode> asts = new ArrayList<>();
            for (int i = 1; i < ast.size(); ++i)
                asts.add(ast.get(i).copy());
            BiFunction<AstNode, Env, Token> df = (inAst, inEnv) -> {
                List<Token> args = new ArrayList<>();
                for (int i = 0; i < inAst.size(); ++i) {
                    inAst.get(i).symbol = Evaluator.eval(inEnv, inAst.get(i).copy());
                    args.add(inAst.get(i).symbol);
                }
                Env newInEnv = env.createChild();
                for (int i = 0; i < params.size(); ++i)
                    newInEnv.put(params.get(i), args.get(i));
                Token inResult = DWord.NIL;
                for (AstNode astNode : asts)
                    inResult = Evaluator.eval(newInEnv, astNode.copy());
                return inResult;
            };
            return DProcedure.newProcedure(df, params.size(), false);
        }), 2, true);
        dEnv.addTokenProcedure("apply", ((args, env) -> {
            if (!(args.getFirst() instanceof DProcedure))
                throw new DevoreCastException(args.getFirst().type(), "procedure");
            List<Token> params = new ArrayList<>();
            for (int i = 1; i < args.size(); ++i)
                params.add(args.get(i));
            AstNode asts = AstNode.emptyAst.copy();
            for (Token arg : params)
                asts.add(new AstNode(arg));
            return ((DProcedure) args.getFirst()).call(asts, env);
        }), 1, true);
        dEnv.addTokenProcedure("act", ((args, env) -> {
            if (!(args.getFirst() instanceof DProcedure))
                throw new DevoreCastException(args.getFirst().type(), "procedure");
            if (!(args.get(1) instanceof DList)) throw new DevoreCastException(args.getFirst().type(), "list");
            List<Token> params = ((DList) args.get(1)).toList();
            AstNode asts = AstNode.emptyAst.copy();
            for (Token arg : params)
                asts.add(new AstNode(arg));
            return ((DProcedure) args.getFirst()).call(asts, env);
        }), 2, false);
        dEnv.addTokenProcedure(">", ((args, env) -> DBool.valueOf(args.getFirst().compareTo(args.get(1)) > 0)), 2, false);
        dEnv.addTokenProcedure("<", ((args, env) -> DBool.valueOf(args.getFirst().compareTo(args.get(1)) < 0)), 2, false);
        dEnv.addTokenProcedure("=", ((args, env) -> DBool.valueOf(args.getFirst().compareTo(args.get(1)) == 0)), 2, false);
        dEnv.addTokenProcedure("/=", ((args, env) -> DBool.valueOf(args.getFirst().compareTo(args.get(1)) != 0)), 2, false);
        dEnv.addTokenProcedure(">=", ((args, env) -> DBool.valueOf(args.getFirst().compareTo(args.get(1)) >= 0)), 2, false);
        dEnv.addTokenProcedure("<=", ((args, env) -> DBool.valueOf(args.getFirst().compareTo(args.get(1)) <= 0)), 2, false);
        dEnv.addSymbolProcedure("unless", (ast, env) -> {
            Token result = DWord.NIL;
            Token condition = Evaluator.eval(env, ast.getFirst().copy());
            if (!(condition instanceof DBool)) throw new DevoreCastException(condition.type(), "bool");
            if (!((DBool) condition).bool) {
                for (int i = 1; i < ast.size(); ++i)
                    result = Evaluator.eval(env, ast.get(i).copy());
            }
            return result;
        }, 2, true);
        dEnv.addSymbolProcedure("when", (ast, env) -> {
            Token result = DWord.NIL;
            Token condition = Evaluator.eval(env, ast.getFirst().copy());
            if (!(condition instanceof DBool)) throw new DevoreCastException(condition.type(), "bool");
            if (((DBool) condition).bool) {
                for (int i = 1; i < ast.size(); ++i)
                    result = Evaluator.eval(env, ast.get(i).copy());
            }
            return result;
        }, 2, true);
        dEnv.addSymbolProcedure("if", (ast, env) -> {
            Token result = DWord.NIL;
            Token condition = Evaluator.eval(env, ast.getFirst().copy());
            if (!(condition instanceof DBool)) throw new DevoreCastException(condition.type(), "bool");
            if (((DBool) condition).bool) result = Evaluator.eval(env, ast.get(1).copy());
            return result;
        }, 2, false);
        dEnv.addSymbolProcedure("if", (ast, env) -> {
            Token result;
            Token condition = Evaluator.eval(env, ast.getFirst().copy());
            if (!(condition instanceof DBool)) throw new DevoreCastException(condition.type(), "bool");
            if (((DBool) condition).bool) result = Evaluator.eval(env, ast.get(1).copy());
            else result = Evaluator.eval(env, ast.get(2).copy());
            return result;
        }, 3, false);
        dEnv.addSymbolProcedure("cond", (ast, env) -> {
            Token result = DWord.NIL;
            for (AstNode node : ast.children) {
                if (node.symbol instanceof DSymbol && "else".equals(node.symbol.toString())) {
                    result = Evaluator.eval(env, node.getFirst().copy());
                    break;
                } else {
                    Token condition = Evaluator.eval(env, node.getFirst().copy());
                    if (!(condition instanceof DBool)) throw new DevoreCastException(condition.type(), "bool");
                    if (((DBool) condition).bool) {
                        Token r = DWord.NIL;
                        for (int i = 1; i < node.size(); ++i)
                            r = Evaluator.eval(env, node.get(i).copy());
                        result = r;
                        break;
                    }
                }
            }
            return result;
        }, 2, true);
        dEnv.addSymbolProcedure("begin", (ast, env) -> {
            Token result = DWord.NIL;
            for (AstNode node : ast.children)
                result = Evaluator.eval(env, node.copy());
            return result;
        }, 1, true);
        dEnv.addSymbolProcedure("while", (ast, env) -> {
            Token result = DWord.NIL;
            Token condition = Evaluator.eval(env, ast.getFirst().copy());
            if (!(condition instanceof DBool)) throw new DevoreCastException(condition.type(), "bool");
            while (((DBool) condition).bool) {
                for (int i = 1; i < ast.size(); ++i)
                    result = Evaluator.eval(env, ast.get(i).copy());
                condition = Evaluator.eval(env, ast.getFirst().copy());
                if (!(condition instanceof DBool)) throw new DevoreCastException(condition.type(), "bool");
            }
            return result;
        }, 2, true);
        dEnv.addTokenProcedure("read-line", ((args, env) -> DString.valueOf(new Scanner(env.io().in()).nextLine())), 0, false);
        dEnv.addTokenProcedure("read-int", ((args, env) -> DNumber.valueOf(new Scanner(env.io().in()).nextBigInteger())), 0, false);
        dEnv.addTokenProcedure("read-float", ((args, env) -> DNumber.valueOf(new Scanner(env.io().in()).nextBigDecimal())), 0, false);
        dEnv.addTokenProcedure("read-bool", ((args, env) -> DBool.valueOf(new Scanner(env.io().in()).nextBoolean())), 0, false);
        dEnv.addTokenProcedure("read", ((args, env) -> DString.valueOf(new Scanner(env.io().in()).next())), 0, false);
        dEnv.addTokenProcedure("newline", ((args, env) -> {
            env.io().out().println();
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
            if (!(args.getFirst() instanceof DBool)) throw new DevoreCastException(args.getFirst().type(), "bool");
            if (((DBool) args.getFirst()).bool) return DBool.FALSE;
            return DBool.TRUE;
        }), 1, false);
        dEnv.addTokenProcedure("random", ((args, env) -> {
            if (!(args.getFirst() instanceof DInt)) throw new DevoreCastException(args.getFirst().type(), "int");
            BigInteger start = BigInteger.ZERO;
            BigInteger end = ((DInt) args.getFirst()).toBigInteger().subtract(BigInteger.ONE);
            Random rand = new Random();
            BigInteger range = end.subtract(start).add(BigInteger.ONE);
            BigInteger randomValue;
            do {
                randomValue = new BigInteger(range.bitLength(), rand);
            } while (randomValue.compareTo(range) >= 0);
            return DNumber.valueOf(randomValue.add(start));
        }), 1, false);
        dEnv.addTokenProcedure("random", ((args, env) -> {
            if (!(args.getFirst() instanceof DInt)) throw new DevoreCastException(args.getFirst().type(), "int");
            if (!(args.get(1) instanceof DInt)) throw new DevoreCastException(args.get(1).type(), "int");
            BigInteger start = ((DInt) args.getFirst()).toBigInteger();
            BigInteger end = ((DInt) args.get(1)).toBigInteger().subtract(BigInteger.ONE);
            Random rand = new Random();
            BigInteger range = end.subtract(start).add(BigInteger.ONE);
            BigInteger randomValue;
            do {
                randomValue = new BigInteger(range.bitLength(), rand);
            } while (randomValue.compareTo(range) >= 0);
            return DNumber.valueOf(randomValue.add(start));
        }), 2, false);
        dEnv.addTokenProcedure("list", ((args, env) -> DList.valueOf(new ArrayList<>(args))), 0, true);
        dEnv.addTokenProcedure("list-contains", ((args, env) -> {
            if (!(args.getFirst() instanceof DList)) throw new DevoreCastException(args.getFirst().type(), "list");
            return DBool.valueOf(((DList) args.getFirst()).contains(args.get(1)));
        }), 2, false);
        dEnv.addTokenProcedure("list-get", ((args, env) -> {
            if (!(args.getFirst() instanceof DList list)) throw new DevoreCastException(args.getFirst().type(), "list");
            if (!(args.get(1) instanceof DInt)) throw new DevoreCastException(args.get(1).type(), "int");
            if (list.size() == 0) return DWord.NIL;
            return list.get(((DInt) args.get(1)).toBigInteger().intValue());
        }), 2, false);
        dEnv.addTokenProcedure("list-set", ((args, env) -> {
            if (!(args.getFirst() instanceof DList list)) throw new DevoreCastException(args.getFirst().type(), "list");
            if (!(args.get(1) instanceof DInt)) throw new DevoreCastException(args.get(1).type(), "int");
            if (list.size() == 0) return DWord.NIL;
            return list.set(((DInt) args.get(1)).toBigInteger().intValue(), args.get(2), false);
        }), 3, false);
        dEnv.addTokenProcedure("list-remove", ((args, env) -> {
            if (!(args.getFirst() instanceof DList list)) throw new DevoreCastException(args.getFirst().type(), "list");
            if (list.size() == 0) return DWord.NIL;
            if (args.get(1) instanceof DInt) return list.remove(((DInt) args.get(1)).toBigInteger().intValue(), false);
            return list.remove(args.get(1), false);
        }), 2, false);
        dEnv.addTokenProcedure("list-add", ((args, env) -> {
            if (!(args.getFirst() instanceof DList list)) throw new DevoreCastException(args.getFirst().type(), "list");
            return list.add(args.get(1), false);
        }), 2, false);
        dEnv.addTokenProcedure("list-add", ((args, env) -> {
            if (!(args.getFirst() instanceof DList list)) throw new DevoreCastException(args.getFirst().type(), "list");
            if (!(args.get(1) instanceof DInt index)) throw new DevoreCastException(args.getFirst().type(), "int");
            return list.add(index.toBigInteger().intValue(), args.get(2), false);
        }), 3, false);
        dEnv.addTokenProcedure("list-set!", ((args, env) -> {
            if (!(args.getFirst() instanceof DList list)) throw new DevoreCastException(args.getFirst().type(), "list");
            if (!(args.get(1) instanceof DInt)) throw new DevoreCastException(args.get(1).type(), "int");
            if (list.size() == 0) return DWord.NIL;
            return list.set(((DInt) args.get(1)).toBigInteger().intValue(), args.get(2), true);
        }), 3, false);
        dEnv.addTokenProcedure("list-remove!", ((args, env) -> {
            if (!(args.getFirst() instanceof DList list)) throw new DevoreCastException(args.getFirst().type(), "list");
            if (list.size() == 0) return DWord.NIL;
            if (args.get(1) instanceof DInt) return list.remove(((DInt) args.get(1)).toBigInteger().intValue(), true);
            return list.remove(args.get(1), false);
        }), 2, false);
        dEnv.addTokenProcedure("list-add!", ((args, env) -> {
            if (!(args.getFirst() instanceof DList)) throw new DevoreCastException(args.getFirst().type(), "list");
            return ((DList) args.getFirst()).add(args.get(1), true);
        }), 2, false);
        dEnv.addTokenProcedure("list-add!", ((args, env) -> {
            if (!(args.getFirst() instanceof DList)) throw new DevoreCastException(args.getFirst().type(), "list");
            if (!(args.get(1) instanceof DInt)) throw new DevoreCastException(args.getFirst().type(), "int");
            return ((DList) args.getFirst()).add(((DInt) args.get(1)).toBigInteger().intValue(), args.get(2), true);
        }), 3, false);
        dEnv.addTokenProcedure("head", ((args, env) -> {
            if (!(args.getFirst() instanceof DList list)) throw new DevoreCastException(args.getFirst().type(), "list");
            if (list.size() == 0) return DWord.NIL;
            return list.getFirst();
        }), 1, false);
        dEnv.addTokenProcedure("last", ((args, env) -> {
            if (!(args.getFirst() instanceof DList list)) throw new DevoreCastException(args.getFirst().type(), "list");
            if (list.size() == 0) return DWord.NIL;
            return list.get(list.size() - 1);
        }), 1, false);
        dEnv.addTokenProcedure("tail", ((args, env) -> {
            if (!(args.getFirst() instanceof DList list)) throw new DevoreCastException(args.getFirst().type(), "list");
            return list.subList(1, list.size());
        }), 1, false);
        dEnv.addTokenProcedure("init", ((args, env) -> {
            if (!(args.getFirst() instanceof DList list)) throw new DevoreCastException(args.getFirst().type(), "list");
            return list.subList(0, list.size() - 1);
        }), 1, false);
        dEnv.addTokenProcedure("length", ((args, env) -> {
            if (!(args.getFirst() instanceof DList)) return DNumber.valueOf(args.getFirst().toString().length());
            return DNumber.valueOf(((DList) args.getFirst()).size());
        }), 1, false);
        dEnv.addTokenProcedure("reverse", ((args, env) -> {
            if (!(args.getFirst() instanceof DList tokens))
                throw new DevoreCastException(args.getFirst().type(), "list");
            List<Token> temp = new ArrayList<>();
            for (int i = tokens.size() - 1; i >= 0; --i)
                temp.add(tokens.get(i));
            return DList.valueOf(new ArrayList<>(temp));
        }), 1, false);
        dEnv.addTokenProcedure("reverse!", ((args, env) -> {
            if (!(args.getFirst() instanceof DList list))
                throw new DevoreCastException(args.getFirst().type(), "list");
            List<Token> temp = new ArrayList<>();
            for (int i = list.size() - 1; i >= 0; --i)
                temp.add(list.get(i));
            list.clear();
            for (Token t : temp)
                list.add(t, true);
            return list;
        }), 1, false);
        dEnv.addTokenProcedure("sort", ((args, env) -> {
            if (!(args.getFirst() instanceof DList)) throw new DevoreCastException(args.getFirst().type(), "list");
            return ((DList) args.getFirst()).sort(false);
        }), 1, false);
        dEnv.addTokenProcedure("sort!", ((args, env) -> {
            if (!(args.getFirst() instanceof DList)) throw new DevoreCastException(args.getFirst().type(), "list");
            return ((DList) args.getFirst()).sort(true);
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
                for (Token arg : args)
                    builder.append(arg.toString());
                result = DString.valueOf(builder.toString());
            }
            return result;
        }), 1, true);
        dEnv.addTokenProcedure("map", ((args, env) -> {
            if (!(args.getFirst() instanceof DProcedure))
                throw new DevoreCastException(args.getFirst().type(), "procedure");
            if (!(args.get(1) instanceof DList)) throw new DevoreCastException(args.get(1).type(), "list");
            List<Token> result = new ArrayList<>();
            List<Token> tokens = ((DList) args.get(1)).toList();
            for (int i = 0; i < tokens.size(); ++i) {
                List<Token> params = new ArrayList<>();
                params.add(tokens.get(i));
                for (int j = 1; j < args.size() - 1; ++j) {
                    if (!(args.get(j + 1) instanceof DList))
                        throw new DevoreCastException(args.get(j + 1).type(), "list");
                    params.add(((DList) args.get(j + 1)).get(i));
                }
                result.add(((DProcedure) args.getFirst()).call(params.toArray(Token[]::new), env.createChild()));
            }
            return DList.valueOf(result);
        }), 2, true);
        dEnv.addTokenProcedure("for-each", ((args, env) -> {
            if (!(args.getFirst() instanceof DProcedure))
                throw new DevoreCastException(args.getFirst().type(), "procedure");
            if (!(args.get(1) instanceof DList)) throw new DevoreCastException(args.get(1).type(), "list");
            List<Token> tokens = ((DList) args.get(1)).toList();
            for (int i = 0; i < tokens.size(); ++i) {
                List<Token> params = new ArrayList<>();
                params.add(tokens.get(i));
                for (int j = 1; j < args.size() - 1; ++j) {
                    if (!(args.get(j + 1) instanceof DList))
                        throw new DevoreCastException(args.get(j + 1).type(), "list");
                    params.add(((DList) args.get(j + 1)).get(i));
                }
                ((DProcedure) args.getFirst()).call(params.toArray(Token[]::new), env.createChild());
            }
            return DWord.NIL;
        }), 2, true);
        dEnv.addTokenProcedure("foldr", ((args, env) -> {
            if (!(args.getFirst() instanceof DProcedure))
                throw new DevoreCastException(args.getFirst().type(), "procedure");
            if (!(args.get(2) instanceof DList)) throw new DevoreCastException(args.get(2).type(), "list");
            var result = args.get(1);
            List<Token> tokens = ((DList) args.get(2)).toList();
            for (int i = tokens.size() - 1; i >= 0; --i)
                result = ((DProcedure) args.getFirst()).call(new Token[]{tokens.get(i), result}, env.createChild());
            return result;
        }), 3, false);
        dEnv.addTokenProcedure("foldl", ((args, env) -> {
            if (!(args.getFirst() instanceof DProcedure))
                throw new DevoreCastException(args.getFirst().type(), "procedure");
            if (!(args.get(2) instanceof DList)) throw new DevoreCastException(args.get(2).type(), "list");
            var result = args.get(1);
            List<Token> tokens = ((DList) args.get(2)).toList();
            for (int i = tokens.size() - 1; i >= 0; --i)
                result = ((DProcedure) args.getFirst()).call(new Token[]{result, tokens.get(i)}, env.createChild());
            return result;
        }), 3, false);
        dEnv.addTokenProcedure("filter", ((args, env) -> {
            if (!(args.getFirst() instanceof DProcedure))
                throw new DevoreCastException(args.getFirst().type(), "procedure");
            if (!(args.get(1) instanceof DList)) throw new DevoreCastException(args.get(1).type(), "list");
            List<Token> result = new ArrayList<>();
            List<Token> tokens = ((DList) args.get(1)).toList();
            for (Token token : tokens) {
                AstNode asts = AstNode.emptyAst.copy();
                asts.add(new AstNode(token));
                Token condition = ((DProcedure) args.getFirst()).call(asts, env.createChild());
                if (!(condition instanceof DBool)) throw new DevoreCastException(condition.type(), "list");
                if (((DBool) condition).bool) result.add(token);
            }
            return DList.valueOf(result);
        }), 2, false);
        dEnv.addTokenProcedure("range", ((args, env) -> {
            if (!(args.getFirst() instanceof DNumber)) throw new DevoreCastException(args.getFirst().type(), "number");
            BigDecimal start = BigDecimal.ZERO;
            BigDecimal step = BigDecimal.ONE;
            BigDecimal end = ((DNumber) args.getFirst()).toBigDecimal().subtract(step);
            List<Token> list = new ArrayList<>();
            for (BigDecimal current = start; current.compareTo(end) <= 0; current = current.add(step))
                list.add(DNumber.valueOf(current));
            return DList.valueOf(list);
        }), 1, false);
        dEnv.addTokenProcedure("range", ((args, env) -> {
            if (!(args.getFirst() instanceof DNumber)) throw new DevoreCastException(args.getFirst().type(), "number");
            if (!(args.get(1) instanceof DNumber)) throw new DevoreCastException(args.get(1).type(), "number");
            BigDecimal start = ((DNumber) args.get(0)).toBigDecimal();
            BigDecimal step = BigDecimal.ONE;
            BigDecimal end = ((DNumber) args.get(1)).toBigDecimal().subtract(step);
            List<Token> list = new ArrayList<>();
            for (BigDecimal current = start; current.compareTo(end) <= 0; current = current.add(step))
                list.add(DNumber.valueOf(current));
            return DList.valueOf(list);
        }), 2, false);
        dEnv.addTokenProcedure("range", ((args, env) -> {
            if (!(args.getFirst() instanceof DNumber)) throw new DevoreCastException(args.getFirst().type(), "number");
            if (!(args.get(1) instanceof DNumber)) throw new DevoreCastException(args.get(1).type(), "number");
            if (!(args.get(2) instanceof DNumber)) throw new DevoreCastException(args.get(2).type(), "number");
            BigDecimal start = ((DNumber) args.get(0)).toBigDecimal();
            BigDecimal step = ((DNumber) args.get(2)).toBigDecimal();
            BigDecimal end = ((DNumber) args.get(1)).toBigDecimal().subtract(step);
            if (step.compareTo(BigDecimal.ZERO) == 0) throw new DevoreRuntimeException("步长不能为零.");
            List<Token> list = new ArrayList<>();
            if (step.compareTo(BigDecimal.ZERO) > 0) {
                for (BigDecimal current = start; current.compareTo(end) <= 0; current = current.add(step))
                    list.add(DNumber.valueOf(current));
            } else {
                for (BigDecimal current = start; current.compareTo(end) >= 0; current = current.add(step))
                    list.add(DNumber.valueOf(current));
            }
            return DList.valueOf(list);
        }), 3, false);
        dEnv.addTokenProcedure("string->symbol", ((args, env) -> {
            if (!(args.getFirst() instanceof DString)) throw new DevoreCastException(args.getFirst().type(), "string");
            return DSymbol.valueOf(args.getFirst().toString());
        }), 1, false);
        dEnv.addTokenProcedure("string->number", ((args, env) -> {
            if (!(args.getFirst() instanceof DString)) throw new DevoreCastException(args.getFirst().type(), "string");
            return DNumber.valueOf(new BigDecimal(args.getFirst().toString()));
        }), 1, false);
        dEnv.addTokenProcedure("string->bool", ((args, env) -> {
            if (!(args.getFirst() instanceof DString)) throw new DevoreCastException(args.getFirst().type(), "string");
            return "true".equals(args.getFirst().toString()) ? DBool.TRUE : DBool.FALSE;
        }), 1, false);
        dEnv.addTokenProcedure("->string", ((args, env) -> DString.valueOf(args.getFirst().toString())), 1, false);
        dEnv.addTokenProcedure("string->list", ((args, env) -> {
            if (!(args.getFirst() instanceof DString)) throw new DevoreCastException(args.getFirst().type(), "string");
            char[] chars = args.getFirst().toString().toCharArray();
            List<Token> tokens = new ArrayList<>();
            for (char c : chars)
                tokens.add(DString.valueOf(String.valueOf(c)));
            return DList.valueOf(tokens);
        }), 1, false);
        dEnv.addTokenProcedure("char->unicode", ((args, env) -> {
            if (!(args.getFirst() instanceof DString)) throw new DevoreCastException(args.getFirst().type(), "string");
            return DNumber.valueOf((int) args.getFirst().toString().charAt(0));
        }), 1, false);
        dEnv.addTokenProcedure("unicode->char", ((args, env) -> {
            if (!(args.getFirst() instanceof DInt)) throw new DevoreCastException(args.getFirst().type(), "int");
            return DString.valueOf(String.valueOf((char) ((DInt) args.getFirst()).toBigInteger().intValue()));
        }), 1, false);
        dEnv.addTokenProcedure("exit", ((args, env) -> {
            if (!(args.getFirst() instanceof DInt)) throw new DevoreCastException(args.getFirst().type(), "int");
            System.exit(((DInt) args.getFirst()).toBigInteger().intValue());
            return DWord.NIL;
        }), 1, false);
        dEnv.addTokenProcedure("sleep", ((args, env) -> {
            if (!(args.getFirst() instanceof DInt)) throw new DevoreCastException(args.getFirst().type(), "int");
            try {
                Thread.sleep(((DInt) args.getFirst()).toBigInteger().longValue());
            } catch (InterruptedException e) {
                return DWord.NIL;
            }
            return DWord.NIL;
        }), 1, false);
        dEnv.addTokenProcedure("type", ((args, env) -> DString.valueOf(args.getFirst().type())), 1, false);
        dEnv.addTokenProcedure("time", ((args, env) -> DNumber.valueOf(System.currentTimeMillis())), 0, false);
        dEnv.addTokenProcedure("table", ((args, env) -> DTable.valueOf(new HashMap<>())), 0, false);
        dEnv.addTokenProcedure("table-get", ((args, env) -> {
            if (!(args.getFirst() instanceof DTable))
                throw new DevoreCastException(args.getFirst().type(), "table");
            return ((DTable) args.getFirst()).get(args.get(1));
        }), 2, false);
        dEnv.addTokenProcedure("table-contains-key", ((args, env) -> {
            if (!(args.getFirst() instanceof DTable))
                throw new DevoreCastException(args.getFirst().type(), "table");
            return ((DTable) args.getFirst()).containsKey(args.get(1));
        }), 2, false);
        dEnv.addTokenProcedure("table-contains-value", ((args, env) -> {
            if (!(args.getFirst() instanceof DTable))
                throw new DevoreCastException(args.getFirst().type(), "table");
            return ((DTable) args.getFirst()).containsValue(args.get(1));
        }), 2, false);
        dEnv.addTokenProcedure("table-size", ((args, env) -> {
            if (!(args.getFirst() instanceof DTable))
                throw new DevoreCastException(args.getFirst().type(), "table");
            return DNumber.valueOf(((DTable) args.getFirst()).size());
        }), 1, false);
        dEnv.addTokenProcedure("table-put", ((args, env) -> {
            if (!(args.getFirst() instanceof DTable))
                throw new DevoreCastException(args.getFirst().type(), "table");
            return ((DTable) args.getFirst()).put(args.get(1), args.get(2), false);
        }), 3, false);
        dEnv.addTokenProcedure("table-put!", ((args, env) -> {
            if (!(args.getFirst() instanceof DTable))
                throw new DevoreCastException(args.getFirst().type(), "table");
            return ((DTable) args.getFirst()).put(args.get(1), args.get(2), true);
        }), 3, false);
        dEnv.addTokenProcedure("table-remove", ((args, env) -> {
            if (!(args.getFirst() instanceof DTable))
                throw new DevoreCastException(args.getFirst().type(), "table");
            return ((DTable) args.getFirst()).remove(args.get(1), false);
        }), 2, false);
        dEnv.addTokenProcedure("table-remove!", ((args, env) -> {
            if (!(args.getFirst() instanceof DTable))
                throw new DevoreCastException(args.getFirst().type(), "table");
            return ((DTable) args.getFirst()).remove(args.get(1), true);
        }), 2, false);
        dEnv.addTokenProcedure("table-keys", ((args, env) -> {
            if (!(args.getFirst() instanceof DTable))
                throw new DevoreCastException(args.getFirst().type(), "table");
            return DList.valueOf(new ArrayList<>(((DTable) args.getFirst()).keys()));
        }), 1, false);
        dEnv.addTokenProcedure("bool?", ((args, env) -> DBool.valueOf(args.getFirst() instanceof DBool)), 1, false);
        dEnv.addTokenProcedure("float?", ((args, env) -> DBool.valueOf(args.getFirst() instanceof DFloat)), 1, false);
        dEnv.addTokenProcedure("int?", ((args, env) -> DBool.valueOf(args.getFirst() instanceof DInt)), 1, false);
        dEnv.addTokenProcedure("list?", ((args, env) -> DBool.valueOf(args.getFirst() instanceof DList)), 1, false);
        dEnv.addTokenProcedure("macro?", ((args, env) -> DBool.valueOf(args.getFirst() instanceof DMacro)), 1, false);
        dEnv.addTokenProcedure("number?", ((args, env) -> DBool.valueOf(args.getFirst() instanceof DNumber)), 1, false);
        dEnv.addTokenProcedure("procedure?", ((args, env) -> DBool.valueOf(args.getFirst() instanceof DProcedure)), 1, false);
        dEnv.addTokenProcedure("string?", ((args, env) -> DBool.valueOf(args.getFirst() instanceof DString)), 1, false);
        dEnv.addTokenProcedure("symbol?", ((args, env) -> DBool.valueOf(args.getFirst() instanceof DSymbol)), 1, false);
        dEnv.addTokenProcedure("table?", ((args, env) -> DBool.valueOf(args.getFirst() instanceof DTable)), 1, false);
        dEnv.addTokenProcedure("word?", ((args, env) -> DBool.valueOf(args.getFirst() instanceof DWord)), 1, false);
        dEnv.addTokenProcedure("nil?", ((args, env) -> DBool.valueOf(args.getFirst() instanceof DWord word && word == DWord.NIL)), 1, false);
        dEnv.addTokenProcedure("zero?", ((args, env) -> DBool.valueOf(args.getFirst() instanceof DNumber number && number.toBigInteger().compareTo(BigInteger.ZERO) == 0)), 1, false);
    }
}