package org.wumoe.devore;

import org.wumoe.devore.lang.Env;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            Repl.repl(System.in, System.out, Env.newEnv());
        } else if ("--version".equals(args[0]) || "-v".equals(args[0]))
            System.out.println(Devore.VERSION_MESSAGE);
        else {
            Devore.call(Env.newEnv(), Files.readString(Path.of(System.getProperty("user.dir") + File.separator + args[0])));
        }
    }
}
