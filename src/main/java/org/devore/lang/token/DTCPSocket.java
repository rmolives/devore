package org.devore.lang.token;

import org.devore.exception.DevoreRuntimeException;

import java.io.IOException;
import java.net.Socket;
import java.util.Objects;

/**
 * TCP连接
 */
public class DTCPSocket extends DToken {
    private final Socket socket;

    private DTCPSocket(Socket socket) {
        this.socket = socket;
    }

    public static DTCPSocket valueOf(Socket socket) {
        return new DTCPSocket(socket);
    }

    public Socket toSocket() {
        return this.socket;
    }

    public void close() {
        try {
            this.socket.close();
        } catch (IOException e) {
            throw new DevoreRuntimeException("关闭TCP连接失败: " + e.getMessage());
        }
    }

    @Override
    public String type() {
        return "tcp-socket";
    }

    @Override
    protected String str() {
        return "<tcp-socket>";
    }

    @Override
    public int compareTo(DToken t) {
        return t instanceof DTCPSocket && this.socket == ((DTCPSocket) t).socket ? 0 : -1;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.type(), this.socket);
    }
}
