package org.devore;

import org.devore.exception.DevoreRuntimeException;
import org.devore.lang.Env;
import org.devore.lang.token.DToken;
import org.devore.lang.token.DWord;
import org.devore.parser.Lexer;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

/**
 * REPL
 */
public class Repl {
    private static final int HISTORY_LIMIT = 100;
    private static final int INDENT_SIZE = 4;
    private static final String ANSI_RESET = "\033[0m";
    private static final String ANSI_DIM = "\033[2m";
    private static final String ANSI_CYAN = "\033[36m";
    private static final String ANSI_GREEN = "\033[32m";
    private static final String ANSI_MAGENTA = "\033[35m";
    private static final String ANSI_SYMBOL = "\033[38;5;111m";
    private static final String ANSI_WHITE = "\033[97m";
    private static final String ANSI_YELLOW = "\033[33m";
    private static final String[] RAINBOW_BRACKET_COLORS = {
            "\033[38;5;214m", "\033[38;5;177m", "\033[38;5;81m",
            "\033[38;5;119m", "\033[38;5;75m", "\033[38;5;203m"
    };
    private static final List<String> HISTORY = new ArrayList<>();
    private static String terminalState;
    private static int windowsConsoleMode = -1;
    private static boolean manualEcho;
    private static boolean windowsNativeLoaded;
    private static boolean skipLineFeed;
    private static boolean endOfInput;
    private static final Reader INPUT = new InputStreamReader(System.in, StandardCharsets.UTF_8);
    private static final Deque<String> PASTED_LINES = new ArrayDeque<>();

