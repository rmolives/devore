package org.devore.parser;

import org.devore.exception.DevoreParseException;
import org.devore.lang.token.DOp;
import org.devore.lang.token.DWord;
import org.devore.lang.token.Token;

import java.util.ArrayDeque;
import java.util.List;

/**
 * 语法分析器
 */
public class Parse {
    /**
     * 语法分析器
     *
     * @param tokens Token序列
     * @return 语法树
     */
    public static AstNode parse(List<Token> tokens) {
        AstNode ast = null;
        ArrayDeque<AstNode> stack = new ArrayDeque<>();
        int state = -1;
        AstNode temp;
        int index = 0;
        while (index < tokens.size()) {
            if (state == 1) {
                if (tokens.get(index) == DWord.WORD_RB) {
                    temp = AstNode.nullAst;
                    stack.push(temp);
                    state = -1;
                    ++index;
                    continue;
                }
                if (tokens.get(index) == DWord.WORD_LB)
                    tokens.add(index, DOp.valueOf("apply"));
                temp = new AstNode(tokens.get(index));
                stack.push(temp);
                ast = temp;
                state = -1;
            } else if (state == 2) {
                if (tokens.get(index) == DWord.WORD_RB) {
                    temp = AstNode.nullAst;
                    if (stack.peek() == null) {
                        throw new DevoreParseException("语法解析中栈顶为null.");
                    }
                    stack.peek().add(temp);
                    state = -1;
                    ++index;
                    continue;
                }
                if (tokens.get(index) == DWord.WORD_LB)
                    tokens.add(index, DOp.valueOf("apply"));
                temp = new AstNode(tokens.get(index));
                if (stack.peek() == null) {
                    throw new DevoreParseException("语法解析中栈顶为null.");
                }
                stack.peek().add(temp);
                stack.push(temp);
                state = -1;
            } else if (tokens.get(index) == DWord.WORD_LB)
                state = stack.isEmpty() ? 1 : 2;
            else if (tokens.get(index) == DWord.WORD_RB) {
                if (index >= 2 && tokens.get(index - 2) == DWord.WORD_LB) {
                    if (stack.peek() == null)
                        throw new DevoreParseException("语法解析中栈顶为null.");
                    stack.peek().type = AstNode.AstType.FUNCTION;
                }
                if (stack.isEmpty())
                    throw new DevoreParseException("语法解析中栈顶为空.");
                stack.pop();
            } else {
                if (stack.isEmpty())
                    throw new DevoreParseException("语法解析中栈顶为空.");
                stack.peek().add(new AstNode(tokens.get(index)));
            }
            ++index;
        }
        if (ast != null)
            return ast;
        throw new DevoreParseException("语法解析出的AST为null.");
    }
}
