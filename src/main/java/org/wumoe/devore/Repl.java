package org.wumoe.devore;

import org.wumoe.devore.lang.Env;
import org.wumoe.devore.lang.token.DWord;
import org.wumoe.devore.lang.token.Token;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class Repl {
    public static void repl(InputStream in, PrintStream out, Env dEnv) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        var codeBuilder = new StringBuilder();
        var env = dEnv;
        var size = 0;
        while (true) {
            out.print("[Devore] >>> ");
            if (size > 0)
                codeBuilder.append(" ");
            while (size-- > 0)
                out.print("    ");
            int index = 0;
            int flag = 0;
            String read = reader.readLine();
            if (read.startsWith(":exit"))
                break;
            else if (read.startsWith(":clear"))
                env = Env.newEnv();
            else if (read.startsWith(":load"))
                codeBuilder.append(Files.readString(Path.of(read.substring(6))));
            else {
                codeBuilder.append(read);
                char[] codeCharArray = codeBuilder.toString().toCharArray();
                boolean skip = false;
                while (codeCharArray[index] != '(')
                    ++index;
                do {
                    if (index < codeCharArray.length - 1 && codeCharArray[index] == '\\') {
                        skip = true;
                        ++index;
                        continue;
                    }
                    if (codeCharArray[index] == '\"') {
                        do {
                            if (skip) {
                                skip = false;
                                ++index;
                                continue;
                            }
                            ++index;
                            if (codeCharArray[index] == '\"')
                                break;
                        } while (index < codeCharArray.length - 1);
                    }
                    if (codeCharArray[index] == '(')
                        ++flag;
                    else if (codeCharArray[index] == ')')
                        --flag;
                    ++index;
                } while (index < codeCharArray.length);
            }
            if (flag == 0) {
                Token result = Devore.call(env, codeBuilder.toString());
                codeBuilder = new StringBuilder();
                if (result != DWord.WORD_NIL)
                    out.println(result.toString());
            }
            size = flag;
        }
    }
}
