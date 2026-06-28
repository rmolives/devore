# Properties

需要导入模块：properties

## properties-read-string

* 作用：解析properties字符串
* 参数数量：1
* 参数作用：properties内容
* 参数类型：string
* 返回值：属性表，key和value均为string
* 返回类型：table
* 示例：(properties-read-string "name=devore\nversion=1")

## properties-write-string

* 作用：把属性表写成properties字符串
* 参数数量：1
* 参数作用：属性表
* 参数类型：table
* 返回值：properties内容
* 返回类型：string
* 示例：(properties-write-string (table ("name" "devore") ("version" "1")))

## properties-get

* 作用：获取指定属性值
* 参数数量：2
* 参数作用：属性表、属性名
* 参数类型：table、string
* 返回值：属性值，不存在时返回nil
* 返回类型：string|word
* 示例：(properties-get props "name")

## properties-read-file

* 作用：读取并解析properties文件，默认使用UTF-8
* 参数数量：1
* 参数作用：path
* 参数类型：string
* 返回值：属性表，key和value均为string
* 返回类型：table
* 示例：(properties-read-file "./app.properties")

## properties-read-file

* 作用：读取并解析properties文件
* 参数数量：2
* 参数作用：path、编码格式
* 参数类型：string、string
* 返回值：属性表，key和value均为string
* 返回类型：table
* 示例：(properties-read-file "./app.properties" "UTF-8")

## properties-write-file

* 作用：把属性表写入properties文件，默认使用UTF-8
* 参数数量：2
* 参数作用：path、属性表
* 参数类型：string、table
* 返回值：nil
* 返回类型：word
* 示例：(properties-write-file "./app.properties" props)

## properties-write-file

* 作用：把属性表写入properties文件
* 参数数量：3
* 参数作用：path、属性表、编码格式
* 参数类型：string、table、string
* 返回值：nil
* 返回类型：word
* 示例：(properties-write-file "./app.properties" props "UTF-8")
