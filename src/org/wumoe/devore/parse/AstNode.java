package org.wumoe.devore.parse;

import org.wumoe.devore.lang.token.Token;

import java.util.ArrayList;
import java.util.List;

public class AstNode {
    public final static AstNode nullAst = new AstNode(null);
    public final List<AstNode> children;
    public Token op;
    public AstType type;
    public AstNode(Token op) {
        this.op = op;
        this.type = AstType.BASIC;
        this.children = new ArrayList<>();
    }

    public AstNode(Token op, ArrayList<AstNode> child) {
        this.op = op;
        this.type = AstType.BASIC;
        this.children = child;
    }

    public AstNode(Token op, AstType type) {
        this.op = op;
        this.type = type;
        this.children = new ArrayList<>();
    }

    public AstNode(Token op, AstType type, ArrayList<AstNode> child) {
        this.op = op;
        this.type = type;
        this.children = child;
    }

    public AstNode copy() {
        ArrayList<AstNode> list = new ArrayList<AstNode>();
        for (AstNode ast : children)
            list.add(ast.copy());
        AstNode newAst = new AstNode(op, type, list);
        return newAst;
    }

    public void add(AstNode node) {
        children.add(node);
    }

    public AstNode get(int i) {
        return this.children.get(i);
    }

    public void clear() {
        children.clear();
    }

    public int size() {
        return children.size();
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public boolean isNotEmpty() {
        return !isEmpty();
    }

    public boolean isNull() {
        return op == null;
    }

    public boolean isNotNull() {
        return !isNull();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (isEmpty())
            builder.append(op.toString());
        else {
            builder.append("(");
            builder.append(op.toString());
            for (AstNode ast : children)
                builder.append(" ").append(ast.toString());
            builder.append(")");
        }
        return builder.toString();
    }

    public enum AstType {
        FUNCTION, BASIC
    }
}
