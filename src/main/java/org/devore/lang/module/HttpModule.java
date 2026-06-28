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
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

/**
 * HTTP请求
 */
public class HttpModule extends DModule {
    private static final int DEFAULT_TIMEOUT = 30000;

    public HttpModule() {
        super("http");
    }

    @Override
    public void init(Env dEnv) {
        initConstants(dEnv);
        initTypeAndUrl(dEnv);
        initServer(dEnv);
        initClient(dEnv);
        initHelpers(dEnv);
    }

    private void initConstants(Env dEnv) {
        dEnv.put("http-status-ok", DNumber.valueOf(200));
        dEnv.put("http-status-created", DNumber.valueOf(201));
        dEnv.put("http-status-no-content", DNumber.valueOf(204));
        dEnv.put("http-status-moved-permanently", DNumber.valueOf(301));
        dEnv.put("http-status-found", DNumber.valueOf(302));
        dEnv.put("http-status-bad-request", DNumber.valueOf(400));
        dEnv.put("http-status-unauthorized", DNumber.valueOf(401));
        dEnv.put("http-status-forbidden", DNumber.valueOf(403));
        dEnv.put("http-status-not-found", DNumber.valueOf(404));
        dEnv.put("http-status-method-not-allowed", DNumber.valueOf(405));
        dEnv.put("http-status-internal-server-error", DNumber.valueOf(500));
    }

    private void initTypeAndUrl(Env dEnv) {
        dEnv.addTokenProcedure("http-server?", (args, env) ->
                DBool.valueOf(args.get(0) instanceof DHttpServer), 1, false);
        dEnv.addTokenProcedure("http-url-encode", (args, env) -> {
            if (!(args.get(0) instanceof DString))
                throw new DevoreCastException(args.get(0).type(), "string");
            return DString.valueOf(urlEncode(args.get(0).toString(), StandardCharsets.UTF_8));
        }, 1, false);
        dEnv.addTokenProcedure("http-url-encode", (args, env) -> {
            if (!(args.get(0) instanceof DString))
                throw new DevoreCastException(args.get(0).type(), "string");
            return DString.valueOf(urlEncode(args.get(0).toString(), toCharset(args.get(1))));
        }, 2, false);
        dEnv.addTokenProcedure("http-url-decode", (args, env) -> {
            if (!(args.get(0) instanceof DString))
                throw new DevoreCastException(args.get(0).type(), "string");
            return DString.valueOf(urlDecode(args.get(0).toString(), StandardCharsets.UTF_8));
        }, 1, false);
        dEnv.addTokenProcedure("http-url-decode", (args, env) -> {
            if (!(args.get(0) instanceof DString))
                throw new DevoreCastException(args.get(0).type(), "string");
            return DString.valueOf(urlDecode(args.get(0).toString(), toCharset(args.get(1))));
        }, 2, false);
        dEnv.addTokenProcedure("http-build-query", (args, env) -> {
            if (!(args.get(0) instanceof DTable))
                throw new DevoreCastException(args.get(0).type(), "table");
            return DString.valueOf(buildQuery((DTable) args.get(0)));
        }, 1, false);
    }

    private void initServer(Env dEnv) {
        dEnv.addTokenProcedure("http-listen", (args, env) -> {
            int port = toPort(args.get(0));
            try {
                return openServer(new InetSocketAddress(port));
            } catch (IOException e) {
                throw new DevoreRuntimeException("HTTP监听失败: " + port + ", " + e.getMessage());
            }
        }, 1, false);
        dEnv.addTokenProcedure("http-listen", (args, env) -> {
            if (!(args.get(0) instanceof DString))
                throw new DevoreCastException(args.get(0).type(), "string");
            int port = toPort(args.get(1));
            String host = args.get(0).toString();
            try {
                return openServer(new InetSocketAddress(host, port));
            } catch (IOException e) {
                throw new DevoreRuntimeException("HTTP监听失败: " + host + ":" + port + ", " + e.getMessage());
            }
        }, 2, false);
        dEnv.addTokenProcedure("http-listen-tls", (args, env) -> {
            if (!(args.get(0) instanceof DString))
                throw new DevoreCastException(args.get(0).type(), "string");
            int port = toPort(args.get(1));
            if (!(args.get(2) instanceof DString))
                throw new DevoreCastException(args.get(2).type(), "string");
            if (!(args.get(3) instanceof DString))
                throw new DevoreCastException(args.get(3).type(), "string");
            String host = args.get(0).toString();
            Path keyStore = Paths.get(args.get(2).toString());
            char[] password = args.get(3).toString().toCharArray();
            try {
                return openTlsServer(new InetSocketAddress(host, port), keyStore, password);
            } catch (Exception e) {
                throw new DevoreRuntimeException("HTTPS监听失败: " + host + ":" + port + ", " + e.getMessage());
            }
        }, 4, false);
        dEnv.addTokenProcedure("http-handler", (args, env) -> registerHandler(args, env, null), 3, false);
        dEnv.addTokenProcedure("http-handler", (args, env) -> registerHandler(args, env, args.get(1)), 4, false);
        dEnv.addTokenProcedure("http-static", (args, env) -> {
            DHttpServer server = toServer(args.get(0));
            String prefix = toPath(args.get(1));
            if (!(args.get(2) instanceof DString))
                throw new DevoreCastException(args.get(2).type(), "string");
            Path root = Paths.get(args.get(2).toString()).toAbsolutePath().normalize();
            server.toHttpServer().createContext(prefix, exchange -> handleStatic(exchange, prefix, root));
            return DWord.NIL;
        }, 3, false);
        dEnv.addTokenProcedure("http-start", (args, env) -> {
            toServer(args.get(0)).start();
            return DWord.NIL;
        }, 1, false);
        dEnv.addTokenProcedure("http-stop", (args, env) -> {
            toServer(args.get(0)).stop(0);
            return DWord.NIL;
        }, 1, false);
        dEnv.addTokenProcedure("http-stop", (args, env) -> {
            DHttpServer server = toServer(args.get(0));
            if (!(args.get(1) instanceof DInt))
                throw new DevoreCastException(args.get(1).type(), "int");
            int delay = DIntUtils.toInt((DInt) args.get(1));
            if (delay < 0)
                throw new DevoreRuntimeException("停止等待时间不能为负数: " + delay);
            server.stop(delay);
            return DWord.NIL;
        }, 2, false);
    }

