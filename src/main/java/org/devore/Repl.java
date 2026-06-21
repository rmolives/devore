package org.devore;

import org.devore.exception.DevoreRuntimeException;
import org.devore.lang.Env;
import org.devore.lang.token.DToken;
import org.devore.lang.token.DWord;
import org.devore.parser.Lexer;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Repl {
    /**
     * REPL
     *
     * @param env 环境
     * @throws IOException 错误
     */
    public static void repl(Env env) throws IOException {
        PrintStream out = env.io.out;
        PrintStream err = env.io.err;
        StringBuilder codeBuilder = new StringBuilder();
        int sourceIndex = 0;
        label:
        while (true) {
            if (codeBuilder.length() == 0 && !env.io.reader.ready())
                out.print("[Devore#" + ++sourceIndex + "] >>> ");
            String read = env.io.reader.readLine();
            if (read == null) {
                if (codeBuilder.length() > 0)
                    printError(err, codeBuilder.toString(), sourceIndex);
                break;
            }
            if (codeBuilder.length() == 0) {
                switch (read.trim()) {
                    case "":
                        continue;
                    case ":exit":
                        break label;
                    case ":help":
                        printHelp(out);
                        continue;
                    case ":version":
                        out.println(Devore.VERSION_MESSAGE);
                        continue;
                }
                if (read.trim().startsWith(":load ")) {
                    loadFiles(env, out, err, read.trim().substring(6).trim());
                    continue;
                }
            }
            if (codeBuilder.length() > 0)
                codeBuilder.append("\n");
            codeBuilder.append(read);
            while (env.io.reader.ready()) {
                String buffered = env.io.reader.readLine();
                if (buffered == null)
                    break;
                codeBuilder.append("\n").append(buffered);
            }
            String code = codeBuilder.toString();
            if (isIncomplete(code))
                continue;
            try {
                DToken result = Devore.call(env, code, "<#" + sourceIndex + ">");
                if (result != DWord.NIL)
                    out.println(result.toString());
                codeBuilder = new StringBuilder();
            } catch (DevoreRuntimeException e) {
                err.println(e.getMessage());
                codeBuilder = new StringBuilder();
            }
        }
    }

    /**
     * 加载文件
     *
     * @param env   环境
     * @param out   输出
     * @param input 文件列表
     * @throws IOException 错误
     */
    private static void loadFiles(Env env, PrintStream out, PrintStream err, String input) throws IOException {
        if (input.isEmpty()) {
            out.println(":load 需要至少一个文件路径.");
            return;
        }
        String[] files = input.split("\\s+");
        for (String file : files) {
            Path path = Paths.get(file);
            if (!Files.exists(path)) {
                out.println("文件不存在: " + file);
                continue;
            }
            String code = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
            try {
                DToken result = Devore.call(env, code, file);
                if (result != DWord.NIL)
                    out.println(result);
            } catch (DevoreRuntimeException e) {
                err.println(e.getMessage());
            }
        }
    }

    /**
     * 判断当前输入是否需要继续读取
     *
     * @param code 代码
     * @return 是否未完成
     */
    private static boolean isIncomplete(String code) {
        try {
            Lexer.splitCode(code);
            return false;
        } catch (RuntimeException e) {
            String message = e.getMessage();
            return message != null && (message.contains("括号未闭合") || message.contains("字符串未闭合"));
        }
    }

    /**
     * 打印错误
     *
     * @param err  输出
     * @param code 代码
     * @param sourceIndex 定位
     */
    private static void printError(PrintStream err, String code, int sourceIndex) {
        try {
            Devore.call(Env.newEnv(), code, "<#" +  sourceIndex + ">");
        } catch (DevoreRuntimeException e) {
            err.println(e.getMessage());
        }
    }

    /**
     * 打印帮助
     *
     * @param out 输出
     */
    private static void printHelp(PrintStream out) {
        out.println(":help      显示帮助");
        out.println(":version   显示版本");
        out.println(":load FILE 加载文件, 可一次加载多个");
        out.println(":exit      退出");
    }
}
