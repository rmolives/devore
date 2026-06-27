package org.devore.lang.module;

import org.devore.exception.DevoreCastException;
import org.devore.exception.DevoreRuntimeException;
import org.devore.lang.Env;
import org.devore.lang.token.*;
import org.devore.utils.DByteUtils;
import org.devore.utils.DIntUtils;
import org.devore.utils.DNetworkUtils;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
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
        dEnv.addTokenProcedure("http-url-encode", (args, env) -> {
            if (!(args.get(0) instanceof DString))
                throw new DevoreCastException(args.get(0).type(), "string");
            try {
                return DString.valueOf(URLEncoder.encode(args.get(0).toString(), StandardCharsets.UTF_8.name()));
            } catch (UnsupportedEncodingException e) {
                throw new DevoreRuntimeException("字符集不存在: " + StandardCharsets.UTF_8.name());
            }
        }, 1, false);
        dEnv.addTokenProcedure("http-url-encode", (args, env) -> {
            if (!(args.get(0) instanceof DString))
                throw new DevoreCastException(args.get(0).type(), "string");
            String charset = toCharset(args.get(1)).name();
            try {
                return DString.valueOf(URLEncoder.encode(args.get(0).toString(), charset));
            } catch (UnsupportedEncodingException e) {
                throw new DevoreRuntimeException("字符集不存在: " + charset);
            }
        }, 2, false);
        dEnv.addTokenProcedure("http-url-decode", (args, env) -> {
            if (!(args.get(0) instanceof DString))
                throw new DevoreCastException(args.get(0).type(), "string");
            try {
                return DString.valueOf(URLDecoder.decode(args.get(0).toString(), StandardCharsets.UTF_8.name()));
            } catch (UnsupportedEncodingException e) {
                throw new DevoreRuntimeException("字符集不存在: " + StandardCharsets.UTF_8.name());
            } catch (IllegalArgumentException e) {
                throw new DevoreRuntimeException("URL解码失败: " + args.get(0));
            }
        }, 1, false);
        dEnv.addTokenProcedure("http-url-decode", (args, env) -> {
            if (!(args.get(0) instanceof DString))
                throw new DevoreCastException(args.get(0).type(), "string");
            String charset = toCharset(args.get(1)).name();
            try {
                return DString.valueOf(URLDecoder.decode(args.get(0).toString(), charset));
            } catch (UnsupportedEncodingException e) {
                throw new DevoreRuntimeException("字符集不存在: " + charset);
            } catch (IllegalArgumentException e) {
                throw new DevoreRuntimeException("URL解码失败: " + args.get(0));
            }
        }, 2, false);
        dEnv.addTokenProcedure("http-listen", (args, env) -> {
            if (!(args.get(0) instanceof DInt))
                throw new DevoreCastException(args.get(0).type(), "int");
            int port = DNetworkUtils.toPort((DInt) args.get(0));
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
            int port = DNetworkUtils.toPort((DInt) args.get(1));
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
            URI uri = toHttpUri(args.get(0));
            try {
                return toResponseTable(request(uri, "GET", null, null));
            } catch (IOException e) {
                throw new DevoreRuntimeException("HTTP请求失败: " + uri + ", " + e.getMessage());
            }
        }, 1, false);
        dEnv.addTokenProcedure("http-get", (args, env) -> {
            URI uri = toHttpUri(args.get(0));
            if (!(args.get(1) instanceof DTable))
                throw new DevoreCastException(args.get(1).type(), "table");
            try {
                return toResponseTable(request(uri, "GET", (DTable) args.get(1), null));
            } catch (IOException e) {
                throw new DevoreRuntimeException("HTTP请求失败: " + uri + ", " + e.getMessage());
            }
        }, 2, false);
        dEnv.addTokenProcedure("http-get-binary", (args, env) -> {
            URI uri = toHttpUri(args.get(0));
            try {
                return DByteUtils.toList(request(uri, "GET", null, null).body);
            } catch (IOException e) {
                throw new DevoreRuntimeException("HTTP请求失败: " + uri + ", " + e.getMessage());
            }
        }, 1, false);
        dEnv.addTokenProcedure("http-get-binary", (args, env) -> {
            URI uri = toHttpUri(args.get(0));
            if (!(args.get(1) instanceof DTable))
                throw new DevoreCastException(args.get(1).type(), "table");
            try {
                return DByteUtils.toList(request(uri, "GET", (DTable) args.get(1), null).body);
            } catch (IOException e) {
                throw new DevoreRuntimeException("HTTP请求失败: " + uri + ", " + e.getMessage());
            }
        }, 2, false);
        dEnv.addTokenProcedure("http-get-string", (args, env) -> {
            URI uri = toHttpUri(args.get(0));
            try {
                return DString.valueOf(new String(request(uri, "GET", null, null).body, StandardCharsets.UTF_8));
            } catch (IOException e) {
                throw new DevoreRuntimeException("HTTP请求失败: " + uri + ", " + e.getMessage());
            }
        }, 1, false);
        dEnv.addTokenProcedure("http-get-string", (args, env) -> {
            URI uri = toHttpUri(args.get(0));
            if (!(args.get(1) instanceof DTable) && !(args.get(1) instanceof DString))
                throw new DevoreCastException(args.get(1).type(), "table|string");
            try {
                Charset charset = StandardCharsets.UTF_8;
                DTable headers = null;
                if (args.get(1) instanceof DTable) {
                    headers = (DTable) args.get(1);
                } else {
                    charset = toCharset(args.get(1));
                }
                return DString.valueOf(new String(request(uri, "GET", headers, null).body, charset));
            } catch (IOException e) {
                throw new DevoreRuntimeException("HTTP请求失败: " + uri + ", " + e.getMessage());
            }
        }, 2, false);
        dEnv.addTokenProcedure("http-get-string", (args, env) -> {
            URI uri = toHttpUri(args.get(0));
            if (!(args.get(1) instanceof DTable))
                throw new DevoreCastException(args.get(1).type(), "table");
            if (!(args.get(2) instanceof DString))
                throw new DevoreCastException(args.get(2).type(), "string");
            try {
                return DString.valueOf(new String(request(uri, "GET", (DTable) args.get(1), null).body,
                        toCharset(args.get(2))));
            } catch (IOException e) {
                throw new DevoreRuntimeException("HTTP请求失败: " + uri + ", " + e.getMessage());
            }
        }, 3, false);
        dEnv.addTokenProcedure("http-post", (args, env) -> {
            URI uri = toHttpUri(args.get(0));
            try {
                return toResponseTable(request(uri, "POST", null, toRequestBody(args.get(1))));
            } catch (IOException e) {
                throw new DevoreRuntimeException("HTTP请求失败: " + uri + ", " + e.getMessage());
            }
        }, 2, false);
        dEnv.addTokenProcedure("http-post", (args, env) -> {
            URI uri = toHttpUri(args.get(0));
            if (!(args.get(1) instanceof DTable))
                throw new DevoreCastException(args.get(1).type(), "table");
            try {
                return toResponseTable(request(uri, "POST", (DTable) args.get(1), toRequestBody(args.get(2))));
            } catch (IOException e) {
                throw new DevoreRuntimeException("HTTP请求失败: " + uri + ", " + e.getMessage());
            }
        }, 3, false);
        dEnv.addTokenProcedure("http-post-binary", (args, env) -> {
            URI uri = toHttpUri(args.get(0));
            try {
                return DByteUtils.toList(request(uri, "POST", null, toRequestBody(args.get(1))).body);
            } catch (IOException e) {
                throw new DevoreRuntimeException("HTTP请求失败: " + uri + ", " + e.getMessage());
            }
        }, 2, false);
        dEnv.addTokenProcedure("http-post-binary", (args, env) -> {
            URI uri = toHttpUri(args.get(0));
            if (!(args.get(1) instanceof DTable))
                throw new DevoreCastException(args.get(1).type(), "table");
            try {
                return DByteUtils.toList(request(uri, "POST", (DTable) args.get(1), toRequestBody(args.get(2))).body);
            } catch (IOException e) {
                throw new DevoreRuntimeException("HTTP请求失败: " + uri + ", " + e.getMessage());
            }
        }, 3, false);
        dEnv.addTokenProcedure("http-post-string", (args, env) -> {
            URI uri = toHttpUri(args.get(0));
            try {
                return DString.valueOf(new String(request(uri, "POST", null, toRequestBody(args.get(1))).body,
                        StandardCharsets.UTF_8));
            } catch (IOException e) {
                throw new DevoreRuntimeException("HTTP请求失败: " + uri + ", " + e.getMessage());
            }
        }, 2, false);
        dEnv.addTokenProcedure("http-post-string", (args, env) -> {
            URI uri = toHttpUri(args.get(0));
            try {
                if (args.get(1) instanceof DTable) {
                    return DString.valueOf(new String(request(uri, "POST", (DTable) args.get(1),
                            toRequestBody(args.get(2))).body, StandardCharsets.UTF_8));
                }
                if (!(args.get(2) instanceof DString))
                    throw new DevoreCastException(args.get(2).type(), "string");
                return DString.valueOf(new String(request(uri, "POST", null, toRequestBody(args.get(1))).body,
                        toCharset(args.get(2))));
            } catch (IOException e) {
                throw new DevoreRuntimeException("HTTP请求失败: " + uri + ", " + e.getMessage());
            }
        }, 3, false);
        dEnv.addTokenProcedure("http-post-string", (args, env) -> {
            URI uri = toHttpUri(args.get(0));
            if (!(args.get(1) instanceof DTable))
                throw new DevoreCastException(args.get(1).type(), "table");
            try {
                return DString.valueOf(new String(request(uri, "POST", (DTable) args.get(1),
                        toRequestBody(args.get(2))).body, toCharset(args.get(3))));
            } catch (IOException e) {
                throw new DevoreRuntimeException("HTTP请求失败: " + uri + ", " + e.getMessage());
            }
        }, 4, false);
    }

    private static URI toHttpUri(DToken token) {
        if (!(token instanceof DString))
            throw new DevoreCastException(token.type(), "string");
        URI uri;
        try {
            uri = new URI(token.toString());
        } catch (URISyntaxException e) {
            throw new DevoreRuntimeException("URL格式错误: " + token);
        }
        if (!"http".equalsIgnoreCase(uri.getScheme()) && !"https".equalsIgnoreCase(uri.getScheme()))
            throw new DevoreRuntimeException("URL协议必须是http或https: " + token);
        return uri;
    }

    private static Response request(URI uri, String method, DTable headers, byte[] requestBody) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
        connection.setRequestMethod(method);
        connection.setConnectTimeout(30000);
        connection.setReadTimeout(30000);
        connection.setInstanceFollowRedirects(true);
        if (headers != null)
            setRequestHeaders(connection, headers);
        if (requestBody != null) {
            connection.setDoOutput(true);
            connection.setFixedLengthStreamingMode(requestBody.length);
            try (OutputStream output = connection.getOutputStream()) {
                output.write(requestBody);
            }
        }
        int status = connection.getResponseCode();
        InputStream input = status >= 400 ? connection.getErrorStream() : connection.getInputStream();
        byte[] body = input == null ? new byte[0] : readAll(input);
        return new Response(status, headersToTable(connection.getHeaderFields()), body);
    }

    private static void setRequestHeaders(HttpURLConnection connection, DTable headers) {
        for (DToken key : headers.keys()) {
            if (!(key instanceof DString))
                throw new DevoreCastException(key.type(), "string");
            DToken value = headers.get(key);
            if (!(value instanceof DString))
                throw new DevoreCastException(value.type(), "string");
            connection.setRequestProperty(key.toString(), value.toString());
        }
    }

    private static byte[] toRequestBody(DToken body) {
        if (body == DWord.NIL)
            return new byte[0];
        if (body instanceof DList)
            return DByteUtils.toBytes((DList) body);
        if (body instanceof DString)
            return body.toString().getBytes(StandardCharsets.UTF_8);
        throw new DevoreCastException(body.type(), "string|list");
    }

    private static Charset toCharset(DToken token) {
        if (!(token instanceof DString))
            throw new DevoreCastException(token.type(), "string");
        try {
            return Charset.forName(token.toString());
        } catch (RuntimeException e) {
            throw new DevoreRuntimeException("字符集不存在: " + token);
        }
    }

    private static Map<DToken, DToken> headersToTable(Map<String, List<String>> fields) {
        Map<DToken, DToken> headers = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : fields.entrySet()) {
            if (entry.getKey() != null)
                headers.put(DString.valueOf(entry.getKey()), DString.valueOf(String.join(",", entry.getValue())));
        }
        return headers;
    }

    private static DTable toResponseTable(Response response) {
        Map<DToken, DToken> result = new HashMap<>();
        result.put(DString.valueOf("status"), DNumber.valueOf(response.status));
        result.put(DString.valueOf("headers"), DTable.valueOf(response.headers));
        result.put(DString.valueOf("body"), DByteUtils.toList(response.body));
        return DTable.valueOf(result);
    }

    private static class Response {
        private final int status;
        private final Map<DToken, DToken> headers;
        private final byte[] body;

        private Response(int status, Map<DToken, DToken> headers, byte[] body) {
            this.status = status;
            this.headers = headers;
            this.body = body;
        }
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
        request.put(DString.valueOf("query"), queryToTable(exchange.getRequestURI().getRawQuery()));
        request.put(DString.valueOf("headers"), toTable(exchange.getRequestHeaders()));
        request.put(DString.valueOf("body"), DByteUtils.toList(readAll(exchange.getRequestBody())));
        request.put(DString.valueOf("remote-host"), DString.valueOf(exchange.getRemoteAddress().getHostString()));
        request.put(DString.valueOf("remote-port"), DNumber.valueOf(exchange.getRemoteAddress().getPort()));
        return DTable.valueOf(request);
    }

    private static DTable queryToTable(String rawQuery) {
        Map<DToken, DToken> query = new HashMap<>();
        if (rawQuery == null || rawQuery.isEmpty())
            return DTable.valueOf(query);
        for (String pair : rawQuery.split("&", -1)) {
            if (pair.isEmpty())
                continue;
            int index = pair.indexOf('=');
            String key = index < 0 ? pair : pair.substring(0, index);
            String value = index < 0 ? "" : pair.substring(index + 1);
            try {
                query.put(DString.valueOf(URLDecoder.decode(key, StandardCharsets.UTF_8.name())),
                        DString.valueOf(URLDecoder.decode(value, StandardCharsets.UTF_8.name())));
            } catch (UnsupportedEncodingException e) {
                throw new DevoreRuntimeException("字符集不存在: " + StandardCharsets.UTF_8.name());
            } catch (IllegalArgumentException e) {
                throw new DevoreRuntimeException("HTTP query格式错误: " + rawQuery);
            }
        }
        return DTable.valueOf(query);
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

    private static String message(Throwable e) {
        return e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
    }
}
