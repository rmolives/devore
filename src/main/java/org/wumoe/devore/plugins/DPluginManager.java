package org.wumoe.devore.plugins;

import org.wumoe.devore.exception.DevoreRuntimeException;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class DPluginManager {
    public record DPluginConfig(DPlugin plugin, URLClassLoader loader) {
    }

    public static List<DPluginConfig> plugins = new ArrayList<>();

    public static void init() throws IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        File[] pluginFiles = new File("./plugins").listFiles();
        if (pluginFiles != null) {
            for (File pluginFile : pluginFiles) {
                if (pluginFile.isFile() && pluginFile.getName().endsWith(".jar")) {
                    String classPath;
                    try (JarFile jarFile = new JarFile(pluginFile)) {
                        JarEntry entry = jarFile.getJarEntry("plugin.classpath");
                        if (entry == null)
                            throw new DevoreRuntimeException(pluginFile.getName() + "不存在plugin.classpath.");
                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(jarFile.getInputStream(entry)))) {
                            StringBuilder stringBuilder = new StringBuilder();
                            String line;
                            while ((line = reader.readLine()) != null)
                                stringBuilder.append(line);
                            classPath = stringBuilder.toString();
                        }
                        URLClassLoader loader = new URLClassLoader(
                                new URL[]{pluginFile.toURI().toURL()},
                                Thread.currentThread().getContextClassLoader());
                        DPlugin plugin = (DPlugin) loader.loadClass(classPath).getDeclaredConstructor().newInstance();
                        plugin.onEnable();
                        plugins.add(new DPluginConfig(plugin, loader));
                    }
                }
            }
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> plugins.forEach(c -> {
            c.plugin.onDisable();
            try {
                c.loader.close();
            } catch (IOException e) {
                throw new DevoreRuntimeException(e.getMessage());
            }
        })));
    }
}
