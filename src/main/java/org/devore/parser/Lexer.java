package org.devore.parser;

import org.devore.lang.token.*;

import java.math.BigDecimal;
import java.math.BigInteger;
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
                else if (codeCharArray[index] == '(' || codeCharArray[index] == '[')
                    break;
                else
                    ++index;
            }
            if (index >= codeCharArray.length)
                return expressions;
            do {
                if (index >= codeCharArray.length)
                    break;
                if (codeCharArray[index] == ';') {
                    while (index < codeCharArray.length &&
                            codeCharArray[index] != '\n' &&
                            codeCharArray[index] != '\r')
                        ++index;
                    continue;
                }
                if (codeCharArray[index] == '"') {
                    builder.append('"');
                    boolean escape = false;
                    while (++index < codeCharArray.length) {
                        char c = codeCharArray[index];          
                        if (escape) {
                            escape = false;
                            builder.append('\\').append(c);
                            continue;
                        }
                        if (c == '\\') {
                            escape = true;
                            continue;
                        }
                        if (c == '"') {
                            builder.append('"');
                            ++index;
                            break;
                        }
                        builder.append(c);
                    }
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
                if (codeCharArray[index] == '(' || codeCharArray[index] == '[')
                    ++flag;
                else if (codeCharArray[index] == ')' || codeCharArray[index] == ']')
                    --flag;
                builder.append(codeCharArray[index++]);
            } while (flag > 0 && index < codeCharArray.length);
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
                StringBuilder builder = new StringBuilder();
                if (negative)
                    builder.append('-');
                while (index < expressionCharArray.length &&
                        Character.isDigit(expressionCharArray[index])) {
                    builder.append(expressionCharArray[index]);
                    ++index;
                }
                if (index < expressionCharArray.length && expressionCharArray[index] == '.') {
                    builder.append('.');
                    ++index;
                    while (index < expressionCharArray.length &&
                            Character.isDigit(expressionCharArray[index])) {
                        builder.append(expressionCharArray[index]);
                        ++index;
                    }
                    tokens.add(DNumber.valueOf(new BigDecimal(builder.toString())));
                } else
                    tokens.add(DNumber.valueOf(new BigInteger(builder.toString())));
                --index;
                continue;
            }
            if (expressionCharArray[index] == '\"') {
                StringBuilder builder = new StringBuilder();
                boolean escape = false;
                while (++index < expressionCharArray.length) {
                    char c = expressionCharArray[index];
                    if (escape) {
                        escape = false;
                        switch (c) {
                            case 'n': builder.append('\n'); break;
                            case 'r': builder.append('\r'); break;
                            case 't': builder.append('\t'); break;
                            case 'b': builder.append('\b'); break;
                            case 'f': builder.append('\f'); break;
                            case '"': builder.append('"'); break;
                            case '\\': builder.append('\\'); break;
                            default: builder.append('\\').append(c); break;
                        }
                        continue;
                    }
                    if (c == '\\') {
                        escape = true;
                        continue;
                    }
                    if (c == '"')
                        break;
                    builder.append(c);
                }
                tokens.add(DString.valueOf(builder.toString()));
                continue;
            }
            if (expressionCharArray[index] != ' ' && expressionCharArray[index] != '(' && expressionCharArray[index] != ')'
                    && expressionCharArray[index] != '[' && expressionCharArray[index] != ']') {
                StringBuilder builder = new StringBuilder();
                while (index < expressionCharArray.length &&
                        expressionCharArray[index] != ' ' &&
                        expressionCharArray[index] != '(' &&
                        expressionCharArray[index] != ')' &&
                        expressionCharArray[index] != '[' &&
                        expressionCharArray[index] != ']') {
                    builder.append(expressionCharArray[index]);
                    ++index;
                }
                --index;
                tokens.add(DSymbol.valueOf(builder.toString()));
            }
        }
        return tokens;
    }
}
