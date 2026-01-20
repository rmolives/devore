package org.devore.lang.token;

import org.devore.exception.DevoreRuntimeException;
import org.devore.parser.AstNode;

import java.util.ArrayList;
import java.util.List;

/**
 * 宏
 */
public class DMacro extends DToken {
    private final List<String> params;
    private final List<AstNode> bodys;
    private final List<DMacro> children;

    private DMacro(List<String> params, List<AstNode> bodys) {
        this.params = params;
        this.bodys = bodys;
        this.children = new ArrayList<>();
    }

    /**
     * 创建宏
     * @param params    params
     * @param bodys     bodys
     * @return this
     */
    public static DMacro newMacro(List<String> params, List<AstNode> bodys) {
        return new DMacro(params, bodys);
    }

    /**
     * 添加宏
     * @param macro 宏
     * @return this
     */
    public DMacro addMacro(DMacro macro) {
        if (this.match(macro.params.size()) != null) throw new DevoreRuntimeException("宏定义冲突.");
        this.children.add(macro);
        return this;
    }

    /**
     * 匹配符合条件的宏
     * @param argc  参数数量
     * @return 符合条件的宏
     */
    private DMacro match(int argc) {
        DMacro macro = null;
        if (this.params.size() == argc) macro = this;
        else {
            for (DMacro dm : children) {
                DMacro temp = dm.match(argc);
                if (temp != null) macro = temp;
            }
        }
        return macro;
    }

    /**
     * 进行替换
     * @param body  body
     * @param asts  asts
     * @return 替换后的ast
     */
    private AstNode expand(AstNode body, List<AstNode> asts) {
        for (int j = 0; j < this.params.size(); ++j)
            if (body.isNotNil()
                    && body.symbol instanceof DSymbol
                    && body.symbol.toString().equals(this.params.get(j))) {
                body.symbol = DSymbol.valueOf("apply");
                body.add(0, asts.get(j));
            }
        for (int i = 0; i < body.size(); ++i) {
            AstNode temp = body.get(i);
            for (int j = 0; j < this.params.size(); ++j)
                if (body.isNotNil()
                        && body.symbol instanceof DSymbol
                        && temp.symbol.toString().equals(this.params.get(j))
                        && temp.isEmpty())
                    body.set(i, asts.get(j));
            if (!body.get(i).isEmpty()) body.set(i, expand(body.get(i), asts));
        }
        return body;
    }

    /**
     * 进行替换
     * @param asts  asts
     * @return 替换后的ast
     */
    public List<AstNode> expand(List<AstNode> asts) {
        DMacro dm = this.match(asts.size());
        if (dm == null) throw new DevoreRuntimeException("找不到匹配条件的宏.");
        List<AstNode> result = new ArrayList<>();
        for (AstNode body : dm.bodys) result.add(dm.expand(body.copy(), asts));
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
    public DToken copy() {
        List<AstNode> temp = new ArrayList<>();
        for (AstNode body : this.bodys) temp.add(body.copy());
        return DMacro.newMacro(new ArrayList<>(this.params), temp);
    }

    @Override
    public int compareTo(DToken t) {
        if (this == t) return 0;
        if (!(t instanceof DMacro)) return -1;
        DMacro other = (DMacro) t;
        if (!this.params.equals(other.params)) return -1;
        if (this.bodys.size() != other.bodys.size()) return -1;
        for (int i = 0; i < this.bodys.size(); ++i)
            if (!this.bodys.get(i).equals(other.bodys.get(i))) return -1;
        if (this.children.size() != other.children.size()) return -1;
        for (int i = 0; i < this.children.size(); ++i)
            if (this.children.get(i).compareTo(other.children.get(i)) != 0) return -1;
        return 0;
    }

    @Override
    public int hashCode() {
        int result = this.type().hashCode();
        result = 31 * result + this.params.hashCode();
        result = 31 * result + this.bodys.hashCode();
        result = 31 * result + this.children.hashCode();
        return result;
    }
}
