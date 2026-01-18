package org.devore;

import org.devore.lang.Env;
import org.devore.lang.token.DWord;
import org.devore.lang.token.Token;

import java.io.*;

public class Repl {
    /**
     * REPL
     *
     * @param dEnv 环境
     * @throws IOException 错误
     */
    public static void repl(Env dEnv) throws IOException {
        InputStream in = dEnv.io.in;
        PrintStream out = dEnv.io.out;
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder codeBuilder = new StringBuilder();
        Env env = dEnv;
        int size = 0;
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
            else if (read.startsWith(":version"))
                out.println(Devore.VERSION_MESSAGE);
            else if (read.startsWith(":clear"))
                env = Env.newEnv();
            else {
                codeBuilder.append(read);
                char[] codeCharArray = codeBuilder.toString().toCharArray();
                boolean skip = false;
                while (codeCharArray[index] != '(' && codeCharArray[index] != '[')
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
                    if (codeCharArray[index] == '(' || codeCharArray[index] == '[')
                        ++flag;
                    else if (codeCharArray[index] == ')' || codeCharArray[index] == ']')
                        --flag;
                    ++index;
                } while (index < codeCharArray.length);
            }
            if (flag == 0) {
                Token result = Devore.call(env, codeBuilder.toString());
                codeBuilder = new StringBuilder();
                if (result != DWord.NIL)
                    out.println(result.toString());
            }
            size = flag;
        }
    }
}
