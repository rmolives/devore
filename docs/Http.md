# HTTP

需要导入模块：http

## 类型

### 新增类型

* http-server: HTTP服务端

### http-server?

* 作用：判断是否为http-server类型
* 参数数量：1
* 参数作用：内容
* 参数类型：token
* 返回值：是否为http-server类型
* 返回类型：bool
* 示例：(http-server? server)

## 常量

* http-status-ok: 200
* http-status-created: 201
* http-status-no-content: 204
* http-status-moved-permanently: 301
* http-status-found: 302
* http-status-bad-request: 400
* http-status-unauthorized: 401
* http-status-forbidden: 403
* http-status-not-found: 404
* http-status-method-not-allowed: 405
* http-status-internal-server-error: 500

## URL和Query

### http-url-encode

* 作用：使用UTF-8进行URL编码
* 参数数量：1
* 参数作用：内容
* 参数类型：string
* 返回值：编码后的内容
* 返回类型：string
* 示例：(http-url-encode "a b")

### http-url-encode

* 作用：使用指定字符集进行URL编码
* 参数数量：2
* 参数作用：内容、编码格式
* 参数类型：string、string
* 返回值：编码后的内容
* 返回类型：string
* 示例：(http-url-encode "a b" "UTF-8")

### http-url-decode

* 作用：使用UTF-8进行URL解码
* 参数数量：1
* 参数作用：内容
* 参数类型：string
* 返回值：解码后的内容
* 返回类型：string
* 示例：(http-url-decode "a+b")

### http-url-decode

* 作用：使用指定字符集进行URL解码
* 参数数量：2
* 参数作用：内容、编码格式
* 参数类型：string、string
* 返回值：解码后的内容
* 返回类型：string
* 示例：(http-url-decode "a+b" "UTF-8")

### http-build-query

* 作用：将表转换为URL查询字符串
* 参数数量：1
* 参数作用：参数表
* 参数类型：table
* 返回值：查询字符串
* 返回类型：string
* 说明：key必须是string；value为list时会生成同名多值参数
* 示例：(http-build-query (table \\["a" "1"\\] \\["b" "x y"\\]))

## HTTP服务端

### http-listen

* 作用：监听HTTP端口
* 参数数量：1
* 参数作用：端口
* 参数类型：int
* 返回值：HTTP服务端
* 返回类型：http-server
* 示例：(http-listen 8080)

### http-listen

* 作用：在指定地址监听HTTP端口
* 参数数量：2
* 参数作用：主机、端口
* 参数类型：string、int
* 返回值：HTTP服务端
* 返回类型：http-server
* 示例：(http-listen "127.0.0.1" 8080)

### http-listen-tls

* 作用：在指定地址监听HTTPS端口
* 参数数量：4
* 参数作用：主机、端口、keystore路径、keystore密码
* 参数类型：string、int、string、string
* 返回值：HTTP服务端
* 返回类型：http-server
* 说明：keystore使用Java默认KeyStore类型，通常为JKS或PKCS12
* 示例：(http-listen-tls "127.0.0.1" 8443 "./server.p12" "password")

### http-handler

* 作用：注册HTTP路由处理过程
* 参数数量：3
* 参数作用：HTTP服务端、路由路径、处理过程
* 参数类型：http-server、string、procedure
* 返回值：nil
* 返回类型：word
* 示例：(http-handler server "/" (lambda \[request\] "hello"))

### http-handler

* 作用：注册指定HTTP方法的路由处理过程
* 参数数量：4
* 参数作用：HTTP服务端、HTTP方法、路由路径、处理过程
* 参数类型：http-server、string、string、procedure
* 返回值：nil
* 返回类型：word
* 示例：(http-handler server "GET" "/users/:id" (lambda \[request\] "hello"))

处理过程接收request表：

