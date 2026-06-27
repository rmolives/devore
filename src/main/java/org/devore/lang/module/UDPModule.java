package org.devore.lang.module;

import org.devore.exception.DevoreCastException;
import org.devore.exception.DevoreRuntimeException;
import org.devore.lang.Env;
import org.devore.lang.token.*;
import org.devore.utils.DByteUtils;
import org.devore.utils.DNetworkUtils;

import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

/**
 * UDP网络
 */
public class UDPModule extends DModule {
    public UDPModule() {
        super("udp");
    }

    @Override
    public void init(Env dEnv) {
        dEnv.addTokenProcedure("udp-socket?", (args, env) ->
                DBool.valueOf(args.get(0) instanceof DUDPSocket), 1, false);
        dEnv.addTokenProcedure("udp-open", (args, env) -> {
            try {
                return DUDPSocket.valueOf(new DatagramSocket());
            } catch (SocketException e) {
                throw new DevoreRuntimeException("打开UDP套接字失败: " + e.getMessage());
            }
        }, 0, false);
        dEnv.addTokenProcedure("udp-bind", (args, env) -> {
            if (!(args.get(0) instanceof DInt))
                throw new DevoreCastException(args.get(0).type(), "int");
            int port = DNetworkUtils.toPort((DInt) args.get(0));
            try {
                return DUDPSocket.valueOf(new DatagramSocket(port));
            } catch (SocketException e) {
                throw new DevoreRuntimeException("UDP绑定失败: " + port + ", " + e.getMessage());
            }
        }, 1, false);
        dEnv.addTokenProcedure("udp-bind", (args, env) -> {
            if (!(args.get(0) instanceof DString))
                throw new DevoreCastException(args.get(0).type(), "string");
            if (!(args.get(1) instanceof DInt))
                throw new DevoreCastException(args.get(1).type(), "int");
            String host = args.get(0).toString();
            int port = DNetworkUtils.toPort((DInt) args.get(1));
            try {
                return DUDPSocket.valueOf(new DatagramSocket(new InetSocketAddress(host, port)));
            } catch (SocketException e) {
                throw new DevoreRuntimeException("UDP绑定失败: " + host + ":" + port + ", " + e.getMessage());
            }
        }, 2, false);
        dEnv.addTokenProcedure("udp-send", (args, env) -> {
            if (!(args.get(0) instanceof DUDPSocket))
                throw new DevoreCastException(args.get(0).type(), "udp-socket");
            if (!(args.get(1) instanceof DString))
                throw new DevoreCastException(args.get(1).type(), "string");
            if (!(args.get(2) instanceof DInt))
                throw new DevoreCastException(args.get(2).type(), "int");
            if (!(args.get(3) instanceof DList))
                throw new DevoreCastException(args.get(3).type(), "list");
            String host = args.get(1).toString();
            int port = DNetworkUtils.toPort((DInt) args.get(2));
            byte[] data = DByteUtils.toBytes((DList) args.get(3));
            try {
                DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName(host), port);
                ((DUDPSocket) args.get(0)).toSocket().send(packet);
                return DWord.NIL;
            } catch (IOException e) {
                throw new DevoreRuntimeException("UDP发送失败: " + host + ":" + port + ", " + e.getMessage());
            }
        }, 4, false);
        dEnv.addTokenProcedure("udp-receive", (args, env) -> {
            if (!(args.get(0) instanceof DUDPSocket))
                throw new DevoreCastException(args.get(0).type(), "udp-socket");
            if (!(args.get(1) instanceof DInt))
                throw new DevoreCastException(args.get(1).type(), "int");
            int size = DNetworkUtils.toPositiveSize((DInt) args.get(1));
            byte[] buffer = new byte[size];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            try {
                ((DUDPSocket) args.get(0)).toSocket().receive(packet);
                byte[] body = new byte[packet.getLength()];
                System.arraycopy(packet.getData(), packet.getOffset(), body, 0, packet.getLength());
                Map<DToken, DToken> result = new HashMap<>();
                result.put(DString.valueOf("host"), DString.valueOf(packet.getAddress().getHostAddress()));
                result.put(DString.valueOf("port"), DNumber.valueOf(packet.getPort()));
                result.put(DString.valueOf("body"), DByteUtils.toList(body));
                return DTable.valueOf(result);
            } catch (SocketTimeoutException e) {
                return DWord.NIL;
            } catch (IOException e) {
                throw new DevoreRuntimeException("UDP接收失败: " + e.getMessage());
            }
        }, 2, false);
        dEnv.addTokenProcedure("udp-set-timeout", (args, env) -> {
            if (!(args.get(0) instanceof DUDPSocket))
                throw new DevoreCastException(args.get(0).type(), "udp-socket");
            if (!(args.get(1) instanceof DInt))
                throw new DevoreCastException(args.get(1).type(), "int");
            try {
                ((DUDPSocket) args.get(0)).toSocket().setSoTimeout(DNetworkUtils.toTimeout((DInt) args.get(1)));
                return DWord.NIL;
            } catch (SocketException e) {
                throw new DevoreRuntimeException("设置UDP超时失败: " + e.getMessage());
            }
        }, 2, false);
        dEnv.addTokenProcedure("udp-close", (args, env) -> {
            if (!(args.get(0) instanceof DUDPSocket))
                throw new DevoreCastException(args.get(0).type(), "udp-socket");
            ((DUDPSocket) args.get(0)).close();
            return DWord.NIL;
        }, 1, false);
    }

}
