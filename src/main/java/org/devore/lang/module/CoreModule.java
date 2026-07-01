package org.devore.lang.module;

import org.devore.Devore;
import org.devore.exception.DevoreCastException;
import org.devore.exception.DevoreRuntimeException;
import org.devore.lang.DSecurity;
import org.devore.lang.Env;
import org.devore.lang.Evaluator;
import org.devore.lang.token.*;
import org.devore.parser.Ast;
import org.devore.utils.DIntUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.BiFunction;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * 核心
 */
public class CoreModule extends DModule {
    /**
     * 创建Core模块实例
     */
    public CoreModule() {
        super("core");
    }

    /**
     * 初始化核心环境，按功能分类注册全部内置常量和过程
     */
    public void init(Env dEnv) {
        initConstants(dEnv);                // 基础常量
        initNumberProcedures(dEnv);         // 基础数值计算
        initIOProcedures(dEnv);             // 标准输入输出
        initErrorProcedures(dEnv);          // 错误输出和抛错
        initModuleProcedures(dEnv);         // 模块加载
        initDefinitionProcedures(dEnv);     // 定义、库定义、宏和函数调用
        initComparisonProcedures(dEnv);     // 通用比较
        initControlProcedures(dEnv);        // 控制流
        initLogicAndRandomProcedures(dEnv); // 布尔逻辑和随机数
        initListProcedures(dEnv);           // 列表、序列和高阶遍历
        initConversionProcedures(dEnv);     // 类型转换
        initSystemProcedures(dEnv);         // 系统级过程
        initTableProcedures(dEnv);          // 表结构
        initAggregateProcedures(dEnv);      // 聚合比较
        initPredicateProcedures(dEnv);      // 类型和特殊值判断
        initStringProcedures(dEnv);         // 字符串处理
    }

    /**
     * 注册基础常量，如nil、true和false
     */
    private void initConstants(Env dEnv) {
        dEnv.put("nil", DWord.NIL);
        dEnv.put("true", DBool.TRUE);
        dEnv.put("false", DBool.FALSE);
    }

    /**
     * 注册基础数值计算相关过程，包括四则运算、平均值、取模和绝对值
     */
    private void initNumberProcedures(Env dEnv) {
        dEnv.addTokenProcedure("+", ((args, env) -> {
            if (!(args.get(0) instanceof DNumber))
                throw new DevoreCastException(args.get(0).type(), "number");
            DNumber number = (DNumber) args.get(0);
            return args.stream()
                    .skip(1)
                    .map(arg -> {
                        if (!(arg instanceof DNumber))
                            throw new DevoreCastException(arg.type(), "number");
                        return (DNumber) arg;
                    })
                    .reduce(number, DNumber::add);
        }), 1, true);
        dEnv.addTokenProcedure("-", ((args, env) -> {
            if (!(args.get(0) instanceof DNumber))
                throw new DevoreCastException(args.get(0).type(), "number");
            DNumber number = (DNumber) args.get(0);
            if (args.size() == 1)
                return DNumber.valueOf(number.toBigDecimal().negate());
            return args.stream()
                    .skip(1)
                    .map(arg -> {
                        if (!(arg instanceof DNumber))
                            throw new DevoreCastException(arg.type(), "number");
                        return (DNumber) arg;
                    })
                    .reduce(number, DNumber::sub);
        }), 1, true);
        dEnv.addTokenProcedure("*", ((args, env) -> {
            if (!(args.get(0) instanceof DNumber))
                throw new DevoreCastException(args.get(0).type(), "number");
            DNumber number = (DNumber) args.get(0);
            return args.stream()
                    .skip(1)
                    .map(arg -> {
                        if (!(arg instanceof DNumber))
                            throw new DevoreCastException(arg.type(), "number");
                        return (DNumber) arg;
                    })
                    .reduce(number, DNumber::mul);
        }), 1, true);
        dEnv.addTokenProcedure("/", ((args, env) -> {
            if (!(args.get(0) instanceof DNumber))
                throw new DevoreCastException(args.get(0).type(), "number");
            DNumber number = (DNumber) args.get(0);
            return args.stream()
                    .skip(1)
                    .map(arg -> {
                        if (!(arg instanceof DNumber))
                            throw new DevoreCastException(arg.type(), "number");
                        return (DNumber) arg;
                    })
                    .reduce(number, DNumber::div);
        }), 1, true);
        dEnv.addTokenProcedure("average", ((args, env) -> {
            DNumber num = args.stream()
                    .map(arg -> {
                        if (!(arg instanceof DNumber))
                            throw new DevoreCastException(arg.type(), "number");
                        return (DNumber) arg;
                    })
                    .reduce(DNumber.valueOf(0), DNumber::add);
            return num.div(DNumber.valueOf(args.size()));
        }), 1, true);
        dEnv.addTokenProcedure("mod", ((args, env) -> {
            if (!(args.get(0) instanceof DInt))
                throw new DevoreCastException(args.get(0).type(), "int");
            if (!(args.get(1) instanceof DInt))
                throw new DevoreCastException(args.get(1).type(), "int");
            if (args.get(1).equals(DNumber.valueOf(0)))
                throw new DevoreRuntimeException("模数不能为0, 被除数=" + args.get(0) + ", 模数=" + args.get(1));
            return DNumber.valueOf(((DInt) args.get(0)).toBigInteger().mod(((DInt) args.get(1)).toBigInteger()));
        }), 2, false);
        dEnv.addTokenProcedure("rem", ((args, env) -> {
            if (!(args.get(0) instanceof DInt))
                throw new DevoreCastException(args.get(0).type(), "int");
            if (!(args.get(1) instanceof DInt))
                throw new DevoreCastException(args.get(1).type(), "int");
            if (args.get(1).equals(DNumber.valueOf(0)))
                throw new DevoreRuntimeException("除数不能为0, 被除数=" + args.get(0) + ", 除数=" + args.get(1));
            return DNumber.valueOf(((DInt) args.get(0)).toBigInteger().remainder(((DInt) args.get(1)).toBigInteger()));
        }), 2, false);
        dEnv.addTokenProcedure("abs", ((args, env) -> {
            if (!(args.get(0) instanceof DNumber))
                throw new DevoreCastException(args.get(0).type(), "number");
            return ((DNumber) args.get(0)).abs();
        }), 1, false);
    }

    /**
     * 注册标准输入输出相关过程，包括print、println、read系列和换行
     */
    private void initIOProcedures(Env dEnv) {
        dEnv.addTokenProcedure("println", ((args, env) -> {
            env.io.out.println(args.stream().map(Object::toString).collect(Collectors.joining()));
            return DWord.NIL;
        }), 1, true);
        dEnv.addTokenProcedure("print", ((args, env) -> {
            env.io.out.print(args.stream().map(Object::toString).collect(Collectors.joining()));
            return DWord.NIL;
        }), 1, true);
        dEnv.addTokenProcedure("read-line", ((args, env) ->
                DString.valueOf(env.io.scanner.nextLine())), 0, false);
        dEnv.addTokenProcedure("read-int", ((args, env) ->
                DNumber.valueOf(env.io.scanner.nextBigInteger())), 0, false);
        dEnv.addTokenProcedure("read-float", ((args, env) ->
                DNumber.valueOf(env.io.scanner.nextBigDecimal())), 0, false);
        dEnv.addTokenProcedure("read-bool", ((args, env) ->
                DBool.valueOf(env.io.scanner.nextBoolean())), 0, false);
        dEnv.addTokenProcedure("read", ((args, env) ->
                DString.valueOf(env.io.scanner.next())), 0, false);
        dEnv.addTokenProcedure("newline", ((args, env) -> {
            env.io.out.println();
            return DWord.NIL;
        }), 0, false);
    }

