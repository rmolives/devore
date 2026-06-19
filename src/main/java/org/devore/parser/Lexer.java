package org.devore.parser;

import org.devore.exception.DevoreParseException;
import org.devore.lang.token.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;

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
                    while (index < codeCharArray.length &&
                            codeCharArray[index] != '\n' &&
                            codeCharArray[index] != '\r')
                        ++index;
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
            List<Character> brackets = new ArrayList<>();
            List<Integer> bracketIndexes = new ArrayList<>();
            while (index < codeCharArray.length) {
                if (codeCharArray[index] == ';') {
                    while (index < codeCharArray.length &&
                            codeCharArray[index] != '\n' &&
                            codeCharArray[index] != '\r')
                        ++index;
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
                    brackets.add(codeCharArray[index]);
                    bracketIndexes.add(index);
                } else if (codeCharArray[index] == ')' || codeCharArray[index] == ']') {
                    if (brackets.isEmpty())
                        throw new DevoreParseException("多余的右括号: " + codeCharArray[index], index);
                    char left = brackets.remove(brackets.size() - 1);
                    bracketIndexes.remove(bracketIndexes.size() - 1);
                    if (!(left == '(' && codeCharArray[index] == ')' || left == '[' && codeCharArray[index] == ']'))
                        throw new DevoreParseException("括号不匹配: " + left + " 与 " + codeCharArray[index], index);
                }
                builder.append(codeCharArray[index++]);
                if (brackets.isEmpty())
                    break;
            }
            if (!brackets.isEmpty())
                throw new DevoreParseException("括号未闭合: " + brackets.get(brackets.size() - 1),
                        bracketIndexes.get(bracketIndexes.size() - 1));
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
                BigInteger v = BigInteger.ZERO;
                while (true) {
                    if (index >= expressionCharArray.length - 1 || !Character.isDigit(expressionCharArray[index])) {
                        --index;
                        break;
                    }
                    v = v.multiply(BigInteger.valueOf(10)).add(BigInteger.valueOf(((int) expressionCharArray[index]) - 48));
                    ++index;
                }
                if (expressionCharArray[index + 1] != '.') {
                    tokens.add(new SourceToken(DNumber.valueOf(negative ? v.negate() : v), baseIndex + tokenIndex));
                    continue;
                }
                BigDecimal x = new BigDecimal(v);
                BigDecimal d = BigDecimal.valueOf(10);
                ++index;
                while (true) {
                    ++index;
                    if (index >= expressionCharArray.length - 1 || !Character.isDigit(expressionCharArray[index])) {
                        --index;
                        break;
                    }
                    x = x.add(BigDecimal.valueOf(Character.getNumericValue(expressionCharArray[index]))
                            .divide(d, MathContext.DECIMAL128));
                    d = d.multiply(BigDecimal.valueOf(10));
                }
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
                                builder.append("\\").append(expressionCharArray[index]);
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
                StringBuilder builder = new StringBuilder();
                while (true) {
                    if (index >= expressionCharArray.length - 1 || Character.isWhitespace(expressionCharArray[index])
                            || expressionCharArray[index] == ')' || expressionCharArray[index] == ']') {
                        --index;
                        break;
                    }
                    builder.append(expressionCharArray[index]);
                    ++index;
                }
                tokens.add(new SourceToken(DSymbol.valueOf(builder.toString()), baseIndex + tokenIndex));
            }
        }
        return tokens;
    }
}
