package org.devore.lang.token;

import org.devore.parser.AstNode;

import java.util.ArrayList;
import java.util.List;

public class DMacro extends Token {
    private final List<String> params;
    private final List<AstNode> bodys;

    public DMacro(List<String> params, List<AstNode> bodys) {
        this.params = params;
        this.bodys = bodys;
    }

    private AstNode expand(AstNode body, List<AstNode> asts) {
        for (int i = 0; i < body.size(); ++i) {
            AstNode temp = body.get(i);
            for (int j = 0; j < params.size(); ++j)
                if (temp.symbol.toString().equals(params.get(j)) && temp.isEmpty())
                    body.set(i, asts.get(j));
            if (!body.get(i).isEmpty())
                body.set(i, expand(body.get(i), asts));
        }
        return body;
    }

    public List<AstNode> expand(List<AstNode> asts) {
        List<AstNode> result = new ArrayList<>();
        for (AstNode body : bodys) result.add(expand(body.copy(), asts));
        return result;
    }

    @Override
    public String type() {
        return "macro";
    }

    @Override
    public String str() {
        return "<macro>";
    }

    @Override
    public Token copy() {
        List<AstNode> temp = new ArrayList<>();
        for (AstNode body : bodys)
            temp.add(body.copy());
        return new DMacro(new ArrayList<>(params), temp);
    }

    @Override
    public int compareTo(Token t) {
        return t.hashCode() == this.hashCode() ? 0 : -1;
    }
}
