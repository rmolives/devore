package org.devore.parser;

import org.devore.exception.DevoreParseException;
import org.devore.lang.token.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 词法分析器
 */
public class Lexer {
    /**
     * 带源码位置的表达式
     */
    public static class SourceExpression {
        public final String expression;  // 表达式
        public final int startIndex;     // 起始位置

        public SourceExpression(String expression, int startIndex) {
            this.expression = expression;
            this.startIndex = startIndex;
        }
    }

    /**
     * 带源码位置的token
     */
    public static class SourceToken {
        public final DToken token;   // token
        public final int index;      // 源码位置

        public SourceToken(DToken token, int index) {
            this.token = token;
            this.index = index;
        }
    }

    /**
     * 分割代码
     * 例如：把(+ 2 3)(- 4 5)分割成(+ 2 3)和(- 4 5)
     *
     * @param code 代码
     * @return 代码片段序列
     */
    public static List<SourceExpression> splitCode(String code) {
        char[] codeCharArray = code.toCharArray();
        List<SourceExpression> expressions = new ArrayList<>();
        int index = 0;
        while (index < codeCharArray.length) {
            StringBuilder builder = new StringBuilder();
            while (index < codeCharArray.length) {
                if (codeCharArray[index] == ';')
                    index = IntStream.range(index, codeCharArray.length)
                            .filter(i -> codeCharArray[i] == '\n' || codeCharArray[i] == '\r')
                            .findFirst()
                            .orElse(codeCharArray.length);
                else if (codeCharArray[index] == '(' || codeCharArray[index] == '[')
                    break;
                else if (codeCharArray[index] == ')' || codeCharArray[index] == ']')
                    throw new DevoreParseException("多余的右括号: " + codeCharArray[index], index);
                else
                    ++index;
            }
            if (index >= codeCharArray.length)
                return expressions;
            int startIndex = index;
            Deque<Character> brackets = new ArrayDeque<>();
            Deque<Integer> bracketIndexes = new ArrayDeque<>();
            while (index < codeCharArray.length) {
                if (codeCharArray[index] == ';') {
                    index = IntStream.range(index, codeCharArray.length)
                            .filter(i -> codeCharArray[i] == '\n' || codeCharArray[i] == '\r')
                            .findFirst()
                            .orElse(codeCharArray.length);
                    continue;
                }
                if (codeCharArray[index] == '\"') {
                    int stringStartIndex = index;
                    builder.append("\"");
                    StringBuilder value = new StringBuilder();
                    boolean skip = false;
                    while (true) {
                        ++index;
                        if (index >= codeCharArray.length)
                            throw new DevoreParseException("字符串未闭合.", stringStartIndex);
                        if (codeCharArray[index] == '\\') {
                            if (skip) {
                                skip = false;
                                value.append("\\\\");
                            } else
                                skip = true;
                            continue;
                        } else if (codeCharArray[index] == '\"') {
                            if (skip) {
                                skip = false;
                                value.append("\\\"");
                                continue;
                            } else
                                break;
                        } else if (skip) {
                            value.append("\\").append(codeCharArray[index]);
                            skip = false;
                            continue;
                        }
                        value.append(codeCharArray[index]);
                    }
                    builder.append(value).append("\"");
                    ++index;
                    continue;
                }
                if (codeCharArray[index] == '(' || codeCharArray[index] == '[') {
                    brackets.push(codeCharArray[index]);
                    bracketIndexes.push(index);
                } else if (codeCharArray[index] == ')' || codeCharArray[index] == ']') {
                    if (brackets.isEmpty())
                        throw new DevoreParseException("多余的右括号: " + codeCharArray[index], index);
                    char left = brackets.pop();
                    bracketIndexes.pop();
                    if (!(left == '(' && codeCharArray[index] == ')' || left == '[' && codeCharArray[index] == ']'))
                        throw new DevoreParseException("括号不匹配: " + left + " 与 " + codeCharArray[index], index);
                }
                builder.append(codeCharArray[index++]);
                if (brackets.isEmpty())
                    break;
            }
            if (!brackets.isEmpty())
                throw new DevoreParseException("括号未闭合: " + brackets.peek(), bracketIndexes.getFirst());
            expressions.add(new SourceExpression(builder.toString(), startIndex));
        }
        return expressions;
    }

