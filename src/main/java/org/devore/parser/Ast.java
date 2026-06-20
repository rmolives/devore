package org.devore.parser;

import org.devore.lang.token.DString;
import org.devore.lang.token.DToken;
import org.devore.lang.token.DWord;

import java.util.ArrayList;
import java.util.List;

/**
 * 抽象语法树
 */
public class Ast extends DToken {
    public final static Ast empty = new Ast(DWord.NIL);         // 空白语法树
    public List<Ast> children;                                  // 子树
    public DToken symbol;                                       // 符号
    public Type type;                                           // 语法树类型
    public int index;                                           // 源码位置
    public String source;                                       // 代码来源
    public String code;                                         // 完整源码

    /**
     * 创建语法树
     *
     * @param symbol 内容
     */
    public Ast(DToken symbol) {
        this.symbol = symbol;
        this.type = Type.BASIC;
        this.children = new ArrayList<>();
        this.index = -1;
        this.source = null;
        this.code = null;
    }

    /**
     * 创建语法树
     *
     * @param symbol   内容
     * @param type     类型
     * @param children 子树
     * @param index    源码位置
     * @param source   代码来源
     * @param code     完整源码
     */
    public Ast(DToken symbol, Type type, List<Ast> children, int index, String source, String code) {
        this.symbol = symbol;
        this.type = type;
        this.children = children;
        this.index = index;
        this.source = source;
        this.code = code;
    }

    /**
     * 复制
     *
     * @return 复制
     */
    public Ast copy() {
        List<Ast> list = new ArrayList<>();
        for (Ast ast : this.children)
            list.add(ast.copy());
        return new Ast(this.symbol, this.type, list, this.index, this.source, this.code);
    }

    /**
     * 设置语法树源码信息
     *
     * @param source 代码来源
     * @param code   完整源码
     */
    public void setSource(String source, String code) {
        this.source = source;
        this.code = code;
        for (Ast child : this.children)
            child.setSource(source, code);
        if (this.symbol instanceof Ast)
            ((Ast) this.symbol).setSource(source, code);
    }

    /**
     * 添加子树
     *
     * @param node 子树
     */
    public void add(Ast node) {
        this.children.add(node);
    }

    /**
     * 插入子树
     *
     * @param i    位置
     * @param node 子树
     */
    public void add(int i, Ast node) {
        this.children.add(i, node);
    }

    /**
     * 设置子树
     *
     * @param i    index
     * @param node 子树
     */
    public void set(int i, Ast node) {
        this.children.set(i, node);
    }

    /**
     * 获取第i位子树
     *
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
     *
     * @return 数量
     */
    public int size() {
        return this.children.size();
    }

    /**
     * 判断语法树是否为空
     *
     * @return 结果
     */
    public boolean isEmpty() {
        return this.size() == 0;
    }

    /**
     * 判断语法树symbol是否为nil
     *
     * @return 结果
     */
    public boolean isNotNil() {
        return this.symbol != DWord.NIL;
    }

    @Override
    public String type() {
        return "ast";
    }

    @Override
    protected String str() {
        StringBuilder builder = new StringBuilder();
        this.appendTo(builder);
        return builder.toString();
    }

    /**
     * 将语法树追加到builder
     *
     * @param builder 字符串构造器
     */
    private void appendTo(StringBuilder builder) {
        if (this.isEmpty()) {
            if (this.symbol instanceof DString)
                this.appendSymbolTo(builder);
            else if (this.type == Type.PROCEDURE)
                builder.append("(").append(this.symbol).append(")");
            else
                builder.append(this.symbol);
        } else {
            builder.append("(");
            this.appendSymbolTo(builder);
            for (Ast node : this.children) {
                builder.append(" ");
                node.appendTo(builder);
            }
            builder.append(")");
        }
    }

    /**
     * 将语法树symbol追加到builder
     *
     * @param builder 字符串构造器
     */
    private void appendSymbolTo(StringBuilder builder) {
        if (this.symbol instanceof DString)
            builder.append("\"").append(this.symbol).append("\"");
        else
            builder.append(this.symbol);
    }

    @Override
    public int compareTo(DToken t) {
        return t instanceof Ast && t.hashCode() == this.hashCode() ? -1 : 0;
    }

    @Override
    public int hashCode() {
        int result = this.type().hashCode();
        result = 31 * result + this.symbol.hashCode();
        result = 31 * result + this.children.hashCode();
        result = 31 * result + this.index;
        result = 31 * result + this.source.hashCode();
        result = 31 * result + this.code.hashCode();
        return result;
    }

    /**
     * 语法树类型
     */
    public enum Type {
        PROCEDURE,   // 过程
        BASIC        // 普通
    }
}
