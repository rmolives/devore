package org.devore;

import org.devore.exception.DevoreRuntimeException;
import org.devore.lang.Env;
import org.devore.lang.Evaluator;
import org.devore.lang.module.DModule;
import org.devore.lang.token.DToken;
import org.devore.lang.token.DWord;
import org.devore.parser.Lexer;
import org.devore.parser.Parse;
import org.devore.exception.DevoreParseException;
import org.devore.parser.Ast;

import java.util.*;
import java.util.stream.IntStream;

public class Devore {
    // 版本
    public static final String VERSION = "0.1-alpha";
    // 版本信息
    public static final String VERSION_MESSAGE = "Devore v" + VERSION + ".\nAuthor: RMOlive (rmolives@wumoe.org)\nGitHub: https://github.com/rmolives/devore";

    /**
     * 创建默认环境
     *
     * @return 环境
     */
    public static Env newEnv() {
        return Env.newEnv();
    }

    /**
     * 创建环境并加载指定模块
     *
     * @param modules 模块名
     * @return 环境
     */
    public static Env newEnv(String... modules) {
        Env env = newEnv();
        Arrays.stream(modules).forEach(env::loadModule);
        return env;
    }

    /**
     * 创建环境并加载指定模块
     *
     * @param modules 模块
     * @return 环境
     */
    public static Env newEnv(DModule... modules) {
        Env env = newEnv();
        Arrays.stream(modules).forEach(env::loadModule);
        return env;
    }

    /**
     * 执行代码
     *
     * @param env    环境
     * @param code   代码
     * @return 返回值
     */
    public static DToken call(Env env, String code) {
        return call(env, code, "<code>");
    }

    /**
     * 执行代码
     *
     * @param env    环境
     * @param code   代码
     * @param source 代码来源
     * @return 返回值
     */
    public static DToken call(Env env, String code, String source) {
        List<Lexer.SourceExpression> codes;
        try {
            codes = Lexer.splitCode(code);
        } catch (DevoreParseException e) {
            throw new DevoreRuntimeException(formatError(code, source, e.index(), "", e));
        }
        return codes.stream()
                .map(exp -> evalExpression(env, code, source, exp))
                .reduce((previous, current) -> current)
                .orElse(DWord.NIL);
    }

    /**
     * 格式化错误信息
     *
     * @param code   代码
     * @param source 代码来源
     * @param exp    表达式
     * @param e      异常
     * @return 错误信息
     */
    private static String formatError(String code, String source, Lexer.SourceExpression exp, Throwable e) {
        return formatError(code, source, exp.startIndex, exp.expression, e);
    }

    private static String formatError(String code, String source, int index, String expression, Throwable e) {
        String message = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
        if (e instanceof DevoreRuntimeException) {
            DevoreRuntimeException runtimeException = (DevoreRuntimeException) e;
            if (runtimeException.index() >= 0)
                index = runtimeException.index();
            if (runtimeException.source() != null)
                source = runtimeException.source();
            if (runtimeException.code() != null)
                code = runtimeException.code();
        }
        Position position = position(code, index);
        StringBuilder builder = new StringBuilder()
                .append("错误位置: ").append(source).append(":").append(position.line).append(":").append(position.column).append("\n");
        String errorExpression = expression;
        if (e instanceof DevoreRuntimeException && ((DevoreRuntimeException) e).expression() != null)
            errorExpression = ((DevoreRuntimeException) e).expression();
        if (!errorExpression.isEmpty())
            builder.append("出错表达式: ").append(preview(errorExpression)).append("\n");
        if (e instanceof DevoreRuntimeException && !((DevoreRuntimeException) e).trace().isEmpty()) {
            builder.append("调用链:\n");
            List<DevoreRuntimeException.Frame> trace = ((DevoreRuntimeException) e).trace();
            final String errorSource = source;
            final String errorCode = code;
            IntStream.range(0, trace.size())
                    .map(i -> trace.size() - i - 1)
                    .mapToObj(trace::get)
                    .forEach(frame -> {
                        String frameSource = frame.source == null ? errorSource : frame.source;
                        String frameCode = frame.code == null ? errorCode : frame.code;
                        Position framePosition = position(frameCode, frame.index);
                        builder.append("  at ")
                                .append(frameSource).append(":").append(framePosition.line).append(":").append(framePosition.column)
                                .append(" ").append(preview(frame.expression)).append("\n");
                    });
        }
        return builder.append("错误信息: ").append(message).toString();
    }

    private static Position position(String code, int index) {
        int end = Math.min(index, code.length());
        int line = (int) IntStream.range(0, end)
                .filter(i -> code.charAt(i) == '\n')
                .count() + 1;
        int lastLineStart = IntStream.range(0, end)
                .map(i -> end - i - 1)
                .filter(i -> code.charAt(i) == '\n')
                .findFirst()
                .orElse(-1) + 1;
        int column = end - lastLineStart + 1;
        return new Position(line, column);
    }

    private static DToken evalExpression(Env env, String code, String source, Lexer.SourceExpression exp) {
        try {
            Ast ast = Parse.parse(Lexer.lexer(exp.expression, exp.startIndex));
            ast.setSource(source, code);
            return Evaluator.eval(env, ast);
        } catch (StackOverflowError e) {
            throw new DevoreRuntimeException(formatError(code, source, exp,
                    new StackOverflowError("栈溢出，可能存在无限递归或递归宏展开.")));
        } catch (RuntimeException e) {
            throw new DevoreRuntimeException(formatError(code, source, exp, e));
        }
    }

    private static class Position {
        private final int line;
        private final int column;

        private Position(int line, int column) {
            this.line = line;
            this.column = column;
        }
    }

    /**
     * 生成表达式预览
     *
     * @param expression 表达式
     * @return 预览
     */
    private static String preview(String expression) {
        String compact = expression.trim().replaceAll("\\s+", " ");
        return compact.length() <= 160 ? compact : compact.substring(0, 157) + "...";
    }
}