    private void initClient(Env dEnv) {
        dEnv.addTokenProcedure("http-request", (args, env) ->
                toResponseTable(requestToken(args.get(1), method(args.get(0)), null, null, DEFAULT_TIMEOUT)), 2, false);
        dEnv.addTokenProcedure("http-request", (args, env) ->
                toResponseTable(requestToken(args.get(1), method(args.get(0)), toHeaders(args.get(2)), null,
                        DEFAULT_TIMEOUT)), 3, false);
        dEnv.addTokenProcedure("http-request", (args, env) ->
                toResponseTable(requestToken(args.get(1), method(args.get(0)), toHeaders(args.get(2)),
                        toOptionalRequestBody(args.get(3)), DEFAULT_TIMEOUT)), 4, false);
        dEnv.addTokenProcedure("http-request", (args, env) ->
                toResponseTable(requestToken(args.get(1), method(args.get(0)), toHeaders(args.get(2)),
                        toOptionalRequestBody(args.get(3)), toTimeout(args.get(4)))), 5, false);
        registerRequestResult(dEnv, "http-request-binary", true);
        registerRequestResult(dEnv, "http-request-string", false);
        dEnv.addTokenProcedure("http-get", (args, env) ->
                toResponseTable(requestToken(args.get(0), "GET", null, null, DEFAULT_TIMEOUT)), 1, false);
        dEnv.addTokenProcedure("http-get", (args, env) -> {
            RequestOptions options = headersOrTimeout(args.get(1));
            return toResponseTable(requestToken(args.get(0), "GET", options.headers, null, options.timeout));
        }, 2, false);
        dEnv.addTokenProcedure("http-get", (args, env) ->
                toResponseTable(requestToken(args.get(0), "GET", toHeaders(args.get(1)), null,
                        toTimeout(args.get(2)))), 3, false);
        registerBodyMethod(dEnv, "http-post", "POST");
        registerBodyMethod(dEnv, "http-put", "PUT");
        registerBodyMethod(dEnv, "http-patch", "PATCH");
        dEnv.addTokenProcedure("http-delete", (args, env) ->
                toResponseTable(requestToken(args.get(0), "DELETE", null, null, DEFAULT_TIMEOUT)), 1, false);
        dEnv.addTokenProcedure("http-delete", (args, env) -> {
            RequestOptions options = headersOrTimeout(args.get(1));
            return toResponseTable(requestToken(args.get(0), "DELETE", options.headers, null, options.timeout));
        }, 2, false);
        dEnv.addTokenProcedure("http-delete", (args, env) ->
                toResponseTable(requestToken(args.get(0), "DELETE", toHeaders(args.get(1)), null,
                        toTimeout(args.get(2)))), 3, false);
        registerNoBodyResult(dEnv, "http-get-binary", "GET", true);
        registerNoBodyResult(dEnv, "http-get-string", "GET", false);
        registerBodyResult(dEnv, "http-post-binary", "POST", true);
        registerBodyResult(dEnv, "http-post-string", "POST", false);
        registerBodyResult(dEnv, "http-put-binary", "PUT", true);
        registerBodyResult(dEnv, "http-put-string", "PUT", false);
        registerBodyResult(dEnv, "http-patch-binary", "PATCH", true);
        registerBodyResult(dEnv, "http-patch-string", "PATCH", false);
        registerNoBodyResult(dEnv, "http-delete-binary", "DELETE", true);
        registerNoBodyResult(dEnv, "http-delete-string", "DELETE", false);
        registerPostForm(dEnv);
        registerPostFormResult(dEnv, "http-post-form-binary", true);
        registerPostFormResult(dEnv, "http-post-form-string", false);
        registerPostMultipart(dEnv);
        registerPostMultipartResult(dEnv, "http-post-multipart-binary", true);
        registerPostMultipartResult(dEnv, "http-post-multipart-string", false);
    }

    private void initHelpers(Env dEnv) {
        dEnv.addTokenProcedure("http-header", (args, env) -> {
            DTable headers = toTableToken(args.get(0));
            if (!(args.get(1) instanceof DString))
                throw new DevoreCastException(args.get(1).type(), "string");
            return getHeader(headers, args.get(1).toString());
        }, 2, false);
        dEnv.addTokenProcedure("http-redirect", (args, env) -> redirect(args.get(0), 302), 1, false);
        dEnv.addTokenProcedure("http-redirect", (args, env) -> redirect(args.get(0), toStatus(args.get(1))), 2, false);
        dEnv.addTokenProcedure("http-cookie", (args, env) -> {
            DTable request = toTableToken(args.get(0));
            if (!(args.get(1) instanceof DString))
                throw new DevoreCastException(args.get(1).type(), "string");
            DToken headers = request.get(DString.valueOf("headers"));
            return headers instanceof DTable ? cookie((DTable) headers, args.get(1).toString()) : DWord.NIL;
        }, 2, false);
        dEnv.addTokenProcedure("http-set-cookie", (args, env) -> setCookie(args.get(0), args.get(1), args.get(2),
                null), 3, false);
        dEnv.addTokenProcedure("http-set-cookie", (args, env) -> setCookie(args.get(0), args.get(1), args.get(2),
                toTableToken(args.get(3))), 4, false);
    }

