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
                    throw new DevoreParseException("语法解析中栈顶为空.");
                closeProcedure(stack.pop());
            } else {
                if (stack.isEmpty())
                    throw new DevoreParseException("语法解析中栈顶为空.");
                stack.peek().add(newNode(token, sourceToken.index));
            }
        }
        if (node == null)
            throw new DevoreParseException("语法解析出的AST为null.");
        if (!stack.isEmpty())
            throw new DevoreParseException("语法解析中括号未闭合.");
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
            node.children = new ArrayList<>(childrenCopy.subList(1, childrenCopy.size()));
            return;
        }
        node.symbol = symbolNode.symbol;
        node.children = new ArrayList<>(symbolNode.children);
        node.children.addAll(childrenCopy.subList(1, childrenCopy.size()));
    }
}
