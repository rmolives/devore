package org.devore.module;

import org.devore.lang.Env;

/**
 * 模块
 */
public abstract class Module {
    /**
     * 初始化
     * @param dEnv  环境
     */
    public abstract void init(Env dEnv);
}