    /**
     * 注册错误输出和主动抛出运行时错误的过程
     */
    private void initErrorProcedures(Env dEnv) {
        dEnv.addTokenProcedure("error-println", ((args, env) -> {
            env.io.err.println(args.stream().map(Object::toString).collect(Collectors.joining()));
            return DWord.NIL;
        }), 1, true);
        dEnv.addTokenProcedure("error-print", ((args, env) -> {
            env.io.err.print(args.stream().map(Object::toString).collect(Collectors.joining()));
            return DWord.NIL;
        }), 1, true);
        dEnv.addTokenProcedure("error", ((args, env) -> {
            throw new DevoreRuntimeException(args.stream().map(Object::toString).collect(Collectors.joining()));
        }), 1, true);
        dEnv.addTokenProcedure("error-newline", ((args, env) -> {
            env.io.err.println();
            return DWord.NIL;
        }), 0, false);
    }

    /**
     * 注册模块加载过程，用于导入并执行外部Devore文件
     */
    private void initModuleProcedures(Env dEnv) {
        dEnv.addAstProcedure("export", ((ast, env) ->
                DExport.valueOf(ast.children.stream().map(child -> {
            DToken key = child.symbol;
            if (child.type == Ast.Type.PROCEDURE)
                key = Evaluator.eval(env, child);
            if (!(key instanceof DSymbol))
                throw new DevoreCastException(key.type(), "symbol");
            return key.toString();
        }).collect(Collectors.toList()))), 1, true);
        dEnv.addTokenProcedure("import", ((args, env) -> {
            args.forEach(name -> {
                if (!(name instanceof DString))
                    throw new DevoreCastException(name.type(), "string");
                if (env.modules.containsKey(name.toString()))
                    env.loadModule(name.toString());
                else {
                    DSecurity.checkRestrictFile(env);
                    String file = name.toString();
                    Path path = Paths.get(file);
                    if (!Files.exists(path))
                        throw new DevoreRuntimeException("模块 " + name + " 不存在.");
                    try {
                        Env inEnvR = Env.newEnv();
                        inEnvR.setSecurity(DSecurity.inherited(env));
                        Env inEnv = inEnvR.createChild();
                        DToken temp = Devore.call(inEnv,
                                new String(Files.readAllBytes(path), StandardCharsets.UTF_8), file);
                        if (temp instanceof DExport)
                            ((DExport) temp).keys.forEach(key -> {
                                DToken token = inEnv.get(key);
                                if (token instanceof DProcedure) {
                                    DProcedure procedure = (DProcedure) token;
                                    env.addProcedure(key, (DProcedure) token);
                                    procedure.getChildren().forEach(p -> env.addProcedure(key, p));
                                    procedure.cleanChildren();
                                } else if (token instanceof DMacro) {
                                    DMacro macro = (DMacro) token;
                                    env.addMacro(key, (DMacro) token);
                                    macro.getChildren().forEach(p -> env.addMacro(key, p));
                                    macro.cleanChildren();
                                } else
                                    env.put(key, token);
                            });
                    } catch (IOException e) {
                        throw new DevoreRuntimeException("读取文件失败: " + file + ", " + e.getMessage());
                    }
                }
            });
            return DWord.NIL;
        }), 1, true);
    }

