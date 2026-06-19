package org.devore;

import org.devore.exception.DevoreRuntimeException;
import org.devore.lang.Env;
import org.devore.lang.Evaluator;
import org.devore.lang.token.DToken;
import org.devore.lang.token.DWord;
import org.devore.parser.Lexer;
import org.devore.parser.Parse;
import org.devore.exception.DevoreParseException;

import java.util.List;

public class Devore {
    // 版本
    public static final String VERSION = "0.1-alpha";
    // 版本信息
    public static final String VERSION_MESSAGE = "Devore v" + VERSION + ".\nAuthor: RMOlive (rmolives@wumoe.org)\nGitHub: https://github.com/rmolives/devore";

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
        DToken result = DWord.NIL;
        for (Lexer.SourceExpression exp : codes) {
            try {
                result = Evaluator.eval(env, Parse.parse(Lexer.lexer(exp.expression, exp.startIndex)));
            } catch (StackOverflowError e) {
                throw new DevoreRuntimeException(formatError(code, source, exp,
                        new StackOverflowError("栈溢出，可能存在无限递归或递归宏展开.")));
            } catch (RuntimeException e) {
                throw new DevoreRuntimeException(formatError(code, source, exp, e));
            }
        }
        return result;
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
        if (e instanceof DevoreRuntimeException && ((DevoreRuntimeException) e).index() >= 0)
            index = ((DevoreRuntimeException) e).index();
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
            for (int i = trace.size() - 1; i >= 0; --i) {
                DevoreRuntimeException.Frame frame = trace.get(i);
                Position framePosition = position(code, frame.index);
                builder.append("  at ")
                        .append(source).append(":").append(framePosition.line).append(":").append(framePosition.column)
                        .append(" ").append(preview(frame.expression)).append("\n");
            }
        }
        return builder.append("错误信息: ").append(message).toString();
    }

    private static Position position(String code, int index) {
        int line = 1;
        int column = 1;
        for (int i = 0; i < index && i < code.length(); ++i) {
            if (code.charAt(i) == '\n') {
                ++line;
                column = 1;
            } else
                ++column;
        }
        return new Position(line, column);
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
