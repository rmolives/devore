package org.devore.parser;

import org.devore.lang.token.DString;
import org.devore.lang.token.Token;

import java.util.ArrayList;
import java.util.List;

/**
 * 抽象语法树
 */
public class AstNode {
    public final static AstNode nullAst = new AstNode(null);    // 空白语法树
    public List<AstNode> children;                                     // 子树
    public Token symbol;                                               // 内容
    public AstType type;                                               // 语法树类型

    /**
     * 创建语法树
     *
     * @param symbol 内容
     */
    public AstNode(Token symbol) {
        this.symbol = symbol;
        this.type = AstType.BASIC;
        this.children = new ArrayList<>();
    }

    /**
     * 创建语法树
     *
     * @param symbol 内容
     * @param type   类型
     * @param child  子树
     */
    public AstNode(Token symbol, AstType type, List<AstNode> child) {
        this.symbol = symbol;
        this.type = type;
        this.children = child;
    }

    /**
     * 复制
     *
     * @return 复制
     */
    public AstNode copy() {
        List<AstNode> list = new ArrayList<>();
        for (AstNode ast : children)
            list.add(ast.copy());
        return new AstNode(symbol, type, list);
    }

    /**
     * 添加子树
     *
     * @param node 子树
     */
    public void add(AstNode node) {
        children.add(node);
    }

    /**
     * 插入子树
     *
     * @param i    位置
     * @param node 子树
     */
    public void insert(int i, AstNode node) {
        children.add(i, node);
    }

    /**
     * 设置子树
     *
     * @param i    index
     * @param node 子树
     */
    public void set(int i, AstNode node) {
        children.set(i, node);
    }

    /**
     * 获取第i位子树
     *
     * @param i index
     * @return 子树
     */
    public AstNode get(int i) {
        return this.children.get(i);
    }

    /**
     * 获取第1位子树
     *
     * @return 子树
     */
    public AstNode getFirst() {
        return get(0);
    }

    /**
     * 清空语法树
     */
    public void clear() {
        children.clear();
    }

    /**
     * 获取语法树子树数量
     *
     * @return 数量
     */
    public int size() {
        return children.size();
    }

    /**
     * 判断语法树是否为空
     *
     * @return 结果
     */
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * 判断语法树op是否为null
     *
     * @return 结果
     */
    public boolean isNull() {
        return symbol == null;
    }

    /**
     * 转换为字符串
     *
     * @return 字符串
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (isEmpty()) {
            if (symbol instanceof DString)
                builder.append("\"").append(symbol).append("\"");
            else
                builder.append(symbol);
        } else {
            builder.append("(");
            if (symbol instanceof DString)
                builder.append("\"").append(symbol).append("\"");
            else
                builder.append(symbol);
            for (AstNode ast : children)
                builder.append(" ").append(ast.toString());
            builder.append(")");
        }
        return builder.toString();
    }

    /**
     * 子树类型
     */
    public enum AstType {
        PROCEDURE,   // 过程
        BASIC        // 普通
    }
}
