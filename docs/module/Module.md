# 模块

**Devore Language**共有以下模块：

* [core](../README.md)：基础运行能力与通用工具
* [http](Http.md)：HTTP客户端与服务端通信能力
* [tcp](TCP.md)：基于TCP协议的网络连接与数据传输
* [udp](UDP.md)：基于UDP协议的数据报发送与接收
* [file](File.md)：文件与目录的读写、查询和管理
* [json](Json.md)：JSON数据的解析、生成与转换
* [csv](Csv.md)：RFC 4180 CSV数据的解析、生成与文件读写
* [xml](Xml.md)：XML数据的解析、生成与节点构造
* [html](Html.md)：HTML实体转义与还原
* [properties](Properties.md)：Properties配置的解析、生成与文件读写
* [regex](Regex.md)：正则表达式匹配、查找、替换与分割
* [reflect](Reflect.md)：Java类加载、对象构造与反射方法调用
* [thread](Thread.md)：线程创建、任务并发与同步控制
* [hash](Hash.md)：常用哈希摘要算法与校验能力
* [binary](Binary.md)：二进制数据的读写、编码与转换
* [base64](Base64.md)：Base64编码与解码
* [sign](Sign.md)：数据签名、验签与签名辅助能力
* [crypto](Crypto.md)：加密、解密及相关密码学工具
* [zip](Zip.md)：ZIP压缩包的创建、读取、解压与条目查询
* [os](OS.md)：操作系统信息、环境变量与系统调用能力
* [uuid](UUID.md)：UUID生成、解析与格式转换

## import

* 作用：导入特定devore文件|模块
* 参数数量：无穷（>= 1）
* 参数作用：\[文件地址|模块\]<\*>
* 参数类型：string<\*>
* 返回值：nil
* 返回类型：word
* 示例：(import "math.devore")

## export

* 作用：模块导出哪些
* 参数数量：无穷（>= 1）
* 参数作用：key<\*>
* 参数类型：symbol<\*>
* 返回值：export
* 返回类型：export
* 示例：(export a b)
