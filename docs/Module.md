# 模块

**Devore Language**共有以下模块：

* core：基础运行能力与通用工具
* http：HTTP客户端与服务端通信能力
* tcp：基于TCP协议的网络连接与数据传输
* udp：基于UDP协议的数据报发送与接收
* file：文件与目录的读写、查询和管理
* json：JSON数据的解析、生成与转换
* thread：线程创建、任务并发与同步控制
* hash：常用哈希摘要算法与校验能力
* binary：二进制数据的读写、编码与转换
* base64：Base64编码与解码
* sign：数据签名、验签与签名辅助能力
* crypto：加密、解密及相关密码学工具
* os：操作系统信息、环境变量与系统调用能力

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
