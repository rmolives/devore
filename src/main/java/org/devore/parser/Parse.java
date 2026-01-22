package org.devore.parser;

import org.devore.exception.DevoreParseException;
import org.devore.lang.token.DSymbol;
import org.devore.lang.token.DToken;
import org.devore.lang.token.DWord;

import java.util.ArrayDeque;
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
    public static Ast parse(List<DToken> tokens) {
        Ast node = null;
        Deque<Ast> stack = new ArrayDeque<>();
        int state = -1;
        Ast temp;
        int index = 0;
        while (index < tokens.size()) {
            if (state == 1) {
                if (tokens.get(index) == DWord.RB) {
                    temp = Ast.empty.copy();
                    stack.push(temp);
                    state = -1;
                    ++index;
                    continue;
                }
                if (tokens.get(index) == DWord.LB) tokens.add(index, DSymbol.valueOf("apply"));
                temp = new Ast(tokens.get(index));
                stack.push(temp);
                node = temp;
                state = -1;
            } else if (state == 2) {
                if (tokens.get(index) == DWord.RB) {
                    temp = Ast.empty.copy();
                    if (stack.peek() == null)
                        throw new DevoreParseException("语法解析中栈顶为null.");
                    stack.peek().add(temp);
                    state = -1;
                    ++index;
                    continue;
                }
                if (tokens.get(index) == DWord.LB)
                    tokens.add(index, DSymbol.valueOf("apply"));
                temp = new Ast(tokens.get(index));
                if (stack.peek() == null)
                    throw new DevoreParseException("语法解析中栈顶为null.");
                stack.peek().add(temp);
                stack.push(temp);
                state = -1;
            } else if (tokens.get(index) == DWord.LB) state = stack.isEmpty() ? 1 : 2;
            else if (tokens.get(index) == DWord.RB) {
                if (index >= 2 && tokens.get(index - 2) == DWord.LB) {
                    if (stack.peek() == null)
                        throw new DevoreParseException("语法解析中栈顶为null.");
                    stack.peek().type = Ast.Type.PROCEDURE;
                }
                if (stack.isEmpty())
                    throw new DevoreParseException("语法解析中栈顶为空.");
                stack.pop();
            } else {
                if (stack.isEmpty())
                    throw new DevoreParseException("语法解析中栈顶为空.");
                stack.peek().add(new Ast(tokens.get(index)));
            }
            ++index;
        }
        if (node == null)
            throw new DevoreParseException("语法解析出的AST为null.");
        return node;
    }
}