* 参数值：{"method"=\<method\>, "path"=\<path\>, "params"=\<params\>, "query"=\<query\>, "headers"=\<headers\>, "body"=\<body\>, "remote-host"=\<remote-host\>, "remote-port"=\<remote-port\>}
* 参数类型：table
* params：路径参数表，key和value均为string；无路径参数时为空表
* query：查询参数表，key和value均为URL解码后的string；无查询参数时为空表
* body：请求体二进制列表

处理过程可以直接返回string或binary，也可以返回response表：

* 返回值：{"status"=\<status\>, "headers"=\<headers\>, "body"=\<body\>}
* 返回类型：table
* status：状态码，默认200
* headers：响应头表
* body：响应体，支持string或binary

### http-static

* 作用：注册静态文件目录
* 参数数量：3
* 参数作用：HTTP服务端、URL前缀、本地目录
* 参数类型：http-server、string、string
* 返回值：nil
* 返回类型：word
* 示例：(http-static server "/assets" "./public")

### http-start

* 作用：启动HTTP服务端
* 参数数量：1
* 参数作用：HTTP服务端
* 参数类型：http-server
* 返回值：nil
* 返回类型：word
* 示例：(http-start server)

### http-stop

* 作用：停止HTTP服务端
* 参数数量：1
* 参数作用：HTTP服务端
* 参数类型：http-server
* 返回值：nil
* 返回类型：word
* 示例：(http-stop server)

### http-stop

* 作用：停止HTTP服务端，并等待指定秒数处理已有请求
* 参数数量：2
* 参数作用：HTTP服务端、等待时间（秒）
* 参数类型：http-server、int
* 返回值：nil
* 返回类型：word
* 示例：(http-stop server 1)

## HTTP客户端

详细响应表格式：

* 返回值：{"status"=\<status\>, "headers"=\<headers\>, "body"=\<body\>}
* 返回类型：table
* status：HTTP状态码
* headers：响应头表
* body：响应体二进制列表

超时参数单位为秒，默认30秒。传入0表示立即超时。

### http-request

* 作用：通用HTTP请求，并返回详细响应
* 参数数量：2
* 参数作用：HTTP方法、url
* 参数类型：string、string
* 返回类型：table
* 示例：(http-request "GET" "http://127.0.0.1/")

### http-request

* 作用：通用HTTP请求，使用headers
* 参数数量：3
* 参数作用：HTTP方法、url、headers
* 参数类型：string、string、table
* 返回类型：table
* 示例：(http-request "GET" "http://127.0.0.1/" (table \["a" "3"\]))

### http-request

* 作用：通用HTTP请求，使用headers和body
* 参数数量：4
* 参数作用：HTTP方法、url、headers、body
* 参数类型：string、string、table、string|list|nil
* 返回类型：table
* 示例：(http-request "PUT" "http://127.0.0.1/" (table \["Content-Type" "text/plain"\]) "hello")

### http-request

* 作用：通用HTTP请求，使用headers、body和超时
* 参数数量：5
* 参数作用：HTTP方法、url、headers、body、超时（秒）
* 参数类型：string、string、table、string|list|nil、int
* 返回类型：table
* 示例：(http-request "PATCH" "http://127.0.0.1/" (table) "hello" 5)

### http-get

* 作用：GET访问http/https，并返回详细响应
* 参数数量：1
* 参数作用：url
* 参数类型：string
* 返回类型：table
* 示例：(http-get "http://127.0.0.1/")

### http-get

* 作用：GET访问http/https，使用headers或超时
* 参数数量：2
* 参数作用：url、headers|超时（秒）
* 参数类型：string、table|int
* 返回类型：table
* 示例：(http-get "http://127.0.0.1/" (table \["a" "3"\]))

### http-get

* 作用：GET访问http/https，使用headers和超时
* 参数数量：3
* 参数作用：url、headers、超时（秒）
* 参数类型：string、table、int
* 返回类型：table
* 示例：(http-get "http://127.0.0.1/" (table \["a" "3"\]) 5)

### http-post / http-put / http-patch

