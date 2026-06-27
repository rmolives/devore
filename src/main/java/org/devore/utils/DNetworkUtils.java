package org.devore.utils;

import org.devore.exception.DevoreRuntimeException;
import org.devore.lang.token.DInt;

/**
 * 网络参数转换与校验
 */
public class DNetworkUtils {
    public static int toPort(DInt value) {
        int port = DIntUtils.toInt(value);
        if (port < 0 || port > 65535)
            throw new DevoreRuntimeException("端口范围必须是0-65535: " + port);
        return port;
    }

    public static int toTimeout(DInt value) {
        int timeout = DIntUtils.toInt(value);
        if (timeout < 0)
            throw new DevoreRuntimeException("超时时间不能为负数: " + timeout);
        return timeout;
    }

    public static int toPositiveSize(DInt value) {
        int size = DIntUtils.toInt(value);
        if (size <= 0)
            throw new DevoreRuntimeException("读取|接受必须大于0: " + size);
        return size;
    }
}