    /**
     * 注册定义和函数调用相关过程，包括def、lib、set!、宏、let、lambda、apply和act
     */
    private void initDefinitionProcedures(Env dEnv) {
        dEnv.addAstProcedure("undef", ((ast, env) -> {
            ast.children.stream()
                    .map(child -> {
                        DToken temp = child.symbol;
                        if (child.type == Ast.Type.PROCEDURE)
                            temp = Evaluator.eval(env, child.copy());
                        if (!(temp instanceof DSymbol))
                            throw new DevoreCastException(temp.type(), "symbol");
                        return temp.toString();
                    })
                    .forEach(env::remove);
            return DWord.NIL;
        }), 1, true);
        dEnv.addAstProcedure("def-macro", ((ast, env) -> {
            List<String> params = ast.get(0).children.stream()
                    .map(param -> {
                        DToken temp = param.symbol;
                        if (param.type == Ast.Type.PROCEDURE)
                            temp = Evaluator.eval(env, param.copy());
                        if (!(temp instanceof DSymbol))
                            throw new DevoreCastException(temp.type(), "symbol");
                        return temp.toString();
                    })
                    .collect(Collectors.toList());
            List<Ast> bodys = ast.children.subList(1, ast.size()).stream()
                    .map(Ast::copy)
                    .collect(Collectors.toList());
            DToken name = ast.get(0).symbol;
            if (ast.get(0).symbol instanceof Ast)
                name = Evaluator.eval(env, ((Ast) ast.get(0).symbol).copy());
            if (!(name instanceof DSymbol))
                throw new DevoreCastException(name.type(), "symbol");
            env.addMacro(name.toString(), params, bodys);
            return DWord.NIL;
        }), 2, true);
        dEnv.addAstProcedure("set-macro!", ((ast, env) -> {
            List<String> params = ast.get(0).children.stream()
                    .map(param -> {
                        DToken temp = param.symbol;
                        if (param.type == Ast.Type.PROCEDURE)
                            temp = Evaluator.eval(env, param.copy());
                        if (!(temp instanceof DSymbol))
                            throw new DevoreCastException(temp.type(), "symbol");
                        return temp.toString();
                    })
                    .collect(Collectors.toList());
            List<Ast> bodys = ast.children.subList(1, ast.size()).stream()
                    .map(Ast::copy)
                    .collect(Collectors.toList());
            DToken name = ast.get(0).symbol;
            if (ast.get(0).symbol instanceof Ast)
                name = Evaluator.eval(env, ((Ast) ast.get(0).symbol).copy());
            if (!(name instanceof DSymbol))
                throw new DevoreCastException(name.type(), "symbol");
            env.setMacro(name.toString(), params, bodys);
            return DWord.NIL;
        }), 2, true);
        dEnv.addAstProcedure("def", ((ast, env) -> {
            if (ast.get(0).isEmpty() && ast.get(0).type != Ast.Type.PROCEDURE) {
                if (!(ast.get(0).symbol instanceof DSymbol))
                    throw new DevoreCastException(ast.get(0).symbol.type(), "symbol");
                Env newEnv = env.createChild();
                DToken result = ast.children.subList(1, ast.size()).stream()
                        .map(node -> Evaluator.eval(newEnv, node.copy()))
                        .reduce((previous, current) -> current)
                        .orElse(DWord.NIL);
                env.put(ast.get(0).symbol.toString(), result);
            } else {
                List<String> params = ast.get(0).children.stream()
                        .map(param -> {
                            DToken temp = param.symbol;
                            if (param.type == Ast.Type.PROCEDURE)
                                temp = Evaluator.eval(env, param.copy());
                            if (!(temp instanceof DSymbol))
                                throw new DevoreCastException(temp.type(), "symbol");
                            return temp.toString();
                        })
                        .collect(Collectors.toList());
                List<Ast> nodes = ast.children.subList(1, ast.size()).stream()
                        .map(Ast::copy)
                        .collect(Collectors.toList());
                DToken name = ast.get(0).symbol;
                if (ast.get(0).symbol instanceof Ast)
                    name = Evaluator.eval(env, ((Ast) ast.get(0).symbol).copy());
                if (!(name instanceof DSymbol))
                    throw new DevoreCastException(name.type(), "symbol");
                env.addTokenProcedure(name.toString(), ((cArgs, cEnv) -> {
                    Env newEnv = env.createChild();
                    IntStream.range(0, params.size())
                            .forEach(i -> newEnv.put(params.get(i), cArgs.get(i)));
                    return nodes.stream()
                            .map(node -> Evaluator.eval(newEnv, node.copy()))
                            .reduce((previous, current) -> current)
                            .orElse(DWord.NIL);
                }), params.size(), false);
            }
            return DWord.NIL;
        }), 2, true);
        dEnv.addAstProcedure("set!", ((ast, env) -> {
            if (!(ast.get(0).symbol instanceof DSymbol))
                throw new DevoreCastException(ast.get(0).symbol.type(), "symbol");
            if (ast.get(0).isEmpty() && ast.get(0).type != Ast.Type.PROCEDURE) {
                Env newEnv = env.createChild();
                DToken result = ast.children.subList(1, ast.size()).stream()
                        .map(node -> Evaluator.eval(newEnv, node.copy()))
                        .reduce((previous, current) -> current)
                        .orElse(DWord.NIL);
                env.set(ast.get(0).symbol.toString(), result);
            } else {
                List<String> params = ast.get(0).children.stream()
                        .map(param -> {
                            DToken temp = param.symbol;
                            if (param.type == Ast.Type.PROCEDURE)
                                temp = Evaluator.eval(env, param.copy());
                            if (!(temp instanceof DSymbol))
                                throw new DevoreCastException(temp.type(), "symbol");
                            return temp.toString();
                        })
                        .collect(Collectors.toList());
                List<Ast> nodes = ast.children.subList(1, ast.size()).stream()
                        .map(Ast::copy)
                        .collect(Collectors.toList());
                DToken name = ast.get(0).symbol;
                if (ast.get(0).symbol instanceof Ast)
                    name = Evaluator.eval(env, ((Ast) ast.get(0).symbol).copy());
                if (!(name instanceof DSymbol))
                    throw new DevoreCastException(name.type(), "symbol");
                env.setTokenProcedure(name.toString(), ((cArgs, cEnv) -> {
                    Env newEnv = env.createChild();
                    IntStream.range(0, params.size())
                            .forEach(i -> newEnv.put(params.get(i), cArgs.get(i)));
                    return nodes.stream()
                            .map(node -> Evaluator.eval(newEnv, node.copy()))
                            .reduce((previous, current) -> current)
                            .orElse(DWord.NIL);
                }), params.size(), false);
            }
            return DWord.NIL;
        }), 2, true);
        dEnv.addAstProcedure("let", ((ast, env) -> {
            Env newEnv = env.createChild();
            List<Ast> nodes = new ArrayList<>(ast.get(0).children);
            if (ast.get(0).symbol instanceof Ast && !isEmptyProcedure(ast.get(0)))
                nodes.add(0, (Ast) ast.get(0).symbol);
            nodes.forEach(node -> {
                DToken name = node.symbol;
                if (node.symbol instanceof Ast)
                    name = Evaluator.eval(env, ((Ast) node.symbol).copy());
                if (!(name instanceof DSymbol))
                    throw new DevoreCastException(name.type(), "symbol");
                if (node.children.size() != 1)
                    throw new DevoreRuntimeException("绑定的内容必须只有一个值.");
                newEnv.put(name.toString(), Evaluator.eval(newEnv, node.children.get(0).copy()));
            });
            return ast.children.subList(1, ast.size()).stream()
                    .map(node -> Evaluator.eval(newEnv, node.copy()))
                    .reduce((previous, current) -> current)
                    .orElse(DWord.NIL);
        }), 2, true);
        dEnv.addAstProcedure("lambda", ((ast, env) -> {
            Ast paramsAst = ast.get(0);
            boolean hasParams = paramsAst.isNotNil() && !isEmptyProcedure(paramsAst);
            List<String> params = hasParams
                    ? Stream.concat(Stream.of(ast.get(0)), ast.get(0).children.stream())
                    .map(param -> {
                        DToken temp = param.symbol;
                        if (param == paramsAst && param.symbol instanceof Ast)
                            temp = Evaluator.eval(env, ((Ast) param.symbol).copy());
                        if (param != paramsAst && param.type == Ast.Type.PROCEDURE)
                            temp = Evaluator.eval(env, param.copy());
                        if (!(temp instanceof DSymbol))
                            throw new DevoreCastException(temp.type(), "symbol");
                        return temp.toString();
                    })
                    .collect(Collectors.toList())
                    : Collections.emptyList();
            List<Ast> node = ast.children.subList(1, ast.size()).stream()
                    .map(Ast::copy)
                    .collect(Collectors.toList());
            BiFunction<Ast, Env, DToken> df = (inAst, inEnv) -> {
                List<DToken> args = IntStream.range(0, inAst.size())
                        .mapToObj(i -> {
                            inAst.get(i).symbol = Evaluator.eval(inEnv, inAst.get(i).copy());
                            return inAst.get(i).symbol;
                        })
                        .collect(Collectors.toList());
                Env newInEnv = env.createChild();
                IntStream.range(0, params.size())
                        .forEach(i -> newInEnv.put(params.get(i), args.get(i)));
                return node.stream()
                        .map(temp -> Evaluator.eval(newInEnv, temp.copy()))
                        .reduce((previous, current) -> current)
                        .orElse(DWord.NIL);
            };
            return DProcedure.newProcedure(df, params.size(), false);
        }), 2, true);
        dEnv.addTokenProcedure("apply", ((args, env) -> {
            if (!(args.get(0) instanceof DProcedure))
                throw new DevoreCastException(args.get(0).type(), "procedure");
            return ((DProcedure) args.get(0)).call(args.subList(1, args.size()), env);
        }), 1, true);
        dEnv.addTokenProcedure("act", ((args, env) -> {
            if (!(args.get(0) instanceof DProcedure))
                throw new DevoreCastException(args.get(0).type(), "procedure");
            if (!(args.get(1) instanceof DList))
                throw new DevoreCastException(args.get(0).type(), "list");
            return ((DProcedure) args.get(0)).call(((DList) args.get(1)).toList(), env);
        }), 2, false);
    }

    /**
     * 注册通用比较过程，包括大小比较、相等和不等
     */
    private void initComparisonProcedures(Env dEnv) {
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
    }