* 作用：发送带body的POST、PUT或PATCH请求，并返回详细响应
* 参数数量：2
* 参数作用：url、body
* 参数类型：string、string|list
* 返回类型：table
* 示例：(http-post "http://127.0.0.1/" "hello")

### http-post / http-put / http-patch

* 作用：发送带body的请求，使用headers或超时
* 参数数量：3
* 参数作用：url、headers、body 或 url、body、超时（秒）
* 参数类型：string、table、string|list 或 string、string|list、int
* 返回类型：table
* 示例：(http-put "http://127.0.0.1/" (table \["Content-Type" "text/plain"\]) "hello")

### http-post / http-put / http-patch

* 作用：发送带body的请求，使用headers和超时
* 参数数量：4
* 参数作用：url、headers、body、超时（秒）
* 参数类型：string、table、string|list、int
* 返回类型：table
* 示例：(http-patch "http://127.0.0.1/" (table \["Content-Type" "text/plain"\]) "hello" 5)

### http-delete

* 作用：DELETE访问http/https，并返回详细响应
* 参数数量：1
* 参数作用：url
* 参数类型：string
* 返回类型：table
* 示例：(http-delete "http://127.0.0.1/")

### http-delete

* 作用：DELETE访问http/https，使用headers或超时
* 参数数量：2
* 参数作用：url、headers|超时（秒）
* 参数类型：string、table|int
* 返回类型：table
* 示例：(http-delete "http://127.0.0.1/" 5)

### http-delete

* 作用：DELETE访问http/https，使用headers和超时
* 参数数量：3
* 参数作用：url、headers、超时（秒）
* 参数类型：string、table、int
* 返回类型：table
* 示例：(http-delete "http://127.0.0.1/" (table \["a" "3"\]) 5)

## 字符串和二进制响应

### http-request-string

* 作用：通用HTTP请求，并返回string
* 参数数量：2
* 参数作用：HTTP方法、url
* 参数类型：string、string
* 返回类型：string
* 示例：(http-request-string "GET" "http://127.0.0.1/")

### http-request-string

* 作用：通用HTTP请求，并采用特定编码格式或使用headers
* 参数数量：3
* 参数作用：HTTP方法、url、编码格式|headers
* 参数类型：string、string、string|table
* 返回类型：string
* 示例：(http-request-string "GET" "http://127.0.0.1/" "UTF-8")

### http-request-string

* 作用：通用HTTP请求，使用headers和body
* 参数数量：4
* 参数作用：HTTP方法、url、headers、body
* 参数类型：string、string、table、string|list|nil
* 返回类型：string
* 示例：(http-request-string "PUT" "http://127.0.0.1/" (table \["Content-Type" "text/plain"\]) "hello")

### http-request-string

* 作用：通用HTTP请求，使用headers、body，并采用特定编码格式或超时
* 参数数量：5
* 参数作用：HTTP方法、url、headers、body、编码格式|超时（秒）
* 参数类型：string、string、table、string|list|nil、string|int
* 返回类型：string
* 示例：(http-request-string "PATCH" "http://127.0.0.1/" (table) "hello" "UTF-8")

### http-request-string

* 作用：通用HTTP请求，使用headers、body、编码格式和超时
* 参数数量：6
* 参数作用：HTTP方法、url、headers、body、编码格式、超时（秒）
* 参数类型：string、string、table、string|list|nil、string、int
* 返回类型：string
* 示例：(http-request-string "PATCH" "http://127.0.0.1/" (table) "hello" "UTF-8" 5)

### http-request-binary

* 作用：通用HTTP请求，并返回binary
* 参数数量：2
* 参数作用：HTTP方法、url
* 参数类型：string、string
* 返回类型：list
* 示例：(http-request-binary "GET" "http://127.0.0.1/")

### http-request-binary

* 作用：通用HTTP请求，使用headers
* 参数数量：3
* 参数作用：HTTP方法、url、headers
* 参数类型：string、string、table
* 返回类型：list
* 示例：(http-request-binary "GET" "http://127.0.0.1/" (table \["a" "3"\]))

