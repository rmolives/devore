package org.devore.lang.module;

import org.devore.exception.DevoreCastException;
import org.devore.exception.DevoreRuntimeException;
import org.devore.lang.Env;
import org.devore.lang.token.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * 操作系统相关操作
 */
public class OSModule extends DModule {
    /**
     * 创建OS模块实例
     */
    public OSModule() {
        super("os");
    }

    /**
     * 初始化操作系统模块，注册系统信息、环境变量、系统属性和进程过程
     */
    @Override
    public void init(Env dEnv) {
        initInfoProcedures(dEnv);     // 系统信息
        initEnvProcedures(dEnv);      // 环境变量
        initPropertyProcedures(dEnv); // 系统属性
        initProcessProcedures(dEnv);  // 外部进程
    }

    /**
     * 注册操作系统和JVM运行信息查询过程
     */
    private void initInfoProcedures(Env dEnv) {
        dEnv.addTokenProcedure("os-name", (args, env) ->
                DString.valueOf(System.getProperty("os.name", "")), 0, false);
        dEnv.addTokenProcedure("os-arch", (args, env) ->
                DString.valueOf(System.getProperty("os.arch", "")), 0, false);
        dEnv.addTokenProcedure("os-version", (args, env) ->
                DString.valueOf(System.getProperty("os.version", "")), 0, false);
        dEnv.addTokenProcedure("os-user-name", (args, env) ->
                DString.valueOf(System.getProperty("user.name", "")), 0, false);
        dEnv.addTokenProcedure("os-user-home", (args, env) ->
                DString.valueOf(System.getProperty("user.home", "")), 0, false);
        dEnv.addTokenProcedure("os-current-dir", (args, env) ->
                DString.valueOf(System.getProperty("user.dir", "")), 0, false);
        dEnv.addTokenProcedure("os-line-separator", (args, env) ->
                DString.valueOf(System.lineSeparator()), 0, false);
        dEnv.addTokenProcedure("os-available-processors", (args, env) ->
                DInt.valueOf(Runtime.getRuntime().availableProcessors()), 0, false);
        dEnv.addTokenProcedure("os-free-memory", (args, env) ->
                DInt.valueOf(Runtime.getRuntime().freeMemory()), 0, false);
        dEnv.addTokenProcedure("os-total-memory", (args, env) ->
                DInt.valueOf(Runtime.getRuntime().totalMemory()), 0, false);
        dEnv.addTokenProcedure("os-max-memory", (args, env) ->
                DInt.valueOf(Runtime.getRuntime().maxMemory()), 0, false);
        dEnv.addTokenProcedure("os-process-id", (args, env) ->
                DInt.valueOf(currentProcessId()), 0, false);
    }

    /**
     * 注册环境变量查询过程
     */
    private void initEnvProcedures(Env dEnv) {
        dEnv.addTokenProcedure("os-env", (args, env) ->
                stringOrNil(System.getenv(stringArg(args.get(0)))), 1, false);
        dEnv.addTokenProcedure("os-envs", (args, env) ->
                stringMap(System.getenv()), 0, false);
    }

    /**
     * 注册Java系统属性查询过程
     */
    private void initPropertyProcedures(Env dEnv) {
        dEnv.addTokenProcedure("os-property", (args, env) ->
                stringOrNil(System.getProperty(stringArg(args.get(0)))), 1, false);
        dEnv.addTokenProcedure("os-properties", (args, env) -> {
            Map<DToken, DToken> table = new HashMap<>();
            Properties properties = System.getProperties();
            for (String name : properties.stringPropertyNames())
                table.put(DString.valueOf(name), DString.valueOf(properties.getProperty(name)));
            return DTable.valueOf(table);
        }, 0, false);
    }

    /**
     * 注册外部命令执行过程
     */
    private void initProcessProcedures(Env dEnv) {
        dEnv.addTokenProcedure("os-exec", (args, env) ->
                exec(command(args.get(0)), null), 1, false);
        dEnv.addTokenProcedure("os-exec", (args, env) ->
                exec(command(args.get(0)), new File(stringArg(args.get(1)))), 2, false);
    }

