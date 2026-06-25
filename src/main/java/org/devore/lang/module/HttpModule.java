package org.devore.lang.module;

import org.devore.exception.DevoreCastException;
import org.devore.exception.DevoreRuntimeException;
import org.devore.lang.Env;
import org.devore.lang.token.*;
import org.devore.utils.DByteUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HTTP请求
 */
public class HttpModule extends Module {
    public HttpModule() {
        super("http");
    }

    @Override
    public void init(Env dEnv) {
        dEnv.addTokenProcedure("http-get", (args, env) -> {
            if (!(args.get(0) instanceof DString))
                throw new DevoreCastException(args.get(0).type(), "string");
            URI uri;
            try {
                uri = new URI(args.get(0).toString());
            } catch (URISyntaxException e) {
                throw new DevoreRuntimeException("URL格式错误: " + args.get(0) + ".");
            }
            if (!"http".equalsIgnoreCase(uri.getScheme()) && !"https".equalsIgnoreCase(uri.getScheme()))
                throw new DevoreRuntimeException("URL协议必须是http或https: " + args.get(0) + ".");
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
                        headers.put(DString.valueOf(entry.getKey()), DString.valueOf(String.join(",", entry.getValue())));
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
                throw new DevoreRuntimeException("URL格式错误: " + args.get(0) + ".");
            }
            if (!"http".equalsIgnoreCase(uri.getScheme()) && !"https".equalsIgnoreCase(uri.getScheme()))
                throw new DevoreRuntimeException("URL协议必须是http或https: " + args.get(0) + ".");
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
                        headers.put(DString.valueOf(entry.getKey()), DString.valueOf(String.join(",", entry.getValue())));
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
                throw new DevoreRuntimeException("URL格式错误: " + args.get(0) + ".");
            }
            if (!"http".equalsIgnoreCase(uri.getScheme()) && !"https".equalsIgnoreCase(uri.getScheme()))
                throw new DevoreRuntimeException("URL协议必须是http或https: " + args.get(0) + ".");
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
                throw new DevoreRuntimeException("URL格式错误: " + args.get(0) + ".");
            }
            if (!"http".equalsIgnoreCase(uri.getScheme()) && !"https".equalsIgnoreCase(uri.getScheme()))
                throw new DevoreRuntimeException("URL协议必须是http或https: " + args.get(0) + ".");
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
                throw new DevoreRuntimeException("URL格式错误: " + args.get(0) + ".");
            }
            if (!"http".equalsIgnoreCase(uri.getScheme()) && !"https".equalsIgnoreCase(uri.getScheme()))
                throw new DevoreRuntimeException("URL协议必须是http或https: " + args.get(0) + ".");
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
            if (!(args.get(1) instanceof DTable))
                throw new DevoreCastException(args.get(1).type(), "table");
            URI uri;
            try {
                uri = new URI(args.get(0).toString());
            } catch (URISyntaxException e) {
                throw new DevoreRuntimeException("URL格式错误: " + args.get(0) + ".");
            }
            if (!"http".equalsIgnoreCase(uri.getScheme()) && !"https".equalsIgnoreCase(uri.getScheme()))
                throw new DevoreRuntimeException("URL协议必须是http或https: " + args.get(0) + ".");
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
        }, 2, false);
    }
}
