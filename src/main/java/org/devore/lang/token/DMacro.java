package org.devore.lang.token;

import org.devore.parser.AstNode;

import java.util.ArrayList;
import java.util.List;

public class DMacro extends Token {
    private final List<String> params;
    private final AstNode body;

    public DMacro(List<String> params, AstNode body) {
        this.params = params;
        this.body = body;
    }

    private AstNode expand(AstNode body, List<AstNode> asts) {
        for (int i = 0; i < body.size(); ++i) {
            AstNode temp = body.get(i);
            for (int j = 0; j < params.size(); ++j)
                if (temp.symbol.str().equals(params.get(j)) && temp.isEmpty())
                    body.set(i, asts.get(j));
            if (!body.get(i).isEmpty())
                body.set(i, expand(body.get(i), asts));
        }
        return body;
    }

    public AstNode expand(List<AstNode> asts) {
        return expand(body.copy(), asts);
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
        return new DMacro(new ArrayList<>(params), body.copy());
    }

    @Override
    public int compareTo(Token t) {
        return t.hashCode() == this.hashCode() ? 0 : -1;
    }
}