### http-request-binary

* 作用：通用HTTP请求，使用headers和body
* 参数数量：4
* 参数作用：HTTP方法、url、headers、body
* 参数类型：string、string、table、string|list|nil
* 返回类型：list
* 示例：(http-request-binary "PUT" "http://127.0.0.1/" (table \["Content-Type" "text/plain"\]) "hello")

### http-request-binary

* 作用：通用HTTP请求，使用headers、body和超时
* 参数数量：5
* 参数作用：HTTP方法、url、headers、body、超时（秒）
* 参数类型：string、string、table、string|list|nil、int
* 返回类型：list
* 示例：(http-request-binary "PATCH" "http://127.0.0.1/" (table) "hello" 5)

### http-get-string / http-delete-string

* 作用：GET或DELETE访问http/https，并返回string
* 参数数量：1
* 参数作用：url
* 参数类型：string
* 返回类型：string
* 示例：(http-get-string "http://127.0.0.1/")

### http-get-string / http-delete-string

* 作用：GET或DELETE访问http/https，并采用特定编码格式或使用headers/超时
* 参数数量：2
* 参数作用：url、编码格式|headers|超时（秒）
* 参数类型：string、string|table|int
* 返回类型：string
* 示例：(http-delete-string "http://127.0.0.1/" "UTF-8")

### http-get-string / http-delete-string

* 作用：GET或DELETE访问http/https，使用headers，并采用特定编码格式或超时
* 参数数量：3
* 参数作用：url、headers、编码格式|超时（秒）
* 参数类型：string、table、string|int
* 返回类型：string
* 示例：(http-get-string "http://127.0.0.1/" (table \["a" "3"\]) "UTF-8")

### http-get-string / http-delete-string

* 作用：GET或DELETE访问http/https，使用headers、编码格式和超时
* 参数数量：4
* 参数作用：url、headers、编码格式、超时（秒）
* 参数类型：string、table、string、int
* 返回类型：string
* 示例：(http-delete-string "http://127.0.0.1/" (table \["a" "3"\]) "UTF-8" 5)

### http-post-string / http-put-string / http-patch-string

* 作用：POST、PUT或PATCH访问http/https，并返回string
* 参数数量：2
* 参数作用：url、body
* 参数类型：string、string|list
* 返回类型：string
* 示例：(http-post-string "http://127.0.0.1/" "hello")

### http-post-string / http-put-string / http-patch-string

* 作用：POST、PUT或PATCH访问http/https，使用编码格式、headers或超时
* 参数数量：3
* 参数作用：url、body、编码格式|超时（秒） 或 url、headers、body
* 参数类型：string、string|list、string|int 或 string、table、string|list
* 返回类型：string
* 示例：(http-put-string "http://127.0.0.1/" "hello" "UTF-8")

### http-post-string / http-put-string / http-patch-string

* 作用：POST、PUT或PATCH访问http/https，使用headers和编码格式或超时
* 参数数量：4
* 参数作用：url、headers、body、编码格式|超时（秒）
* 参数类型：string、table、string|list、string|int
* 返回类型：string
* 示例：(http-patch-string "http://127.0.0.1/" (table \["Content-Type" "text/plain"\]) "hello" "UTF-8")

### http-post-string / http-put-string / http-patch-string

* 作用：POST、PUT或PATCH访问http/https，使用headers、编码格式和超时
* 参数数量：5
* 参数作用：url、headers、body、编码格式、超时（秒）
* 参数类型：string、table、string|list、string、int
* 返回类型：string
* 示例：(http-post-string "http://127.0.0.1/" (table \["Content-Type" "text/plain"\]) "hello" "UTF-8" 5)

### http-get-binary / http-delete-binary

* 作用：GET或DELETE访问http/https，并返回binary
* 参数数量：1
* 参数作用：url
* 参数类型：string
* 返回类型：list
* 示例：(http-get-binary "http://127.0.0.1/")

### http-get-binary / http-delete-binary

