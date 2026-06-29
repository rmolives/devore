package org.devore.lang.module;

import org.devore.exception.DevoreCastException;
import org.devore.exception.DevoreRuntimeException;
import org.devore.lang.Env;
import org.devore.lang.token.*;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 网络信息与DNS查询
 */
public class NetModule extends DModule {
    /**
     * 创建Net模块实例
     */
    public NetModule() {
        super("net");
    }

    /**
     * 初始化网络信息模块，注册DNS、主机、IP、MAC和网卡查询过程
     */
    @Override
    public void init(Env dEnv) {
        initNetworkProcedures(dEnv); // 网络信息
    }

    /**
     * 注册DNS、主机、IP、MAC和网卡查询过程
     */
    private void initNetworkProcedures(Env dEnv) {
        dEnv.addTokenProcedure("dns-lookup", (args, env) ->
                addresses(stringArg(args.get(0))), 1, false);
        dEnv.addTokenProcedure("dns-reverse-lookup", (args, env) ->
                DString.valueOf(address(stringArg(args.get(0))).getHostName()), 1, false);
        dEnv.addTokenProcedure("hostname", (args, env) -> {
            try {
                return DString.valueOf(InetAddress.getLocalHost().getHostName());
            } catch (UnknownHostException e) {
                throw new DevoreRuntimeException("获取主机名失败: " + e.getMessage());
            }
        }, 0, false);
        dEnv.addTokenProcedure("ip", (args, env) -> {
            try {
                return DString.valueOf(InetAddress.getLocalHost().getHostAddress());
            } catch (UnknownHostException e) {
                throw new DevoreRuntimeException("获取本机IP失败: " + e.getMessage());
            }
        }, 0, false);
        dEnv.addTokenProcedure("ips", (args, env) ->
                interfaceAddresses(null), 0, false);
        dEnv.addTokenProcedure("ips", (args, env) ->
                interfaceAddresses(interfaceByName(stringArg(args.get(0)))), 1, false);
        dEnv.addTokenProcedure("mac", (args, env) -> {
            NetworkInterface networkInterface = defaultInterface();
            return networkInterface == null ? DWord.NIL : macAddress(networkInterface);
        }, 0, false);
        dEnv.addTokenProcedure("mac", (args, env) ->
                macAddress(interfaceByName(stringArg(args.get(0)))), 1, false);
        dEnv.addTokenProcedure("network-interface", (args, env) ->
                interfaceInfo(interfaceByName(stringArg(args.get(0)))), 1, false);
        dEnv.addTokenProcedure("network-interfaces", (args, env) ->
                networkInterfaces(), 0, false);
    }

    /**
     * 解析主机名对应的全部IP地址
     */
    private static DList addresses(String host) {
        try {
            List<DToken> list = new ArrayList<>();
            for (InetAddress address : InetAddress.getAllByName(host))
                list.add(DString.valueOf(address.getHostAddress()));
            return DList.valueOf(list);
        } catch (UnknownHostException e) {
            throw new DevoreRuntimeException("DNS查询失败: " + host + ", " + e.getMessage());
        }
    }

    /**
     * 解析单个主机或IP地址
     */
    private static InetAddress address(String value) {
        try {
            return InetAddress.getByName(value);
        } catch (UnknownHostException e) {
            throw new DevoreRuntimeException("地址解析失败: " + value + ", " + e.getMessage());
        }
    }

    /**
     * 读取全部网络接口信息
     */
    private static DList networkInterfaces() {
        try {
            List<DToken> list = new ArrayList<>();
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            for (NetworkInterface networkInterface : Collections.list(interfaces))
                list.add(interfaceInfo(networkInterface));
            return DList.valueOf(list);
        } catch (SocketException e) {
            throw new DevoreRuntimeException("获取网络接口失败: " + e.getMessage());
        }
    }

