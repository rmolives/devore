package org.devore.lang;

import org.devore.exception.DevoreRuntimeException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 环境安全限制
 */
public class DSecurity {
    private final List<Restriction> restrictions;

    /**
     * 使用指定限制列表创建安全配置
     *
     * @param restrictions 安全限制列表
     */
    public DSecurity(List<Restriction> restrictions) {
        this.restrictions = restrictions;
    }

    /**
     * 创建默认安全配置，默认限制全部敏感操作
     */
    public DSecurity() {
        this.restrictions = new ArrayList<>(Arrays.asList(Restriction.FILE, Restriction.EXEC, Restriction.SECURITY));
    }

    /**
     * 判断是否包含指定安全限制
     *
     * @param restriction 安全限制
     * @return 是否包含指定安全限制
     */
    public boolean contains(Restriction restriction) {
        return this.restrictions.contains(restriction);
    }

    /**
     * 获取安全限制列表副本
     *
     * @return 安全限制列表副本
     */
    public List<Restriction> restrictions() {
        return new ArrayList<>(this.restrictions);
    }

    /**
     * 获取当前环境及父环境合并后的安全配置
     *
     * @param env 环境
     * @return 继承后的安全配置
     */
    public static DSecurity inherited(Env env) {
        List<Restriction> inherited = new ArrayList<>();
        Env temp = env;
        while (temp != null) {
            if (temp.security != null) {
                for (Restriction restriction : temp.security.restrictions)
                    if (!inherited.contains(restriction))
                        inherited.add(restriction);
            }
            temp = temp.father;
        }
        return new DSecurity(inherited);
    }

    /**
     * 检查当前环境是否禁止文件操作
     *
     * @param env 环境
     */
    public static void checkRestrictFile(Env env) {
        Env temp = env;
        while (temp != null && (temp.security == null || !temp.security.contains(Restriction.FILE)))
            temp = temp.father;
        if (temp != null)
            throw new DevoreRuntimeException("当前环境禁止<File>操作.");
    }

    /**
     * 检查当前环境是否禁止执行操作
     *
     * @param env 环境
     */
    public static void checkRestrictExec(Env env) {
        Env temp = env;
        while (temp != null && (temp.security == null
                || !temp.security.contains(Restriction.EXEC)))
            temp = temp.father;
        if (temp != null)
            throw new DevoreRuntimeException("当前环境禁止<Exec>操作.");
    }

    /**
     * 检查当前环境是否禁止修改安全限制
     *
     * @param env 环境
     */
    public static void checkRestrictSecurity(Env env) {
        Env temp = env;
        while (temp != null && (temp.security == null
                || !temp.security.contains(Restriction.SECURITY)))
            temp = temp.father;
        if (temp != null)
            throw new DevoreRuntimeException("当前环境禁止<Security>操作.");
    }

    /**
     * 安全限制类型
     */
    public static enum Restriction {
        FILE,       // 文件操作
        EXEC,       // 执行操作
        SECURITY    // 安全限制修改
    }
}
