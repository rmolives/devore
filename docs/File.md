# 文件

需要导入模块：file

## file-absolute-path

* 作用：获取绝对路径
* 参数数量：1
* 参数作用：path
* 参数类型：string
* 返回值：path
* 返回类型：string
* 示例：(file-absolute-path "./xxx")

## file-read-binary

* 作用：访问文件，返回binary
* 参数数量：1
* 参数作用：path
* 参数类型：string
* 返回值：binary
* 返回类型：list
* 示例：(file-read-binary "./xxx")

## file-read-string

* 作用：访问文件，返回string
* 参数数量：1
* 参数作用：path
* 参数类型：string
* 返回值：string
* 返回类型：string
* 示例：(file-read-string "./xxx")

## file-read-string

* 作用：访问文件，返回string
* 参数数量：2
* 参数作用：path、编码格式
* 参数类型：string、string
* 返回值：string
* 返回类型：string
* 示例：(file-read-string "./xxx" "UTF-8")

## file-write-string

* 作用：写文件
* 参数数量：2
* 参数作用：path、内容
* 参数类型：string、string
* 返回值：nil
* 返回类型：word
* 示例：(file-write-string "./xxx" "abc")

## file-write-string

* 作用：写文件
* 参数数量：3
* 参数作用：path、内容、编码格式
* 参数类型：string、string、string
* 返回值：nil
* 返回类型：word
* 示例：(file-write-string "./xxx" "abc" "UTF-8")

## file-append-string

* 作用：文件追加内容
* 参数数量：2
* 参数作用：path、内容
* 参数类型：string、string
* 返回值：nil
* 返回类型：word
* 示例：(file-append-string "./xxx" "abc")

## file-append-string

* 作用：文件追加内容
* 参数数量：3
* 参数作用：path、内容、编码格式
* 参数类型：string、string、string
* 返回值：nil
* 返回类型：word
* 示例：(file-append-string "./xxx" "abc" "UTF-8")

## file-append-binary

* 作用：文件追加内容
* 参数数量：2
* 参数作用：path、内容
* 参数类型：string、list
* 返回值：nil
* 返回类型：word
* 示例：(file-append-binary "./xxx" xxx)

## file-write-binary

* 作用：写文件
* 参数数量：2
* 参数作用：path、内容
* 参数类型：string、list
* 返回值：nil
* 返回类型：word
* 示例：(file-write-binary "./xxx" xxx)

## file-exists?

* 作用：判断文件是否存在
* 参数数量：1
* 参数作用：path
* 参数类型：string
* 返回值：bool
* 返回类型：bool
* 示例：(file-exists? "./xxx")

## file-directory?

* 作用：判断文件是否是文件夹
* 参数数量：1
* 参数作用：path
* 参数类型：string
* 返回值：bool
* 返回类型：bool
* 示例：(file-directory? "./xxx")

## file-regular?

* 作用：判断文件是否是文件
* 参数数量：1
* 参数作用：path
* 参数类型：string
* 返回值：bool
* 返回类型：bool
* 示例：(file-regular? "./xxx")

## file-size

* 作用：获取文件大小
* 参数数量：1
* 参数作用：path
* 参数类型：string
* 返回值：int
* 返回类型：int
* 示例：(file-size "./xxx")

## file-list

* 作用：获取文件夹内有哪些文件/文件夹
* 参数数量：1
* 参数作用：path
* 参数类型：string
* 返回值：list
* 返回类型：list
* 示例：(file-list "./xxx")

## file-create-dirs

* 作用：创建文件夹
* 参数数量：1
* 参数作用：path
* 参数类型：string
* 返回值：nil
* 返回类型：word
* 示例：(file-create-dirs "./xxx")

## file-delete!

* 作用：如果文件存在，就删除掉
* 参数数量：1
* 参数作用：path
* 参数类型：string
* 返回值：nil
* 返回类型：word
* 示例：(file-delete! "./xxx")
