package org.wumoe.devore;

import org.wumoe.devore.lang.Env;
import org.wumoe.devore.plugins.DPluginManager;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) throws IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        if (args.length == 0) {
            DPluginManager.init();
            Repl.repl(System.in, System.out, Env.newEnv());
        } else if ("--version".equals(args[0]) || "-v".equals(args[0]))
            System.out.println("Devore v" + Devore.VERSION + ".\nAuthor: RMOlive (rmolives@wumoe.org)");
        else {
            DPluginManager.init();
            Devore.call(Env.newEnv(), Files.readString(Path.of(System.getProperty("user.dir") + File.separator + args[0])));
        }
    }
}