    /**
     * REPL
     *
     * @param env 环境
     * @throws IOException 错误
     */
    public static void repl(Env env) throws IOException {
        StringBuilder codeBuilder = new StringBuilder();
        int sourceIndex = 0;
        resetInputState();
        enableManualEcho();
        try {
            while (true) {
                int promptIndex = codeBuilder.length() == 0 ? sourceIndex + 1 : sourceIndex;
                String prompt = "[Devore#" + promptIndex + "] >>> ";
                System.out.print(prompt);
                System.out.flush();
                String highlightContext = codeBuilder.toString();
                String read = readLine(prompt, highlightContext, indentationForNextLine(highlightContext));
                if (read == null) {
                    if (codeBuilder.length() > 0)
                        printError(codeBuilder.toString(), sourceIndex);
                    break;
                }
                String trimmed = read.trim();
                if (codeBuilder.length() == 0) {
                    if (trimmed.isEmpty())
                        continue;
                    switch (trimmed) {
                        case ":exit":
                            recordHistory(read);
                            return;
                        case ":help":
                            recordHistory(read);
                            printHelp();
                            continue;
                        case ":version":
                            recordHistory(read);
                            System.out.println(Devore.VERSION_MESSAGE);
                            continue;
                    }
                    if (trimmed.startsWith(":load ")) {
                        recordHistory(read);
                        loadFiles(env, trimmed.substring(6).trim());
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
                recordHistory(code);
                try {
                    DToken result = Devore.call(env, code, "<#" + sourceIndex + ">");
                    if (result != DWord.NIL)
                        System.out.println(result.toString());
                    codeBuilder = new StringBuilder();
                } catch (DevoreRuntimeException e) {
                    System.err.println(e.getMessage());
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
     * @param input 文件列表
     * @throws IOException 错误
     */
    private static void loadFiles(Env env, String input) throws IOException {
        if (input.isEmpty()) {
            System.err.println(":load 需要至少一个文件路径.");
            return;
        }
        String[] files = input.split("\\s+");
        for (String file : files) {
            Path path = Paths.get(file);
            if (!Files.exists(path)) {
                System.err.println("文件不存在: " + file);
                continue;
            }
            String code = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
            try {
                DToken result = Devore.call(env, code, file);
                if (result != DWord.NIL)
                    System.out.println(result);
            } catch (DevoreRuntimeException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    /**
     * 打印帮助
     */
    private static void printHelp() {
        System.out.println(":help      显示帮助");
        System.out.println(":version   显示版本");
        System.out.println(":load FILE 加载文件, 可一次加载多个");
        System.out.println(":exit      退出");
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
     * @param code        代码
     * @param sourceIndex 定位
     */
    private static void printError(String code, int sourceIndex) {
        try {
            Devore.call(Env.newEnv(), code, "<#" + sourceIndex + ">");
        } catch (DevoreRuntimeException e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * 在交互终端中关闭行缓冲和终端回显
     * 输入改由本程序逐字符回显，因而多行粘贴时每行都能先显示提示符
     */
    private static void enableManualEcho() {
        if (isWindows())
            enableWindowsManualEcho();
        else
            enableUnixManualEcho();
    }

    private static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
    }

    private static void enableWindowsManualEcho() {
        if (!loadWindowsNative())
            return;
        int mode = enableWindowsConsoleManualEcho();
        if (mode < 0)
            return;
        windowsConsoleMode = mode;
        manualEcho = true;
        Runtime.getRuntime().addShutdownHook(new Thread(Repl::restoreTerminal));
    }

    private static boolean loadWindowsNative() {
        if (windowsNativeLoaded)
            return true;
        String resource = "/native/" + System.mapLibraryName("devore-repl-console");
        try (InputStream input = Repl.class.getResourceAsStream(resource)) {
            if (input == null)
                return false;
            Path library = Files.createTempFile("devore-repl-console-", ".dll");
            Files.copy(input, library, StandardCopyOption.REPLACE_EXISTING);
            library.toFile().deleteOnExit();
            System.load(library.toAbsolutePath().toString());
            windowsNativeLoaded = true;
            return true;
        } catch (UnsatisfiedLinkError | IOException | SecurityException ignored) {
            windowsNativeLoaded = false;
            return false;
        }
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

    private static synchronized void restoreTerminal() {
        if (!manualEcho)
            return;
        manualEcho = false;
        if (isWindows()) {
            if (windowsNativeLoaded && windowsConsoleMode >= 0)
                restoreWindowsConsole(windowsConsoleMode);
            windowsConsoleMode = -1;
        } else if (terminalState != null) {
            try {
                runStty(terminalState);
            } catch (Exception ignored) {
                // JVM 退出时不能再向调用方报告恢复失败。
            }
        }
    }

    private static native int enableWindowsConsoleManualEcho();

    private static native void restoreWindowsConsole(int mode);

    /**
     * 从标准输入逐字符读取一行。交互模式下同时负责字符回显和退格
     */
    private static String readLine(String prompt, String highlightContext, String initialLine) throws IOException {
        if (endOfInput)
            return null;
        if (!PASTED_LINES.isEmpty()) {
            String pastedLine = PASTED_LINES.removeFirst();
            if (manualEcho) {
                System.out.print(highlightLine(highlightContext, pastedLine));
                System.out.print(ANSI_RESET);
                System.out.println();
            } else
                System.out.println(pastedLine);
            return pastedLine;
        }
        StringBuilder line = new StringBuilder(initialLine);
        int[] cursorIndex = {line.length()};
        int historyCursor = HISTORY.size();
        int terminalColumns = terminalColumns();
        int[] renderedRows = {displayRows(prompt, line.toString(), terminalColumns)};
        int[] renderedCursorRow = {0};
        String currentLine = null;
        if (!initialLine.isEmpty()) {
            if (manualEcho) {
                System.out.print(highlightLine(highlightContext, initialLine));
                System.out.print(ANSI_RESET);
            } else
                System.out.print(initialLine);
            System.out.flush();
        }
        while (true) {
            int c = INPUT.read();
            if (skipLineFeed) {
                skipLineFeed = false;
                if (c == '\n')
                    continue;
            }
            if (c == -1 || c == 4) {
                endOfInput = true;
                if (manualEcho) {
                    renderedRows[0] = redrawLine(prompt, line.toString(), highlightContext, line.length(),
                            renderedRows[0], renderedCursorRow, terminalColumns);
                    System.out.println();
                }
                return line.length() == 0 ? null : line.toString();
            }
            if (manualEcho && c != 27 && c != 8 && c != 127 && INPUT.ready()) {
                String pastedCode = readPasteChunk((char) c);
                String formattedCode = formatPastedCode(pastedCode, highlightContext, line, cursorIndex[0]);
                boolean multiline = formattedCode.indexOf('\n') >= 0 || formattedCode.indexOf('\r') >= 0;
                String[] pastedLines = formattedCode.split("\\R", -1);
                if (isOnlyIndentBeforeCursor(line, cursorIndex[0])) {
                    line.delete(0, cursorIndex[0]);
                    cursorIndex[0] = 0;
                }
                line.insert(cursorIndex[0], pastedLines[0]);
                cursorIndex[0] += pastedLines[0].length();
                historyCursor = HISTORY.size();
                currentLine = null;
                renderedRows[0] = redrawLine(prompt, line.toString(), highlightContext,
                        cursorIndex[0], renderedRows[0], renderedCursorRow, terminalColumns);
                if (multiline) {
                    for (int index = 1; index < pastedLines.length; ++index)
                        PASTED_LINES.addLast(pastedLines[index]);
                    System.out.println();
                    return line.toString();
                }
                continue;
            }
            if (c == '\r') {
                skipLineFeed = true;
                if (manualEcho) {
                    renderedRows[0] = redrawLine(prompt, line.toString(), highlightContext, line.length(),
                            renderedRows[0], renderedCursorRow, terminalColumns);
                    System.out.println();
                }
                return line.toString();
            }
            if (c == '\n') {
                if (manualEcho) {
                    renderedRows[0] = redrawLine(prompt, line.toString(), highlightContext, line.length(),
                            renderedRows[0], renderedCursorRow, terminalColumns);
                    System.out.println();
                }
                return line.toString();
            }
            if (manualEcho && (c == 8 || c == 127)) {
                if (cursorIndex[0] > 0) {
                    line.deleteCharAt(cursorIndex[0] - 1);
                    --cursorIndex[0];
                    renderedRows[0] = redrawLine(prompt, line.toString(), highlightContext,
                            cursorIndex[0], renderedRows[0], renderedCursorRow, terminalColumns);
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
                    historyCursor = updateHistoryLine(prompt, line, historyCursor, currentLine, key,
                            highlightContext, cursorIndex, terminalColumns, renderedRows, renderedCursorRow);
                    if (historyCursor == HISTORY.size())
                        currentLine = null;
                } else if (key == 'C') {
                    if (cursorIndex[0] < line.length()) {
                        ++cursorIndex[0];
                        renderedRows[0] = redrawLine(prompt, line.toString(), highlightContext,
                                cursorIndex[0], renderedRows[0], renderedCursorRow, terminalColumns);
                    }
                } else if (key == 'D') {
                    if (cursorIndex[0] > 0) {
                        --cursorIndex[0];
                        renderedRows[0] = redrawLine(prompt, line.toString(), highlightContext,
                                cursorIndex[0], renderedRows[0], renderedCursorRow, terminalColumns);
                    }
                }
                continue;
            }
            if (manualEcho && (c == ')' || c == ']') && isOnlyIndentBeforeCursor(line, cursorIndex[0])) {
                int deleteCount = Math.min(INDENT_SIZE, cursorIndex[0]);
                line.delete(cursorIndex[0] - deleteCount, cursorIndex[0]);
                cursorIndex[0] -= deleteCount;
            }
            line.insert(cursorIndex[0], (char) c);
            ++cursorIndex[0];
            historyCursor = HISTORY.size();
            currentLine = null;
            if (manualEcho)
                renderedRows[0] = redrawLine(prompt, line.toString(), highlightContext,
                        cursorIndex[0], renderedRows[0], renderedCursorRow, terminalColumns);
        }
    }

    private static String readPasteChunk(char firstChar) throws IOException {
        StringBuilder builder = new StringBuilder().append(firstChar);
        while (INPUT.ready())
            builder.append((char) INPUT.read());
        return builder.toString();
    }

    private static String formatPastedCode(String code, String highlightContext, StringBuilder line, int cursorIndex) {
        String trimmed = trimBlankEdges(code.replace("\r\n", "\n").replace('\r', '\n'));
        if (trimmed.isEmpty())
            return "";
        int baseDepth = indentState(highlightContext + "\n" + line.substring(0, cursorIndex)).bracketDepth;
        return reindentCode(trimmed, baseDepth);
    }

    private static String trimBlankEdges(String code) {
        int start = 0;
        int end = code.length();
        while (start < end && Character.isWhitespace(code.charAt(start)))
            ++start;
        while (end > start && Character.isWhitespace(code.charAt(end - 1)))
            --end;
        return code.substring(start, end);
    }

    private static String reindentCode(String code, int baseDepth) {
        String[] lines = code.split("\n", -1);
        StringBuilder builder = new StringBuilder();
        int relativeDepth = 0;
        for (int index = 0; index < lines.length; ++index) {
            String content = lines[index].trim();
            if (index > 0)
                builder.append('\n');
            if (content.isEmpty())
                continue;
            int lineDepth = Math.max(0, baseDepth + relativeDepth - leadingClosingBrackets(content));
            builder.append(
                    String.join("", Collections.nCopies(lineDepth * INDENT_SIZE, " "))
            ).append(content);
            relativeDepth += depthDelta(content);
            if (baseDepth + relativeDepth < 0)
                relativeDepth = -baseDepth;
        }
        return builder.toString();
    }

    private static int leadingClosingBrackets(String line) {
        int count = 0;
        while (count < line.length() && (line.charAt(count) == ')' || line.charAt(count) == ']'))
            ++count;
        return count;
    }

    private static int depthDelta(String line) {
        int depth = 0;
        boolean inString = false;
        boolean escaped = false;
        for (int index = 0; index < line.length(); ++index) {
            char c = line.charAt(index);
            if (inString) {
                if (escaped)
                    escaped = false;
                else if (c == '\\')
                    escaped = true;
                else if (c == '"')
                    inString = false;
                continue;
            }
            if (c == ';')
                break;
            if (c == '"')
                inString = true;
            else if (c == '(' || c == '[')
                ++depth;
            else if (c == ')' || c == ']')
                --depth;
        }
        return depth;
    }

    private static String indentationForNextLine(String code) {
        if (code.isEmpty())
            return "";
        IndentState state = indentState(code);
        if (state.inString)
            return "";
        int width = Math.max(0, state.bracketDepth) * INDENT_SIZE;
        return String.join("", Collections.nCopies(width, " "));
    }

    private static IndentState indentState(String code) {
        IndentState state = new IndentState();
        for (int index = 0; index < code.length(); ++index) {
            char c = code.charAt(index);
            if (state.inString) {
                if (state.escaped)
                    state.escaped = false;
                else if (c == '\\')
                    state.escaped = true;
                else if (c == '"')
                    state.inString = false;
                continue;
            }
            if (c == ';') {
                while (index + 1 < code.length() && code.charAt(index + 1) != '\n'
                        && code.charAt(index + 1) != '\r')
                    ++index;
                continue;
            }
            if (c == '"') {
                state.inString = true;
                state.escaped = false;
            } else if (c == '(' || c == '[')
                ++state.bracketDepth;
            else if ((c == ')' || c == ']') && state.bracketDepth > 0)
                --state.bracketDepth;
        }
        return state;
    }

    private static boolean isOnlyIndentBeforeCursor(StringBuilder line, int cursorIndex) {
        if (cursorIndex == 0)
            return false;
        for (int index = 0; index < cursorIndex; ++index)
            if (line.charAt(index) != ' ')
                return false;
        return true;
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

    private static int updateHistoryLine(String prompt, StringBuilder line, int cursor, String currentLine, int key,
                                         String highlightContext, int[] cursorIndex, int terminalColumns,
                                         int[] renderedRows, int[] renderedCursorRow) {
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
        cursorIndex[0] = line.length();
        renderedRows[0] = redrawLine(prompt, line.toString(), highlightContext, cursorIndex[0],
                renderedRows[0], renderedCursorRow, terminalColumns);
        return cursor;
    }

    private static int redrawLine(String prompt, String line, String highlightContext, int cursorIndex,
                                  int previousRows, int[] previousCursorRow, int terminalColumns) {
        if (!manualEcho)
            return previousRows;
        clearRenderedInput(previousRows, previousCursorRow[0]);
        System.out.print(prompt);
        System.out.print(highlightLine(highlightContext, line));
        System.out.print(ANSI_RESET);
        System.out.print("\033[K");
        moveCursorToInputIndex(prompt, line, cursorIndex, terminalColumns);
        System.out.flush();
        previousCursorRow[0] = displayPosition(prompt, line, cursorIndex, terminalColumns).row;
        return displayRows(prompt, line, terminalColumns);
    }

    private static void clearRenderedInput(int rows, int cursorRow) {
        System.out.print("\r");
        for (int i = 0; i < cursorRow; ++i)
            System.out.print("\033[1A");
        for (int i = 0; i < rows; ++i) {
            System.out.print("\033[2K");
            if (i < rows - 1)
                System.out.print("\033[1B");
        }
        System.out.print("\r");
        for (int i = 1; i < rows; ++i)
            System.out.print("\033[1A");
    }

    private static int terminalColumns() {
        String columns = System.getenv("COLUMNS");
        if (columns != null) {
            try {
                int value = Integer.parseInt(columns);
                if (value > 0)
                    return value;
            } catch (NumberFormatException ignored) {
                // 继续尝试 stty。
            }
        }
        try {
            String[] size = runStty("size").trim().split("\\s+");
            if (size.length == 2) {
                int value = Integer.parseInt(size[1]);
                if (value > 0)
                    return value;
            }
        } catch (Exception ignored) {
            // 非交互输入或不支持 stty 时使用保守默认值。
        }
        return 80;
    }

    private static int displayRows(String prompt, String line, int terminalColumns) {
        return displayPosition(prompt, line, line.length(), terminalColumns).row + 1;
    }

    private static void moveCursorToInputIndex(String prompt, String line, int cursorIndex, int terminalColumns) {
        DisplayPosition end = displayPosition(prompt, line, line.length(), terminalColumns);
        DisplayPosition cursor = displayPosition(prompt, line, cursorIndex, terminalColumns);
        if (end.row > cursor.row)
            System.out.print("\033[" + (end.row - cursor.row) + "A");
        else if (end.row < cursor.row)
            System.out.print("\033[" + (cursor.row - end.row) + "B");
        System.out.print("\r");
        if (cursor.column > 0)
            System.out.print("\033[" + cursor.column + "C");
    }

    private static DisplayPosition displayPosition(String prompt, String line, int index, int terminalColumns) {
        DisplayPosition position = new DisplayPosition();
        advanceDisplayPosition(position, prompt, prompt.length(), terminalColumns);
        advanceDisplayPosition(position, line, Math.min(index, line.length()), terminalColumns);
        return position;
    }

    private static void advanceDisplayPosition(DisplayPosition position, String text, int endIndex,
                                               int terminalColumns) {
        for (int index = 0; index < endIndex; ) {
            int codePoint = text.codePointAt(index);
            if (codePoint == '\n') {
                ++position.row;
                position.column = 0;
            } else if (codePoint == '\r') {
                position.column = 0;
            } else if (codePoint == '\t') {
                advanceColumns(position, 4, terminalColumns);
            } else if (!Character.isISOControl(codePoint)) {
                advanceColumns(position, isWideCodePoint(codePoint) ? 2 : 1, terminalColumns);
            }
            index += Character.charCount(codePoint);
        }
    }

    private static void advanceColumns(DisplayPosition position, int columns, int terminalColumns) {
        while (columns > 0) {
            int remaining = terminalColumns - position.column;
            if (columns < remaining) {
                position.column += columns;
                return;
            }
            columns -= remaining;
            ++position.row;
            position.column = 0;
        }
    }

    private static boolean isWideCodePoint(int codePoint) {
        Character.UnicodeBlock block = Character.UnicodeBlock.of(codePoint);
        return block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || block == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || block == Character.UnicodeBlock.HIRAGANA
                || block == Character.UnicodeBlock.KATAKANA
                || block == Character.UnicodeBlock.HANGUL_SYLLABLES
                || block == Character.UnicodeBlock.HANGUL_JAMO
                || block == Character.UnicodeBlock.HANGUL_COMPATIBILITY_JAMO;
    }

    private static String highlightLine(String context, String line) {
        StringBuilder builder = new StringBuilder();
        HighlightState state = highlightState(context);
        scanHighlight(line, state, builder);
        return builder.toString();
    }

    private static HighlightState highlightState(String context) {
        HighlightState state = new HighlightState();
        scanHighlight(context, state, null);
        return state;
    }

    private static void scanHighlight(String text, HighlightState state, StringBuilder builder) {
        int index = 0;
        while (index < text.length()) {
            char c = text.charAt(index);
            if (state.inString) {
                appendColored(builder, String.valueOf(c), state.stringColor);
                if (state.escaped)
                    state.escaped = false;
                else if (c == '\\')
                    state.escaped = true;
                else if (c == '"') {
                    state.inString = false;
                    state.stringColor = ANSI_GREEN;
                }
                ++index;
                continue;
            }
            if (c == ';') {
                int end = readCommentEnd(text, index);
                appendColored(builder, text.substring(index, end), ANSI_DIM + ANSI_GREEN);
                index = end;
                continue;
            }
            if (c == '"') {
                boolean listHead = isExpectingHead(state);
                String bracketColor = currentBracketColor(state);
                consumeExpectedHead(state);
                state.stringColor = listHead ? avoidColor(ANSI_GREEN, bracketColor) : ANSI_GREEN;
                appendColored(builder, String.valueOf(c), state.stringColor);
                state.inString = true;
                state.escaped = false;
                ++index;
                continue;
            }
            if (c == '(' || c == '[') {
                consumeExpectedHead(state);
                String color = bracketColor(state.bracketColors.size());
                state.bracketColors.add(color);
                state.expectingHead.add(!state.lambdaParamsPending);
                state.lambdaParamsPending = false;
                appendColored(builder, String.valueOf(c), color);
                ++index;
                continue;
            }
            if (c == ')' || c == ']') {
                String color = state.bracketColors.isEmpty()
                        ? bracketColor(0)
                        : state.bracketColors.remove(state.bracketColors.size() - 1);
                appendColored(builder, String.valueOf(c), color);
                if (!state.expectingHead.isEmpty())
                    state.expectingHead.remove(state.expectingHead.size() - 1);
                ++index;
                continue;
            }
            if (Character.isWhitespace(c)) {
                if (builder != null)
                    builder.append(c);
                ++index;
                continue;
            }
            int tokenEnd = readTokenEnd(text, index);
            String token = text.substring(index, tokenEnd);
            boolean listHead = isExpectingHead(state);
            String bracketColor = currentBracketColor(state);
            consumeExpectedHead(state);
            appendColored(builder, token, colorForToken(token, isCommandToken(text, index), listHead, bracketColor));
            if (listHead && "lambda".equals(token))
                state.lambdaParamsPending = true;
            else if (!Character.isWhitespace(c))
                state.lambdaParamsPending = false;
            index = tokenEnd;
        }
    }

    private static int readCommentEnd(String text, int start) {
        int index = start;
        while (index < text.length() && text.charAt(index) != '\n' && text.charAt(index) != '\r')
            ++index;
        return index;
    }

    private static int readTokenEnd(String line, int start) {
        int index = start;
        while (index < line.length()) {
            char c = line.charAt(index);
            if (Character.isWhitespace(c) || c == '(' || c == ')' || c == '[' || c == ']' || c == ';')
                break;
            ++index;
        }
        return index;
    }

    private static String bracketColor(int depth) {
        return RAINBOW_BRACKET_COLORS[depth % RAINBOW_BRACKET_COLORS.length];
    }

    private static boolean isExpectingHead(HighlightState state) {
        return !state.expectingHead.isEmpty() && state.expectingHead.get(state.expectingHead.size() - 1);
    }

    private static String currentBracketColor(HighlightState state) {
        if (state.bracketColors.isEmpty())
            return null;
        return state.bracketColors.get(state.bracketColors.size() - 1);
    }

    private static void consumeExpectedHead(HighlightState state) {
        if (isExpectingHead(state))
            state.expectingHead.set(state.expectingHead.size() - 1, false);
    }

    private static boolean isCommandToken(String text, int tokenStart) {
        if (tokenStart >= text.length() || text.charAt(tokenStart) != ':')
            return false;
        for (int index = 0; index < tokenStart; ++index)
            if (!Character.isWhitespace(text.charAt(index)))
                return false;
        return true;
    }

    private static String colorForToken(String token, boolean commandToken, boolean listHead, String bracketColor) {
        if (commandToken)
            return ANSI_CYAN;
        String color;
        if (isNumberToken(token))
            color = ANSI_YELLOW;
        else if ("true".equals(token) || "false".equals(token) || "nil".equals(token))
            color = ANSI_MAGENTA;
        else if (listHead)
            color = ANSI_CYAN;
        else
            color = ANSI_SYMBOL;
        return listHead ? avoidColor(color, bracketColor) : avoidColor(color, bracketColor, ANSI_CYAN);
    }

    private static String avoidColor(String color, String... forbiddenColors) {
        if (color == null)
            return null;
        for (String forbiddenColor : forbiddenColors) {
            if (color.equals(forbiddenColor))
                return ANSI_WHITE;
        }
        return color;
    }

    private static boolean isNumberToken(String token) {
        int index = token.startsWith("-") ? 1 : 0;
        if (index == token.length())
            return false;
        boolean digit = false;
        while (index < token.length() && Character.isDigit(token.charAt(index))) {
            digit = true;
            ++index;
        }
        if (index < token.length() && token.charAt(index) == '.') {
            ++index;
            while (index < token.length() && Character.isDigit(token.charAt(index))) {
                digit = true;
                ++index;
            }
        }
        return digit && index == token.length();
    }

    private static void appendColored(StringBuilder builder, String text, String color) {
        if (builder == null)
            return;
        if (color == null)
            builder.append(text);
        else
            builder.append(color).append(text).append(ANSI_RESET);
    }

    private static class HighlightState {
        private final List<Boolean> expectingHead = new ArrayList<>();
        private final List<String> bracketColors = new ArrayList<>();
        private boolean inString;
        private boolean escaped;
        private boolean lambdaParamsPending;
        private String stringColor = ANSI_GREEN;
    }

    private static class IndentState {
        private int bracketDepth;
        private boolean inString;
        private boolean escaped;
    }

    private static class DisplayPosition {
        private int row;
        private int column;
    }
}
