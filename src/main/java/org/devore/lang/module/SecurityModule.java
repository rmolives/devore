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
    public SecurityModule() {
        super("security");
    }

    @Override
    public void init(Env dEnv) {
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
            restrictions.removeAll(toRestrictions(args));
            env.setSecurity(new DSecurity(restrictions));
            return DWord.NIL;
        }, 1, true);
        dEnv.addTokenProcedure("security-restrictions", (args, env) ->
                toList(DSecurity.inherited(env).restrictions()), 0, false);
        dEnv.addTokenProcedure("security-restrict?", (args, env) ->
                DBool.valueOf(DSecurity.inherited(env).contains(toRestriction(args.get(0)))), 1, false);
    }

    private static List<DSecurity.Restriction> toRestrictions(List<DToken> tokens) {
        return tokens.stream()
                .map(SecurityModule::toRestriction)
                .distinct()
                .collect(Collectors.toList());
    }

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

    private static DList toList(List<DSecurity.Restriction> restrictions) {
        return DList.valueOf(restrictions.stream()
                .map(restriction -> DString.valueOf(restriction.name().toLowerCase(Locale.ROOT)))
                .collect(Collectors.toList()));
    }
}