    private void registerBodyMethod(Env dEnv, String name, String method) {
        dEnv.addTokenProcedure(name, (args, env) ->
                toResponseTable(requestToken(args.get(0), method, null, toRequestBody(args.get(1)),
                        DEFAULT_TIMEOUT)), 2, false);
        dEnv.addTokenProcedure(name, (args, env) -> {
            if (args.get(1) instanceof DTable)
                return toResponseTable(requestToken(args.get(0), method, (DTable) args.get(1),
                        toRequestBody(args.get(2)), DEFAULT_TIMEOUT));
            return toResponseTable(requestToken(args.get(0), method, null, toRequestBody(args.get(1)),
                    toTimeout(args.get(2))));
        }, 3, false);
        dEnv.addTokenProcedure(name, (args, env) ->
                toResponseTable(requestToken(args.get(0), method, toHeaders(args.get(1)),
                        toRequestBody(args.get(2)), toTimeout(args.get(3)))), 4, false);
    }

    private void registerRequestResult(Env dEnv, String name, boolean binary) {
        dEnv.addTokenProcedure(name, (args, env) ->
                responseBody(requestToken(args.get(1), method(args.get(0)), null, null, DEFAULT_TIMEOUT), binary,
                        StandardCharsets.UTF_8), 2, false);
        dEnv.addTokenProcedure(name, (args, env) -> {
            if (binary)
                return responseBody(requestToken(args.get(1), method(args.get(0)), toHeaders(args.get(2)), null,
                        DEFAULT_TIMEOUT), true, StandardCharsets.UTF_8);
            if (args.get(2) instanceof DString)
                return responseBody(requestToken(args.get(1), method(args.get(0)), null, null, DEFAULT_TIMEOUT),
                        false, toCharset(args.get(2)));
            return responseBody(requestToken(args.get(1), method(args.get(0)), toHeaders(args.get(2)), null,
                    DEFAULT_TIMEOUT), false, StandardCharsets.UTF_8);
        }, 3, false);
        dEnv.addTokenProcedure(name, (args, env) ->
                responseBody(requestToken(args.get(1), method(args.get(0)), toHeaders(args.get(2)),
                        toOptionalRequestBody(args.get(3)), DEFAULT_TIMEOUT), binary, StandardCharsets.UTF_8),
                4, false);
        dEnv.addTokenProcedure(name, (args, env) -> {
            if (binary)
                return responseBody(requestToken(args.get(1), method(args.get(0)), toHeaders(args.get(2)),
                        toOptionalRequestBody(args.get(3)), toTimeout(args.get(4))), true, StandardCharsets.UTF_8);
            if (args.get(4) instanceof DInt)
                return responseBody(requestToken(args.get(1), method(args.get(0)), toHeaders(args.get(2)),
                        toOptionalRequestBody(args.get(3)), toTimeout(args.get(4))), false, StandardCharsets.UTF_8);
            return responseBody(requestToken(args.get(1), method(args.get(0)), toHeaders(args.get(2)),
                    toOptionalRequestBody(args.get(3)), DEFAULT_TIMEOUT), false, toCharset(args.get(4)));
        }, 5, false);
        if (!binary) {
            dEnv.addTokenProcedure(name, (args, env) ->
                    responseBody(requestToken(args.get(1), method(args.get(0)), toHeaders(args.get(2)),
                            toOptionalRequestBody(args.get(3)), toTimeout(args.get(5))), false,
                            toCharset(args.get(4))), 6, false);
        }
    }

    private void registerNoBodyResult(Env dEnv, String name, String method, boolean binary) {
        dEnv.addTokenProcedure(name, (args, env) ->
                responseBody(requestToken(args.get(0), method, null, null, DEFAULT_TIMEOUT), binary,
                        StandardCharsets.UTF_8), 1, false);
        dEnv.addTokenProcedure(name, (args, env) -> {
            if (binary) {
                RequestOptions options = headersOrTimeout(args.get(1));
                return responseBody(requestToken(args.get(0), method, options.headers, null, options.timeout), true,
                        StandardCharsets.UTF_8);
            }
            if (args.get(1) instanceof DString)
                return responseBody(requestToken(args.get(0), method, null, null, DEFAULT_TIMEOUT), false,
                        toCharset(args.get(1)));
            RequestOptions options = headersOrTimeout(args.get(1));
            return responseBody(requestToken(args.get(0), method, options.headers, null, options.timeout), false,
                    StandardCharsets.UTF_8);
        }, 2, false);
        dEnv.addTokenProcedure(name, (args, env) -> {
            if (binary)
                return responseBody(requestToken(args.get(0), method, toHeaders(args.get(1)), null,
                        toTimeout(args.get(2))), true, StandardCharsets.UTF_8);
            if (args.get(2) instanceof DInt)
                return responseBody(requestToken(args.get(0), method, toHeaders(args.get(1)), null,
                        toTimeout(args.get(2))), false, StandardCharsets.UTF_8);
            return responseBody(requestToken(args.get(0), method, toHeaders(args.get(1)), null, DEFAULT_TIMEOUT),
                    false, toCharset(args.get(2)));
        }, 3, false);
        if (!binary) {
            dEnv.addTokenProcedure(name, (args, env) ->
                    responseBody(requestToken(args.get(0), method, toHeaders(args.get(1)), null,
                            toTimeout(args.get(3))), false, toCharset(args.get(2))), 4, false);
        }
    }

