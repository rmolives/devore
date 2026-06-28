# UDP

需要导入模块：udp

UDP模块使用二进制数据收发内容，二进制数据类型为list，可配合binary模块中的string->binary、binary->string使用。

## 类型

### 新增类型

* udp-socket: UDP套接字

### udp-socket?

* 作用：判断是否为udp-socket类型
* 参数数量：1
* 参数作用：内容
* 参数类型：token
* 返回值：是否为udp-socket类型
* 返回类型：bool
* 示例：(udp-socket? socket)

## udp-open

* 作用：打开未绑定固定端口的UDP套接字
* 参数数量：0
* 参数作用：
* 参数类型：
* 返回值：UDP套接字
* 返回类型：udp-socket
* 示例：(udp-open)

## udp-bind

* 作用：绑定UDP端口
* 参数数量：1
* 参数作用：端口
* 参数类型：int
* 返回值：UDP套接字
* 返回类型：udp-socket
* 示例：(udp-bind 9000)

## udp-bind

* 作用：在指定地址绑定UDP端口
* 参数数量：2
* 参数作用：主机、端口
* 参数类型：string、int
* 返回值：UDP套接字
* 返回类型：udp-socket
* 示例：(udp-bind "127.0.0.1" 9000)

## udp-send

* 作用：发送UDP数据包
* 参数数量：4
* 参数作用：UDP套接字、目标主机、目标端口、二进制数据
* 参数类型：udp-socket、string、int、list
* 返回值：nil
* 返回类型：word
* 示例：(udp-send socket "127.0.0.1" 9000 (string->binary "hello"))

## udp-receive

* 作用：接收UDP数据包；如果设置了超时且超时未收到数据，返回nil
* 参数数量：2
* 参数作用：UDP套接字、最大接收长度
* 参数类型：udp-socket、int
* 返回值：{"host"=\<host\>, "port"=\<port\>, "body"=\<body\>}或nil
* 返回类型：table|word
* 示例：(udp-receive socket 1024)

## udp-set-timeout

* 作用：设置UDP接收超时时间，0表示不超时
* 参数数量：2
* 参数作用：UDP套接字、超时时间（毫秒）
* 参数类型：udp-socket、int
* 返回值：nil
* 返回类型：word
* 示例：(udp-set-timeout socket 3000)

## udp-close

* 作用：关闭UDP套接字
* 参数数量：1
* 参数作用：UDP套接字
* 参数类型：udp-socket
* 返回值：nil
* 返回类型：word
* 示例：(udp-close socket)
