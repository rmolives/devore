package org.devore.lang.module;

import org.devore.exception.DevoreCastException;
import org.devore.exception.DevoreRuntimeException;
import org.devore.lang.Env;
import org.devore.lang.token.*;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Java反射
 */
public class ReflectModule extends DModule {
    /**
     * 创建Reflect模块实例
     */
    public ReflectModule() {
        super("reflect");
    }

    /**
     * 初始化Java反射模块，注册类加载、构造和方法调用过程
     */
    @Override
    public void init(Env dEnv) {
        initReflectProcedures(dEnv); // Java反射
    }

    /**
     * 注册Java类加载、对象构造和方法调用过程
     */
    private void initReflectProcedures(Env dEnv) {
        dEnv.addTokenProcedure("java-object?", (args, env) ->
                DBool.valueOf(args.get(0) instanceof DJavaObject), 1, false);
        dEnv.addTokenProcedure("reflect-class", (args, env) ->
                DJavaObject.valueOf(classArg(args.get(0))), 1, false);
        dEnv.addTokenProcedure("reflect-class-name", (args, env) ->
                DString.valueOf(classArg(args.get(0)).getName()), 1, false);
        dEnv.addTokenProcedure("reflect-new", (args, env) -> {
            Class<?> clazz = classArg(args.get(0));
            List<DToken> values = args.subList(1, args.size());
            Constructor<?> constructor = bestConstructor(clazz, values);
            try {
                return toToken(constructor.newInstance(convertArgs(constructor.getParameterTypes(), values)));
            } catch (InstantiationException | IllegalAccessException e) {
                throw new DevoreRuntimeException("创建Java对象失败: " + clazz.getName() + ", " + e.getMessage());
            } catch (InvocationTargetException e) {
                throw invocationError(e);
            }
        }, 1, true);
        dEnv.addTokenProcedure("reflect-call", (args, env) -> {
            Object target = objectArg(args.get(0));
            String name = stringArg(args.get(1));
            List<DToken> values = args.subList(2, args.size());
            boolean staticCall = target instanceof Class<?>;
            Class<?> clazz = staticCall ? (Class<?>) target : target.getClass();
            Method method = bestMethod(clazz, name, values, staticCall);
            try {
                Object receiver = Modifier.isStatic(method.getModifiers()) ? null : target;
                return toToken(method.invoke(receiver, convertArgs(method.getParameterTypes(), values)));
            } catch (IllegalAccessException e) {
                throw new DevoreRuntimeException("调用Java方法失败: " + clazz.getName() + "." + name + ", " + e.getMessage());
            } catch (InvocationTargetException e) {
                throw invocationError(e);
            }
        }, 2, true);
        dEnv.addTokenProcedure("reflect-static-call", (args, env) -> {
            Class<?> clazz = classArg(args.get(0));
            String name = stringArg(args.get(1));
            List<DToken> values = args.subList(2, args.size());
            Method method = bestMethod(clazz, name, values, true);
            try {
                return toToken(method.invoke(null, convertArgs(method.getParameterTypes(), values)));
            } catch (IllegalAccessException e) {
                throw new DevoreRuntimeException("调用Java静态方法失败: " + clazz.getName() + "." + name + ", " + e.getMessage());
            } catch (InvocationTargetException e) {
                throw invocationError(e);
            }
        }, 2, true);
    }

    /**
     * 将字符串或Java对象参数转换为Class
     */
    private static Class<?> classArg(DToken token) {
        if (token instanceof DJavaObject && ((DJavaObject) token).value() instanceof Class<?>)
            return (Class<?>) ((DJavaObject) token).value();
        if (!(token instanceof DString))
            throw new DevoreCastException(token.type(), "string|java-object");
        try {
            return Class.forName(token.toString());
        } catch (ClassNotFoundException e) {
            throw new DevoreRuntimeException("Java类不存在: " + token);
        }
    }

