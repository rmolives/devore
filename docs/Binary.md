需要导入模块：binary

# random-binary
* 作用：随机字节
* 参数数量：1
* 参数作用：位数
* 参数类型：int
* 返回值：binary
* 返回类型：list
* 示例：(random-binary 25)

# string->binary
* 作用：string转binary
* 参数数量：1
* 参数作用：string
* 参数类型：string
* 返回值：binary
* 返回类型：list
* 示例：(string->binary xxx)

# string->binary
* 作用：string转binary，并采用特定编码格式
* 参数数量：2
* 参数作用：string、编码格式
* 参数类型：string、string
* 返回值：binary
* 返回类型：list
* 示例：(string->binary xxx "UTF-8")

# binary->string
* 作用：binary转string
* 参数数量：1
* 参数作用：binary
* 参数类型：list
* 返回值：string
* 返回类型：string
* 示例：(binary->string xxx)

# binary->string
* 作用：binary转string，并采用特定编码格式
* 参数数量：2
* 参数作用：binary、编码格式
* 参数类型：list、string
* 返回值：string
* 返回类型：string
* 示例：(binary->string xxx "UTF-8")

# binary->hex
* 作用：binary转十六进制
* 参数数量：1
* 参数作用：binary
* 参数类型：list
* 返回值：hex
* 返回类型：string
* 示例：(binary->hex xxx)

# hex->binary
* 作用：十六进制转binary
* 参数数量：1
* 参数作用：hex
* 参数类型：string
* 返回值：binary
* 返回类型：list
* 示例：(hex->binary xxx)
