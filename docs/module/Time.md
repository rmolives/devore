# Time

时间格式化和解析模块。

## format-time

* 作用：按默认格式yyyy-MM-dd HH:mm:ss格式化时间戳
* 参数数量：1
* 参数作用：时间戳
* 参数类型：int
* 返回值：格式化后的时间
* 返回类型：string
* 示例：(format-time (time))

## format-time

* 作用：按指定格式格式化时间戳
* 参数数量：2
* 参数作用：时间戳、时间格式
* 参数类型：int、string
* 返回值：格式化后的时间
* 返回类型：string
* 示例：(format-time (time) "yyyy-MM-dd HH:mm:ss")

## parse-time

* 作用：按默认格式yyyy-MM-dd HH:mm:ss解析时间
* 参数数量：1
* 参数作用：格式化后的时间
* 参数类型：string
* 返回值：时间戳
* 返回类型：int
* 示例：(parse-time "2026-06-28 12:00:00")

## parse-time

* 作用：按指定格式解析时间
* 参数数量：2
* 参数作用：格式化后的时间、时间格式
* 参数类型：string、string
* 返回值：时间戳
* 返回类型：int
* 示例：(parse-time "2026-06-28 12:00:00" "yyyy-MM-dd HH:mm:ss")
