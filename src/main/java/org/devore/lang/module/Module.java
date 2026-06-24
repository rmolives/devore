package org.devore.lang.module;

import org.devore.lang.Env;

public abstract class Module {
    public final String name;

    public Module(String name) {
        this.name = name;
    }

    public abstract void init(Env dEnv);
}