    /**
     * 注册控制流过程，包括条件分支、异常捕获、begin和while
     */
    private void initControlProcedures(Env dEnv) {
        dEnv.addAstProcedure("unless", (ast, env) -> {
            DToken condition = Evaluator.eval(env, ast.get(0).copy());
            if (!(condition instanceof DBool))
                throw new DevoreCastException(condition.type(), "bool");
            if (!((DBool) condition).bool)
                return ast.children.subList(1, ast.size()).stream()
                        .map(node -> Evaluator.eval(env, node.copy()))
                        .reduce((previous, current) -> current)
                        .orElse(DWord.NIL);
            return DWord.NIL;
        }, 2, true);
        dEnv.addAstProcedure("when", (ast, env) -> {
            DToken condition = Evaluator.eval(env, ast.get(0).copy());
            if (!(condition instanceof DBool))
                throw new DevoreCastException(condition.type(), "bool");
            if (((DBool) condition).bool)
                return ast.children.subList(1, ast.size()).stream()
                        .map(node -> Evaluator.eval(env, node.copy()))
                        .reduce((previous, current) -> current)
                        .orElse(DWord.NIL);
            return DWord.NIL;
        }, 2, true);
        dEnv.addAstProcedure("if", (ast, env) -> {
            DToken condition = Evaluator.eval(env, ast.get(0).copy());
            if (!(condition instanceof DBool))
                throw new DevoreCastException(condition.type(), "bool");
            return ((DBool) condition).bool ? Evaluator.eval(env, ast.get(1).copy()) : DWord.NIL;
        }, 2, false);
        dEnv.addAstProcedure("if", (ast, env) -> {
            DToken condition = Evaluator.eval(env, ast.get(0).copy());
            if (!(condition instanceof DBool))
                throw new DevoreCastException(condition.type(), "bool");
            return ((DBool) condition).bool ? Evaluator.eval(env, ast.get(1).copy())
                    : Evaluator.eval(env, ast.get(2).copy());
        }, 3, false);
        dEnv.addAstProcedure("cond", (ast, env) -> ast.children.stream()
                .filter(node -> {
                    if (node.symbol instanceof DSymbol && "else".equals(node.symbol.toString()))
                        return true;
                    DToken condition = Evaluator.eval(env, new Ast(node.symbol).copy());
                    if (!(condition instanceof DBool))
                        throw new DevoreCastException(condition.type(), "bool");
                    return ((DBool) condition).bool;
                })
                .findFirst()
                .map(node -> node.children.stream()
                        .map(body -> Evaluator.eval(env, body.copy()))
                        .reduce((previous, current) -> current)
                        .orElse(DWord.NIL))
                .orElse(DWord.NIL), 2, true);
        dEnv.addAstProcedure("try", (ast, env) -> {
            Ast catchNode = ast.get(ast.size() - 1);
            if (!(catchNode.symbol instanceof DSymbol && "catch".equals(catchNode.symbol.toString())))
                throw new DevoreRuntimeException("try要求最后一个表达式为catch子句.");
            if (catchNode.size() < 2)
                throw new DevoreRuntimeException("catch子句必须包含错误变量和处理表达式.");
            if (!(catchNode.get(0).symbol instanceof DSymbol) || !catchNode.get(0).isEmpty())
                throw new DevoreCastException(catchNode.get(0).symbol.type(), "symbol");
            try {
                return ast.children.subList(0, ast.size() - 1).stream()
                        .map(node -> Evaluator.eval(env, node.copy()))
                        .reduce((previous, current) -> current)
                        .orElse(DWord.NIL);
            } catch (DevoreRuntimeException e) {
                Env catchEnv = env.createChild();
                catchEnv.put(catchNode.get(0).symbol.toString(), DString.valueOf(e.getMessage()));
                return catchNode.children.subList(1, catchNode.size()).stream()
                        .map(node -> Evaluator.eval(catchEnv, node.copy()))
                        .reduce((previous, current) -> current)
                        .orElse(DWord.NIL);
            }
        }, 2, true);
        dEnv.addTokenProcedure("begin", (arg, env) -> arg.get(arg.size() - 1), 1, true);
        dEnv.addAstProcedure("while", (ast, env) -> {
            DToken condition = Evaluator.eval(env, ast.get(0).copy());
            if (!(condition instanceof DBool))
                throw new DevoreCastException(condition.type(), "bool");
            DToken result = DWord.NIL;
            while (((DBool) condition).bool) {
                result = ast.children.subList(1, ast.size()).stream()
                        .map(node -> Evaluator.eval(env, node.copy()))
                        .reduce((previous, current) -> current)
                        .orElse(DWord.NIL);
                condition = Evaluator.eval(env, ast.get(0).copy());
                if (!(condition instanceof DBool))
                    throw new DevoreCastException(condition.type(), "bool");
            }
            return result;
        }, 2, true);
    }

    /**
     * 注册布尔逻辑和随机数相关过程
     */
    private void initLogicAndRandomProcedures(Env dEnv) {
        dEnv.addTokenProcedure("and", ((args, env) -> DBool.valueOf(args.stream()
                .map(arg -> {
                    if (!(arg instanceof DBool))
                        throw new DevoreCastException(arg.type(), "bool");
                    return (DBool) arg;
                })
                .allMatch(arg -> arg.bool))), 1, true);
        dEnv.addTokenProcedure("or", ((args, env) -> DBool.valueOf(args.stream()
                .map(arg -> {
                    if (!(arg instanceof DBool))
                        throw new DevoreCastException(arg.type(), "bool");
                    return (DBool) arg;
                })
                .anyMatch(arg -> arg.bool))), 1, true);
        dEnv.addTokenProcedure("not", ((args, env) -> {
            if (!(args.get(0) instanceof DBool))
                throw new DevoreCastException(args.get(0).type(), "bool");
            return ((DBool) args.get(0)).bool ? DBool.FALSE : DBool.TRUE;
        }), 1, false);
        dEnv.addTokenProcedure("random", ((args, env) -> {
            if (!(args.get(0) instanceof DInt))
                throw new DevoreCastException(args.get(0).type(), "int");
            BigInteger start = BigInteger.ZERO;
            BigInteger end = ((DInt) args.get(0)).toBigInteger().subtract(BigInteger.ONE);
            Random rand = new Random();
            BigInteger range = end.subtract(start).add(BigInteger.ONE);
            BigInteger randomValue;
            do {
                randomValue = new BigInteger(range.bitLength(), rand);
            } while (randomValue.compareTo(range) >= 0);
            return DNumber.valueOf(randomValue.add(start));
        }), 1, false);
        dEnv.addTokenProcedure("random", ((args, env) -> {
            if (!(args.get(0) instanceof DInt))
                throw new DevoreCastException(args.get(0).type(), "int");
            if (!(args.get(1) instanceof DInt))
                throw new DevoreCastException(args.get(1).type(), "int");
            BigInteger start = ((DInt) args.get(0)).toBigInteger();
            BigInteger end = ((DInt) args.get(1)).toBigInteger().subtract(BigInteger.ONE);
            Random rand = new Random();
            BigInteger range = end.subtract(start).add(BigInteger.ONE);
            BigInteger randomValue;
            do {
                randomValue = new BigInteger(range.bitLength(), rand);
            } while (randomValue.compareTo(range) >= 0);
            return DNumber.valueOf(randomValue.add(start));
        }), 2, false);
    }