* 作用：GET或DELETE访问http/https，使用headers或超时
* 参数数量：2
* 参数作用：url、headers|超时（秒）
* 参数类型：string、table|int
* 返回类型：list
* 示例：(http-delete-binary "http://127.0.0.1/" (table \["a" "3"\]))

### http-get-binary / http-delete-binary

* 作用：GET或DELETE访问http/https，使用headers和超时
* 参数数量：3
* 参数作用：url、headers、超时（秒）
* 参数类型：string、table、int
* 返回类型：list
* 示例：(http-get-binary "http://127.0.0.1/" (table \["a" "3"\]) 5)

### http-post-binary / http-put-binary / http-patch-binary

* 作用：POST、PUT或PATCH访问http/https，并返回binary
* 参数数量：2
* 参数作用：url、body
* 参数类型：string、string|list
* 返回类型：list
* 示例：(http-post-binary "http://127.0.0.1/" "hello")

### http-post-binary / http-put-binary / http-patch-binary

* 作用：POST、PUT或PATCH访问http/https，使用headers或超时
* 参数数量：3
* 参数作用：url、headers、body 或 url、body、超时（秒）
* 参数类型：string、table、string|list 或 string、string|list、int
* 返回类型：list
* 示例：(http-put-binary "http://127.0.0.1/" (table \["Content-Type" "text/plain"\]) "hello")

### http-post-binary / http-put-binary / http-patch-binary

* 作用：POST、PUT或PATCH访问http/https，使用headers和超时
* 参数数量：4
* 参数作用：url、headers、body、超时（秒）
* 参数类型：string、table、string|list、int
* 返回类型：list
* 示例：(http-patch-binary "http://127.0.0.1/" (table \["Content-Type" "text/plain"\]) "hello" 5)

## 表单和上传

### http-post-form

* 作用：以application/x-www-form-urlencoded提交表单，并返回详细响应
* 参数数量：2
* 参数作用：url、表单
* 参数类型：string、table
* 返回类型：table
* 示例：(http-post-form "http://127.0.0.1/login" (table \["user" "root"\]))

### http-post-form

* 作用：以application/x-www-form-urlencoded提交表单，使用headers
* 参数数量：3
* 参数作用：url、headers、表单 或 url、表单、超时（秒）
* 参数类型：string、table、table 或 string、table、int
* 返回类型：table
* 示例：(http-post-form "http://127.0.0.1/login" (table \["a" "3"\]) (table \["user" "root"\]))

### http-post-form

* 作用：以application/x-www-form-urlencoded提交表单，使用headers和超时
* 参数数量：4
* 参数作用：url、headers、表单、超时（秒）
* 参数类型：string、table、table、int
* 返回类型：table
* 示例：(http-post-form "http://127.0.0.1/login" (table \["a" "3"\]) (table \["user" "root"\]) 5)

### http-post-multipart

* 作用：以multipart/form-data提交字段和文件，并返回详细响应
* 参数数量：3
* 参数作用：url、字段表、文件表
* 参数类型：string、table、table
* 返回类型：table
* 示例：(http-post-multipart "http://127.0.0.1/upload" (table \["name" "a"\]) (table \["file" "./a.txt"\]))

### http-post-multipart

* 作用：以multipart/form-data提交字段和文件，使用headers或超时
* 参数数量：4
* 参数作用：url、headers、字段表、文件表 或 url、字段表、文件表、超时（秒）
* 参数类型：string、table、table、table 或 string、table、table、int
* 返回类型：table
* 示例：(http-post-multipart "http://127.0.0.1/upload" (table) (table \["name" "a"\]) (table \["file" "./a.txt"\]))

### http-post-multipart

* 作用：以multipart/form-data提交字段和文件，使用headers和超时
* 参数数量：5
* 参数作用：url、headers、字段表、文件表、超时（秒）
* 参数类型：string、table、table、table、int
* 返回类型：table
* 示例：(http-post-multipart "http://127.0.0.1/upload" (table) (table \["name" "a"\]) (table \["file" "./a.txt"\]) 5)

