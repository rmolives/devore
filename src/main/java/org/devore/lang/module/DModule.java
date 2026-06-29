package org.devore.lang.module;

import org.devore.lang.Env;

public abstract class DModule {
    public final String name;

    /**
     * 创建模块基类并记录模块名
     */
    public DModule(String name) {
        this.name = name;
    }

    public abstract void init(Env dEnv);
}
