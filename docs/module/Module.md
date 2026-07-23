# 模块

**Devore Language**共有以下模块：

* [core](../README.md)：基础运行能力与通用工具
* [math](Math.md)：复杂数学运算、取整、对数、三角函数与数论能力
* [regex](Regex.md)：正则表达式匹配、查找、替换与分割
* [security](Security.md)：当前环境安全限制设置与查询
* [thread](Thread.md)：线程创建、任务并发与同步控制
* [time](Time.md)：时间戳格式化与时间字符串解析
* [hash](Hash.md)：常用哈希摘要算法与校验能力
* [binary](Binary.md)：二进制数据的读写、编码与转换
* [base64](Base64.md)：Base64编码与解码
* [crypto](Crypto.md)：加密、解密及相关密码学工具
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