    /**
     * 注册列表、序列和高阶遍历相关过程，包括list、map、fold、filter和range
     */
    private void initListProcedures(Env dEnv) {
        dEnv.addTokenProcedure("list", ((args, env) ->
                DList.valueOf(new ArrayList<>(args))), 0, true);
        dEnv.addTokenProcedure("list-contains", ((args, env) -> {
            if (!(args.get(0) instanceof DList))
                throw new DevoreCastException(args.get(0).type(), "list");
            return DBool.valueOf(((DList) args.get(0)).contains(args.get(1)));
        }), 2, false);
        dEnv.addTokenProcedure("list-clear!", ((args, env) -> {
            if (!(args.get(0) instanceof DList))
                throw new DevoreCastException(args.get(0).type(), "list");
            ((DList) args.get(0)).clear();
            return args.get(0);
        }), 1, false);
        dEnv.addTokenProcedure("list-index", ((args, env) -> {
            if (!(args.get(0) instanceof DList))
                throw new DevoreCastException(args.get(0).type(), "list");
            return DNumber.valueOf(((DList) args.get(0)).indexOf(args.get(1)));
        }), 2, false);
        dEnv.addTokenProcedure("list-index-last", ((args, env) -> {
            if (!(args.get(0) instanceof DList))
                throw new DevoreCastException(args.get(0).type(), "list");
            return DNumber.valueOf(((DList) args.get(0)).lastIndexOf(args.get(1)));
        }), 2, false);
        dEnv.addTokenProcedure("list-get", ((args, env) -> {
            if (!(args.get(0) instanceof DList))
                throw new DevoreCastException(args.get(0).type(), "list");
            if (!(args.get(1) instanceof DInt))
                throw new DevoreCastException(args.get(1).type(), "int");
            return ((DList) args.get(0)).get(DIntUtils.toIndex((DInt) args.get(1)));
        }), 2, false);
        dEnv.addTokenProcedure("list-set", ((args, env) -> {
            if (!(args.get(0) instanceof DList))
                throw new DevoreCastException(args.get(0).type(), "list");
            if (!(args.get(1) instanceof DInt))
                throw new DevoreCastException(args.get(1).type(), "int");
            return ((DList) args.get(0)).set(DIntUtils.toIndex((DInt) args.get(1)), args.get(2), false);
        }), 3, false);
        dEnv.addTokenProcedure("list-remove", ((args, env) -> {
            if (!(args.get(0) instanceof DList))
                throw new DevoreCastException(args.get(0).type(), "list");
            if (!(args.get(1) instanceof DInt))
                throw new DevoreCastException(args.get(1).type(), "int");
            return ((DList) args.get(0)).remove(DIntUtils.toIndex((DInt) args.get(1)), false);
        }), 2, false);
        dEnv.addTokenProcedure("list-add", ((args, env) -> {
            if (!(args.get(0) instanceof DList))
                throw new DevoreCastException(args.get(0).type(), "list");
            return ((DList) args.get(0)).add(args.get(1), false);
        }), 2, false);
        dEnv.addTokenProcedure("list-add", ((args, env) -> {
            if (!(args.get(0) instanceof DList))
                throw new DevoreCastException(args.get(0).type(), "list");
            if (!(args.get(1) instanceof DInt))
                throw new DevoreCastException(args.get(0).type(), "int");
            return ((DList) args.get(0)).add(DIntUtils.toIndex((DInt) args.get(1)), args.get(2), false);
        }), 3, false);
        dEnv.addTokenProcedure("list-set!", ((args, env) -> {
            if (!(args.get(0) instanceof DList))
                throw new DevoreCastException(args.get(0).type(), "list");
            if (!(args.get(1) instanceof DInt))
                throw new DevoreCastException(args.get(1).type(), "int");
            return ((DList) args.get(0)).set(DIntUtils.toIndex((DInt) args.get(1)), args.get(2), true);
        }), 3, false);
        dEnv.addTokenProcedure("list-remove!", ((args, env) -> {
            if (!(args.get(0) instanceof DList))
                throw new DevoreCastException(args.get(0).type(), "list");
            if (!(args.get(1) instanceof DInt))
                throw new DevoreCastException(args.get(1).type(), "int");
            return ((DList) args.get(0)).remove(DIntUtils.toIndex((DInt) args.get(1)), true);
        }), 2, false);
        dEnv.addTokenProcedure("list-add!", ((args, env) -> {
            if (!(args.get(0) instanceof DList))
                throw new DevoreCastException(args.get(0).type(), "list");
            return ((DList) args.get(0)).add(args.get(1), true);
        }), 2, false);
        dEnv.addTokenProcedure("list-add!", ((args, env) -> {
            if (!(args.get(0) instanceof DList))
                throw new DevoreCastException(args.get(0).type(), "list");
            if (!(args.get(1) instanceof DInt))
                throw new DevoreCastException(args.get(0).type(), "int");
            return ((DList) args.get(0)).add(DIntUtils.toIndex((DInt) args.get(1)), args.get(2), true);
        }), 3, false);
        dEnv.addTokenProcedure("head", ((args, env) -> {
            if (!(args.get(0) instanceof DList))
                throw new DevoreCastException(args.get(0).type(), "list");
            return ((DList) args.get(0)).get(0);
        }), 1, false);
        dEnv.addTokenProcedure("last", ((args, env) -> {
            if (!(args.get(0) instanceof DList))
                throw new DevoreCastException(args.get(0).type(), "list");
            DList list = (DList) args.get(0);
            return list.get(list.size() - 1);
        }), 1, false);
        dEnv.addTokenProcedure("tail", ((args, env) -> {
            if (!(args.get(0) instanceof DList))
                throw new DevoreCastException(args.get(0).type(), "list");
            DList list = (DList) args.get(0);
            return list.subList(1, list.size(), false);
        }), 1, false);
        dEnv.addTokenProcedure("init", ((args, env) -> {
            if (!(args.get(0) instanceof DList))
                throw new DevoreCastException(args.get(0).type(), "list");
            DList list = (DList) args.get(0);
            return list.subList(0, list.size() - 1, false);
        }), 1, false);
        dEnv.addTokenProcedure("length", ((args, env) -> {
            if (args.get(0) instanceof DList)
                return DNumber.valueOf(((DList) args.get(0)).size());
            if (args.get(0) instanceof DTable)
                return DNumber.valueOf(((DTable) args.get(0)).size());
            return DNumber.valueOf(args.get(0).toString().length());
        }), 1, false);
        dEnv.addTokenProcedure("list-sub", ((args, env) -> {
            if (!(args.get(0) instanceof DList))
                throw new DevoreCastException(args.get(0).type(), "list");
            if (!(args.get(1) instanceof DInt))
                throw new DevoreCastException(args.get(0).type(), "int");
            if (!(args.get(2) instanceof DInt))
                throw new DevoreCastException(args.get(0).type(), "int");
            return ((DList) args.get(0)).subList(DIntUtils.toIndex((DInt) args.get(1)),
                    DIntUtils.toIndex((DInt) args.get(2)), false);
        }), 3, false);
        dEnv.addTokenProcedure("list-sub!", ((args, env) -> {
            if (!(args.get(0) instanceof DList))
                throw new DevoreCastException(args.get(0).type(), "list");
            if (!(args.get(1) instanceof DInt))
                throw new DevoreCastException(args.get(0).type(), "int");
            if (!(args.get(2) instanceof DInt))
                throw new DevoreCastException(args.get(0).type(), "int");
            return ((DList) args.get(0)).subList(DIntUtils.toIndex((DInt) args.get(1)),
                    DIntUtils.toIndex((DInt) args.get(2)), true);
        }), 3, false);
        dEnv.addTokenProcedure("reverse", ((args, env) -> {
            if (!(args.get(0) instanceof DList))
                throw new DevoreCastException(args.get(0).type(), "list");
            return ((DList) args.get(0)).reverse(false);
        }), 1, false);
        dEnv.addTokenProcedure("reverse!", ((args, env) -> {
            if (!(args.get(0) instanceof DList))
                throw new DevoreCastException(args.get(0).type(), "list");
            return ((DList) args.get(0)).reverse(true);
        }), 1, false);
        dEnv.addTokenProcedure("sort", ((args, env) -> {
            if (!(args.get(0) instanceof DList))
                throw new DevoreCastException(args.get(0).type(), "list");
            return ((DList) args.get(0)).sort(false);
        }), 1, false);
        dEnv.addTokenProcedure("sort!", ((args, env) -> {
            if (!(args.get(0) instanceof DList))
                throw new DevoreCastException(args.get(0).type(), "list");
            return ((DList) args.get(0)).sort(true);
        }), 1, true);
        dEnv.addTokenProcedure("++", ((args, env) -> {
            boolean flag = args.stream().anyMatch(arg -> arg instanceof DList);
            DToken result;
            if (flag) {
                List<DToken> list = args.stream()
                        .flatMap(arg -> arg instanceof DList ? ((DList) arg).toList().stream() : Stream.of(arg))
                        .collect(Collectors.toList());
                result = DList.valueOf(list);
            } else {
                result = DString.valueOf(args.stream().map(Object::toString).collect(Collectors.joining()));
            }
            return result;
        }), 1, true);
        dEnv.addTokenProcedure("map", ((args, env) -> {
            if (!(args.get(0) instanceof DProcedure))
                throw new DevoreCastException(args.get(0).type(), "procedure");
            if (!(args.get(1) instanceof DList))
                throw new DevoreCastException(args.get(1).type(), "list");
            List<DToken> tokens = ((DList) args.get(1)).toList();
            List<DToken> result = IntStream.range(0, tokens.size())
                    .mapToObj(i -> {
                        List<DToken> params = Stream.concat(Stream.of(tokens.get(i)),
                                        IntStream.range(1, args.size() - 1)
                                                .mapToObj(j -> {
                                                    if (!(args.get(j + 1) instanceof DList))
                                                        throw new DevoreCastException(args.get(j + 1).type(), "list");
                                                    return ((DList) args.get(j + 1)).get(i);
                                                }))
                                .collect(Collectors.toList());
                        return ((DProcedure) args.get(0)).call(params, env.createChild());
                    })
                    .collect(Collectors.toList());
            return DList.valueOf(result);
        }), 2, true);
        dEnv.addTokenProcedure("for-each", ((args, env) -> {
            if (!(args.get(0) instanceof DProcedure))
                throw new DevoreCastException(args.get(0).type(), "procedure");
            if (!(args.get(1) instanceof DList))
                throw new DevoreCastException(args.get(1).type(), "list");
            List<DToken> tokens = ((DList) args.get(1)).toList();
            IntStream.range(0, tokens.size())
                    .forEach(i -> {
                        List<DToken> params = Stream.concat(Stream.of(tokens.get(i)),
                                        IntStream.range(1, args.size() - 1)
                                                .mapToObj(j -> {
                                                    if (!(args.get(j + 1) instanceof DList))
                                                        throw new DevoreCastException(args.get(j + 1).type(), "list");
                                                    return ((DList) args.get(j + 1)).get(i);
                                                }))
                                .collect(Collectors.toList());
                        ((DProcedure) args.get(0)).call(params, env.createChild());
                    });
            return DWord.NIL;
        }), 2, true);
        dEnv.addTokenProcedure("foldr", ((args, env) -> {
            if (!(args.get(0) instanceof DProcedure))
                throw new DevoreCastException(args.get(0).type(), "procedure");
            if (!(args.get(2) instanceof DList))
                throw new DevoreCastException(args.get(2).type(), "list");
            List<DToken> tokens = ((DList) args.get(2)).toList();
            return IntStream.range(0, tokens.size())
                    .mapToObj(i -> tokens.get(tokens.size() - i - 1))
                    .reduce(args.get(1),
                            (result, token) -> ((DProcedure) args.get(0)).call(Arrays.asList(token, result), env.createChild()),
                            (left, right) -> right);
        }), 3, false);
        dEnv.addTokenProcedure("foldl", ((args, env) -> {
            if (!(args.get(0) instanceof DProcedure))
                throw new DevoreCastException(args.get(0).type(), "procedure");
            if (!(args.get(2) instanceof DList))
                throw new DevoreCastException(args.get(2).type(), "list");
            List<DToken> tokens = ((DList) args.get(2)).toList();
            return tokens.stream()
                    .reduce(args.get(1),
                            (result, token) -> ((DProcedure) args.get(0)).call(Arrays.asList(result, token), env.createChild()),
                            (left, right) -> right);
        }), 3, false);
        dEnv.addTokenProcedure("filter", ((args, env) -> {
            if (!(args.get(0) instanceof DProcedure))
                throw new DevoreCastException(args.get(0).type(), "procedure");
            if (!(args.get(1) instanceof DList))
                throw new DevoreCastException(args.get(1).type(), "list");
            List<DToken> tokens = ((DList) args.get(1)).toList();
            List<DToken> result = tokens.stream()
                    .filter(token -> {
                        DToken condition = ((DProcedure) args.get(0)).call(Collections.singletonList(token), env.createChild());
                        if (!(condition instanceof DBool))
                            throw new DevoreCastException(condition.type(), "list");
                        return ((DBool) condition).bool;
                    })
                    .collect(Collectors.toList());
            return DList.valueOf(result);
        }), 2, false);
        dEnv.addTokenProcedure("range", ((args, env) -> {
            if (!(args.get(0) instanceof DNumber))
                throw new DevoreCastException(args.get(0).type(), "number");
            BigDecimal start = BigDecimal.ZERO;
            BigDecimal target = ((DNumber) args.get(0)).toBigDecimal();
            BigDecimal step = start.compareTo(target) <= 0 ? BigDecimal.ONE : BigDecimal.ONE.negate();
            BigDecimal end = target.subtract(step);
            BigDecimal distance = end.subtract(start);
            if (distance.compareTo(BigDecimal.ZERO) != 0 && distance.signum() != step.signum())
                return DList.valueOf(Collections.emptyList());
            long count = distance.abs().divideToIntegralValue(step.abs()).longValue() + 1;
            return DList.valueOf(Stream.iterate(start, current -> current.add(step))
                    .limit(count)
                    .map(DNumber::valueOf)
                    .collect(Collectors.toList()));
        }), 1, false);
        dEnv.addTokenProcedure("range", ((args, env) -> {
            if (!(args.get(0) instanceof DNumber))
                throw new DevoreCastException(args.get(0).type(), "number");
            if (!(args.get(1) instanceof DNumber))
                throw new DevoreCastException(args.get(1).type(), "number");
            BigDecimal start = ((DNumber) args.get(0)).toBigDecimal();
            BigDecimal target = ((DNumber) args.get(1)).toBigDecimal();
            BigDecimal step = start.compareTo(target) <= 0 ? BigDecimal.ONE : BigDecimal.ONE.negate();
            BigDecimal end = target.subtract(step);
            BigDecimal distance = end.subtract(start);
            if (distance.compareTo(BigDecimal.ZERO) != 0 && distance.signum() != step.signum())
                return DList.valueOf(Collections.emptyList());
            long count = distance.abs().divideToIntegralValue(step.abs()).longValue() + 1;
            return DList.valueOf(Stream.iterate(start, current -> current.add(step))
                    .limit(count)
                    .map(DNumber::valueOf)
                    .collect(Collectors.toList()));
        }), 2, false);
        dEnv.addTokenProcedure("range", ((args, env) -> {
            if (!(args.get(0) instanceof DNumber))
                throw new DevoreCastException(args.get(0).type(), "number");
            if (!(args.get(1) instanceof DNumber))
                throw new DevoreCastException(args.get(1).type(), "number");
            if (!(args.get(2) instanceof DNumber))
                throw new DevoreCastException(args.get(2).type(), "number");
            BigDecimal start = ((DNumber) args.get(0)).toBigDecimal();
            BigDecimal target = ((DNumber) args.get(1)).toBigDecimal();
            BigDecimal step = ((DNumber) args.get(2)).toBigDecimal();
            if (step.compareTo(BigDecimal.ZERO) == 0)
                throw new DevoreRuntimeException("步长不能为零, start=" + start.toPlainString()
                        + ", end=" + target.toPlainString()
                        + ", step=" + step.toPlainString());
            BigDecimal actualStep = start.compareTo(target) <= 0 ? step.abs() : step.abs().negate();
            BigDecimal end = target.subtract(actualStep);
            BigDecimal distance = end.subtract(start);
            if (distance.compareTo(BigDecimal.ZERO) != 0 && distance.signum() != actualStep.signum())
                return DList.valueOf(Collections.emptyList());
            long count = distance.abs().divideToIntegralValue(actualStep.abs()).longValue() + 1;
            return DList.valueOf(Stream.iterate(start, current -> current.add(actualStep))
                    .limit(count)
                    .map(DNumber::valueOf)
                    .collect(Collectors.toList()));
        }), 3, false);
    }

