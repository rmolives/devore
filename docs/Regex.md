# 正则表达式

需要导入模块：regex

## regex-match

* 作用：判断字符串是否完整匹配正则表达式
* 参数数量：2
* 参数作用：正则表达式、字符串
* 参数类型：string、string
* 返回值：bool
* 返回类型：bool
* 示例：(regex-match "\\d+" "123")

## regex-find

* 作用：查找第一个符合正则表达式的内容
* 参数数量：2
* 参数作用：正则表达式、字符串
* 参数类型：string、string
* 返回值：匹配结果列表，未找到返回nil。结果格式为[匹配文本 起始下标 结束下标 分组列表]
* 返回类型：list|word
* 示例：(regex-find "\\d+" "abc123def")

## regex-find-all

* 作用：查找所有符合正则表达式的内容
* 参数数量：2
* 参数作用：正则表达式、字符串
* 参数类型：string、string
* 返回值：匹配结果列表，每个结果格式为[匹配文本 起始下标 结束下标 分组列表]
* 返回类型：list
* 示例：(regex-find-all "\\d+" "abc123def456")

## regex-replace

* 作用：替换所有符合正则表达式的内容
* 参数数量：3
* 参数作用：正则表达式、字符串、替换内容
* 参数类型：string、string、string
* 返回值：替换后的内容
* 返回类型：string
* 示例：(regex-replace "\\d+" "abc123def" "456")

## regex-split

* 作用：按正则表达式分割字符串
* 参数数量：2
* 参数作用：正则表达式、字符串
* 参数类型：string、string
* 返回值：分割后的列表，保留尾部空字符串
* 返回类型：list
* 示例：(regex-split "," "a,b,c")

## regex-quote

* 作用：转义字符串为字面量正则表达式
* 参数数量：1
* 参数作用：字符串
* 参数类型：string
* 返回值：转义后的正则表达式
* 返回类型：string
* 示例：(regex-quote "a.b")
