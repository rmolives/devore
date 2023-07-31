package org.wumoe.devore;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, ScriptException {
        // Repl.repl(System.in, System.out, Env.newEnv().load("core"));

        ScriptEngineManager manager = new ScriptEngineManager(ClassLoader.getSystemClassLoader());

        ScriptEngine engine = manager.getEngineByName("devore");

        System.out.println(engine.eval("(+ 2 3)"));
    }
}
