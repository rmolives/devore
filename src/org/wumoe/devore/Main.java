package org.wumoe.devore;

import org.wumoe.devore.lang.Env;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length == 0)
            Repl.repl(System.in, System.out, Env.newEnv().load("core"));
        else if ("--version".equals(args[0]) || "-v".equals(args[0]))
            System.out.println("Devore v" + Devore.VERSION + ".\nWuMoe Community.");
        else
            Devore.call(Env.newEnv().load("core"), Files.readString(Path.of(args[0])));
    }
}