    private void registerBodyResult(Env dEnv, String name, String method, boolean binary) {
        dEnv.addTokenProcedure(name, (args, env) ->
                responseBody(requestToken(args.get(0), method, null, toRequestBody(args.get(1)), DEFAULT_TIMEOUT),
                        binary, StandardCharsets.UTF_8), 2, false);
        dEnv.addTokenProcedure(name, (args, env) -> {
            if (args.get(1) instanceof DTable)
                return responseBody(requestToken(args.get(0), method, (DTable) args.get(1),
                        toRequestBody(args.get(2)), DEFAULT_TIMEOUT), binary, StandardCharsets.UTF_8);
            if (binary)
                return responseBody(requestToken(args.get(0), method, null, toRequestBody(args.get(1)),
                        toTimeout(args.get(2))), true, StandardCharsets.UTF_8);
            if (args.get(2) instanceof DInt)
                return responseBody(requestToken(args.get(0), method, null, toRequestBody(args.get(1)),
                        toTimeout(args.get(2))), false, StandardCharsets.UTF_8);
            return responseBody(requestToken(args.get(0), method, null, toRequestBody(args.get(1)),
                    DEFAULT_TIMEOUT), false, toCharset(args.get(2)));
        }, 3, false);
        dEnv.addTokenProcedure(name, (args, env) -> {
            if (binary)
                return responseBody(requestToken(args.get(0), method, toHeaders(args.get(1)),
                        toRequestBody(args.get(2)), toTimeout(args.get(3))), true, StandardCharsets.UTF_8);
            if (args.get(3) instanceof DInt)
                return responseBody(requestToken(args.get(0), method, toHeaders(args.get(1)),
                        toRequestBody(args.get(2)), toTimeout(args.get(3))), false, StandardCharsets.UTF_8);
            return responseBody(requestToken(args.get(0), method, toHeaders(args.get(1)),
                    toRequestBody(args.get(2)), DEFAULT_TIMEOUT), false, toCharset(args.get(3)));
        }, 4, false);
        if (!binary) {
            dEnv.addTokenProcedure(name, (args, env) ->
                    responseBody(requestToken(args.get(0), method, toHeaders(args.get(1)),
                            toRequestBody(args.get(2)), toTimeout(args.get(4))), false, toCharset(args.get(3))),
                    5, false);
        }
    }

    private void registerPostForm(Env dEnv) {
        dEnv.addTokenProcedure("http-post-form", (args, env) ->
                toResponseTable(postForm(args.get(0), null, toTableToken(args.get(1)), DEFAULT_TIMEOUT)), 2, false);
        dEnv.addTokenProcedure("http-post-form", (args, env) -> {
            if (args.get(2) instanceof DInt)
                return toResponseTable(postForm(args.get(0), null, toTableToken(args.get(1)), toTimeout(args.get(2))));
            return toResponseTable(postForm(args.get(0), toHeaders(args.get(1)), toTableToken(args.get(2)),
                    DEFAULT_TIMEOUT));
        }, 3, false);
        dEnv.addTokenProcedure("http-post-form", (args, env) ->
                toResponseTable(postForm(args.get(0), toHeaders(args.get(1)), toTableToken(args.get(2)),
                        toTimeout(args.get(3)))), 4, false);
    }

    private void registerPostFormResult(Env dEnv, String name, boolean binary) {
        dEnv.addTokenProcedure(name, (args, env) ->
                responseBody(postForm(args.get(0), null, toTableToken(args.get(1)), DEFAULT_TIMEOUT), binary,
                        StandardCharsets.UTF_8), 2, false);
        dEnv.addTokenProcedure(name, (args, env) -> {
            if (args.get(2) instanceof DInt)
                return responseBody(postForm(args.get(0), null, toTableToken(args.get(1)), toTimeout(args.get(2))),
                        binary, StandardCharsets.UTF_8);
            if (!binary && args.get(2) instanceof DString)
                return responseBody(postForm(args.get(0), null, toTableToken(args.get(1)), DEFAULT_TIMEOUT), false,
                        toCharset(args.get(2)));
            return responseBody(postForm(args.get(0), toHeaders(args.get(1)), toTableToken(args.get(2)),
                    DEFAULT_TIMEOUT), binary, StandardCharsets.UTF_8);
        }, 3, false);
        dEnv.addTokenProcedure(name, (args, env) -> {
            if (args.get(2) instanceof DString && args.get(3) instanceof DInt) {
                if (binary)
                    throw new DevoreCastException(args.get(2).type(), "table");
                return responseBody(postForm(args.get(0), null, toTableToken(args.get(1)), toTimeout(args.get(3))),
                        false, toCharset(args.get(2)));
            }
            if (binary)
                return responseBody(postForm(args.get(0), toHeaders(args.get(1)), toTableToken(args.get(2)),
                        toTimeout(args.get(3))), true, StandardCharsets.UTF_8);
            if (args.get(3) instanceof DInt)
                return responseBody(postForm(args.get(0), toHeaders(args.get(1)), toTableToken(args.get(2)),
                        toTimeout(args.get(3))), false, StandardCharsets.UTF_8);
            return responseBody(postForm(args.get(0), toHeaders(args.get(1)), toTableToken(args.get(2)),
                    DEFAULT_TIMEOUT), false, toCharset(args.get(3)));
        }, 4, false);
        if (!binary) {
            dEnv.addTokenProcedure(name, (args, env) ->
                    responseBody(postForm(args.get(0), toHeaders(args.get(1)), toTableToken(args.get(2)),
                            toTimeout(args.get(4))), false, toCharset(args.get(3))), 5, false);
        }
    }

    private void registerPostMultipart(Env dEnv) {
        dEnv.addTokenProcedure("http-post-multipart", (args, env) ->
                toResponseTable(postMultipart(args.get(0), null, toTableToken(args.get(1)),
                        toTableToken(args.get(2)), DEFAULT_TIMEOUT)), 3, false);
        dEnv.addTokenProcedure("http-post-multipart", (args, env) -> {
            if (args.get(3) instanceof DInt)
                return toResponseTable(postMultipart(args.get(0), null, toTableToken(args.get(1)),
                        toTableToken(args.get(2)), toTimeout(args.get(3))));
            return toResponseTable(postMultipart(args.get(0), toHeaders(args.get(1)), toTableToken(args.get(2)),
                    toTableToken(args.get(3)), DEFAULT_TIMEOUT));
        }, 4, false);
        dEnv.addTokenProcedure("http-post-multipart", (args, env) ->
                toResponseTable(postMultipart(args.get(0), toHeaders(args.get(1)), toTableToken(args.get(2)),
                        toTableToken(args.get(3)), toTimeout(args.get(4)))), 5, false);
    }

