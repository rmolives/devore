package org.devore.lang.token;

import com.sun.net.httpserver.HttpServer;
import org.devore.exception.DevoreRuntimeException;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

/**
 * HTTP服务端
 */
public class DHttpServer extends DToken {
    private final HttpServer server;                        // HTTP服务端
    private final ExecutorService executor;                 // 请求处理线程池
    private final Map<Integer, DToken> statusResponses;     // 状态码响应表

    private DHttpServer(HttpServer server, ExecutorService executor) {
        this.server = server;
        this.executor = executor;
        this.statusResponses = new ConcurrentHashMap<>();
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
     * 设置状态码对应的响应
     *
     * @param status   HTTP状态码
     * @param response 响应
     */
    public void setStatusResponse(int status, DToken response) {
        this.statusResponses.put(status, response);
    }

    /**
     * 获取状态码对应的响应
     *
     * @param status HTTP状态码
     * @return 响应
     */
    public DToken getStatusResponse(int status) {
        return this.statusResponses.getOrDefault(status, DWord.NIL);
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
