# HTML

需要导入模块：html

## html-escape

* 作用：把HTML特殊字符转义为HTML实体
* 参数数量：1
* 参数作用：原始字符串
* 参数类型：string
* 返回值：转义后的HTML字符串
* 返回类型：string
* 示例：(html-escape "<div title=\"a&b\">Tom's</div>")

## html-unescape

* 作用：把HTML实体还原为普通字符
* 参数数量：1
* 参数作用：HTML字符串
* 参数类型：string
* 返回值：还原后的字符串
* 返回类型：string
* 示例：(html-unescape "&lt;div&gt;Tom&#39;s&lt;/div&gt;")
