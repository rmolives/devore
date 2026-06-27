package org.devore.lang.token;

import org.devore.exception.DevoreRuntimeException;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Objects;

/**
 * TCP服务端
 */
public class DTCPServer extends DToken {
    private final ServerSocket serverSocket;

    private DTCPServer(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public static DTCPServer valueOf(ServerSocket serverSocket) {
        return new DTCPServer(serverSocket);
    }

    public ServerSocket toServerSocket() {
        return this.serverSocket;
    }

    public void close() {
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            throw new DevoreRuntimeException("关闭TCP服务端失败: " + e.getMessage());
        }
    }

    @Override
    public String type() {
        return "tcp-server";
    }

    @Override
    protected String str() {
        return "<tcp-server>";
    }

    @Override
    public int compareTo(DToken t) {
        return t instanceof DTCPServer && this.serverSocket == ((DTCPServer) t).serverSocket ? 0 : -1;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.type(), this.serverSocket);
    }
}
