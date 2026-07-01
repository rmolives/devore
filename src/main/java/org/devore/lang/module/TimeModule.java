package org.devore.lang.module;

import org.devore.exception.DevoreCastException;
import org.devore.exception.DevoreRuntimeException;
import org.devore.lang.Env;
import org.devore.lang.token.DInt;
import org.devore.lang.token.DNumber;
import org.devore.lang.token.DString;
import org.devore.lang.token.DToken;
import org.devore.utils.DIntUtils;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class TimeModule extends DModule {
    private static final String DEFAULT_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    /**
     * 创建Time模块实例
     */
    public TimeModule() {
        super("time");
    }

    /**
     * 初始化Time模块，注册时间格式化和解析过程
     */
    @Override
    public void init(Env dEnv) {
        initTimeProcedures(dEnv);   // 时间格式化和解析过程
    }

    /**
     * 注册时间格式化和解析过程
     */
    private void initTimeProcedures(Env dEnv) {
        dEnv.addTokenProcedure("format-time", ((args, env) ->
                DString.valueOf(formatTime(DIntUtils.toLong(intArg(args.get(0))), DEFAULT_TIME_PATTERN))), 1, false);
        dEnv.addTokenProcedure("format-time", ((args, env) ->
                DString.valueOf(formatTime(DIntUtils.toLong(intArg(args.get(0))), stringArg(args.get(1))))), 2, false);
        dEnv.addTokenProcedure("parse-time", ((args, env) ->
                DNumber.valueOf(parseTime(stringArg(args.get(0)), DEFAULT_TIME_PATTERN))), 1, false);
        dEnv.addTokenProcedure("parse-time", ((args, env) ->
                DNumber.valueOf(parseTime(stringArg(args.get(0)), stringArg(args.get(1))))), 2, false);
    }

    /**
     * 校验并取得整数参数
     */
    private static DInt intArg(DToken token) {
        if (!(token instanceof DInt))
            throw new DevoreCastException(token.type(), "int");
        return (DInt) token;
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
     * 按指定格式格式化时间戳
     */
    private static String formatTime(long timestamp, String pattern) {
        DateTimeFormatter formatter = timeFormatter(pattern);
        try {
            return Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).format(formatter);
        } catch (DateTimeException e) {
            throw new DevoreRuntimeException("格式化时间失败: " + e.getMessage());
        }
    }

    /**
     * 按指定格式解析时间字符串
     */
    private static long parseTime(String time, String pattern) {
        DateTimeFormatter formatter = timeFormatter(pattern);
        ZoneId zone = ZoneId.systemDefault();
        try {
            return ZonedDateTime.parse(time, formatter).toInstant().toEpochMilli();
        } catch (DateTimeParseException ignored) {
            try {
                return OffsetDateTime.parse(time, formatter).toInstant().toEpochMilli();
            } catch (DateTimeParseException ignoredOffset) {
                try {
                    return LocalDateTime.parse(time, formatter).atZone(zone).toInstant().toEpochMilli();
                } catch (DateTimeParseException ignoredLocalDateTime) {
                    try {
                        return LocalDate.parse(time, formatter).atStartOfDay(zone).toInstant().toEpochMilli();
                    } catch (DateTimeParseException e) {
                        throw new DevoreRuntimeException("解析时间失败: " + time);
                    }
                }
            }
        }
    }

    /**
     * 创建并校验时间格式化器
     */
    private static DateTimeFormatter timeFormatter(String pattern) {
        try {
            return DateTimeFormatter.ofPattern(pattern);
        } catch (IllegalArgumentException e) {
            throw new DevoreRuntimeException("时间格式错误: " + pattern);
        }
    }
}
