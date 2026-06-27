package org.devore.lang.module;

import org.devore.exception.DevoreCastException;
import org.devore.exception.DevoreRuntimeException;
import org.devore.lang.Env;
import org.devore.lang.token.*;
import org.devore.utils.DByteUtils;
import org.devore.utils.DIntUtils;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * HTTP请求
 */
public class HttpModule extends DModule {
    public HttpModule() {
        super("http");
    }

    @Override
    public void init(Env dEnv) {
        dEnv.addTokenProcedure("http-server?", (args, env) ->
                DBool.valueOf(args.get(0) instanceof DHttpServer), 1, false);
        dEnv.addTokenProcedure("http-listen", (args, env) -> {
            if (!(args.get(0) instanceof DInt))
                throw new DevoreCastException(args.get(0).type(), "int");
            int port = toPort((DInt) args.get(0));
            try {
                return openServer(new InetSocketAddress(port));
            } catch (IOException e) {
                throw new DevoreRuntimeException("HTTP监听失败: " + port + ", " + e.getMessage());
            }
        }, 1, false);
        dEnv.addTokenProcedure("http-listen", (args, env) -> {
            if (!(args.get(0) instanceof DString))
                throw new DevoreCastException(args.get(0).type(), "string");
            if (!(args.get(1) instanceof DInt))
                throw new DevoreCastException(args.get(1).type(), "int");
            String host = args.get(0).toString();
            int port = toPort((DInt) args.get(1));
            try {
                return openServer(new InetSocketAddress(host, port));
            } catch (IOException e) {
                throw new DevoreRuntimeException("HTTP监听失败: " + host + ":" + port + ", " + e.getMessage());
            }
        }, 2, false);
        dEnv.addTokenProcedure("http-handler", (args, env) -> {
            if (!(args.get(0) instanceof DHttpServer))
                throw new DevoreCastException(args.get(0).type(), "http-server");
            if (!(args.get(1) instanceof DString))
                throw new DevoreCastException(args.get(1).type(), "string");
            if (!(args.get(2) instanceof DProcedure))
                throw new DevoreCastException(args.get(2).type(), "procedure");
            DHttpServer server = (DHttpServer) args.get(0);
            String path = args.get(1).toString();
            if (!path.startsWith("/"))
                throw new DevoreRuntimeException("HTTP路由路径必须以/开头: " + path);
            DProcedure handler = (DProcedure) args.get(2);
            try {
                server.toHttpServer().createContext(path, exchange ->
                        handleExchange(exchange, handler, env.createChild()));
                return DWord.NIL;
            } catch (IllegalArgumentException e) {
                throw new DevoreRuntimeException("HTTP路由注册失败: " + path + ", " + e.getMessage());
            }
        }, 3, false);
        dEnv.addTokenProcedure("http-start", (args, env) -> {
            if (!(args.get(0) instanceof DHttpServer))
                throw new DevoreCastException(args.get(0).type(), "http-server");
            ((DHttpServer) args.get(0)).start();
            return DWord.NIL;
        }, 1, false);
        dEnv.addTokenProcedure("http-stop", (args, env) -> {
            if (!(args.get(0) instanceof DHttpServer))
                throw new DevoreCastException(args.get(0).type(), "http-server");
            ((DHttpServer) args.get(0)).stop(0);
            return DWord.NIL;
        }, 1, false);
        dEnv.addTokenProcedure("http-stop", (args, env) -> {
            if (!(args.get(0) instanceof DHttpServer))
                throw new DevoreCastException(args.get(0).type(), "http-server");
            if (!(args.get(1) instanceof DInt))
                throw new DevoreCastException(args.get(1).type(), "int");
            int delay = DIntUtils.toInt((DInt) args.get(1));
            if (delay < 0)
                throw new DevoreRuntimeException("停止等待时间不能为负数: " + delay);
            ((DHttpServer) args.get(0)).stop(delay);
            return DWord.NIL;
        }, 2, false);
        dEnv.addTokenProcedure("http-get", (args, env) -> {
            if (!(args.get(0) instanceof DString))
                throw new DevoreCastException(args.get(0).type(), "string");
            URI uri;
            try {
                uri = new URI(args.get(0).toString());
            } catch (URISyntaxException e) {
                throw new DevoreRuntimeException("URL格式错误: " + args.get(0));
            }
            if (!"http".equalsIgnoreCase(uri.getScheme()) && !"https".equalsIgnoreCase(uri.getScheme()))
                throw new DevoreRuntimeException("URL协议必须是http或https: " + args.get(0));
            try {
                HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(30000);
                connection.setReadTimeout(30000);
                connection.setInstanceFollowRedirects(true);
                int status = connection.getResponseCode();
                InputStream input = status >= 400 ? connection.getErrorStream() : connection.getInputStream();
                byte[] body = new byte[0];
                if (input != null) {
                    try (InputStream in = input; ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                        byte[] buffer = new byte[8192];
                        int n;
                        while ((n = in.read(buffer)) >= 0)
                            out.write(buffer, 0, n);
                        body = out.toByteArray();
                    }
                }
                Map<DToken, DToken> headers = new HashMap<>();
                for (Map.Entry<String, List<String>> entry : connection.getHeaderFields().entrySet()) {
                    if (entry.getKey() != null)
                        headers.put(DString.valueOf(entry.getKey()),
                                DString.valueOf(String.join(",", entry.getValue())));
                }
                Map<DToken, DToken> result = new HashMap<>();
                result.put(DString.valueOf("status"), DNumber.valueOf(status));
                result.put(DString.valueOf("headers"), DTable.valueOf(headers));
                result.put(DString.valueOf("body"), DByteUtils.toList(body));
                return DTable.valueOf(result);
            } catch (IOException e) {
                throw new DevoreRuntimeException("HTTP请求失败: " + uri + ", " + e.getMessage());
            }
        }, 1, false);
        dEnv.addTokenProcedure("http-get", (args, env) -> {
            if (!(args.get(0) instanceof DString))
                throw new DevoreCastException(args.get(0).type(), "string");
            if (!(args.get(1) instanceof DTable))
                throw new DevoreCastException(args.get(1).type(), "table");
            URI uri;
            try {
                uri = new URI(args.get(0).toString());
            } catch (URISyntaxException e) {
                throw new DevoreRuntimeException("URL格式错误: " + args.get(0));
            }
            if (!"http".equalsIgnoreCase(uri.getScheme()) && !"https".equalsIgnoreCase(uri.getScheme()))
                throw new DevoreRuntimeException("URL协议必须是http或https: " + args.get(0));
            try {
                HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(30000);
                connection.setReadTimeout(30000);
                connection.setInstanceFollowRedirects(true);
                DTable requestHeaders = (DTable) args.get(1);
                for (DToken key : requestHeaders.keys()) {
                    if (!(key instanceof DString))
                        throw new DevoreCastException(key.type(), "string");
                    DToken value = requestHeaders.get(key);
                    if (!(value instanceof DString))
                        throw new DevoreCastException(value.type(), "string");
                    connection.setRequestProperty(key.toString(), value.toString());
                }
                int status = connection.getResponseCode();
                InputStream input = status >= 400 ? connection.getErrorStream() : connection.getInputStream();
                byte[] body = new byte[0];
                if (input != null) {
                    try (InputStream in = input; ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                        byte[] buffer = new byte[8192];
                        int n;
                        while ((n = in.read(buffer)) >= 0)
                            out.write(buffer, 0, n);
                        body = out.toByteArray();
                    }
                }
                Map<DToken, DToken> headers = new HashMap<>();
                for (Map.Entry<String, List<String>> entry : connection.getHeaderFields().entrySet()) {
                    if (entry.getKey() != null)
                        headers.put(DString.valueOf(entry.getKey()),
                                DString.valueOf(String.join(",", entry.getValue())));
                }
                Map<DToken, DToken> result = new HashMap<>();
                result.put(DString.valueOf("status"), DNumber.valueOf(status));
                result.put(DString.valueOf("headers"), DTable.valueOf(headers));
                result.put(DString.valueOf("body"), DByteUtils.toList(body));
                return DTable.valueOf(result);
            } catch (IOException e) {
                throw new DevoreRuntimeException("HTTP请求失败: " + uri + ", " + e.getMessage());
            }
        }, 2, false);
        dEnv.addTokenProcedure("http-get-binary", (args, env) -> {
            if (!(args.get(0) instanceof DString))
                throw new DevoreCastException(args.get(0).type(), "string");
            URI uri;
            try {
                uri = new URI(args.get(0).toString());
            } catch (URISyntaxException e) {
                throw new DevoreRuntimeException("URL格式错误: " + args.get(0));
            }
            if (!"http".equalsIgnoreCase(uri.getScheme()) && !"https".equalsIgnoreCase(uri.getScheme()))
                throw new DevoreRuntimeException("URL协议必须是http或https: " + args.get(0));
            try {
                HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(30000);
                connection.setReadTimeout(30000);
                connection.setInstanceFollowRedirects(true);
                int status = connection.getResponseCode();
                InputStream input = status >= 400 ? connection.getErrorStream() : connection.getInputStream();
                if (input == null)
                    return DByteUtils.toList(new byte[0]);
                try (InputStream in = input; ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                    byte[] buffer = new byte[8192];
                    int n;
                    while ((n = in.read(buffer)) >= 0)
                        out.write(buffer, 0, n);
                    return DByteUtils.toList(out.toByteArray());
                }
            } catch (IOException e) {
                throw new DevoreRuntimeException("HTTP请求失败: " + uri + ", " + e.getMessage());
            }
        }, 1, false);
        dEnv.addTokenProcedure("http-get-binary", (args, env) -> {
            if (!(args.get(0) instanceof DString))
                throw new DevoreCastException(args.get(0).type(), "string");
            if (!(args.get(1) instanceof DTable))
                throw new DevoreCastException(args.get(1).type(), "table");
            URI uri;
            try {
                uri = new URI(args.get(0).toString());
            } catch (URISyntaxException e) {
                throw new DevoreRuntimeException("URL格式错误: " + args.get(0));
            }
            if (!"http".equalsIgnoreCase(uri.getScheme()) && !"https".equalsIgnoreCase(uri.getScheme()))
                throw new DevoreRuntimeException("URL协议必须是http或https: " + args.get(0));
            try {
                HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(30000);
                connection.setReadTimeout(30000);
                connection.setInstanceFollowRedirects(true);
                DTable requestHeaders = (DTable) args.get(1);
                for (DToken key : requestHeaders.keys()) {
                    if (!(key instanceof DString))
                        throw new DevoreCastException(key.type(), "string");
                    DToken value = requestHeaders.get(key);
                    if (!(value instanceof DString))
                        throw new DevoreCastException(value.type(), "string");
                    connection.setRequestProperty(key.toString(), value.toString());
                }
                int status = connection.getResponseCode();
                InputStream input = status >= 400 ? connection.getErrorStream() : connection.getInputStream();
                if (input == null)
                    return DByteUtils.toList(new byte[0]);
                try (InputStream in = input; ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                    byte[] buffer = new byte[8192];
                    int n;
                    while ((n = in.read(buffer)) >= 0)
                        out.write(buffer, 0, n);
                    return DByteUtils.toList(out.toByteArray());
                }
            } catch (IOException e) {
                throw new DevoreRuntimeException("HTTP请求失败: " + uri + ", " + e.getMessage());
            }
        }, 2, false);
        dEnv.addTokenProcedure("http-get-string", (args, env) -> {
            if (!(args.get(0) instanceof DString))
                throw new DevoreCastException(args.get(0).type(), "string");
            URI uri;
            try {
                uri = new URI(args.get(0).toString());
            } catch (URISyntaxException e) {
                throw new DevoreRuntimeException("URL格式错误: " + args.get(0));
            }
            if (!"http".equalsIgnoreCase(uri.getScheme()) && !"https".equalsIgnoreCase(uri.getScheme()))
                throw new DevoreRuntimeException("URL协议必须是http或https: " + args.get(0));
            try {
                HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(30000);
                connection.setReadTimeout(30000);
                connection.setInstanceFollowRedirects(true);
                int status = connection.getResponseCode();
                InputStream input = status >= 400 ? connection.getErrorStream() : connection.getInputStream();
                if (input == null)
                    return DString.valueOf("");
                try (InputStream in = input; ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                    byte[] buffer = new byte[8192];
                    int n;
                    while ((n = in.read(buffer)) >= 0)
                        out.write(buffer, 0, n);
                    return DString.valueOf(new String(out.toByteArray(), StandardCharsets.UTF_8));
                }
            } catch (IOException e) {
                throw new DevoreRuntimeException("HTTP请求失败: " + uri + ", " + e.getMessage());
            }
        }, 1, false);
        dEnv.addTokenProcedure("http-get-string", (args, env) -> {
            if (!(args.get(0) instanceof DString))
                throw new DevoreCastException(args.get(0).type(), "string");
            if (!(args.get(1) instanceof DTable) && !(args.get(1) instanceof DString))
                throw new DevoreCastException(args.get(1).type(), "table|string");
            URI uri;
            try {
                uri = new URI(args.get(0).toString());
            } catch (URISyntaxException e) {
                throw new DevoreRuntimeException("URL格式错误: " + args.get(0));
            }
            if (!"http".equalsIgnoreCase(uri.getScheme()) && !"https".equalsIgnoreCase(uri.getScheme()))
                throw new DevoreRuntimeException("URL协议必须是http或https: " + args.get(0));
            try {
                HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(30000);
                connection.setReadTimeout(30000);
                connection.setInstanceFollowRedirects(true);
                Charset charset = StandardCharsets.UTF_8;
                if (args.get(1) instanceof DTable) {
                    DTable requestHeaders = (DTable) args.get(1);
                    for (DToken key : requestHeaders.keys()) {
                        if (!(key instanceof DString))
                            throw new DevoreCastException(key.type(), "string");
                        DToken value = requestHeaders.get(key);
                        if (!(value instanceof DString))
                            throw new DevoreCastException(value.type(), "string");
                        connection.setRequestProperty(key.toString(), value.toString());
                    }
                } else {
                    try {
                        charset = Charset.forName(args.get(1).toString());
                    } catch (RuntimeException e) {
                        throw new DevoreRuntimeException("字符集不存在: " + args.get(1));
                    }
                }
                int status = connection.getResponseCode();
                InputStream input = status >= 400 ? connection.getErrorStream() : connection.getInputStream();
                if (input == null)
                    return DString.valueOf("");
                try (InputStream in = input; ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                    byte[] buffer = new byte[8192];
                    int n;
                    while ((n = in.read(buffer)) >= 0)
                        out.write(buffer, 0, n);
                    return DString.valueOf(new String(out.toByteArray(), charset));
                }
            } catch (IOException e) {
                throw new DevoreRuntimeException("HTTP请求失败: " + uri + ", " + e.getMessage());
            }
        }, 2, false);
        dEnv.addTokenProcedure("http-get-string", (args, env) -> {
            if (!(args.get(0) instanceof DString))
                throw new DevoreCastException(args.get(0).type(), "string");
            if (!(args.get(1) instanceof DTable))
                throw new DevoreCastException(args.get(1).type(), "table");
            if (!(args.get(2) instanceof DTable))
                throw new DevoreCastException(args.get(2).type(), "string");
            URI uri;
            try {
                uri = new URI(args.get(0).toString());
            } catch (URISyntaxException e) {
                throw new DevoreRuntimeException("URL格式错误: " + args.get(0));
            }
            if (!"http".equalsIgnoreCase(uri.getScheme()) && !"https".equalsIgnoreCase(uri.getScheme()))
                throw new DevoreRuntimeException("URL协议必须是http或https: " + args.get(0));
            try {
                HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(30000);
                connection.setReadTimeout(30000);
                connection.setInstanceFollowRedirects(true);
                DTable requestHeaders = (DTable) args.get(1);
                for (DToken key : requestHeaders.keys()) {
                    if (!(key instanceof DString))
                        throw new DevoreCastException(key.type(), "string");
                    DToken value = requestHeaders.get(key);
                    if (!(value instanceof DString))
                        throw new DevoreCastException(value.type(), "string");
                    connection.setRequestProperty(key.toString(), value.toString());
                }
                Charset charset;
                try {
                    charset = Charset.forName(args.get(2).toString());
                } catch (RuntimeException e) {
                    throw new DevoreRuntimeException("字符集不存在: " + args.get(2));
                }
                int status = connection.getResponseCode();
                InputStream input = status >= 400 ? connection.getErrorStream() : connection.getInputStream();
                if (input == null)
                    return DString.valueOf("");
                try (InputStream in = input; ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                    byte[] buffer = new byte[8192];
                    int n;
                    while ((n = in.read(buffer)) >= 0)
                        out.write(buffer, 0, n);
                    return DString.valueOf(new String(out.toByteArray(), charset));
                }
            } catch (IOException e) {
                throw new DevoreRuntimeException("HTTP请求失败: " + uri + ", " + e.getMessage());
            }
        }, 3, false);
    }

