# 模块

**Devore Language**共有以下模块：

* core：核心模块
* http：http模块
* tcp：TCP网络模块
* udp：UDP网络模块
* file：文件模块
* json：json模块
* thread：并发模块
* hash：hash模块
* binary：二进程模块
* base64：Base64模块
* sign：签名模块
* crypto：加解密模块
* os：系统模块

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
