package org.devore.utils;

import org.devore.exception.DevoreRuntimeException;
import org.devore.lang.token.DInt;

/**
 * 网络参数转换与校验
 */
public class DNetworkUtils {
    /**
     * 将整数Token转换为端口号，并检查端口范围
     *
     * @param value DInt值
     * @return 端口号
     */
    public static int toPort(DInt value) {
        int port = DIntUtils.toInt(value);
        if (port < 0 || port > 65535)
            throw new DevoreRuntimeException("端口范围必须是0-65535: " + port);
        return port;
    }

    /**
     * 将整数Token转换为超时时间，并检查不能为负数
     *
     * @param value DInt值
     * @return 超时时间
     */
    public static int toTimeout(DInt value) {
        int timeout = DIntUtils.toInt(value);
        if (timeout < 0)
            throw new DevoreRuntimeException("超时时间不能为负数: " + timeout);
        return timeout;
    }

    /**
     * 将整数Token转换为正数长度，用于网络读取和接收大小
     *
     * @param value DInt值
     * @return 正数长度
     */
    public static int toPositiveSize(DInt value) {
        int size = DIntUtils.toInt(value);
        if (size <= 0)
            throw new DevoreRuntimeException("读取|接受必须大于0: " + size);
        return size;
    }
}
