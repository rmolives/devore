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

### http-handler

* 作用：注册HTTP路由处理过程
* 参数数量：3
* 参数作用：HTTP服务端、路由路径、处理过程
* 参数类型：http-server、string、procedure
* 返回值：nil
* 返回类型：word
* 示例：(http-handler server "/" (lambda [request] "hello"))

处理过程接收request表：

* 参数值：{"method"=\<method\>, "path"=\<path\>, "query"=\<query\>, "headers"=\<headers\>, "body"=\<body\>, "remote-host"=\<remote-host\>, "remote-port"=\<remote-port\>}
* 参数类型：table

处理过程可以直接返回string或binary，也可以返回response表：

* 返回值：{"status"=\<status\>, "headers"=\<headers\>, "body"=\<body\>}
* 返回类型：table
* status：状态码，默认200
* headers：响应头表
* body：响应体，支持string或binary

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

## http-get

* 作用：访问http/https，并返回详细信息
* 参数数量：1
* 参数作用：url
* 参数类型：string
* 返回值：{"status"=\<status\>, "headers"=\<headers\>, "body"=\<body\>}
* 返回类型：table
* 示例：(http-get "http://127.0.0.1/")

## http-get

* 作用：访问http/https，并返回详细信息，使用headers
* 参数数量：1
* 参数作用：url、headers
* 参数类型：string、table
* 返回值：{"status"=\<status\>, "headers"=\<headers\>, "body"=\<body\>}
* 返回类型：table
* 示例：(http-get "http://127.0.0.1/" (table ["a" 3]))

## http-get-string

* 作用：访问http/https，并返回string
* 参数数量：1
* 参数作用：url
* 参数类型：string
* 返回值：string
* 返回类型：string
* 示例：(http-get-string "http://127.0.0.1/")

## http-get-string

* 作用：访问http/https，并返回string，并采用特定编码格式|使用headers
* 参数数量：2
* 参数作用：url、[编码格式|headers]
* 参数类型：string、[string|table]
* 返回值：string
* 返回类型：string
* 示例：(http-get-string "http://127.0.0.1/" "UTF-8")

## http-get-string

* 作用：访问http/https，并返回string，使用headers，并采用特定编码格式
* 参数数量：2
* 参数作用：url、headers、编码格式
* 参数类型：string、table、string
* 返回值：string
* 返回类型：string
* 示例：(http-get-string "http://127.0.0.1/" (table ["a" 3]) "UTF-8")

## http-get-binary

* 作用：访问http/https，并返回binary
* 参数数量：1
* 参数作用：url
* 参数类型：string
* 返回值：binary
* 返回类型：list
* 示例：(http-get-binary "http://127.0.0.1/")

## http-get-binary

* 作用：访问http/https，并返回binary，使用headers
* 参数数量：1
* 参数作用：url、headers
* 参数类型：string、table
* 返回值：binary
* 返回类型：list
* 示例：(http-get-binary "http://127.0.0.1/" (table ["a" 3]))
