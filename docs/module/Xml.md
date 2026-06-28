# XML

需要导入模块：xml

XML模块支持XML字符串和文件的解析、生成，以及XML节点的构造与校验。解析时会禁用DOCTYPE和外部实体。

XML值使用节点表表示：

* document：`(table ("type" "document") ("children" children))`
* element：`(table ("type" "element") ("name" name) ("attrs" attrs) ("children" children))`
* text：`(table ("type" "text") ("text" text))`
* cdata：`(table ("type" "cdata") ("text" text))`
* comment：`(table ("type" "comment") ("text" text))`
* pi：`(table ("type" "pi") ("target" target) ("data" data))`

其中`children`是节点list，`attrs`是属性表，属性key和value均为string。`xml-read-string`和`xml-read-file`返回document节点；`xml-write-string`和`xml-write-file`接受document或element等XML节点。

## xml-read-string

* 作用：解析XML字符串
* 参数数量：1
* 参数作用：XML内容
* 参数类型：string
* 返回值：document节点
* 返回类型：table
* 示例：(xml-read-string "<root name=\"devore\">text</root>")

## xml-write-string

* 作用：把XML节点写成XML字符串
* 参数数量：1
* 参数作用：XML节点
* 参数类型：table
* 返回值：XML内容
* 返回类型：string
* 示例：(xml-write-string (xml-element "root" (table ("name" "devore")) (list (xml-text "text"))))

## xml-read-file

* 作用：读取并解析XML文件，默认使用UTF-8
* 参数数量：1
* 参数作用：path
* 参数类型：string
* 返回值：document节点
* 返回类型：table
* 示例：(xml-read-file "./data.xml")

## xml-read-file

* 作用：读取并解析XML文件
* 参数数量：2
* 参数作用：path、编码格式
* 参数类型：string、string
* 返回值：document节点
* 返回类型：table
* 示例：(xml-read-file "./data.xml" "UTF-8")

## xml-write-file

* 作用：把XML节点写入XML文件，默认使用UTF-8
* 参数数量：2
* 参数作用：path、XML节点
* 参数类型：string、table
* 返回值：nil
* 返回类型：word
* 示例：(xml-write-file "./data.xml" node)

## xml-write-file

* 作用：把XML节点写入XML文件
* 参数数量：3
* 参数作用：path、XML节点、编码格式
* 参数类型：string、table、string
* 返回值：nil
* 返回类型：word
* 示例：(xml-write-file "./data.xml" node "UTF-8")

## xml-document

* 作用：构造document节点
* 参数数量：1
* 参数作用：子节点列表
* 参数类型：list
* 返回值：document节点
* 返回类型：table
* 示例：(xml-document (list (xml-element "root" (table) (list))))

## xml-element

* 作用：构造element节点
* 参数数量：3
* 参数作用：元素名、属性表、子节点列表
* 参数类型：string、table、list
* 返回值：element节点
* 返回类型：table
* 示例：(xml-element "root" (table ("id" "1")) (list (xml-text "hello")))

## xml-text

* 作用：构造text节点
* 参数数量：1
* 参数作用：文本内容
* 参数类型：string
* 返回值：text节点
* 返回类型：table
* 示例：(xml-text "hello")

## xml-cdata

* 作用：构造cdata节点
* 参数数量：1
* 参数作用：CDATA内容
* 参数类型：string
* 返回值：cdata节点
* 返回类型：table
* 示例：(xml-cdata "<raw>")

## xml-comment

* 作用：构造comment节点
* 参数数量：1
* 参数作用：注释内容
* 参数类型：string
* 返回值：comment节点
* 返回类型：table
* 示例：(xml-comment "note")

## xml-pi

* 作用：构造处理指令节点
* 参数数量：2
* 参数作用：目标、内容
* 参数类型：string、string
* 返回值：pi节点
* 返回类型：table
* 示例：(xml-pi "xml-stylesheet" "type=\"text/xsl\" href=\"style.xsl\"")

## xml?

* 作用：判断值是否是可写出的XML节点
* 参数数量：1
* 参数作用：值
* 参数类型：任意
* 返回值：bool
* 返回类型：bool
* 示例：(xml? (xml-element "root" (table) (list)))