    /**
     * 词法分析器
     *
     * @param expression 代码片段
     * @param baseIndex  基础位置
     * @return Token序列
     */
    public static List<SourceToken> lexer(String expression, int baseIndex) {
        char[] expressionCharArray = expression.toCharArray();
        List<SourceToken> tokens = new ArrayList<>();
        int index = -1;
        while (++index < expressionCharArray.length) {
            switch (expressionCharArray[index]) {
                case '(':
                case '[':
                    tokens.add(new SourceToken(DWord.LB, baseIndex + index));
                    continue;
                case ')':
                case ']':
                    tokens.add(new SourceToken(DWord.RB, baseIndex + index));
                    continue;
            }
            if (Character.isWhitespace(expressionCharArray[index]))
                continue;
            int tokenIndex = index;
            boolean negative = false;
            if (expressionCharArray[index] == '-' && index < expressionCharArray.length - 1
                    && Character.isDigit(expressionCharArray[index + 1])) {
                negative = true;
                ++index;
            }
            if (Character.isDigit(expressionCharArray[index])) {
                int integerStart = index;
                while (index < expressionCharArray.length && Character.isDigit(expressionCharArray[index]))
                    ++index;
                int integerEnd = index;
                BigInteger v = IntStream.range(integerStart, integerEnd)
                        .mapToObj(i -> BigInteger.valueOf(Character.digit(expressionCharArray[i], 10)))
                        .reduce(BigInteger.ZERO, (result, digit) -> result.multiply(BigInteger.TEN).add(digit));
                if (index >= expressionCharArray.length || expressionCharArray[index] != '.'
                        || index >= expressionCharArray.length - 1 || !Character.isDigit(expressionCharArray[index + 1])) {
                    --index;
                    tokens.add(new SourceToken(DNumber.valueOf(negative ? v.negate() : v), baseIndex + tokenIndex));
                    continue;
                }
                ++index;
                int fractionStart = index;
                while (index < expressionCharArray.length && Character.isDigit(expressionCharArray[index]))
                    ++index;
                int fractionEnd = index;
                BigDecimal x = IntStream.range(fractionStart, fractionEnd)
                        .mapToObj(i -> BigDecimal.valueOf(Character.digit(expressionCharArray[i], 10))
                                .divide(BigDecimal.TEN.pow(i - fractionStart + 1), MathContext.DECIMAL128))
                        .reduce(new BigDecimal(v), BigDecimal::add);
                --index;
                tokens.add(new SourceToken(DNumber.valueOf(negative ? x.negate() : x), baseIndex + tokenIndex));
                continue;
            }
            if (expressionCharArray[index] == '\"') {
                StringBuilder builder = new StringBuilder();
                boolean skip = false;
                while (true) {
                    ++index;
                    if (index < expressionCharArray.length - 1 && expressionCharArray[index] == '\\') {
                        if (skip) {
                            skip = false;
                            builder.append("\\");
                        } else
                            skip = true;
                        continue;
                    } else if (index >= expressionCharArray.length - 1 || expressionCharArray[index] == '\"') {
                        if (skip) {
                            skip = false;
                            builder.append("\"");
                            continue;
                        } else
                            break;
                    }
                    if (skip) {
                        skip = false;
                        switch (expressionCharArray[index]) {
                            case 'n':
                                builder.append("\n");
                                break;
                            case 'r':
                                builder.append("\r");
                                break;
                            case 't':
                                builder.append("\t");
                                break;
                            case 'b':
                                builder.append("\b");
                                break;
                            case 'f':
                                builder.append("\f");
                                break;
                            default:
                                builder.append(expressionCharArray[index]);
                                break;
                        }
                    } else
                        builder.append(expressionCharArray[index]);
                }
                tokens.add(new SourceToken(DString.valueOf(builder.toString()), baseIndex + tokenIndex));
                continue;
            }
            if (!Character.isWhitespace(expressionCharArray[index]) && expressionCharArray[index] != '(' && expressionCharArray[index] != ')'
                    && expressionCharArray[index] != '[' && expressionCharArray[index] != ']') {
                int symbolStart = index;
                while (index < expressionCharArray.length && !Character.isWhitespace(expressionCharArray[index])
                        && expressionCharArray[index] != ')' && expressionCharArray[index] != ']')
                    ++index;
                String symbol = IntStream.range(symbolStart, index)
                        .mapToObj(i -> String.valueOf(expressionCharArray[i]))
                        .collect(Collectors.joining());
                --index;
                tokens.add(new SourceToken(DSymbol.valueOf(symbol), baseIndex + tokenIndex));
            }
        }
        return tokens;
    }
}