    /**
     * 注册类型转换过程，包括字符串、符号、数字、布尔、列表和Unicode字符转换
     */
    private void initConversionProcedures(Env dEnv) {
        dEnv.addTokenProcedure("string->symbol", ((args, env) -> {
            if (!(args.get(0) instanceof DString))
                throw new DevoreCastException(args.get(0).type(), "string");
            return DSymbol.valueOf(args.get(0).toString());
        }), 1, false);
        dEnv.addTokenProcedure("string->number", ((args, env) -> {
            if (!(args.get(0) instanceof DString))
                throw new DevoreCastException(args.get(0).type(), "string");
            return DNumber.valueOf(new BigDecimal(args.get(0).toString()));
        }), 1, false);
        dEnv.addTokenProcedure("string->bool", ((args, env) -> {
            if (!(args.get(0) instanceof DString))
                throw new DevoreCastException(args.get(0).type(), "string");
            return "true".equals(args.get(0).toString()) ? DBool.TRUE : DBool.FALSE;
        }), 1, false);
        dEnv.addTokenProcedure("->string", ((args, env) ->
                DString.valueOf(args.get(0).toString())), 1, false);
        dEnv.addTokenProcedure("string->list", ((args, env) -> {
            if (!(args.get(0) instanceof DString))
                throw new DevoreCastException(args.get(0).type(), "string");
            List<DToken> tokens = args.get(0).toString().chars()
                    .mapToObj(c -> DString.valueOf(String.valueOf((char) c)))
                    .collect(Collectors.toList());
            return DList.valueOf(tokens);
        }), 1, false);
        dEnv.addTokenProcedure("char->unicode", ((args, env) -> {
            if (!(args.get(0) instanceof DString))
                throw new DevoreCastException(args.get(0).type(), "string");
            return DNumber.valueOf((int) args.get(0).toString().charAt(0));
        }), 1, false);
        dEnv.addTokenProcedure("unicode->char", ((args, env) -> {
            if (!(args.get(0) instanceof DInt))
                throw new DevoreCastException(args.get(0).type(), "int");
            return DString.valueOf(String.valueOf((char) DIntUtils.toInt((DInt) args.get(0))));
        }), 1, false);
    }

