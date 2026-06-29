package org.devore.lang.module;

import org.devore.exception.DevoreCastException;
import org.devore.lang.Env;
import org.devore.lang.token.DBool;
import org.devore.lang.token.DList;
import org.devore.lang.token.DNumber;
import org.devore.lang.token.DString;
import org.devore.lang.token.DToken;
import org.devore.lang.token.DWord;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class RegexModule extends DModule {
    /**
     * 创建Regex模块实例
     */
    public RegexModule() {
        super("regex");
    }

    /**
     * 初始化正则模块，注册匹配、查找、替换、拆分和转义过程
     */
    @Override
    public void init(Env dEnv) {
        initRegexProcedures(dEnv); // 正则表达式
    }

    /**
     * 注册正则匹配、查找、替换、拆分和转义过程
     */
    private void initRegexProcedures(Env dEnv) {
        dEnv.addTokenProcedure("regex-match", (args, env) ->
                DBool.valueOf(pattern(args.get(0)).matcher(string(args.get(1))).matches()), 2, false);
        dEnv.addTokenProcedure("regex-find", (args, env) -> {
            Matcher matcher = pattern(args.get(0)).matcher(string(args.get(1)));
            return matcher.find() ? matchResult(matcher) : DWord.NIL;
        }, 2, false);
        dEnv.addTokenProcedure("regex-find-all", (args, env) -> {
            Matcher matcher = pattern(args.get(0)).matcher(string(args.get(1)));
            List<DToken> results = new ArrayList<>();
            while (matcher.find())
                results.add(matchResult(matcher));
            return DList.valueOf(results);
        }, 2, false);
        dEnv.addTokenProcedure("regex-replace", (args, env) ->
                DString.valueOf(pattern(args.get(0)).matcher(string(args.get(1)))
                        .replaceAll(string(args.get(2)))), 3, false);
        dEnv.addTokenProcedure("regex-split", (args, env) ->
                DList.valueOf(Arrays.stream(pattern(args.get(0)).split(string(args.get(1)), -1))
                        .map(DString::valueOf)
                        .collect(Collectors.toList())), 2, false);
        dEnv.addTokenProcedure("regex-quote", (args, env) ->
                DString.valueOf(Pattern.quote(string(args.get(0)))), 1, false);
    }

    /**
     * 将字符串参数编译为正则模式
     */
    private static Pattern pattern(DToken token) {
        return Pattern.compile(string(token));
    }

    /**
     * 校验并取得字符串参数
     */
    private static String string(DToken token) {
        if (!(token instanceof DString))
            throw new DevoreCastException(token.type(), "string");
        return token.toString();
    }

    /**
     * 将正则匹配结果转换为Devore列表
     */
    private static DList matchResult(Matcher matcher) {
        List<DToken> groups = new ArrayList<>();
        for (int i = 0; i <= matcher.groupCount(); i++)
            groups.add(group(matcher, i));
        return DList.valueOf(Arrays.asList(
                DString.valueOf(matcher.group()),
                DNumber.valueOf(matcher.start()),
                DNumber.valueOf(matcher.end()),
                DList.valueOf(groups)
        ));
    }

    /**
     * 读取匹配分组，未匹配时返回nil
     */
    private static DToken group(Matcher matcher, int index) {
        String group = matcher.group(index);
        return group == null ? DWord.NIL : DString.valueOf(group);
    }
}