    private static DHttpServer openServer(InetSocketAddress address) throws IOException {
        HttpServer server = HttpServer.create(address, 0);
        ExecutorService executor = Executors.newCachedThreadPool();
        server.setExecutor(executor);
        return DHttpServer.valueOf(server, executor);
    }

    private static void handleExchange(HttpExchange exchange, DProcedure handler, Env env) throws IOException {
        try {
            DToken response = handler.call(Collections.singletonList(toRequest(exchange)), env.createChild());
            sendResponse(exchange, response);
        } catch (Throwable e) {
            byte[] body = ("HTTP服务端处理失败: " + message(e)).getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
            exchange.sendResponseHeaders(500, body.length);
            try (OutputStream output = exchange.getResponseBody()) {
                output.write(body);
            }
        } finally {
            exchange.close();
        }
    }

    private static DTable toRequest(HttpExchange exchange) throws IOException {
        Map<DToken, DToken> request = new HashMap<>();
        request.put(DString.valueOf("method"), DString.valueOf(exchange.getRequestMethod()));
        request.put(DString.valueOf("path"), DString.valueOf(exchange.getRequestURI().getPath()));
        request.put(DString.valueOf("query"), DString.valueOf(
                exchange.getRequestURI().getRawQuery() == null ? "" : exchange.getRequestURI().getRawQuery()));
        request.put(DString.valueOf("headers"), toTable(exchange.getRequestHeaders()));
        request.put(DString.valueOf("body"), DByteUtils.toList(readAll(exchange.getRequestBody())));
        request.put(DString.valueOf("remote-host"), DString.valueOf(exchange.getRemoteAddress().getHostString()));
        request.put(DString.valueOf("remote-port"), DNumber.valueOf(exchange.getRemoteAddress().getPort()));
        return DTable.valueOf(request);
    }

