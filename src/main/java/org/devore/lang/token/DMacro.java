package org.devore.lang.token;

import org.devore.exception.DevoreRuntimeException;
import org.devore.parser.Ast;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 宏
 */
public class DMacro extends DToken {
    private final String name;              // 宏名
    private final List<String> params;      // params
    private final List<Ast> bodys;          // bodys
    private final List<DMacro> children;    // 子宏

    private DMacro(String name, List<String> params, List<Ast> bodys, List<DMacro> children) {
        this.name = name;
        this.params = params;
        this.bodys = bodys;
        this.children = children;
    }

    /**
     * 创建宏
     *
     * @param params   params
     * @param bodys    bodys
     * @param children 子宏
     * @return this
     */
    public static DMacro newMacro(String name, List<String> params, List<Ast> bodys, List<DMacro> children) {
        return new DMacro(name, params, bodys, children);
    }

    /**
     * 创建宏
     *
     * @param name   宏名
     * @param params params
     * @param bodys  bodys
     * @return this
     */
    public static DMacro newMacro(String name, List<String> params, List<Ast> bodys) {
        return newMacro(name, params, bodys, new ArrayList<>());
    }

    /**
     * 添加宏
     *
     * @param name  宏名
     * @param macro 宏
     * @return this
     */
    public DMacro addMacro(String name, DMacro macro) {
        if (this.match(macro.params.size()) != null)
            throw new DevoreRuntimeException("定义冲突: " + name);
        this.children.add(macro);
        return this;
    }

    /**
     * 复制宏及其所有重载
     *
     * @return 复制后的宏
     */
    public DMacro copy() {
        List<DMacro> copiedChildren = new ArrayList<>();
        for (DMacro child : this.children)
            copiedChildren.add(child.copy());
        return new DMacro(this.name, this.params, this.bodys, copiedChildren);
    }

    /**
     * 替换相同参数数量的宏，不存在时添加为新重载
     *
     * @param macro 宏
     * @return this
     */
    public DMacro setMacro(DMacro macro) {
        if (this.params.size() == macro.params.size()) {
            macro.children.addAll(this.children);
            return macro;
        }
        for (int i = 0; i < this.children.size(); ++i) {
            if (this.children.get(i).params.size() == macro.params.size()) {
                this.children.set(i, macro);
                return this;
            }
        }
        this.children.add(macro);
        return this;
    }

    /**
     * 匹配符合条件的宏
     *
     * @param argc 参数数量
     * @return 符合条件的宏
     */
    private DMacro match(int argc) {
        if (this.params.size() == argc)
            return this;
        return children.stream()
                .filter(dm -> dm.params.size() == argc)
                .findFirst()
                .orElse(null);
    }

    /**
     * 进行替换
     *
     * @param body  body
     * @param nodes nodes
     * @return 替换后的ast
     */
    private Ast expand(Ast body, List<Ast> nodes) {
        if (body.symbol instanceof Ast)
            body.symbol = expand(((Ast) body.symbol).copy(), nodes);
        int paramIndex = paramIndex(body.symbol);
        if (paramIndex >= 0 && body.isEmpty())
            return nodes.get(paramIndex).copy();
        if (paramIndex >= 0)
            body.symbol = nodes.get(paramIndex).copy();
        IntStream.range(0, body.size())
                .forEach(i -> body.set(i, expand(body.get(i), nodes)));
        return body;
    }

    /**
     * 查询符号对应的宏参数位置
     *
     * @param token token
     * @return 参数位置, 不匹配时返回-1
     */
    private int paramIndex(DToken token) {
        if (!(token instanceof DSymbol))
            return -1;
        return IntStream.range(0, this.params.size())
                .filter(i -> token.toString().equals(this.params.get(i)))
                .findFirst()
                .orElse(-1);
    }

    /**
     * 进行替换
     *
     * @param nodes nodes
     * @return 替换后的ast
     */
    public List<Ast> expand(List<Ast> nodes) {
        DMacro dm = this.match(nodes.size());
        if (dm == null)
            throw new DevoreRuntimeException("找不到匹配条件的宏: " + this.name);
        return dm.bodys.stream()
                .map(body -> dm.expand(body.copy(), nodes))
                .collect(Collectors.toList());
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
    public int compareTo(DToken t) {
        if (this == t)
            return 0;
        if (!(t instanceof DMacro))
            return -1;
        DMacro other = (DMacro) t;
        if (!this.name.equals(other.name))
            return -1;
        if (!this.params.equals(other.params))
            return -1;
        if (this.bodys.size() != other.bodys.size())
            return -1;
        if (!IntStream.range(0, this.bodys.size())
                .allMatch(i -> this.bodys.get(i).equals(other.bodys.get(i))))
            return -1;
        return 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.type(), this.name, this.params, this.bodys);
    }
}
