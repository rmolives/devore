package org.devore.parser;

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
     * 分割代码
     * 例如：把(+ 2 3)(- 4 5)分割成(+ 2 3)和(- 4 5)
     *
     * @param code 代码
     * @return 代码片段序列
     */
    public static List<String> splitCode(String code) {
        char[] codeCharArray = code.toCharArray();
        List<String> expressions = new ArrayList<>();
        int index = 0;
        while (index < codeCharArray.length) {
            int flag = 0;
            StringBuilder builder = new StringBuilder();
            while (index < codeCharArray.length) {
                if (codeCharArray[index] == ';')
                    while (index < codeCharArray.length &&
                            codeCharArray[index] != '\n' &&
                            codeCharArray[index] != '\r')
                        ++index;
                else if (codeCharArray[index] == '(' || codeCharArray[index] == '[') break;
                else ++index;
            }
            if (index >= codeCharArray.length) return expressions;
            do {
                if (codeCharArray[index] == ';') {
                    while (index < codeCharArray.length &&
                            codeCharArray[index] != '\n' &&
                            codeCharArray[index] != '\r')
                        ++index;
                    continue;
                }
                if (codeCharArray[index] == '\"') {
                    builder.append("\"");
                    StringBuilder value = new StringBuilder();
                    boolean skip = false;
                    while (true) {
                        ++index;
                        if (index < codeCharArray.length - 1 && codeCharArray[index] == '\\') {
                            if (skip) {
                                skip = false;
                                value.append("\\\\");
                            } else skip = true;
                            continue;
                        } else if (index >= codeCharArray.length - 1 || codeCharArray[index] == '\"') {
                            if (skip) {
                                skip = false;
                                value.append("\\\"");
                                continue;
                            } else break;
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
                if (codeCharArray[index] == '\n' || codeCharArray[index] == '\r')
                    codeCharArray[index] = ' ';
                while (index < codeCharArray.length - 1 &&
                        (codeCharArray[index] == ' ' || codeCharArray[index] == '\t') &&
                        ((codeCharArray[index + 1] == ' ' || codeCharArray[index + 1] == '\t')
                                || codeCharArray[index + 1] == ')'
                                || codeCharArray[index + 1] == ']'))
                    ++index;
                if (codeCharArray[index] == '(' || codeCharArray[index] == '[') ++flag;
                else if (codeCharArray[index] == ')' || codeCharArray[index] == ']') --flag;
                builder.append(codeCharArray[index++]);
            } while (flag > 0);
            expressions.add(builder.toString());
        }
        return expressions;
    }

    /**
     * 词法分析器
     *
     * @param expression 代码片段
     * @return Token序列
     */
    public static List<DToken> lexer(String expression) {
        char[] expressionCharArray = expression.toCharArray();
        List<DToken> tokens = new ArrayList<>();
        int index = -1;
        while (++index < expressionCharArray.length) {
            switch (expressionCharArray[index]) {
                case '(':
                case '[':
                    tokens.add(DWord.LB);
                    continue;
                case ')':
                case ']':
                    tokens.add(DWord.RB);
                    continue;
                case ' ':
                    continue;
            }
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
                    tokens.add(DNumber.valueOf(negative ? v.negate() : v));
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
                tokens.add(DNumber.valueOf(negative ? x.negate() : x));
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
                        } else skip = true;
                        continue;
                    } else if (index >= expressionCharArray.length - 1 || expressionCharArray[index] == '\"') {
                        if (skip) {
                            skip = false;
                            builder.append("\"");
                            continue;
                        } else break;
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
                    } else builder.append(expressionCharArray[index]);
                }
                tokens.add(DString.valueOf(builder.toString()));
                continue;
            }
            if (expressionCharArray[index] != ' ' && expressionCharArray[index] != '(' && expressionCharArray[index] != ')'
                    && expressionCharArray[index] != '[' && expressionCharArray[index] != ']') {
                StringBuilder builder = new StringBuilder();
                while (true) {
                    if (index >= expressionCharArray.length - 1 || expressionCharArray[index] == ' '
                            || expressionCharArray[index] == ')' || expressionCharArray[index] == ']') {
                        --index;
                        break;
                    }
                    builder.append(expressionCharArray[index]);
                    ++index;
                }
                tokens.add(DSymbol.valueOf(builder.toString()));
            }
        }
        return tokens;
    }
}
