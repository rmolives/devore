package org.devore.lang.module;

import org.devore.exception.DevoreCastException;
import org.devore.exception.DevoreRuntimeException;
import org.devore.lang.DSecurity;
import org.devore.lang.Env;
import org.devore.lang.token.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * 安全限制
 */
public class SecurityModule extends DModule {
    /**
     * 创建Security模块实例
     */
    public SecurityModule() {
        super("security");
    }

    /**
     * 初始化安全限制环境，注册安全限制设置、删除和查询过程
     */
    @Override
    public void init(Env dEnv) {
        initMutationProcedures(dEnv);   // 安全限制设置和删除
        initQueryProcedures(dEnv);      // 安全限制查询
    }

    /**
     * 注册修改安全限制相关过程，包括设置、清空和删除限制
     */
    private void initMutationProcedures(Env dEnv) {
        dEnv.addTokenProcedure("security", (args, env) -> {
            DSecurity.checkRestrictSecurity(env);
            env.setSecurity(new DSecurity(toRestrictions(args)));
            return DWord.NIL;
        }, 0, true);
        dEnv.addTokenProcedure("security-clear!", (args, env) -> {
            DSecurity.checkRestrictSecurity(env);
            env.setSecurity(new DSecurity(new ArrayList<>()));
            return DWord.NIL;
        }, 0, false);
        dEnv.addTokenProcedure("security-remove!", (args, env) -> {
            DSecurity.checkRestrictSecurity(env);
            List<DSecurity.Restriction> restrictions = env.security.restrictions();
            List<DSecurity.Restriction> removeRestrictions = toRestrictions(args);
            for (DSecurity.Restriction restriction : removeRestrictions) {
                if (!restrictions.contains(restriction))
                    throw new DevoreRuntimeException("只能删除当前环境直接设置的安全限制: "
                            + restriction.name().toLowerCase(Locale.ROOT));
            }
            restrictions.removeAll(removeRestrictions);
            env.setSecurity(new DSecurity(restrictions));
            return DWord.NIL;
        }, 1, true);
    }

    /**
     * 注册查询安全限制相关过程，包括获取生效限制和判断指定限制是否生效
     */
    private void initQueryProcedures(Env dEnv) {
        dEnv.addTokenProcedure("security-restrictions", (args, env) ->
                toList(DSecurity.inherited(env).restrictions()), 0, false);
        dEnv.addTokenProcedure("security-restrict?", (args, env) ->
                DBool.valueOf(DSecurity.inherited(env).contains(toRestriction(args.get(0)))), 1, false);
    }

    /**
     * 将Token列表转换为去重后的安全限制列表
     */
    private static List<DSecurity.Restriction> toRestrictions(List<DToken> tokens) {
        return tokens.stream()
                .map(SecurityModule::toRestriction)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 将字符串Token转换为安全限制枚举
     */
    private static DSecurity.Restriction toRestriction(DToken token) {
        if (!(token instanceof DString))
            throw new DevoreCastException(token.type(), "string");
        String name = token.toString().trim().replace('-', '_').toUpperCase(Locale.ROOT);
        try {
            return DSecurity.Restriction.valueOf(name);
        } catch (IllegalArgumentException e) {
            throw new DevoreRuntimeException("未知安全限制: " + token + ", 可选值: file, net, exec, reflect, security.");
        }
    }

    /**
     * 将安全限制列表转换为语言层字符串列表
     */
    private static DList toList(List<DSecurity.Restriction> restrictions) {
        return DList.valueOf(restrictions.stream()
                .map(restriction -> DString.valueOf(restriction.name().toLowerCase(Locale.ROOT)))
                .collect(Collectors.toList()));
    }
}
