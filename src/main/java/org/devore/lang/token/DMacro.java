package org.devore.lang.token;

import org.devore.exception.DevoreRuntimeException;
import org.devore.parser.AstNode;

import java.util.ArrayList;
import java.util.List;

/**
 * 宏
 */
public class DMacro extends Token {
    private final List<String> params;
    private final List<AstNode> bodys;
    private final List<DMacro> children;

    private DMacro(List<String> params, List<AstNode> bodys) {
        this.params = params;
        this.bodys = bodys;
        this.children = new ArrayList<>();
    }

    public DMacro addMacro(DMacro macro) {
        if (match(macro.params.size()) != null)
            throw new DevoreRuntimeException("宏定义冲突.");
        this.children.add(macro);
        return this;
    }

    private DMacro match(int argSize) {
        DMacro macro = null;
        if (this.params.size() == argSize)
            macro = this;
        else {
            for (DMacro dm : children) {
                DMacro temp = dm.match(argSize);
                if (temp != null)
                    macro = temp;
            }
        }
        return macro;
    }

    public static DMacro newMacro(List<String> params, List<AstNode> bodys) {
        return new DMacro(params, bodys);
    }

    private AstNode expand(AstNode body, List<AstNode> asts) {
        for (int j = 0; j < params.size(); ++j)
            if (body.isNotNil()
                    && body.symbol instanceof DSymbol
                    && body.symbol.toString().equals(params.get(j))) {
                body.symbol = DSymbol.valueOf("apply");
                body.add(0, asts.get(j));
            }
        for (int i = 0; i < body.size(); ++i) {
            AstNode temp = body.get(i);
            for (int j = 0; j < params.size(); ++j)
                if (body.isNotNil()
                        && body.symbol instanceof DSymbol
                        && temp.symbol.toString().equals(params.get(j))
                        && temp.isEmpty())
                    body.set(i, asts.get(j));
            if (!body.get(i).isEmpty())
                body.set(i, expand(body.get(i), asts));
        }
        return body;
    }

    public List<AstNode> expand(List<AstNode> asts) {
        DMacro dm = match(asts.size());
        if (dm == null)
            throw new DevoreRuntimeException("找不到匹配条件的宏.");
        List<AstNode> result = new ArrayList<>();
        for (AstNode body : dm.bodys)
            result.add(dm.expand(body.copy(), asts));
        return result;
    }

    @Override
    public String type() {
        return "macro";
    }

    @Override
    protected String str() {
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
