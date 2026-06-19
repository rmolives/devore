package org.devore.parser;

import org.devore.exception.DevoreParseException;
import org.devore.lang.token.DToken;
import org.devore.lang.token.DWord;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
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
    public static Ast parse(List<Lexer.SourceToken> tokens) {
        Ast node = null;
        Deque<Ast> stack = new ArrayDeque<>();
        int state = -1;
        Ast temp;
        int index = 0;
        int expressionIndex = -1;
        while (index < tokens.size()) {
            DToken token = tokens.get(index).token;
            int tokenIndex = tokens.get(index).index;
            if (state == 1) {
                if (token == DWord.RB) {
                    temp = Ast.empty.copy();
                    temp.index = expressionIndex;
                    stack.push(temp);
                    state = -1;
                    ++index;
                    continue;
                }
                if (token == DWord.LB) {
                    tokens.add(index, new Lexer.SourceToken(Ast.empty, tokenIndex));
                    continue;
                }
                temp = new Ast(token);
                temp.index = expressionIndex;
                stack.push(temp);
                node = temp;
                state = -1;
            } else if (state == 2) {
                if (token == DWord.RB) {
                    temp = Ast.empty.copy();
                    temp.index = expressionIndex;
                    if (stack.peek() == null)
                        throw new DevoreParseException("语法解析中栈顶为null.");
                    stack.peek().add(temp);
                    state = -1;
                    ++index;
                    continue;
                }
                if (token == DWord.LB) {
                    tokens.add(index, new Lexer.SourceToken(Ast.empty, tokenIndex));
                    continue;
                }
                temp = new Ast(token);
                temp.index = expressionIndex;
                if (stack.peek() == null)
                    throw new DevoreParseException("语法解析中栈顶为null.");
                stack.peek().add(temp);
                stack.push(temp);
                state = -1;
            } else if (token == DWord.LB) {
                expressionIndex = tokenIndex;
                state = stack.isEmpty() ? 1 : 2;
            } else if (token == DWord.RB) {
                if (index >= 2 && tokens.get(index - 2).token == DWord.LB) {
                    if (stack.peek() == null)
                        throw new DevoreParseException("语法解析中栈顶为null.");
                    stack.peek().type = Ast.Type.PROCEDURE;
                }
                if (stack.isEmpty())
                    throw new DevoreParseException("语法解析中栈顶为空.");
                if (stack.peek().symbol == Ast.empty) {
                    List<Ast> childrenCopy = new ArrayList<>(stack.peek().children);
                    stack.peek().type = Ast.Type.PROCEDURE;
                    stack.peek().symbol = stack.peek().children.isEmpty() ? Ast.empty : stack.peek().children.get(0);
                    stack.peek().children = stack.peek().children.size() > 1 ? childrenCopy.subList(1, childrenCopy.size()) : new ArrayList<>();
                }
                stack.pop();
            } else {
                if (stack.isEmpty())
                    throw new DevoreParseException("语法解析中栈顶为空.");
                temp = new Ast(token);
                temp.index = tokenIndex;
                if (stack.peek() == null)
                    throw new DevoreParseException("语法解析中栈顶为null.");
                stack.peek().add(temp);
            }
            ++index;
        }
        if (node == null)
            throw new DevoreParseException("语法解析出的AST为null.");
        return node;
    }
}