    private void registerPostMultipartResult(Env dEnv, String name, boolean binary) {
        dEnv.addTokenProcedure(name, (args, env) ->
                responseBody(postMultipart(args.get(0), null, toTableToken(args.get(1)), toTableToken(args.get(2)),
                        DEFAULT_TIMEOUT), binary, StandardCharsets.UTF_8), 3, false);
        dEnv.addTokenProcedure(name, (args, env) -> {
            if (args.get(3) instanceof DInt)
                return responseBody(postMultipart(args.get(0), null, toTableToken(args.get(1)),
                        toTableToken(args.get(2)), toTimeout(args.get(3))), binary, StandardCharsets.UTF_8);
            if (!binary && args.get(3) instanceof DString)
                return responseBody(postMultipart(args.get(0), null, toTableToken(args.get(1)),
                        toTableToken(args.get(2)), DEFAULT_TIMEOUT), false, toCharset(args.get(3)));
            return responseBody(postMultipart(args.get(0), toHeaders(args.get(1)), toTableToken(args.get(2)),
                    toTableToken(args.get(3)), DEFAULT_TIMEOUT), binary, StandardCharsets.UTF_8);
        }, 4, false);
        dEnv.addTokenProcedure(name, (args, env) -> {
            if (args.get(3) instanceof DString && args.get(4) instanceof DInt) {
                if (binary)
                    throw new DevoreCastException(args.get(3).type(), "table");
                return responseBody(postMultipart(args.get(0), null, toTableToken(args.get(1)),
                        toTableToken(args.get(2)), toTimeout(args.get(4))), false, toCharset(args.get(3)));
            }
            if (binary)
                return responseBody(postMultipart(args.get(0), toHeaders(args.get(1)), toTableToken(args.get(2)),
                        toTableToken(args.get(3)), toTimeout(args.get(4))), true, StandardCharsets.UTF_8);
            if (args.get(4) instanceof DInt)
                return responseBody(postMultipart(args.get(0), toHeaders(args.get(1)), toTableToken(args.get(2)),
                        toTableToken(args.get(3)), toTimeout(args.get(4))), false, StandardCharsets.UTF_8);
            return responseBody(postMultipart(args.get(0), toHeaders(args.get(1)), toTableToken(args.get(2)),
                    toTableToken(args.get(3)), DEFAULT_TIMEOUT), false, toCharset(args.get(4)));
        }, 5, false);
        if (!binary) {
            dEnv.addTokenProcedure(name, (args, env) ->
                    responseBody(postMultipart(args.get(0), toHeaders(args.get(1)), toTableToken(args.get(2)),
                            toTableToken(args.get(3)), toTimeout(args.get(5))), false, toCharset(args.get(4))),
                    6, false);
        }
    }

    private static Response postForm(DToken url, DTable headers, DTable form, int timeout) {
        DTable requestHeaders = contentType(headers, "application/x-www-form-urlencoded; charset=UTF-8");
        return requestToken(url, "POST", requestHeaders, buildQuery(form).getBytes(StandardCharsets.UTF_8), timeout);
    }

    private static Response postMultipart(DToken url, DTable headers, DTable fields, DTable files, int timeout) {
        MultipartBody body = multipart(fields, files);
        DTable requestHeaders = contentType(headers, "multipart/form-data; boundary=" + body.boundary);
        return requestToken(url, "POST", requestHeaders, body.body, timeout);
    }

    private static DToken registerHandler(List<DToken> args, Env env, DToken methodToken) {
        DHttpServer server = toServer(args.get(0));
        String method = methodToken == null ? null : method(methodToken);
        int pathIndex = methodToken == null ? 1 : 2;
        int handlerIndex = methodToken == null ? 2 : 3;
        String pattern = toPath(args.get(pathIndex));
        if (!(args.get(handlerIndex) instanceof DProcedure))
            throw new DevoreCastException(args.get(handlerIndex).type(), "procedure");
        DProcedure handler = (DProcedure) args.get(handlerIndex);
        try {
            server.toHttpServer().createContext(contextPath(pattern), exchange ->
                    handleExchange(exchange, method, pattern, handler, env.createChild()));
            return DWord.NIL;
        } catch (IllegalArgumentException e) {
            throw new DevoreRuntimeException("HTTP路由注册失败: " + pattern + ", " + e.getMessage());
        }
    }

    private static void handleExchange(HttpExchange exchange, String method, String pattern, DProcedure handler,
                                       Env env) throws IOException {
        try {
            if (method != null && !method.equalsIgnoreCase(exchange.getRequestMethod())) {
                sendPlain(exchange, 405, "HTTP方法不允许: " + exchange.getRequestMethod());
                return;
            }
            Map<DToken, DToken> params = matchPath(pattern, exchange.getRequestURI().getPath());
            if (params == null) {
                sendPlain(exchange, 404, "HTTP路由不存在: " + exchange.getRequestURI().getPath());
                return;
            }
            DToken response = handler.call(Collections.singletonList(toRequest(exchange, params)), env.createChild());
            sendResponse(exchange, response);
        } catch (Throwable e) {
            sendPlain(exchange, 500, "HTTP服务端处理失败: " + message(e));
        } finally {
            exchange.close();
        }
    }

