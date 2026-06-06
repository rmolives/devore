package org.devore;

import org.devore.lang.Env;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length == 0)
            Repl.repl(Env.newEnv());
        else if ("--version".equals(args[0]) || "-v".equals(args[0]))
            System.out.println(Devore.VERSION_MESSAGE);
        else {
            Env env = Env.newEnv();
            for (String arg : args)
                Devore.call(env, Files.readString(Paths.get(arg)));
        }
    }
}
