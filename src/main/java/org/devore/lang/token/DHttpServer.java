package org.devore.lang.token;

import com.sun.net.httpserver.HttpServer;
import org.devore.exception.DevoreRuntimeException;

import java.util.Objects;
import java.util.concurrent.ExecutorService;

/**
 * HTTP服务端
 */
public class DHttpServer extends DToken {
    private final HttpServer server;                        // HTTP服务端
    private final ExecutorService executor;                 // 请求处理线程池

    private DHttpServer(HttpServer server, ExecutorService executor) {
        this.server = server;
        this.executor = executor;
    }

    /**
     * 创建HTTP服务端
     *
     * @param server   Java HTTP服务端
     * @param executor 请求处理线程池
     * @return HTTP服务端
     */
    public static DHttpServer valueOf(HttpServer server, ExecutorService executor) {
        return new DHttpServer(server, executor);
    }

    /**
     * 转换为Java的HttpServer
     *
     * @return Java HTTP服务端
     */
    public HttpServer toHttpServer() {
        return this.server;
    }

    /**
     * 启动HTTP服务端
     */
    public void start() {
        try {
            this.server.start();
        } catch (IllegalStateException e) {
            throw new DevoreRuntimeException("HTTP服务端已经启动或已经停止.");
        }
    }

    /**
     * 停止HTTP服务端
     *
     * @param delay 等待请求处理完成的最大秒数
     */
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
