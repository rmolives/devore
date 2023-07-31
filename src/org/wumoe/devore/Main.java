package org.wumoe.devore;

import org.wumoe.devore.module.Core;
import org.wumoe.devore.lang.Env;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        Env env = Env.newEnv();
        Core.init(env);
        Repl.repl(System.in, System.out, env);
    }
}
