package org.devore;

import org.devore.exception.DevoreRuntimeException;
import org.devore.lang.Env;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            Env env = Env.newEnv();
            if (Repl.hasInteractiveInput())
                Repl.repl(env);
            else
                Repl.stream(env);
        }
        else if ("--version".equals(args[0]) || "-v".equals(args[0]))
            System.out.println(Devore.VERSION_MESSAGE);
        else {
            Env env = Env.newEnv();
            try {
                Arrays.stream(args).forEach(arg -> {
                    Path path = Paths.get(arg);
                    if (!Files.exists(path)) {
                        System.err.println("文件不存在: " + arg);
                        return;
                    }
                    try {
                        Devore.call(env, new String(Files.readAllBytes(path), StandardCharsets.UTF_8), arg);
                    } catch (DevoreRuntimeException e) {
                        System.err.println(e.getMessage());
                        System.exit(1);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
            } catch (UncheckedIOException e) {
                throw e.getCause();
            }
        }
    }
}
