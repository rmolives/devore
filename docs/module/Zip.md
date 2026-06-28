# ZIP

需要导入模块：zip

## zip-create

* 作用：创建ZIP压缩包，条目名默认为源路径的文件名或目录名
* 参数数量：2
* 参数作用：ZIP路径、源路径
* 参数类型：string、string
* 返回值：nil
* 返回类型：word
* 示例：(zip-create "./out.zip" "./data")

## zip-create

* 作用：创建ZIP压缩包，并指定根条目名
* 参数数量：3
* 参数作用：ZIP路径、源路径、条目名
* 参数类型：string、string、string
* 返回值：nil
* 返回类型：word
* 示例：(zip-create "./out.zip" "./data" "backup")

## zip-list

* 作用：列出ZIP压缩包内的条目
* 参数数量：1
* 参数作用：ZIP路径
* 参数类型：string
* 返回值：条目信息列表，每个条目为table，包含name、directory?、size、compressed-size、crc、time
* 返回类型：list
* 示例：(zip-list "./out.zip")

## zip-entry-exists?

* 作用：判断ZIP压缩包中是否存在指定条目
* 参数数量：2
* 参数作用：ZIP路径、条目名
* 参数类型：string、string
* 返回值：bool
* 返回类型：bool
* 示例：(zip-entry-exists? "./out.zip" "backup/a.txt")

## zip-read-entry

* 作用：读取ZIP压缩包中的文件条目，返回binary
* 参数数量：2
* 参数作用：ZIP路径、条目名
* 参数类型：string、string
* 返回值：binary
* 返回类型：list
* 示例：(zip-read-entry "./out.zip" "backup/a.txt")

## zip-extract

* 作用：解压整个ZIP压缩包到目标目录
* 参数数量：2
* 参数作用：ZIP路径、目标目录
* 参数类型：string、string
* 返回值：nil
* 返回类型：word
* 示例：(zip-extract "./out.zip" "./target")

## zip-extract-entry

* 作用：解压ZIP压缩包中的单个条目到目标路径
* 参数数量：3
* 参数作用：ZIP路径、条目名、目标路径
* 参数类型：string、string、string
* 返回值：nil
* 返回类型：word
* 示例：(zip-extract-entry "./out.zip" "backup/a.txt" "./target/a.txt")

## gzip-compress

* 作用：将binary压缩为GZIP格式的binary
* 参数数量：1
* 参数作用：binary
* 参数类型：list
* 返回值：GZIP binary
* 返回类型：list
* 示例：(gzip-compress (string->binary "hello"))

## gzip-decompress

* 作用：解压GZIP格式的binary
* 参数数量：1
* 参数作用：GZIP binary
* 参数类型：list
* 返回值：binary
* 返回类型：list
* 示例：(gzip-decompress data)

## gzip-compress-file

* 作用：将单个文件压缩为GZIP文件
* 参数数量：2
* 参数作用：源文件路径、GZIP路径
* 参数类型：string、string
* 返回值：nil
* 返回类型：word
* 示例：(gzip-compress-file "./a.txt" "./a.txt.gz")

## gzip-decompress-file

* 作用：解压GZIP文件到目标路径
* 参数数量：2
* 参数作用：GZIP路径、目标路径
* 参数类型：string、string
* 返回值：nil
* 返回类型：word
* 示例：(gzip-decompress-file "./a.txt.gz" "./a.txt")