    /**
     * 将Java字符串转换为字符串token或nil
     */
    private static DToken stringOrNil(String value) {
        return value == null ? DWord.NIL : DString.valueOf(value);
    }

    /**
     * 将字符串Map转换为Devore表
     */
    private static DTable stringMap(Map<String, String> map) {
        Map<DToken, DToken> table = new HashMap<>();
        map.forEach((key, value) -> table.put(DString.valueOf(key), DString.valueOf(value)));
        return DTable.valueOf(table);
    }

    /**
     * 校验并取得字符串参数
     */
    private static String stringArg(DToken token) {
        if (!(token instanceof DString))
            throw new DevoreCastException(token.type(), "string");
        return token.toString();
    }

    /**
     * 将字符串或列表参数转换为命令参数列表
     */
    private static List<String> command(DToken token) {
        if (token instanceof DString)
            return shellCommand(token.toString());
        if (!(token instanceof DList))
            throw new DevoreCastException(token.type(), "string|list");
        List<String> command = new ArrayList<>();
        for (DToken item : ((DList) token).toList())
            command.add(stringArg(item));
        if (command.isEmpty())
            throw new DevoreRuntimeException("命令不能为空.");
        return command;
    }

    /**
     * 将命令字符串包装为当前系统Shell命令
     */
    private static List<String> shellCommand(String command) {
        List<String> result = new ArrayList<>();
        if (isWindows()) {
            result.add("cmd");
            result.add("/c");
        } else {
            result.add("sh");
            result.add("-c");
        }
        result.add(command);
        return result;
    }

    /**
     * 判断当前系统是否为Windows
     */
    private static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }

    /**
     * 执行外部命令并收集退出码和输出
     */
    private static DTable exec(List<String> command, File directory) {
        ProcessBuilder builder = new ProcessBuilder(command);
        if (directory != null)
            builder.directory(directory);
        try {
            Process process = builder.start();
            StreamReader stdout = new StreamReader(process.getInputStream());
            StreamReader stderr = new StreamReader(process.getErrorStream());
            stdout.start();
            stderr.start();
            int exitCode = process.waitFor();
            stdout.join();
            stderr.join();
            Map<DToken, DToken> result = new HashMap<>();
            result.put(DString.valueOf("exit-code"), DNumber.valueOf(exitCode));
            result.put(DString.valueOf("stdout"), DString.valueOf(stdout.content()));
            result.put(DString.valueOf("stderr"), DString.valueOf(stderr.content()));
            return DTable.valueOf(result);
        } catch (IOException e) {
            throw new DevoreRuntimeException("执行命令失败: " + String.join(" ", command) + ", " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new DevoreRuntimeException("执行命令被中断: " + String.join(" ", command));
        }
    }

    /**
     * 读取当前JVM进程ID
     */
    private static long currentProcessId() {
        String name = ManagementFactory.getRuntimeMXBean().getName();
        int index = name.indexOf('@');
        if (index < 0)
            return -1L;
        try {
            return Long.parseLong(name.substring(0, index));
        } catch (NumberFormatException e) {
            return -1L;
        }
    }

    private static final class StreamReader extends Thread {
        private final InputStream input;
        private final ByteArrayOutputStream output;
        private IOException exception;

        /**
         * 创建进程输出读取线程
         */
        private StreamReader(InputStream input) {
            this.input = input;
            this.output = new ByteArrayOutputStream();
            setDaemon(true);
        }

        /**
         * 读取进程输出流内容
         */
        @Override
        public void run() {
            byte[] buffer = new byte[8192];
            try {
                int size;
                while ((size = this.input.read(buffer)) != -1)
                    this.output.write(buffer, 0, size);
            } catch (IOException e) {
                this.exception = e;
            }
        }

        /**
         * 返回进程输出文本并传播读取错误
         */
        private String content() {
            if (this.exception != null)
                throw new DevoreRuntimeException("读取命令输出失败: " + this.exception.getMessage());
            return new String(this.output.toByteArray(), StandardCharsets.UTF_8);
        }
    }
}
