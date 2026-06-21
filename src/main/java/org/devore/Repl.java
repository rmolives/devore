package org.devore;

import org.devore.exception.DevoreRuntimeException;
import org.devore.lang.Env;
import org.devore.lang.token.DToken;
import org.devore.lang.token.DWord;
import org.devore.parser.Lexer;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Repl {
    private static final int HISTORY_LIMIT = 100;
    private static final List<String> HISTORY = new ArrayList<>();
    private static String terminalState;
    private static boolean manualEcho;
    private static boolean skipLineFeed;
    private static boolean endOfInput;
    private static final Reader INPUT = new InputStreamReader(System.in, StandardCharsets.UTF_8);

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
        resetInputState();
        enableManualEcho();
        try {
            while (true) {
                int promptIndex = codeBuilder.length() == 0 ? sourceIndex + 1 : sourceIndex;
                String prompt = "[Devore#" + promptIndex + "] >>> ";
                out.print(prompt);
                out.flush();
                String read = readLine(prompt);
                if (read == null) {
                    if (codeBuilder.length() > 0)
                        printError(err, codeBuilder.toString(), sourceIndex);
                    break;
                }
                String trimmed = read.trim();
                if (":exit".equals(trimmed))
                    return;
                recordHistory(read);
                if (codeBuilder.length() == 0) {
                    if (trimmed.isEmpty())
                        continue;
                    switch (trimmed) {
                        case ":help":
                            printHelp(out);
                            continue;
                        case ":version":
                            out.println(Devore.VERSION_MESSAGE);
                            continue;
                    }
                    if (trimmed.startsWith(":load ")) {
                        loadFiles(env, out, err, trimmed.substring(6).trim());
                        continue;
                    }
                }
                if (codeBuilder.length() == 0)
                    ++sourceIndex;
                else
                    codeBuilder.append("\n");
                codeBuilder.append(read);
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
        } finally {
            restoreTerminal();
        }
    }

    private static void resetInputState() {
        skipLineFeed = false;
        endOfInput = false;
    }

    private static void recordHistory(String line) {
        if (line.trim().isEmpty())
            return;
        if (!HISTORY.isEmpty() && HISTORY.get(HISTORY.size() - 1).equals(line))
            return;
        HISTORY.add(line);
        if (HISTORY.size() > HISTORY_LIMIT)
            HISTORY.remove(0);
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
            err.println(":load 需要至少一个文件路径.");
            return;
        }
        String[] files = input.split("\\s+");
        for (String file : files) {
            Path path = Paths.get(file);
            if (!Files.exists(path)) {
                err.println("文件不存在: " + file);
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

    /**
     * 判断当前输入是否需要继续读取。
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
     * @param err         输出
     * @param code        代码
     * @param sourceIndex 定位
     */
    private static void printError(PrintStream err, String code, int sourceIndex) {
        try {
            Devore.call(Env.newEnv(), code, "<#" + sourceIndex + ">");
        } catch (DevoreRuntimeException e) {
            err.println(e.getMessage());
        }
    }

    /**
     * 在交互终端中关闭行缓冲和终端回显。
     * 输入改由本程序逐字符回显，因而多行粘贴时每行都能先显示提示符。
     */
    private static void enableManualEcho() {
        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("win")) {
            enableWindowsManualEcho();
            return;
        }
        enableUnixManualEcho();
    }

    private static void enableUnixManualEcho() {
        try {
            terminalState = runStty("-g").trim();
            if (terminalState.isEmpty()) {
                terminalState = null;
                return;
            }
            runStty("-icanon", "-echo", "min", "1", "time", "0");
            manualEcho = true;
            Runtime.getRuntime().addShutdownHook(new Thread(Repl::restoreTerminal));
        } catch (Exception ignored) {
            terminalState = null;
            manualEcho = false;
        }
    }

    private static void enableWindowsManualEcho() {
        try {
            int mode = Integer.parseInt(runPowerShell(windowsConsoleModeScript(null)).trim());
            int newMode = mode & ~0x0006; // ENABLE_LINE_INPUT | ENABLE_ECHO_INPUT
            if (newMode != mode)
                runPowerShell(windowsConsoleModeScript(newMode));
            terminalState = "windows:" + mode;
            manualEcho = true;
            Runtime.getRuntime().addShutdownHook(new Thread(Repl::restoreTerminal));
        } catch (Exception ignored) {
            terminalState = null;
            manualEcho = false;
        }
    }

    private static String runStty(String... arguments) throws IOException, InterruptedException {
        String[] command = new String[arguments.length + 1];
        command[0] = "stty";
        System.arraycopy(arguments, 0, command, 1, arguments.length);
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectInput(ProcessBuilder.Redirect.INHERIT);
        builder.redirectErrorStream(true);
        Process process = builder.start();
        StringBuilder output = new StringBuilder();
        int c;
        while ((c = process.getInputStream().read()) != -1)
            output.append((char) c);
        if (process.waitFor() != 0)
            throw new IOException("stty failed");
        return output.toString();
    }

    private static String runPowerShell(String script) throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder(
                "powershell.exe", "-NoProfile", "-NonInteractive", "-Command", script);
        builder.redirectInput(ProcessBuilder.Redirect.INHERIT);
        builder.redirectErrorStream(true);
        Process process = builder.start();
        StringBuilder output = new StringBuilder();
        int c;
        while ((c = process.getInputStream().read()) != -1)
            output.append((char) c);
        if (process.waitFor() != 0)
            throw new IOException("powershell failed");
        return output.toString();
    }

    private static String windowsConsoleModeScript(Integer mode) {
        StringBuilder script = new StringBuilder()
                .append("$signature=@'\n")
                .append("using System;\n")
                .append("using System.Runtime.InteropServices;\n")
                .append("public static class ConsoleMode {\n")
                .append("[DllImport(\"kernel32.dll\", SetLastError=true)] ")
                .append("public static extern IntPtr GetStdHandle(int nStdHandle);\n")
                .append("[DllImport(\"kernel32.dll\", SetLastError=true)] ")
                .append("public static extern bool GetConsoleMode(IntPtr hConsoleHandle, out int lpMode);\n")
                .append("[DllImport(\"kernel32.dll\", SetLastError=true)] ")
                .append("public static extern bool SetConsoleMode(IntPtr hConsoleHandle, int dwMode);\n")
                .append("}\n")
                .append("'@;\n")
                .append("Add-Type $signature;\n")
                .append("$handle=[ConsoleMode]::GetStdHandle(-10);\n");
        if (mode == null)
            script.append("$mode=0;\n")
                    .append("if(-not [ConsoleMode]::GetConsoleMode($handle,[ref]$mode)){exit 1};\n")
                    .append("Write-Output $mode;\n");
        else
            script.append("if(-not [ConsoleMode]::SetConsoleMode($handle,")
                    .append(mode)
                    .append(")){exit 1};\n");
        return script.toString();
    }

    private static synchronized void restoreTerminal() {
        if (!manualEcho || terminalState == null)
            return;
        manualEcho = false;
        try {
            if (terminalState.startsWith("windows:"))
                runPowerShell(windowsConsoleModeScript(Integer.parseInt(terminalState.substring(8))));
            else
                runStty(terminalState);
        } catch (Exception ignored) {
            // JVM 退出时不能再向调用方报告恢复失败。
        }
    }

    /**
     * 从标准输入逐字符读取一行。交互模式下同时负责字符回显和退格。
     */
    private static String readLine(String prompt) throws IOException {
        if (endOfInput)
            return null;
        StringBuilder line = new StringBuilder();
        int historyCursor = HISTORY.size();
        String currentLine = null;
        while (true) {
            int c = INPUT.read();
            if (skipLineFeed) {
                skipLineFeed = false;
                if (c == '\n')
                    continue;
            }
            if (c == -1 || c == 4) {
                endOfInput = true;
                if (manualEcho)
                    System.out.println();
                return line.length() == 0 ? null : line.toString();
            }
            if (c == '\r') {
                skipLineFeed = true;
                if (manualEcho)
                    System.out.println();
                return line.toString();
            }
            if (c == '\n') {
                if (manualEcho)
                    System.out.println();
                return line.toString();
            }
            if (manualEcho && (c == 8 || c == 127)) {
                if (line.length() > 0) {
                    line.deleteCharAt(line.length() - 1);
                    System.out.print("\b \b");
                    System.out.flush();
                }
                historyCursor = HISTORY.size();
                currentLine = null;
                continue;
            }
            if (c == 27) {
                int key = readEscapeSequence();
                if (key == 'A' || key == 'B') {
                    if (currentLine == null)
                        currentLine = line.toString();
                    historyCursor = updateHistoryLine(prompt, line, historyCursor, currentLine, key);
                    if (historyCursor == HISTORY.size())
                        currentLine = null;
                }
                continue;
            }
            line.append((char) c);
            historyCursor = HISTORY.size();
            currentLine = null;
            if (manualEcho) {
                System.out.print((char) c);
                System.out.flush();
            }
        }
    }

    private static int readEscapeSequence() throws IOException {
        if (!INPUT.ready())
            return 0;
        int c = INPUT.read();
        if (c != '[' && c != 'O')
            return 0;
        while (INPUT.ready()) {
            c = INPUT.read();
            if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || c == '~')
                return c;
        }
        return 0;
    }

    private static int updateHistoryLine(String prompt, StringBuilder line, int cursor, String currentLine, int key) {
        if (HISTORY.isEmpty())
            return cursor;
        if (key == 'A' && cursor > 0)
            --cursor;
        else if (key == 'B' && cursor < HISTORY.size())
            ++cursor;
        else
            return cursor;
        line.setLength(0);
        if (cursor == HISTORY.size())
            line.append(currentLine);
        else
            line.append(HISTORY.get(cursor));
        redrawLine(prompt, line.toString());
        return cursor;
    }

    private static void redrawLine(String prompt, String line) {
        if (!manualEcho)
            return;
        System.out.print("\r");
        System.out.print(prompt);
        System.out.print(line);
        System.out.print("\033[K");
        System.out.flush();
    }
}