    private static DTable toTable(Headers headers) {
        Map<DToken, DToken> table = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : headers.entrySet())
            table.put(DString.valueOf(entry.getKey()), DString.valueOf(String.join(",", entry.getValue())));
        return DTable.valueOf(table);
    }

    private static void sendResponse(HttpExchange exchange, DToken response) throws IOException {
        int status = 200;
        DToken headers = DWord.NIL;
        DToken body = response;
        if (response instanceof DTable) {
            DTable table = (DTable) response;
            DToken statusToken = table.get(DString.valueOf("status"));
            if (statusToken != DWord.NIL) {
                if (!(statusToken instanceof DInt))
                    throw new DevoreCastException(statusToken.type(), "int");
                status = DIntUtils.toInt((DInt) statusToken);
            }
            headers = table.get(DString.valueOf("headers"));
            body = table.get(DString.valueOf("body"));
        }
        if (status < 100 || status > 599)
            throw new DevoreRuntimeException("HTTP状态码范围必须是100-599: " + status);
        if (headers != DWord.NIL)
            setResponseHeaders(exchange, headers);
        byte[] bodyBytes = toBodyBytes(body);
        exchange.sendResponseHeaders(status, bodyBytes.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(bodyBytes);
        }
    }

    private static void setResponseHeaders(HttpExchange exchange, DToken headers) {
        if (!(headers instanceof DTable))
            throw new DevoreCastException(headers.type(), "table");
        DTable table = (DTable) headers;
        for (DToken key : table.keys()) {
            if (!(key instanceof DString))
                throw new DevoreCastException(key.type(), "string");
            DToken value = table.get(key);
            if (!(value instanceof DString))
                throw new DevoreCastException(value.type(), "string");
            exchange.getResponseHeaders().set(key.toString(), value.toString());
        }
    }

    private static byte[] toBodyBytes(DToken body) {
        if (body == DWord.NIL)
            return new byte[0];
        if (body instanceof DList)
            return DByteUtils.toBytes((DList) body);
        if (body instanceof DString)
            return body.toString().getBytes(StandardCharsets.UTF_8);
        return body.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static byte[] readAll(InputStream input) throws IOException {
        try (InputStream in = input; ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int n;
            while ((n = in.read(buffer)) >= 0)
                out.write(buffer, 0, n);
            return out.toByteArray();
        }
    }

    private static int toPort(DInt value) {
        int port = DIntUtils.toInt(value);
        if (port < 0 || port > 65535)
            throw new DevoreRuntimeException("端口范围必须是0-65535: " + port);
        return port;
    }

    private static String message(Throwable e) {
        return e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
    }
}