    /**
     * 读取指定或全部网络接口的IP地址
     */
    private static DList interfaceAddresses(NetworkInterface target) {
        try {
            List<DToken> list = new ArrayList<>();
            List<NetworkInterface> interfaces = target == null ? allInterfaces() : Collections.singletonList(target);
            for (NetworkInterface networkInterface : interfaces) {
                for (InetAddress address : Collections.list(networkInterface.getInetAddresses()))
                    list.add(DString.valueOf(address.getHostAddress()));
            }
            return DList.valueOf(list);
        } catch (SocketException e) {
            throw new DevoreRuntimeException("获取IP地址失败: " + e.getMessage());
        }
    }

    /**
     * 将网络接口信息转换为Devore表
     */
    private static DTable interfaceInfo(NetworkInterface networkInterface) {
        Map<DToken, DToken> table = new HashMap<>();
        try {
            table.put(DString.valueOf("name"), DString.valueOf(networkInterface.getName()));
            table.put(DString.valueOf("display-name"), DString.valueOf(networkInterface.getDisplayName()));
            table.put(DString.valueOf("mac"), macAddress(networkInterface));
            table.put(DString.valueOf("ips"), interfaceAddresses(networkInterface));
            table.put(DString.valueOf("up?"), DBool.valueOf(networkInterface.isUp()));
            table.put(DString.valueOf("loopback?"), DBool.valueOf(networkInterface.isLoopback()));
            table.put(DString.valueOf("virtual?"), DBool.valueOf(networkInterface.isVirtual()));
            table.put(DString.valueOf("point-to-point?"), DBool.valueOf(networkInterface.isPointToPoint()));
            table.put(DString.valueOf("multicast?"), DBool.valueOf(networkInterface.supportsMulticast()));
            table.put(DString.valueOf("mtu"), DNumber.valueOf(networkInterface.getMTU()));
            return DTable.valueOf(table);
        } catch (SocketException e) {
            throw new DevoreRuntimeException("获取网络接口信息失败: " + networkInterface.getName() + ", " + e.getMessage());
        }
    }

    /**
     * 读取网络接口MAC地址
     */
    private static DToken macAddress(NetworkInterface networkInterface) {
        try {
            byte[] hardwareAddress = networkInterface.getHardwareAddress();
            if (hardwareAddress == null)
                return DWord.NIL;
            List<String> parts = new ArrayList<>();
            for (byte value : hardwareAddress)
                parts.add(String.format("%02X", value & 0xff));
            return DString.valueOf(String.join(":", parts));
        } catch (SocketException e) {
            throw new DevoreRuntimeException("获取MAC地址失败: " + networkInterface.getName() + ", " + e.getMessage());
        }
    }

    /**
     * 查找默认网络接口
     */
    private static NetworkInterface defaultInterface() {
        try {
            InetAddress local = InetAddress.getLocalHost();
            NetworkInterface networkInterface = NetworkInterface.getByInetAddress(local);
            if (networkInterface != null)
                return networkInterface;
            for (NetworkInterface item : allInterfaces()) {
                if (item.isUp() && !item.isLoopback() && item.getHardwareAddress() != null)
                    return item;
            }
            return null;
        } catch (SocketException | UnknownHostException e) {
            throw new DevoreRuntimeException("获取默认网络接口失败: " + e.getMessage());
        }
    }

    /**
     * 按名称查找网络接口
     */
    private static NetworkInterface interfaceByName(String name) {
        try {
            NetworkInterface networkInterface = NetworkInterface.getByName(name);
            if (networkInterface == null)
                throw new DevoreRuntimeException("网络接口不存在: " + name);
            return networkInterface;
        } catch (SocketException e) {
            throw new DevoreRuntimeException("获取网络接口失败: " + name + ", " + e.getMessage());
        }
    }

    /**
     * 读取系统全部网络接口
     */
    private static List<NetworkInterface> allInterfaces() throws SocketException {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        return Collections.list(interfaces);
    }

    /**
     * 校验并取得字符串参数
     */
    private static String stringArg(DToken token) {
        if (!(token instanceof DString))
            throw new DevoreCastException(token.type(), "string");
        return token.toString();
    }
}
