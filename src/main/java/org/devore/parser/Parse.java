package org.devore.parser;

import org.devore.exception.DevoreParseException;
import org.devore.lang.token.DToken;
import org.devore.lang.token.DWord;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        if (tokens == null)
            throw new DevoreParseException("语法解析中传入为null.");
        Ast node = null;
        Deque<Ast> stack = new ArrayDeque<>();
        for (Lexer.SourceToken sourceToken : tokens) {
            DToken token = sourceToken.token;
            if (token == DWord.LB) {
                Ast current = newNode(Ast.empty, sourceToken.index);
                if (stack.isEmpty())
                    node = current;
                else
                    stack.peek().add(current);
                stack.push(current);
            } else if (token == DWord.RB) {
                if (stack.isEmpty())
                    throw new DevoreParseException("语法解析中栈顶为空.", sourceToken.index);
                closeProcedure(stack.pop());
            } else {
                if (stack.isEmpty())
                    throw new DevoreParseException("语法解析中栈顶为空.", sourceToken.index);
                stack.peek().add(newNode(token, sourceToken.index));
            }
        }
        if (node == null)
            return Ast.empty;
        return node;
    }

    /**
     * 创建带源码位置的语法树节点
     *
     * @param token 内容
     * @param index 源码位置
     * @return 语法树节点
     */
    private static Ast newNode(DToken token, int index) {
        Ast node = new Ast(token);
        node.index = index;
        return node;
    }

    /**
     * 归一化过程节点：首个子节点作为调用目标，剩余子节点作为参数
     *
     * @param node 过程节点
     */
    private static void closeProcedure(Ast node) {
        node.type = Ast.Type.PROCEDURE;
        if (node.symbol != Ast.empty)
            return;
        if (node.children.isEmpty())
            return;
        List<Ast> childrenCopy = new ArrayList<>(node.children);
        Ast symbolNode = childrenCopy.get(0);
        if (symbolNode.type == Ast.Type.PROCEDURE) {
            node.symbol = symbolNode;
            node.children = childrenCopy.stream()
                    .skip(1)
                    .collect(Collectors.toList());
            return;
        }
        node.symbol = symbolNode.symbol;
        node.children = Stream.concat(symbolNode.children.stream(), childrenCopy.stream().skip(1))
                .collect(Collectors.toList());
    }
}
