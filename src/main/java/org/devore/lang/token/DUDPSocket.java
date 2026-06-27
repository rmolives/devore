package org.devore.lang.token;

import java.net.DatagramSocket;
import java.util.Objects;

/**
 * UDP套接字
 */
public class DUDPSocket extends DToken {
    private final DatagramSocket socket;

    private DUDPSocket(DatagramSocket socket) {
        this.socket = socket;
    }

    public static DUDPSocket valueOf(DatagramSocket socket) {
        return new DUDPSocket(socket);
    }

    public DatagramSocket toSocket() {
        return this.socket;
    }

    public void close() {
        this.socket.close();
    }

    @Override
    public String type() {
        return "udp-socket";
    }

    @Override
    protected String str() {
        return "<udp-socket>";
    }

    @Override
    public int compareTo(DToken t) {
        return t instanceof DUDPSocket && this.socket == ((DUDPSocket) t).socket ? 0 : -1;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.type(), this.socket);
    }
}
