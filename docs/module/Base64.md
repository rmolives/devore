# Base64

需要导入模块：base64

## base64-encode

* 作用：把原始字节编码成Base64字符串
* 参数数量：1
* 参数作用：原始字节
* 参数类型：\[string|list\]
* 返回值：Base64
* 返回类型：string
* 示例：(base64-encode "abc")

## base64-encode

* 作用：把原始字符串按指定编码格式编码成Base64字符串
* 参数数量：2
* 参数作用：原始字符串、编码格式
* 参数类型：string、string
* 返回值：Base64
* 返回类型：string
* 示例：(base64-encode "中文" "UTF-8")

## base64-decode

* 作用：接收Base64格式的string，解码成binary
* 参数数量：1
* 参数作用：Base64
* 参数类型：string
* 返回值：binary
* 返回类型：binary
* 示例：(base64-decode xxx)
