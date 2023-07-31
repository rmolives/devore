package org.wumoe.devore;

import org.wumoe.devore.lang.Env;

import javax.script.ScriptException;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        Repl.repl(System.in, System.out, Env.newEnv().load("core"));
    }
}
