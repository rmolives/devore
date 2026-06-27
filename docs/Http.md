# HTTP

需要导入模块：http

## http-get

* 作用：访问http/https，并返回详细信息
* 参数数量：1
* 参数作用：url
* 参数类型：string
* 返回值：{"status"=\<status\>, "headers"=\<headers\>, "body"=\<body\>}
* 返回类型：table
* 示例：(http-get "http://127.0.0.1/")

## http-get

* 作用：访问http/https，并返回详细信息，使用headers
* 参数数量：1
* 参数作用：url、headers
* 参数类型：string、table
* 返回值：{"status"=\<status\>, "headers"=\<headers\>, "body"=\<body\>}
* 返回类型：table
* 示例：(http-get "http://127.0.0.1/" (table ["a" 3]))

## http-get-string

* 作用：访问http/https，并返回string
* 参数数量：1
* 参数作用：url
* 参数类型：string
* 返回值：string
* 返回类型：string
* 示例：(http-get-string "http://127.0.0.1/")

## http-get-string

* 作用：访问http/https，并返回string，并采用特定编码格式|使用headers
* 参数数量：2
* 参数作用：url、[编码格式|headers]
* 参数类型：string、[string|table]
* 返回值：string
* 返回类型：string
* 示例：(http-get-string "http://127.0.0.1/" "UTF-8")

## http-get-string

* 作用：访问http/https，并返回string，使用headers，并采用特定编码格式
* 参数数量：2
* 参数作用：url、headers、编码格式
* 参数类型：string、table、string
* 返回值：string
* 返回类型：string
* 示例：(http-get-string "http://127.0.0.1/" (table ["a" 3]) "UTF-8")

## http-get-binary

* 作用：访问http/https，并返回binary
* 参数数量：1
* 参数作用：url
* 参数类型：string
* 返回值：binary
* 返回类型：list
* 示例：(http-get-binary "http://127.0.0.1/")

## http-get-binary

* 作用：访问http/https，并返回binary，使用headers
* 参数数量：1
* 参数作用：url、headers
* 参数类型：string、table
* 返回值：binary
* 返回类型：list
* 示例：(http-get-binary "http://127.0.0.1/" (table ["a" 3]))
