package org.devore.lang;

import org.devore.exception.DevoreRuntimeException;

import java.util.ArrayList;
import java.util.List;

public class DSecurity {
    private final List<Restriction> restrictions;

    public DSecurity(List<Restriction> restrictions) {
        this.restrictions = restrictions;
    }

    public boolean contains(Restriction restriction) {
        return this.restrictions.contains(restriction);
    }

    public static DSecurity inherited(Env env) {
        List<Restriction> inherited = new ArrayList<>();
        Env temp = env;
        while (temp != null) {
            for (Restriction restriction : temp.security.restrictions) {
                if (!inherited.contains(restriction))
                    inherited.add(restriction);
            }
            temp = temp.father;
        }
        return new DSecurity(inherited);
    }

    public static void checkRestrictFile(Env env) {
        Env temp = env;
        while (temp != null && !temp.security.contains(Restriction.FILE))
            temp = temp.father;
        if (temp != null)
            throw new DevoreRuntimeException("当前环境禁止<File>操作.");
    }

    public static void checkRestrictNet(Env env) {
        Env temp = env;
        while (temp != null && !temp.security.contains(Restriction.NET))
            temp = temp.father;
        if (temp != null)
            throw new DevoreRuntimeException("当前环境禁止<Net>操作.");
    }

    public static void checkRestrictExec(Env env) {
        Env temp = env;
        while (temp != null && !temp.security.contains(Restriction.EXEC))
            temp = temp.father;
        if (temp != null)
            throw new DevoreRuntimeException("当前环境禁止<Exec>操作.");
    }

    public static void checkRestrictReflect(Env env) {
        Env temp = env;
        while (temp != null && !temp.security.contains(Restriction.REFLECT))
            temp = temp.father;
        if (temp != null)
            throw new DevoreRuntimeException("当前环境禁止<Reflect>操作.");
    }

    public static enum Restriction {
        FILE, NET, EXEC, REFLECT
    }
}