### http-post-form-string

* 作用：以application/x-www-form-urlencoded提交表单，并返回string
* 参数数量：2
* 参数作用：url、表单
* 参数类型：string、table
* 返回类型：string
* 示例：(http-post-form-string "http://127.0.0.1/login" (table \["user" "root"\]))

### http-post-form-string

* 作用：以application/x-www-form-urlencoded提交表单，使用编码格式、headers或超时
* 参数数量：3
* 参数作用：url、表单、编码格式|超时（秒） 或 url、headers、表单
* 参数类型：string、table、string|int 或 string、table、table
* 返回类型：string
* 示例：(http-post-form-string "http://127.0.0.1/login" (table \["user" "root"\]) "UTF-8")

### http-post-form-string

* 作用：以application/x-www-form-urlencoded提交表单，使用headers和编码格式或超时，或使用编码格式和超时
* 参数数量：4
* 参数作用：url、headers、表单、编码格式|超时（秒） 或 url、表单、编码格式、超时（秒）
* 参数类型：string、table、table、string|int 或 string、table、string、int
* 返回类型：string
* 示例：(http-post-form-string "http://127.0.0.1/login" (table \["a" "3"\]) (table \["user" "root"\]) "UTF-8")

### http-post-form-string

* 作用：以application/x-www-form-urlencoded提交表单，使用headers、编码格式和超时
* 参数数量：5
* 参数作用：url、headers、表单、编码格式、超时（秒）
* 参数类型：string、table、table、string、int
* 返回类型：string
* 示例：(http-post-form-string "http://127.0.0.1/login" (table \["a" "3"\]) (table \["user" "root"\]) "UTF-8" 5)

### http-post-form-binary

* 作用：以application/x-www-form-urlencoded提交表单，并返回binary
* 参数数量：2
* 参数作用：url、表单
* 参数类型：string、table
* 返回类型：list
* 示例：(http-post-form-binary "http://127.0.0.1/login" (table \["user" "root"\]))

### http-post-form-binary

* 作用：以application/x-www-form-urlencoded提交表单，使用headers或超时
* 参数数量：3
* 参数作用：url、headers、表单 或 url、表单、超时（秒）
* 参数类型：string、table、table 或 string、table、int
* 返回类型：list
* 示例：(http-post-form-binary "http://127.0.0.1/login" (table \["a" "3"\]) (table \["user" "root"\]))

### http-post-form-binary

* 作用：以application/x-www-form-urlencoded提交表单，使用headers和超时
* 参数数量：4
* 参数作用：url、headers、表单、超时（秒）
* 参数类型：string、table、table、int
* 返回类型：list
* 示例：(http-post-form-binary "http://127.0.0.1/login" (table \["a" "3"\]) (table \["user" "root"\]) 5)

### http-post-multipart-string

* 作用：以multipart/form-data提交字段和文件，并返回string
* 参数数量：3
* 参数作用：url、字段表、文件表
* 参数类型：string、table、table
* 返回类型：string
* 示例：(http-post-multipart-string "http://127.0.0.1/upload" (table \["name" "a"\]) (table \["file" "./a.txt"\]))

### http-post-multipart-string

* 作用：以multipart/form-data提交字段和文件，使用编码格式、headers或超时
* 参数数量：4
* 参数作用：url、字段表、文件表、编码格式|超时（秒） 或 url、headers、字段表、文件表
* 参数类型：string、table、table、string|int 或 string、table、table、table
* 返回类型：string
* 示例：(http-post-multipart-string "http://127.0.0.1/upload" (table \["name" "a"\]) (table \["file" "./a.txt"\]) "UTF-8")

### http-post-multipart-string