    /**
     * 注册系统级过程，包括退出、休眠、取类型和当前时间戳
     */
    private void initSystemProcedures(Env dEnv) {
        dEnv.addTokenProcedure("exit", ((args, env) -> {
            DSecurity.checkRestrictExec(env);
            if (!(args.get(0) instanceof DInt))
                throw new DevoreCastException(args.get(0).type(), "int");
            System.exit(DIntUtils.toInt((DInt) args.get(0)));
            return DWord.NIL;
        }), 1, false);
        dEnv.addTokenProcedure("sleep", ((args, env) -> {
            if (!(args.get(0) instanceof DInt))
                throw new DevoreCastException(args.get(0).type(), "int");
            try {
                Thread.sleep(DIntUtils.toLong((DInt) args.get(0)));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new DevoreRuntimeException("线程休眠被中断.");
            }
            return DWord.NIL;
        }), 1, false);
        dEnv.addTokenProcedure("type", ((args, env) ->
                DString.valueOf(args.get(0).type())), 1, false);
        dEnv.addTokenProcedure("time", ((args, env) ->
                DNumber.valueOf(System.currentTimeMillis())), 0, false);
    }

    /**
     * 注册表结构相关过程，包括创建、查询、写入、删除和获取键列表
     */
    private void initTableProcedures(Env dEnv) {
        dEnv.addAstProcedure("table", ((ast, env) -> {
            Map<DToken, DToken> table = IntStream.range(0, ast.size())
                    .mapToObj(ast::get)
                    .peek(pair -> {
                        if (pair.type != Ast.Type.PROCEDURE || pair.symbol == DWord.NIL || pair.size() != 1)
                            throw new DevoreRuntimeException("table参数必须为键值对: [key value]");
                    })
                    .collect(Collectors.toMap(
                            pair -> {
                                Ast key = pair.symbol instanceof Ast ? ((Ast) pair.symbol).copy() : new Ast(pair.symbol);
                                return Evaluator.eval(env, key);
                            },
                            pair -> Evaluator.eval(env, pair.get(0).copy()),
                            (left, right) -> right,
                            HashMap::new));
            return DTable.valueOf(table);
        }), 0, true);
        dEnv.addTokenProcedure("table-get", ((args, env) -> {
            if (!(args.get(0) instanceof DTable))
                throw new DevoreCastException(args.get(0).type(), "table");
            return ((DTable) args.get(0)).get(args.get(1));
        }), 2, false);
        dEnv.addTokenProcedure("table-contains-key", ((args, env) -> {
            if (!(args.get(0) instanceof DTable))
                throw new DevoreCastException(args.get(0).type(), "table");
            return ((DTable) args.get(0)).containsKey(args.get(1));
        }), 2, false);
        dEnv.addTokenProcedure("table-contains-value", ((args, env) -> {
            if (!(args.get(0) instanceof DTable))
                throw new DevoreCastException(args.get(0).type(), "table");
            return ((DTable) args.get(0)).containsValue(args.get(1));
        }), 2, false);
        dEnv.addTokenProcedure("table-clear!", ((args, env) -> {
            if (!(args.get(0) instanceof DTable))
                throw new DevoreCastException(args.get(0).type(), "table");
            ((DTable) args.get(0)).clear();
            return args.get(0);
        }), 1, false);
        dEnv.addTokenProcedure("table-put", ((args, env) -> {
            if (!(args.get(0) instanceof DTable))
                throw new DevoreCastException(args.get(0).type(), "table");
            return ((DTable) args.get(0)).put(args.get(1), args.get(2), false);
        }), 3, false);
        dEnv.addTokenProcedure("table-put!", ((args, env) -> {
            if (!(args.get(0) instanceof DTable))
                throw new DevoreCastException(args.get(0).type(), "table");
            return ((DTable) args.get(0)).put(args.get(1), args.get(2), true);
        }), 3, false);
        dEnv.addTokenProcedure("table-remove", ((args, env) -> {
            if (!(args.get(0) instanceof DTable))
                throw new DevoreCastException(args.get(0).type(), "table");
            return ((DTable) args.get(0)).remove(args.get(1), false);
        }), 2, false);
        dEnv.addTokenProcedure("table-remove!", ((args, env) -> {
            if (!(args.get(0) instanceof DTable))
                throw new DevoreCastException(args.get(0).type(), "table");
            return ((DTable) args.get(0)).remove(args.get(1), true);
        }), 2, false);
        dEnv.addTokenProcedure("table-keys", ((args, env) -> {
            if (!(args.get(0) instanceof DTable))
                throw new DevoreCastException(args.get(0).type(), "table");
            return DList.valueOf(new ArrayList<>(((DTable) args.get(0)).keys()));
        }), 1, false);
    }

    /**
     * 注册聚合比较过程，如max和min
     */
    private void initAggregateProcedures(Env dEnv) {
        dEnv.addTokenProcedure("max", ((args, env) -> args.stream()
                .max(DToken::compareTo).orElse(args.get(0))), 1, true);
        dEnv.addTokenProcedure("min", ((args, env) -> args.stream()
                .min(DToken::compareTo).orElse(args.get(0))), 1, true);
    }

