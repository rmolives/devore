package org.devore.lang.module;

import org.devore.exception.DevoreCastException;
import org.devore.exception.DevoreRuntimeException;
import org.devore.lang.DSecurity;
import org.devore.lang.Env;
import org.devore.lang.token.*;
import org.devore.utils.DByteUtils;
import org.devore.utils.DNetworkUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * TCP网络
 */
public class TCPModule extends DModule {
    /**
     * 创建TCP模块实例
     */
    public TCPModule() {
        super("tcp");
    }

    /**
     * 初始化TCP模块，注册连接、监听、读写、超时和关闭过程
     */
    @Override
    public void init(Env dEnv) {
        initTcpProcedures(dEnv); // TCP网络
    }

    /**
     * 注册TCP连接、监听、读写、超时和关闭过程
     */
    private void initTcpProcedures(Env dEnv) {
        dEnv.addTokenProcedure("tcp-socket?", (args, env) ->
                DBool.valueOf(args.get(0) instanceof DTCPSocket), 1, false);
        dEnv.addTokenProcedure("tcp-server?", (args, env) ->
                DBool.valueOf(args.get(0) instanceof DTCPServer), 1, false);
        dEnv.addTokenProcedure("tcp-connect", (args, env) -> {
            DSecurity.checkRestrictNet(env);
            if (!(args.get(0) instanceof DString))
                throw new DevoreCastException(args.get(0).type(), "string");
            if (!(args.get(1) instanceof DInt))
                throw new DevoreCastException(args.get(1).type(), "int");
            String host = args.get(0).toString();
            int port = DNetworkUtils.toPort((DInt) args.get(1));
            try {
                return DTCPSocket.valueOf(new Socket(host, port));
            } catch (IOException e) {
                throw new DevoreRuntimeException("TCP连接失败: " + host + ":" + port + ", " + e.getMessage());
            }
        }, 2, false);
        dEnv.addTokenProcedure("tcp-connect", (args, env) -> {
            DSecurity.checkRestrictNet(env);
            if (!(args.get(0) instanceof DString))
                throw new DevoreCastException(args.get(0).type(), "string");
            if (!(args.get(1) instanceof DInt))
                throw new DevoreCastException(args.get(1).type(), "int");
            if (!(args.get(2) instanceof DInt))
                throw new DevoreCastException(args.get(2).type(), "int");
            String host = args.get(0).toString();
            int port = DNetworkUtils.toPort((DInt) args.get(1));
            int timeout = DNetworkUtils.toTimeout((DInt) args.get(2));
            try {
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress(host, port), timeout);
                return DTCPSocket.valueOf(socket);
            } catch (IOException e) {
                throw new DevoreRuntimeException("TCP连接失败: " + host + ":" + port + ", " + e.getMessage());
            }
        }, 3, false);
        dEnv.addTokenProcedure("tcp-listen", (args, env) -> {
            DSecurity.checkRestrictNet(env);
            if (!(args.get(0) instanceof DInt))
                throw new DevoreCastException(args.get(0).type(), "int");
            int port = DNetworkUtils.toPort((DInt) args.get(0));
            try {
                return DTCPServer.valueOf(new ServerSocket(port));
            } catch (IOException e) {
                throw new DevoreRuntimeException("TCP监听失败: " + port + ", " + e.getMessage());
            }
        }, 1, false);
        dEnv.addTokenProcedure("tcp-listen", (args, env) -> {
            DSecurity.checkRestrictNet(env);
            if (!(args.get(0) instanceof DString))
                throw new DevoreCastException(args.get(0).type(), "string");
            if (!(args.get(1) instanceof DInt))
                throw new DevoreCastException(args.get(1).type(), "int");
            String host = args.get(0).toString();
            int port = DNetworkUtils.toPort((DInt) args.get(1));
            try {
                ServerSocket serverSocket = new ServerSocket();
                serverSocket.bind(new InetSocketAddress(host, port));
                return DTCPServer.valueOf(serverSocket);
            } catch (IOException e) {
                throw new DevoreRuntimeException("TCP监听失败: " + host + ":" + port + ", " + e.getMessage());
            }
        }, 2, false);
        dEnv.addTokenProcedure("tcp-accept", (args, env) -> {
            DSecurity.checkRestrictNet(env);
            if (!(args.get(0) instanceof DTCPServer))
                throw new DevoreCastException(args.get(0).type(), "tcp-server");
            try {
                return DTCPSocket.valueOf(((DTCPServer) args.get(0)).toServerSocket().accept());
            } catch (SocketTimeoutException e) {
                return DWord.NIL;
            } catch (IOException e) {
                throw new DevoreRuntimeException("TCP接受连接失败: " + e.getMessage());
            }
        }, 1, false);
        dEnv.addTokenProcedure("tcp-read", (args, env) -> {
            DSecurity.checkRestrictNet(env);
            if (!(args.get(0) instanceof DTCPSocket))
                throw new DevoreCastException(args.get(0).type(), "tcp-socket");
            if (!(args.get(1) instanceof DInt))
                throw new DevoreCastException(args.get(1).type(), "int");
            int size = DNetworkUtils.toPositiveSize((DInt) args.get(1));
            byte[] buffer = new byte[size];
            try {
                InputStream input = ((DTCPSocket) args.get(0)).toSocket().getInputStream();
                int count = input.read(buffer);
                if (count < 0)
                    return DWord.NIL;
                byte[] result = new byte[count];
                System.arraycopy(buffer, 0, result, 0, count);
                return DByteUtils.toList(result);
            } catch (SocketTimeoutException e) {
                return DWord.NIL;
            } catch (IOException e) {
                throw new DevoreRuntimeException("TCP读取失败: " + e.getMessage());
            }
        }, 2, false);
        dEnv.addTokenProcedure("tcp-write", (args, env) -> {
            DSecurity.checkRestrictNet(env);
            if (!(args.get(0) instanceof DTCPSocket))
                throw new DevoreCastException(args.get(0).type(), "tcp-socket");
            if (!(args.get(1) instanceof DList))
                throw new DevoreCastException(args.get(1).type(), "list");
            try {
                OutputStream output = ((DTCPSocket) args.get(0)).toSocket().getOutputStream();
                output.write(DByteUtils.toBytes((DList) args.get(1)));
                output.flush();
                return DWord.NIL;
            } catch (IOException e) {
                throw new DevoreRuntimeException("TCP写入失败: " + e.getMessage());
            }
        }, 2, false);
        dEnv.addTokenProcedure("tcp-set-timeout", (args, env) -> {
            DSecurity.checkRestrictNet(env);
            if (!(args.get(0) instanceof DTCPSocket))
                throw new DevoreCastException(args.get(0).type(), "tcp-socket");
            if (!(args.get(1) instanceof DInt))
                throw new DevoreCastException(args.get(1).type(), "int");
            int timeout = DNetworkUtils.toTimeout((DInt) args.get(1));
            try {
                ((DTCPSocket) args.get(0)).toSocket().setSoTimeout(timeout);
                return DWord.NIL;
            } catch (IOException e) {
                throw new DevoreRuntimeException("设置TCP超时失败: " + e.getMessage());
            }
        }, 2, false);
        dEnv.addTokenProcedure("tcp-server-set-timeout", (args, env) -> {
            DSecurity.checkRestrictNet(env);
            if (!(args.get(0) instanceof DTCPServer))
                throw new DevoreCastException(args.get(0).type(), "tcp-server");
            if (!(args.get(1) instanceof DInt))
                throw new DevoreCastException(args.get(1).type(), "int");
            try {
                ((DTCPServer) args.get(0)).toServerSocket().setSoTimeout(DNetworkUtils.toTimeout((DInt) args.get(1)));
                return DWord.NIL;
            } catch (IOException e) {
                throw new DevoreRuntimeException("设置TCP服务端超时失败: " + e.getMessage());
            }
        }, 2, false);
        dEnv.addTokenProcedure("tcp-close", (args, env) -> {
            DSecurity.checkRestrictNet(env);
            if (args.get(0) instanceof DTCPSocket) {
                ((DTCPSocket) args.get(0)).close();
                return DWord.NIL;
            }
            if (args.get(0) instanceof DTCPServer) {
                ((DTCPServer) args.get(0)).close();
                return DWord.NIL;
            }
            throw new DevoreCastException(args.get(0).type(), "tcp-socket|tcp-server");
        }, 1, false);
    }

}