    private static void handleStatic(HttpExchange exchange, String prefix, Path root) throws IOException {
        try {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())
                    && !"HEAD".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendPlain(exchange, 405, "HTTP方法不允许: " + exchange.getRequestMethod());
                return;
            }
            String path = exchange.getRequestURI().getPath();
            String relative = path.length() <= prefix.length() ? "" : path.substring(prefix.length());
            while (relative.startsWith("/"))
                relative = relative.substring(1);
            Path file = root.resolve(urlDecode(relative, StandardCharsets.UTF_8)).normalize();
            if (!file.startsWith(root) || !Files.isRegularFile(file)) {
                sendPlain(exchange, 404, "HTTP静态文件不存在: " + path);
                return;
            }
            String type = Files.probeContentType(file);
            if (type != null)
                exchange.getResponseHeaders().set("Content-Type", type);
            byte[] body = Files.readAllBytes(file);
            exchange.sendResponseHeaders(200, "HEAD".equalsIgnoreCase(exchange.getRequestMethod()) ? -1 : body.length);
            if (!"HEAD".equalsIgnoreCase(exchange.getRequestMethod())) {
                try (OutputStream output = exchange.getResponseBody()) {
                    output.write(body);
                }
            }
        } catch (Throwable e) {
            sendPlain(exchange, 500, "HTTP静态文件处理失败: " + message(e));
        } finally {
            exchange.close();
        }
    }

    private static DTable toRequest(HttpExchange exchange, Map<DToken, DToken> params) throws IOException {
        Map<DToken, DToken> request = new HashMap<>();
        request.put(DString.valueOf("method"), DString.valueOf(exchange.getRequestMethod()));
        request.put(DString.valueOf("path"), DString.valueOf(exchange.getRequestURI().getPath()));
        request.put(DString.valueOf("params"), DTable.valueOf(params));
        request.put(DString.valueOf("query"), queryToTable(exchange.getRequestURI().getRawQuery()));
        request.put(DString.valueOf("headers"), toTable(exchange.getRequestHeaders()));
        request.put(DString.valueOf("body"), DByteUtils.toList(readAll(exchange.getRequestBody())));
        request.put(DString.valueOf("remote-host"), DString.valueOf(exchange.getRemoteAddress().getHostString()));
        request.put(DString.valueOf("remote-port"), DNumber.valueOf(exchange.getRemoteAddress().getPort()));
        return DTable.valueOf(request);
    }

    private static Map<DToken, DToken> matchPath(String pattern, String path) {
        List<String> patternParts = pathParts(pattern);
        List<String> pathParts = pathParts(path);
        if (patternParts.size() != pathParts.size())
            return null;
        Map<DToken, DToken> params = new HashMap<>();
        for (int i = 0; i < patternParts.size(); ++i) {
            String patternPart = patternParts.get(i);
            String pathPart = pathParts.get(i);
            if (patternPart.startsWith(":")) {
                if (patternPart.length() == 1)
                    throw new DevoreRuntimeException("HTTP路径参数名不能为空: " + pattern);
                params.put(DString.valueOf(patternPart.substring(1)),
                        DString.valueOf(urlDecode(pathPart, StandardCharsets.UTF_8)));
            } else if (!patternPart.equals(pathPart)) {
                return null;
            }
        }
        return params;
    }

    private static List<String> pathParts(String path) {
        List<String> parts = new ArrayList<>();
        for (String part : path.split("/", -1)) {
            if (!part.isEmpty())
                parts.add(part);
        }
        return parts;
    }

    private static String contextPath(String pattern) {
        if ("/".equals(pattern))
            return "/";
        int parameter = pattern.indexOf("/:");
        if (parameter < 0)
            return pattern;
        return parameter == 0 ? "/" : pattern.substring(0, parameter);
    }

    private static DHttpServer openServer(InetSocketAddress address) throws IOException {
        HttpServer server = HttpServer.create(address, 0);
        ExecutorService executor = Executors.newCachedThreadPool();
        server.setExecutor(executor);
        return DHttpServer.valueOf(server, executor);
    }

    private static DHttpServer openTlsServer(InetSocketAddress address, Path keyStorePath, char[] password)
            throws Exception {
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        try (InputStream input = Files.newInputStream(keyStorePath.toFile().toPath())) {
            keyStore.load(input, password);
        }
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, password);
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(keyManagerFactory.getKeyManagers(), null, null);

        HttpsServer server = HttpsServer.create(address, 0);
        server.setHttpsConfigurator(new HttpsConfigurator(sslContext));
        ExecutorService executor = Executors.newCachedThreadPool();
        server.setExecutor(executor);
        return DHttpServer.valueOf(server, executor);
    }

    private static Response requestToken(DToken url, String method, DTable headers, byte[] requestBody, int timeout) {
        URI uri = toHttpUri(url);
        try {
            return request(uri, method, headers, requestBody, timeout);
        } catch (IOException e) {
            throw new DevoreRuntimeException("HTTP请求失败: " + uri + ", " + e.getMessage());
        }
    }

    private static Response request(URI uri, String method, DTable headers, byte[] requestBody, int timeout)
            throws IOException {
        HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
        connection.setRequestMethod(method);
        connection.setConnectTimeout(timeout);
        connection.setReadTimeout(timeout);
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

    private static DTable toResponseTable(Response response) {
        Map<DToken, DToken> result = new HashMap<>();
        result.put(DString.valueOf("status"), DNumber.valueOf(response.status));
        result.put(DString.valueOf("headers"), DTable.valueOf(response.headers));
        result.put(DString.valueOf("body"), DByteUtils.toList(response.body));
        return DTable.valueOf(result);
    }

    private static DToken responseBody(Response response, boolean binary, Charset charset) {
        return binary ? DByteUtils.toList(response.body) : DString.valueOf(new String(response.body, charset));
    }

    private static DTable contentType(DTable headers, String contentType) {
        Map<DToken, DToken> result = new HashMap<>();
        if (headers != null) {
            for (DToken key : headers.keys())
                result.put(key, headers.get(key));
        }
        if (getHeader(DTable.valueOf(result), "Content-Type") == DWord.NIL)
            result.put(DString.valueOf("Content-Type"), DString.valueOf(contentType));
        return DTable.valueOf(result);
    }

    private static String buildQuery(DTable table) {
        StringJoiner joiner = new StringJoiner("&");
        for (DToken key : table.keys()) {
            if (!(key instanceof DString))
                throw new DevoreCastException(key.type(), "string");
            DToken value = table.get(key);
            if (value instanceof DList) {
                for (DToken item : ((DList) value).toList())
                    joiner.add(urlEncode(key.toString(), StandardCharsets.UTF_8) + "="
                            + urlEncode(item.toString(), StandardCharsets.UTF_8));
            } else {
                joiner.add(urlEncode(key.toString(), StandardCharsets.UTF_8) + "="
                        + urlEncode(value.toString(), StandardCharsets.UTF_8));
            }
        }
        return joiner.toString();
    }

    private static MultipartBody multipart(DTable fields, DTable files) {
        String boundary = "----DevoreBoundary" + Long.toHexString(System.nanoTime());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            for (DToken key : fields.keys()) {
                if (!(key instanceof DString))
                    throw new DevoreCastException(key.type(), "string");
                writeAscii(out, "--" + boundary + "\r\n");
                writeAscii(out, "Content-Disposition: form-data; name=\"" + escapeQuote(key.toString()) + "\"\r\n\r\n");
                out.write(fields.get(key).toString().getBytes(StandardCharsets.UTF_8));
                writeAscii(out, "\r\n");
            }
            for (DToken key : files.keys()) {
                if (!(key instanceof DString))
                    throw new DevoreCastException(key.type(), "string");
                FilePart part = toFilePart(files.get(key));
                writeAscii(out, "--" + boundary + "\r\n");
                writeAscii(out, "Content-Disposition: form-data; name=\"" + escapeQuote(key.toString())
                        + "\"; filename=\"" + escapeQuote(part.filename) + "\"\r\n");
                writeAscii(out, "Content-Type: " + part.contentType + "\r\n\r\n");
                out.write(part.body);
                writeAscii(out, "\r\n");
            }
            writeAscii(out, "--" + boundary + "--\r\n");
        } catch (IOException e) {
            throw new DevoreRuntimeException("HTTP multipart构造失败: " + e.getMessage());
        }
        return new MultipartBody(boundary, out.toByteArray());
    }

    private static FilePart toFilePart(DToken token) throws IOException {
        if (token instanceof DString) {
            Path path = Paths.get(token.toString());
            String type = Files.probeContentType(path);
            return new FilePart(path.getFileName().toString(), type == null ? "application/octet-stream" : type,
                    Files.readAllBytes(path));
        }
        if (token instanceof DTable) {
            DTable table = (DTable) token;
            DToken filename = table.get(DString.valueOf("filename"));
            DToken body = table.get(DString.valueOf("body"));
            DToken type = table.get(DString.valueOf("content-type"));
            if (!(filename instanceof DString))
                throw new DevoreCastException(filename.type(), "string");
            return new FilePart(filename.toString(),
                    type instanceof DString ? type.toString() : "application/octet-stream", toRequestBody(body));
        }
        throw new DevoreCastException(token.type(), "string|table");
    }

    private static void writeAscii(ByteArrayOutputStream out, String value) throws IOException {
        out.write(value.getBytes(StandardCharsets.US_ASCII));
    }

    private static String escapeQuote(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static DToken redirect(DToken locationToken, int status) {
        if (!(locationToken instanceof DString))
            throw new DevoreCastException(locationToken.type(), "string");
        Map<DToken, DToken> headers = new HashMap<>();
        headers.put(DString.valueOf("Location"), locationToken);
        Map<DToken, DToken> response = new HashMap<>();
        response.put(DString.valueOf("status"), DNumber.valueOf(status));
        response.put(DString.valueOf("headers"), DTable.valueOf(headers));
        response.put(DString.valueOf("body"), DWord.NIL);
        return DTable.valueOf(response);
    }

    private static DToken setCookie(DToken responseToken, DToken nameToken, DToken valueToken, DTable options) {
        if (!(nameToken instanceof DString))
            throw new DevoreCastException(nameToken.type(), "string");
        if (!(valueToken instanceof DString))
            throw new DevoreCastException(valueToken.type(), "string");
        DTable response;
        if (responseToken instanceof DTable) {
            response = (DTable) responseToken;
        } else {
            Map<DToken, DToken> wrapped = new HashMap<>();
            wrapped.put(DString.valueOf("status"), DNumber.valueOf(200));
            wrapped.put(DString.valueOf("headers"), DTable.valueOf(new HashMap<>()));
            wrapped.put(DString.valueOf("body"), responseToken);
            response = DTable.valueOf(wrapped);
        }
        DTable headers = response.get(DString.valueOf("headers")) instanceof DTable
                ? (DTable) response.get(DString.valueOf("headers")) : DTable.valueOf(new HashMap<>());
        StringBuilder cookie = new StringBuilder(nameToken.toString()).append("=")
                .append(urlEncode(valueToken.toString(), StandardCharsets.UTF_8));
        if (options != null) {
            for (DToken key : options.keys()) {
                if (!(key instanceof DString))
                    throw new DevoreCastException(key.type(), "string");
                DToken value = options.get(key);
                cookie.append("; ").append(key);
                if (value != DWord.NIL && !(value instanceof DBool && !Boolean.parseBoolean(value.toString())))
                    cookie.append("=").append(value);
            }
        }
        Map<DToken, DToken> newHeaders = new HashMap<>();
        for (DToken key : headers.keys())
            newHeaders.put(key, headers.get(key));
        DToken old = getHeader(headers, "Set-Cookie");
        newHeaders.put(DString.valueOf("Set-Cookie"), old == DWord.NIL ? DString.valueOf(cookie.toString())
                : DString.valueOf(old + "," + cookie));

        Map<DToken, DToken> newResponse = new HashMap<>();
        for (DToken key : response.keys())
            newResponse.put(key, response.get(key));
        newResponse.put(DString.valueOf("headers"), DTable.valueOf(newHeaders));
        return DTable.valueOf(newResponse);
    }

    private static DToken cookie(DTable headers, String name) {
        DToken cookieHeader = getHeader(headers, "Cookie");
        if (!(cookieHeader instanceof DString))
            return DWord.NIL;
        for (String part : cookieHeader.toString().split(";")) {
            int index = part.indexOf('=');
            if (index < 0)
                continue;
            String key = part.substring(0, index).trim();
            if (key.equals(name))
                return DString.valueOf(urlDecode(part.substring(index + 1).trim(), StandardCharsets.UTF_8));
        }
        return DWord.NIL;
    }

    private static DToken getHeader(DTable headers, String name) {
        for (DToken key : headers.keys()) {
            if (key instanceof DString && key.toString().equalsIgnoreCase(name))
                return headers.get(key);
        }
        return DWord.NIL;
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
            query.put(DString.valueOf(urlDecode(key, StandardCharsets.UTF_8)),
                    DString.valueOf(urlDecode(value, StandardCharsets.UTF_8)));
        }
        return DTable.valueOf(query);
    }

    private static DTable toTable(Headers headers) {
        Map<DToken, DToken> table = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : headers.entrySet())
            table.put(DString.valueOf(entry.getKey()), DString.valueOf(String.join(",", entry.getValue())));
        return DTable.valueOf(table);
    }

    private static Map<DToken, DToken> headersToTable(Map<String, List<String>> fields) {
        Map<DToken, DToken> headers = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : fields.entrySet()) {
            if (entry.getKey() != null)
                headers.put(DString.valueOf(entry.getKey()), DString.valueOf(String.join(",", entry.getValue())));
        }
        return headers;
    }

    private static void sendResponse(HttpExchange exchange, DToken response) throws IOException {
        int status = 200;
        DToken headers = DWord.NIL;
        DToken body = response;
        if (response instanceof DTable) {
            DTable table = (DTable) response;
            DToken statusToken = table.get(DString.valueOf("status"));
            if (statusToken != DWord.NIL)
                status = toStatus(statusToken);
            headers = table.get(DString.valueOf("headers"));
            body = table.get(DString.valueOf("body"));
        }
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

    private static void sendPlain(HttpExchange exchange, int status, String message) throws IOException {
        byte[] body = message.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        exchange.sendResponseHeaders(status, body.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(body);
        }
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

    private static String method(DToken token) {
        if (!(token instanceof DString))
            throw new DevoreCastException(token.type(), "string");
        return token.toString().toUpperCase();
    }

    private static DHttpServer toServer(DToken token) {
        if (!(token instanceof DHttpServer))
            throw new DevoreCastException(token.type(), "http-server");
        return (DHttpServer) token;
    }

    private static String toPath(DToken token) {
        if (!(token instanceof DString))
            throw new DevoreCastException(token.type(), "string");
        String path = token.toString();
        if (!path.startsWith("/"))
            throw new DevoreRuntimeException("HTTP路由路径必须以/开头: " + path);
        return path;
    }

    private static DTable toHeaders(DToken token) {
        if (!(token instanceof DTable))
            throw new DevoreCastException(token.type(), "table");
        return (DTable) token;
    }

    private static DTable toTableToken(DToken token) {
        if (!(token instanceof DTable))
            throw new DevoreCastException(token.type(), "table");
        return (DTable) token;
    }

    private static byte[] toOptionalRequestBody(DToken body) {
        return body == DWord.NIL ? null : toRequestBody(body);
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

    private static int toPort(DToken token) {
        if (!(token instanceof DInt))
            throw new DevoreCastException(token.type(), "int");
        return DNetworkUtils.toPort((DInt) token);
    }

    private static int toTimeout(DToken token) {
        if (!(token instanceof DInt))
            throw new DevoreCastException(token.type(), "int");
        int seconds = DIntUtils.toInt((DInt) token);
        if (seconds < 0)
            throw new DevoreRuntimeException("HTTP超时时间不能为负数: " + seconds);
        return seconds * 1000;
    }

    private static int toStatus(DToken token) {
        if (!(token instanceof DInt))
            throw new DevoreCastException(token.type(), "int");
        int status = DIntUtils.toInt((DInt) token);
        if (status < 100 || status > 599)
            throw new DevoreRuntimeException("HTTP状态码范围必须是100-599: " + status);
        return status;
    }

    private static RequestOptions headersOrTimeout(DToken token) {
        if (token instanceof DTable)
            return new RequestOptions((DTable) token, DEFAULT_TIMEOUT);
        if (token instanceof DInt)
            return new RequestOptions(null, toTimeout(token));
        throw new DevoreCastException(token.type(), "table|int");
    }

    private static String urlEncode(String value, Charset charset) {
        try {
            return URLEncoder.encode(value, charset.name());
        } catch (UnsupportedEncodingException e) {
            throw new DevoreRuntimeException("字符集不存在: " + charset.name());
        }
    }

    private static String urlDecode(String value, Charset charset) {
        try {
            return URLDecoder.decode(value, charset.name());
        } catch (UnsupportedEncodingException e) {
            throw new DevoreRuntimeException("字符集不存在: " + charset.name());
        } catch (IllegalArgumentException e) {
            throw new DevoreRuntimeException("URL解码失败: " + value);
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

    private static final class Response {
        private final int status;
        private final Map<DToken, DToken> headers;
        private final byte[] body;

        private Response(int status, Map<DToken, DToken> headers, byte[] body) {
            this.status = status;
            this.headers = headers;
            this.body = body;
        }
    }

    private static final class RequestOptions {
        private final DTable headers;
        private final int timeout;

        private RequestOptions(DTable headers, int timeout) {
            this.headers = headers;
            this.timeout = timeout;
        }
    }

    private static final class MultipartBody {
        private final String boundary;
        private final byte[] body;

        private MultipartBody(String boundary, byte[] body) {
            this.boundary = boundary;
            this.body = body;
        }
    }

    private static final class FilePart {
        private final String filename;
        private final String contentType;
        private final byte[] body;

        private FilePart(String filename, String contentType, byte[] body) {
            this.filename = filename;
            this.contentType = contentType;
            this.body = body;
        }
    }
}