* 作用：以multipart/form-data提交字段和文件，使用headers和编码格式或超时，或使用编码格式和超时
* 参数数量：5
* 参数作用：url、headers、字段表、文件表、编码格式|超时（秒） 或 url、字段表、文件表、编码格式、超时（秒）
* 参数类型：string、table、table、table、string|int 或 string、table、table、string、int
* 返回类型：string
* 示例：(http-post-multipart-string "http://127.0.0.1/upload" (table) (table \["name" "a"\]) (table \["file" "./a.txt"\]) "UTF-8")

### http-post-multipart-string

* 作用：以multipart/form-data提交字段和文件，使用headers、编码格式和超时
* 参数数量：6
* 参数作用：url、headers、字段表、文件表、编码格式、超时（秒）
* 参数类型：string、table、table、table、string、int
* 返回类型：string
* 示例：(http-post-multipart-string "http://127.0.0.1/upload" (table) (table \["name" "a"\]) (table \["file" "./a.txt"\]) "UTF-8" 5)

### http-post-multipart-binary

* 作用：以multipart/form-data提交字段和文件，并返回binary
* 参数数量：3
* 参数作用：url、字段表、文件表
* 参数类型：string、table、table
* 返回类型：list
* 示例：(http-post-multipart-binary "http://127.0.0.1/upload" (table \["name" "a"\]) (table \["file" "./a.txt"\]))

### http-post-multipart-binary

* 作用：以multipart/form-data提交字段和文件，使用headers或超时
* 参数数量：4
* 参数作用：url、headers、字段表、文件表 或 url、字段表、文件表、超时（秒）
* 参数类型：string、table、table、table 或 string、table、table、int
* 返回类型：list
* 示例：(http-post-multipart-binary "http://127.0.0.1/upload" (table) (table \["name" "a"\]) (table \["file" "./a.txt"\]))

### http-post-multipart-binary

* 作用：以multipart/form-data提交字段和文件，使用headers和超时
* 参数数量：5
* 参数作用：url、headers、字段表、文件表、超时（秒）
* 参数类型：string、table、table、table、int
* 返回类型：list
* 示例：(http-post-multipart-binary "http://127.0.0.1/upload" (table) (table \["name" "a"\]) (table \["file" "./a.txt"\]) 5)

文件表的value支持两种格式：

* string：作为本地文件路径读取，filename使用文件名，content-type自动探测
* table：{"filename"=\<filename\>, "content-type"=\<content-type\>, "body"=\<body\>}，body支持string或binary

## 响应辅助

### http-header

* 作用：按大小写不敏感方式获取header值
* 参数数量：2
* 参数作用：headers、header名
* 参数类型：table、string
* 返回值：header值；不存在时返回nil
* 返回类型：string|nil
* 示例：(http-header headers "content-type")

### http-redirect

* 作用：创建302重定向响应表
* 参数数量：1
* 参数作用：目标地址
* 参数类型：string
* 返回类型：table
* 示例：(http-redirect "/login")

### http-redirect

* 作用：创建指定状态码的重定向响应表
* 参数数量：2
* 参数作用：目标地址、状态码
* 参数类型：string、int
* 返回类型：table
* 示例：(http-redirect "/login" 301)

### http-cookie

* 作用：从request表中读取指定Cookie
* 参数数量：2
* 参数作用：request、Cookie名
* 参数类型：table、string
* 返回值：Cookie值；不存在时返回nil
* 返回类型：string|nil
* 示例：(http-cookie request "token")

### http-set-cookie

* 作用：向响应表添加Set-Cookie
* 参数数量：3
* 参数作用：响应、Cookie名、Cookie值
* 参数类型：table|string|list、string、string
* 返回类型：table
* 示例：(http-set-cookie "ok" "token" "abc")

### http-set-cookie

* 作用：向响应表添加Set-Cookie，并使用选项
* 参数数量：4
* 参数作用：响应、Cookie名、Cookie值、选项
* 参数类型：table|string|list、string、string、table
* 返回类型：table
* 说明：选项表会追加为Cookie属性，如Path、Max-Age、HttpOnly、Secure
* 示例：(http-set-cookie "ok" "token" "abc" (table \["Path" "/"\] \["HttpOnly" true\]))
