# TCP

需要导入模块：tcp

TCP模块使用二进制数据收发内容，二进制数据类型为list，可配合binary模块中的string->binary、binary->string使用。

## 类型

### 新增类型

* tcp-server: TCP服务端
* tcp-socket: TCP连接

### tcp-socket?

* 作用：判断是否为tcp-socket类型
* 参数数量：1
* 参数作用：内容
* 参数类型：token
* 返回值：是否为tcp-socket类型
* 返回类型：bool
* 示例：(tcp-socket? socket)

### tcp-server?

* 作用：判断是否为tcp-server类型
* 参数数量：1
* 参数作用：内容
* 参数类型：token
* 返回值：是否为tcp-server类型
* 返回类型：bool
* 示例：(tcp-server? server)

## tcp-connect

* 作用：连接TCP服务端
* 参数数量：2
* 参数作用：主机、端口
* 参数类型：string、int
* 返回值：TCP连接
* 返回类型：tcp-socket
* 示例：(tcp-connect "127.0.0.1" 8080)

## tcp-connect

* 作用：连接TCP服务端，并设置连接超时
* 参数数量：3
* 参数作用：主机、端口、超时时间（毫秒）
* 参数类型：string、int、int
* 返回值：TCP连接
* 返回类型：tcp-socket
* 示例：(tcp-connect "127.0.0.1" 8080 3000)

## tcp-listen

* 作用：监听TCP端口
* 参数数量：1
* 参数作用：端口
* 参数类型：int
* 返回值：TCP服务端
* 返回类型：tcp-server
* 示例：(tcp-listen 8080)

## tcp-listen

* 作用：在指定地址监听TCP端口
* 参数数量：2
* 参数作用：主机、端口
* 参数类型：string、int
* 返回值：TCP服务端
* 返回类型：tcp-server
* 示例：(tcp-listen "127.0.0.1" 8080)

## tcp-accept

* 作用：接受TCP连接；如果设置了服务端超时且超时未收到连接，返回nil
* 参数数量：1
* 参数作用：TCP服务端
* 参数类型：tcp-server
* 返回值：TCP连接或nil
* 返回类型：tcp-socket|nil
* 示例：(tcp-accept server)

## tcp-read

* 作用：从TCP连接读取最多指定长度的二进制数据；连接关闭或读取超时时返回nil
* 参数数量：2
* 参数作用：TCP连接、最大读取长度
* 参数类型：tcp-socket、int
* 返回值：binary或nil
* 返回类型：list|nil
* 示例：(tcp-read socket 1024)

## tcp-write

* 作用：向TCP连接写入二进制数据
* 参数数量：2
* 参数作用：TCP连接、二进制数据
* 参数类型：tcp-socket、list
* 返回值：nil
* 返回类型：nil
* 示例：(tcp-write socket (string->binary "hello"))

## tcp-set-timeout

* 作用：设置TCP连接读取超时时间，0表示不超时
* 参数数量：2
* 参数作用：TCP连接、超时时间（毫秒）
* 参数类型：tcp-socket、int
* 返回值：nil
* 返回类型：nil
* 示例：(tcp-set-timeout socket 3000)

## tcp-server-set-timeout

* 作用：设置TCP服务端接受连接的超时时间，0表示不超时
* 参数数量：2
* 参数作用：TCP服务端、超时时间（毫秒）
* 参数类型：tcp-server、int
* 返回值：nil
* 返回类型：nil
* 示例：(tcp-server-set-timeout server 3000)

## tcp-close

* 作用：关闭TCP连接或TCP服务端
* 参数数量：1
* 参数作用：TCP连接或TCP服务端
* 参数类型：tcp-socket|tcp-server
* 返回值：nil
* 返回类型：nil
* 示例：(tcp-close socket)
