package org.devore.lang.module;

import org.devore.lang.Env;

public abstract class DModule {
    public final String name;

    public DModule(String name) {
        this.name = name;
    }

    public abstract void init(Env dEnv);
}