    /**
     * 注册类型、绑定状态和特殊值判断过程，如bound?、number?、list?、nil?和zero?
     */
    private void initPredicateProcedures(Env dEnv) {
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
        dEnv.addAstProcedure("bound?", ((ast, env) -> {
            Ast arg = ast.get(0);
            if (arg.isEmpty() && arg.symbol instanceof DSymbol)
                return DBool.valueOf(env.contains(arg.symbol.toString()));
            DToken token = Evaluator.eval(env, arg.copy());
            if (!(token instanceof DSymbol))
                throw new DevoreCastException(token.type(), "symbol");
            return DBool.valueOf(env.contains(token.toString()));
        }), 1, false);
        dEnv.addTokenProcedure("export?", ((args, env) ->
                DBool.valueOf(args.get(0) instanceof DExport)), 1, false);
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

    /**
     * 注册字符串处理过程，包括拆分、裁剪、大小写、替换、匹配、索引和截取
     */
    private void initStringProcedures(Env dEnv) {
        dEnv.addTokenProcedure("string-split", ((args, env) -> {
            if (!(args.get(0) instanceof DString))
                throw new DevoreCastException(args.get(0).type(), "string");
            if (!(args.get(1) instanceof DString))
                throw new DevoreCastException(args.get(1).type(), "string");
            String str = args.get(0).toString();
            String sep = args.get(1).toString();
            String[] parts = str.split(Pattern.quote(sep), -1);
            return DList.valueOf(Arrays.stream(parts)
                    .map(DString::valueOf)
                    .collect(Collectors.toList()));
        }), 2, false);
        dEnv.addTokenProcedure("string-trim", ((args, env) -> {
            if (!(args.get(0) instanceof DString))
                throw new DevoreCastException(args.get(0).type(), "string");
            return DString.valueOf(args.get(0).toString().trim());
        }), 1, false);
        dEnv.addTokenProcedure("string-trim-left", ((args, env) -> {
            if (!(args.get(0) instanceof DString))
                throw new DevoreCastException(args.get(0).type(), "string");
            return DString.valueOf(args.get(0).toString()
                    .replaceFirst("^\\s+", ""));
        }), 1, false);
        dEnv.addTokenProcedure("string-trim-right", ((args, env) -> {
            if (!(args.get(0) instanceof DString))
                throw new DevoreCastException(args.get(0).type(), "string");
            return DString.valueOf(args.get(0).toString()
                    .replaceFirst("\\s+$", ""));
        }), 1, false);
        dEnv.addTokenProcedure("string-upper", ((args, env) -> {
            if (!(args.get(0) instanceof DString))
                throw new DevoreCastException(args.get(0).type(), "string");
            return DString.valueOf(args.get(0).toString().toUpperCase());
        }), 1, false);
        dEnv.addTokenProcedure("string-lower", ((args, env) -> {
            if (!(args.get(0) instanceof DString))
                throw new DevoreCastException(args.get(0).type(), "string");
            return DString.valueOf(args.get(0).toString().toLowerCase());
        }), 1, false);
        dEnv.addTokenProcedure("string-replace", ((args, env) -> {
            if (!(args.get(0) instanceof DString))
                throw new DevoreCastException(args.get(0).type(), "string");
            if (!(args.get(1) instanceof DString))
                throw new DevoreCastException(args.get(1).type(), "string");
            if (!(args.get(2) instanceof DString))
                throw new DevoreCastException(args.get(2).type(), "string");
            return DString.valueOf(args.get(0).toString()
                    .replace(args.get(1).toString(), args.get(2).toString()));
        }), 3, false);
        dEnv.addTokenProcedure("string-index", ((args, env) -> {
            if (!(args.get(0) instanceof DString))
                throw new DevoreCastException(args.get(0).type(), "string");
            if (!(args.get(1) instanceof DString))
                throw new DevoreCastException(args.get(1).type(), "string");
            return DInt.valueOf(args.get(0).toString().indexOf(args.get(1).toString()));
        }), 2, false);
        dEnv.addTokenProcedure("string-index-last", ((args, env) -> {
            if (!(args.get(0) instanceof DString))
                throw new DevoreCastException(args.get(0).type(), "string");
            if (!(args.get(1) instanceof DString))
                throw new DevoreCastException(args.get(1).type(), "string");
            return DInt.valueOf(args.get(0).toString().lastIndexOf(args.get(1).toString()));
        }), 2, false);
        dEnv.addTokenProcedure("string-contains", ((args, env) -> {
            if (!(args.get(0) instanceof DString))
                throw new DevoreCastException(args.get(0).type(), "string");
            if (!(args.get(1) instanceof DString))
                throw new DevoreCastException(args.get(1).type(), "string");
            return DBool.valueOf(args.get(0).toString().contains(args.get(1).toString()));
        }), 2, false);
        dEnv.addTokenProcedure("string-empty?", ((args, env) -> {
            if (!(args.get(0) instanceof DString))
                throw new DevoreCastException(args.get(0).type(), "string");
            return DBool.valueOf(args.get(0).toString().isEmpty());
        }), 1, false);
        dEnv.addTokenProcedure("string-starts-with", ((args, env) -> {
            if (!(args.get(0) instanceof DString))
                throw new DevoreCastException(args.get(0).type(), "string");
            if (!(args.get(1) instanceof DString))
                throw new DevoreCastException(args.get(1).type(), "string");
            return DBool.valueOf(args.get(0).toString().startsWith(args.get(1).toString()));
        }), 2, false);
        dEnv.addTokenProcedure("string-ends-with", ((args, env) -> {
            if (!(args.get(0) instanceof DString))
                throw new DevoreCastException(args.get(0).type(), "string");
            if (!(args.get(1) instanceof DString))
                throw new DevoreCastException(args.get(1).type(), "string");
            return DBool.valueOf(args.get(0).toString().endsWith(args.get(1).toString()));
        }), 2, false);
        dEnv.addTokenProcedure("string-get", ((args, env) -> {
            if (!(args.get(0) instanceof DString))
                throw new DevoreCastException(args.get(0).type(), "string");
            if (!(args.get(1) instanceof DInt))
                throw new DevoreCastException(args.get(1).type(), "int");
            String s = args.get(0).toString();
            int index = DIntUtils.toIndex((DInt) args.get(1));
            if (index >= s.length())
                throw new DevoreRuntimeException("字符串访问过界, 下标=" + index + ", 但字符串只有" + s.length() + "个字符.");
            return DString.valueOf(String.valueOf(args.get(0).toString().toCharArray()[index]));
        }), 2, false);
        dEnv.addTokenProcedure("string-sub", ((args, env) -> {
            if (!(args.get(0) instanceof DString))
                throw new DevoreCastException(args.get(0).type(), "string");
            if (!(args.get(1) instanceof DInt))
                throw new DevoreCastException(args.get(1).type(), "int");
            String s = args.get(0).toString();
            int fromIndex = DIntUtils.toIndex((DInt) args.get(1));
            if (fromIndex >= s.length() || fromIndex < 0)
                throw new DevoreRuntimeException("字符串截取过界, 下标=" + fromIndex + ", 但字符串只有" + s.length() + "个字符.");
            return DString.valueOf(s.substring(fromIndex));
        }), 2, false);
        dEnv.addTokenProcedure("string-sub", ((args, env) -> {
            if (!(args.get(0) instanceof DString))
                throw new DevoreCastException(args.get(0).type(), "string");
            if (!(args.get(1) instanceof DInt))
                throw new DevoreCastException(args.get(1).type(), "int");
            if (!(args.get(2) instanceof DInt))
                throw new DevoreCastException(args.get(2).type(), "int");
            String s = args.get(0).toString();
            int fromIndex = DIntUtils.toIndex((DInt) args.get(1));
            int toIndex = DIntUtils.toIndex((DInt) args.get(2));
            if (fromIndex > toIndex)
                throw new DevoreRuntimeException("字符串截取起始下标大于目标下标, fromIndex=" + fromIndex
                        + ", toIndex=" + toIndex + ", length=" + s.length());
            if (fromIndex >= s.length() || fromIndex < 0)
                throw new DevoreRuntimeException("字符串截取过界, fromIndex=" + fromIndex + ", 但字符串只有" + s.length() + "个字符.");
            if (toIndex >= s.length())
                throw new DevoreRuntimeException("字符串截取过界, toIndex=" + toIndex + ", 但字符串只有" + s.length() + "个字符.");
            return DString.valueOf(s.substring(fromIndex, toIndex));
        }), 3, false);
    }

    /**
     * 判断AST节点是否为空过程占位
     */
    private boolean isEmptyProcedure(Ast ast) {
        return ast.type == Ast.Type.PROCEDURE
                && ast.isEmpty()
                && ast.symbol instanceof Ast
                && !((Ast) ast.symbol).isNotNil();
    }
}
