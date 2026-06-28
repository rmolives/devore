# JSON

需要导入模块：json

## json-read

* 作用：解析JSON字符串
* 参数数量：1
* 参数作用：JSON内容
* 参数类型：string
* 返回值：解析后的值
* 返回类型：任意支持的JSON类型
* 示例：(json-read "{\"name\":\"devore\"}")

## json-write

* 作用：把值序列化为JSON字符串
* 参数数量：1
* 参数作用：值
* 参数类型：任意支持的JSON类型
* 返回值：JSON内容
* 返回类型：string
* 示例：(json-write (table ("name" "devore")))

## json?

* 作用：判断值是否可表示为JSON
* 参数数量：1
* 参数作用：值
* 参数类型：任意
* 返回值：bool
* 返回类型：bool
* 示例：(json? (list 1 true nil))
