package org.devore;

import org.devore.exception.DevoreRuntimeException;
import org.devore.lang.Env;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length == 0)
            Repl.repl(Env.newEnv());
        else if ("--version".equals(args[0]) || "-v".equals(args[0]))
            System.out.println(Devore.VERSION_MESSAGE);
        else {
            Env env = Env.newEnv();
            for (String arg : args) {
                Path path = Paths.get(arg);
                if (!Files.exists(path)) {
                    System.err.println("文件不存在: " + arg);
                    continue;
                }
                try {
                    Devore.call(env, new String(Files.readAllBytes(path), StandardCharsets.UTF_8), arg);
                } catch (DevoreRuntimeException e) {
                    System.err.println(e.getMessage());
                    System.exit(1);
                }
            }
        }
    }
}
