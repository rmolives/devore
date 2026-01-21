package org.devore.parser;

import org.devore.lang.token.DString;
import org.devore.lang.token.DWord;
import org.devore.lang.token.DToken;

import java.util.ArrayList;
import java.util.List;

/**
 * 抽象语法树
 */
public class Ast {
    public final static Ast empty = new Ast(DWord.NIL);         // 空白语法树
    public List<Ast> children;                                  // 子树
    public DToken symbol;                                       // 符号
    public Type type;                                           // 语法树类型

    /**
     * 创建语法树
     * @param symbol 内容
     */
    public Ast(DToken symbol) {
        this.symbol = symbol;
        this.type = Type.BASIC;
        this.children = new ArrayList<>();
    }

    /**
     * 创建语法树
     * @param symbol 内容
     * @param type   类型
     * @param child  子树
     */
    public Ast(DToken symbol, Type type, List<Ast> child) {
        this.symbol = symbol;
        this.type = type;
        this.children = child;
    }

    /**
     * 复制
     * @return 复制
     */
    public Ast copy() {
        List<Ast> list = new ArrayList<>();
        for (Ast ast : this.children) list.add(ast.copy());
        return new Ast(this.symbol, this.type, list);
    }

    /**
     * 添加子树
     * @param node 子树
     */
    public void add(Ast node) {
        this.children.add(node);
    }

    /**
     * 插入子树
     * @param i    位置
     * @param node 子树
     */
    public void add(int i, Ast node) {
        this.children.add(i, node);
    }

    /**
     * 设置子树
     * @param i    index
     * @param node 子树
     */
    public void set(int i, Ast node) {
        this.children.set(i, node);
    }

    /**
     * 获取第i位子树
     * @param i index
     * @return 子树
     */
    public Ast get(int i) {
        return this.children.get(i);
    }

    /**
     * 清空语法树
     */
    public void clear() {
        this.children.clear();
    }

    /**
     * 获取语法树子树数量
     * @return 数量
     */
    public int size() {
        return this.children.size();
    }

    /**
     * 判断语法树是否为空
     * @return 结果
     */
    public boolean isEmpty() {
        return this.size() == 0;
    }

    /**
     * 判断语法树symbol是否为nil
     * @return 结果
     */
    public boolean isNotNil() {
        return this.symbol != DWord.NIL;
    }

    /**
     * 转换为字符串
     * @return 字符串
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (this.isEmpty()) {
            if (this.symbol instanceof DString) builder.append("\"").append(this.symbol).append("\"");
            else if (this.type == Type.PROCEDURE) builder.append("(").append(this.symbol).append(")");
            else builder.append(this.symbol);
        } else {
            builder.append("(");
            if (this.symbol instanceof DString) builder.append("\"").append(symbol).append("\"");
            else builder.append(this.symbol);
            for (Ast node : this.children) builder.append(" ").append(node.toString());
            builder.append(")");
        }
        return builder.toString();
    }

    /**
     * 语法树类型
     */
    public enum Type {
        PROCEDURE,   // 过程
        BASIC        // 普通
    }
}
