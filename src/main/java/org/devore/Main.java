package org.devore;

import org.devore.lang.Env;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            Repl.repl(Env.newEnv());
        } else if ("--version".equals(args[0]) || "-v".equals(args[0]))
            System.out.println(Devore.VERSION_MESSAGE);
        else {
            Devore.call(Env.newEnv(), new String(Files.readAllBytes(
                            Paths.get(System.getProperty("user.dir") + File.separator + args[0])),
                            StandardCharsets.UTF_8));
        }
    }
}
