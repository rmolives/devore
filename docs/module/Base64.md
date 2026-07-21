# Base64

需要导入模块：base64

## base64-encode

* 作用：把原始字节编码成Base64字符串
* 参数数量：1/2
* 参数作用：原始字节/原始字符串与字符集
* 参数类型：\[string|list\]/\[string,string\]
* 返回值：Base64
* 返回类型：string
* 示例：(base64-encode "abc")
* 示例：(base64-encode "中文" "UTF-8")

## base64-decode

* 作用：接收Base64格式的string，解码成binary
* 参数数量：1
* 参数作用：Base64
* 参数类型：string
* 返回值：binary
* 返回类型：binary
* 示例：(base64-decode xxx)
