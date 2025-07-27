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
     * @param code  代码
     * @return      代码片段序列
     */
    public static List<String> splitCode(String code) {
        char[] codeCharArray = code.toCharArray();
        List<String> expressions = new ArrayList<>();
        int index = 0;
        while (index < codeCharArray.length) {
            int flag = 0;
            StringBuilder builder = new StringBuilder();
            while (index < codeCharArray.length && codeCharArray[index] != '(')
                ++index;
            if (index >= codeCharArray.length)
                return expressions;
            do {
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
                            } else
                                skip = true;
                            continue;
                        } else if (index >= codeCharArray.length - 1 || codeCharArray[index] == '\"') {
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
                    builder.append(value.append("\""));
                    ++index;
                    continue;
                }
                while (index < codeCharArray.length - 1 && (codeCharArray[index] == ' '
                        || codeCharArray[index] == '\n' || codeCharArray[index] == '\r' || codeCharArray[index] == '\t')
                        && ((codeCharArray[index + 1] == ' '
                        || codeCharArray[index + 1] == '\n' || codeCharArray[index + 1] == '\r' || codeCharArray[index + 1] == '\t')
                        || codeCharArray[index + 1] == ')'))
                    ++index;
                if (codeCharArray[index] == '(')
                    ++flag;
                else if (codeCharArray[index] == ')')
                    --flag;
                builder.append(codeCharArray[index++]);
            } while (flag > 0);
            expressions.add(builder.toString());
        }
        return expressions;
    }

    /**
     * 词法分析器
     * @param expression    代码片段
     * @return              Token序列
     */
    public static List<Token> lexer(String expression) {
        char[] expressionCharArray = expression.toCharArray();
        List<Token> tokens = new ArrayList<>();
        int index = -1;
        while (++index < expressionCharArray.length) {
            switch (expressionCharArray[index]) {
                case '(' -> {
                    tokens.add(DWord.WORD_LB);
                    continue;
                }
                case ')' -> {
                    tokens.add(DWord.WORD_RB);
                    continue;
                }
                case ' ' -> {
                    continue;
                }
            }
            var negative = false;
            if (expressionCharArray[index] == '-' && index < expressionCharArray.length - 1 && Character.isDigit(expressionCharArray[index + 1])) {
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
                    tokens.add(DInt.valueOf(negative ? v.subtract(v.multiply(BigInteger.TWO)) : v));
                    continue;
                }
                var x = new BigDecimal(v);
                var d = BigDecimal.valueOf(10);
                ++index;
                while (true) {
                    ++index;
                    if (index >= expressionCharArray.length - 1 || !Character.isDigit(expressionCharArray[index])) {
                        --index;
                        break;
                    }
                    x = x.add(BigDecimal.valueOf(Character.getNumericValue(expressionCharArray[index])).divide(d, MathContext.DECIMAL128));
                    d = d.multiply(BigDecimal.valueOf(10));
                }
                tokens.add(DFloat.valueOf(negative ? x.subtract(x.multiply(BigDecimal.valueOf(2))) : x));
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
                            case 'n' -> builder.append("\n");
                            case 'r' -> builder.append("\r");
                            case 't' -> builder.append("\t");
                            case 'b' -> builder.append("\b");
                            default -> builder.append("\\\\").append(expressionCharArray[index]);
                        }
                    } else
                        builder.append(expressionCharArray[index]);
                }
                tokens.add(DString.valueOf(builder.toString()));
                continue;
            }
            if (expressionCharArray[index] != ' ' && expressionCharArray[index] != '(' && expressionCharArray[index] != ')') {
                StringBuilder builder = new StringBuilder();
                while (true) {
                    if (index >= expressionCharArray.length - 1 || expressionCharArray[index] == ' ' || expressionCharArray[index] == ')') {
                        --index;
                        break;
                    }
                    builder.append(expressionCharArray[index]);
                    ++index;
                }
                tokens.add(DOp.valueOf(builder.toString()));
            }
        }
        return tokens;
    }
}
