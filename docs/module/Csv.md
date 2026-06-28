# CSV

需要导入模块：csv

CSV解析与生成遵循RFC 4180。记录分隔符为CRLF，字段内的双引号使用两个双引号转义，字段包含逗号、双引号、CR、LF或为空字符串时会被引号包裹。

CSV数据表示为二维list：每一行是一个list，每个字段都是string。

## csv-read-string

* 作用：解析CSV字符串
* 参数数量：1
* 参数作用：CSV内容
* 参数类型：string
* 返回值：二维字段列表
* 返回类型：list
* 示例：(csv-read-string "name,age\r\ndevore,1")

## csv-write-string

* 作用：把二维字段列表写成CSV字符串
* 参数数量：1
* 参数作用：二维字段列表
* 参数类型：list
* 返回值：CSV内容
* 返回类型：string
* 示例：(csv-write-string (list (list "name" "note") (list "devore" "a,b")))

## csv-read-file

* 作用：读取并解析CSV文件，默认使用UTF-8
* 参数数量：1
* 参数作用：path
* 参数类型：string
* 返回值：二维字段列表
* 返回类型：list
* 示例：(csv-read-file "./data.csv")

## csv-read-file

* 作用：读取并解析CSV文件
* 参数数量：2
* 参数作用：path、编码格式
* 参数类型：string、string
* 返回值：二维字段列表
* 返回类型：list
* 示例：(csv-read-file "./data.csv" "UTF-8")

## csv-write-file

* 作用：把二维字段列表写入CSV文件，默认使用UTF-8
* 参数数量：2
* 参数作用：path、二维字段列表
* 参数类型：string、list
* 返回值：nil
* 返回类型：word
* 示例：(csv-write-file "./data.csv" rows)

## csv-write-file

* 作用：把二维字段列表写入CSV文件
* 参数数量：3
* 参数作用：path、二维字段列表、编码格式
* 参数类型：string、list、string
* 返回值：nil
* 返回类型：word
* 示例：(csv-write-file "./data.csv" rows "UTF-8")
