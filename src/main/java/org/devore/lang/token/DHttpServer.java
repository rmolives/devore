package org.devore.lang.token;

import com.sun.net.httpserver.HttpServer;
import org.devore.exception.DevoreRuntimeException;

import java.util.Objects;
import java.util.concurrent.ExecutorService;

/**
 * HTTP服务端
 */
public class DHttpServer extends DToken {
    private final HttpServer server;
    private final ExecutorService executor;

    private DHttpServer(HttpServer server, ExecutorService executor) {
        this.server = server;
        this.executor = executor;
    }

    public static DHttpServer valueOf(HttpServer server, ExecutorService executor) {
        return new DHttpServer(server, executor);
    }

    public HttpServer toHttpServer() {
        return this.server;
    }

    public void start() {
        try {
            this.server.start();
        } catch (IllegalStateException e) {
            throw new DevoreRuntimeException("HTTP服务端已经启动或已经停止.");
        }
    }

    public void stop(int delay) {
        this.server.stop(delay);
        this.executor.shutdownNow();
    }

    @Override
    public String type() {
        return "http-server";
    }

    @Override
    protected String str() {
        return "<http-server>";
    }

    @Override
    public int compareTo(DToken t) {
        return t instanceof DHttpServer && this.server == ((DHttpServer) t).server ? 0 : -1;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.type(), this.server);
    }
}