    /**
     * 将参数转换为反射调用目标对象
     */
    private static Object objectArg(DToken token) {
        if (token instanceof DJavaObject)
            return ((DJavaObject) token).value();
        if (token instanceof DString) {
            try {
                return Class.forName(token.toString());
            } catch (ClassNotFoundException ignored) {
                return token.toString();
            }
        }
        return toJavaValue(token);
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
     * 选择最匹配的公开构造方法
     */
    private static Constructor<?> bestConstructor(Class<?> clazz, List<DToken> args) {
        return java.util.Arrays.stream(clazz.getConstructors())
                .filter(constructor -> constructor.getParameterCount() == args.size())
                .map(constructor -> new Match<>(constructor, score(constructor.getParameterTypes(), args)))
                .filter(match -> match.score >= 0)
                .min(Comparator.comparingInt(match -> match.score))
                .map(match -> match.value)
                .orElseThrow(() -> new DevoreRuntimeException("找不到匹配的Java构造方法: " + clazz.getName()
                        + "/" + args.size()));
    }

    /**
     * 选择最匹配的公开方法
     */
    private static Method bestMethod(Class<?> clazz, String name, List<DToken> args, boolean staticCall) {
        return java.util.Arrays.stream(clazz.getMethods())
                .filter(method -> method.getName().equals(name))
                .filter(method -> method.getParameterCount() == args.size())
                .filter(method -> !staticCall || Modifier.isStatic(method.getModifiers()))
                .map(method -> new Match<>(method, score(method.getParameterTypes(), args)))
                .filter(match -> match.score >= 0)
                .min(Comparator.comparingInt(match -> match.score))
                .map(match -> match.value)
                .orElseThrow(() -> new DevoreRuntimeException("找不到匹配的Java方法: " + clazz.getName()
                        + "." + name + "/" + args.size()));
    }

    /**
     * 计算参数类型和Devore参数的匹配分数
     */
    private static int score(Class<?>[] parameterTypes, List<DToken> args) {
        int score = 0;
        for (int i = 0; i < parameterTypes.length; ++i) {
            int current = score(parameterTypes[i], args.get(i));
            if (current < 0)
                return -1;
            score += current;
        }
        return score;
    }

    /**
     * 计算参数类型和Devore参数的匹配分数
     */
    private static int score(Class<?> target, DToken token) {
        Class<?> wrapped = wrap(target);
        if (token == DWord.NIL)
            return target.isPrimitive() ? -1 : 8;
        if (token instanceof DJavaObject) {
            Object value = ((DJavaObject) token).value();
            if (value == null)
                return target.isPrimitive() ? -1 : 8;
            return wrapped.isInstance(value) ? 0 : -1;
        }
        if (wrapped == Object.class)
            return 50;
        if (token instanceof DString)
            return wrapped == String.class || wrapped == CharSequence.class ? 0
                    : wrapped == Character.class && token.toString().length() == 1 ? 2 : -1;
        if (token instanceof DBool)
            return wrapped == Boolean.class ? 0 : -1;
        if (token instanceof DInt)
            return Number.class.isAssignableFrom(wrapped) || wrapped == BigInteger.class ? 1 : -1;
        if (token instanceof DNumber)
            return Number.class.isAssignableFrom(wrapped) || wrapped == BigDecimal.class ? 2 : -1;
        if (token instanceof DList)
            return target.isArray() || List.class.isAssignableFrom(wrapped) ? 3 : -1;
        if (token instanceof DTable)
            return Map.class.isAssignableFrom(wrapped) ? 3 : -1;
        return -1;
    }

    /**
     * 按目标参数类型转换全部实参
     */
    private static Object[] convertArgs(Class<?>[] parameterTypes, List<DToken> args) {
        Object[] values = new Object[args.size()];
        for (int i = 0; i < args.size(); ++i)
            values[i] = convert(parameterTypes[i], args.get(i));
        return values;
    }

    /**
     * 将单个Devore值转换为Java值
     */
    private static Object convert(Class<?> target, DToken token) {
        if (token == DWord.NIL)
            return null;
        if (token instanceof DJavaObject)
            return ((DJavaObject) token).value();
        if (token instanceof DString) {
            String value = token.toString();
            if (target == char.class || target == Character.class) {
                if (value.length() != 1)
                    throw new DevoreRuntimeException("字符串不能转换为char: " + value);
                return value.charAt(0);
            }
            return value;
        }
        if (token instanceof DBool)
            return ((DBool) token).bool;
        if (token instanceof DNumber)
            return convertNumber(target, (DNumber) token);
        if (token instanceof DList) {
            List<DToken> values = ((DList) token).toList();
            if (target.isArray()) {
                Class<?> component = target.getComponentType();
                Object array = Array.newInstance(component, values.size());
                for (int i = 0; i < values.size(); ++i)
                    Array.set(array, i, convert(component, values.get(i)));
                return array;
            }
            return values.stream().map(ReflectModule::toJavaValue).collect(Collectors.toList());
        }
        if (token instanceof DTable)
            return toJavaMap((DTable) token);
        return toJavaValue(token);
    }

    /**
     * 将Devore值转换为通用Java值
     */
    private static Object toJavaValue(DToken token) {
        if (token == DWord.NIL)
            return null;
        if (token instanceof DJavaObject)
            return ((DJavaObject) token).value();
        if (token instanceof DString)
            return token.toString();
        if (token instanceof DBool)
            return ((DBool) token).bool;
        if (token instanceof DInt)
            return ((DInt) token).toBigInteger();
        if (token instanceof DNumber)
            return ((DNumber) token).toBigDecimal();
        if (token instanceof DList)
            return ((DList) token).toList().stream().map(ReflectModule::toJavaValue).collect(Collectors.toList());
        if (token instanceof DTable)
            return toJavaMap((DTable) token);
        return token;
    }

    private static Map<Object, Object> toJavaMap(DTable table) {
        Map<Object, Object> result = new HashMap<>();
        table.toMap().forEach((key, value) -> result.put(toJavaValue(key), toJavaValue(value)));
        return result;
    }

    /**
     * 按目标数值类型转换Devore数值
     */
    private static Object convertNumber(Class<?> target, DNumber number) {
        Class<?> wrapped = wrap(target);
        if (wrapped == Byte.class)
            return number.toBigInteger().byteValue();
        if (wrapped == Short.class)
            return number.toBigInteger().shortValue();
        if (wrapped == Integer.class)
            return number.toBigInteger().intValue();
        if (wrapped == Long.class)
            return number.toBigInteger().longValue();
        if (wrapped == Float.class)
            return number.toBigDecimal().floatValue();
        if (wrapped == Double.class)
            return number.toBigDecimal().doubleValue();
        if (wrapped == BigInteger.class)
            return number.toBigInteger();
        if (wrapped == BigDecimal.class || wrapped == Number.class || wrapped == Object.class)
            return number.toBigDecimal();
        return number.toBigDecimal();
    }

    /**
     * 将Java返回值转换为Devore token
     */
    private static DToken toToken(Object value) {
        if (value == null)
            return DWord.NIL;
        if (value instanceof DToken)
            return (DToken) value;
        if (value instanceof String || value instanceof Character || value instanceof Enum<?>)
            return DString.valueOf(value.toString());
        if (value instanceof Boolean)
            return DBool.valueOf((Boolean) value);
        if (value instanceof Byte || value instanceof Short || value instanceof Integer || value instanceof Long)
            return DNumber.valueOf(((Number) value).longValue());
        if (value instanceof BigInteger)
            return DNumber.valueOf((BigInteger) value);
        if (value instanceof Float || value instanceof Double)
            return DNumber.valueOf(((Number) value).doubleValue());
        if (value instanceof BigDecimal)
            return DNumber.valueOf((BigDecimal) value);
        if (value.getClass().isArray()) {
            int length = Array.getLength(value);
            List<DToken> list = new ArrayList<>();
            for (int i = 0; i < length; ++i)
                list.add(toToken(Array.get(value, i)));
            return DList.valueOf(list);
        }
        if (value instanceof Iterable<?>) {
            List<DToken> list = new ArrayList<>();
            for (Object item : (Iterable<?>) value)
                list.add(toToken(item));
            return DList.valueOf(list);
        }
        return DJavaObject.valueOf(value);
    }

    /**
     * 将基本类型转换为包装类型
     */
    private static Class<?> wrap(Class<?> type) {
        if (!type.isPrimitive())
            return type;
        if (type == boolean.class)
            return Boolean.class;
        if (type == byte.class)
            return Byte.class;
        if (type == short.class)
            return Short.class;
        if (type == int.class)
            return Integer.class;
        if (type == long.class)
            return Long.class;
        if (type == float.class)
            return Float.class;
        if (type == double.class)
            return Double.class;
        if (type == char.class)
            return Character.class;
        if (type == void.class)
            return Void.class;
        return type;
    }

    /**
     * 将反射调用异常转换为运行时异常
     */
    private static DevoreRuntimeException invocationError(InvocationTargetException e) {
        Throwable cause = e.getCause() == null ? e : e.getCause();
        return new DevoreRuntimeException("Java调用异常: " + cause.getClass().getName()
                + (cause.getMessage() == null ? "" : ", " + cause.getMessage()));
    }

    private static final class Match<T> {
        private final T value;
        private final int score;

        /**
         * 记录反射候选项及其匹配分数
         */
        private Match(T value, int score) {
            this.value = value;
            this.score = score;
        }
    }
}
