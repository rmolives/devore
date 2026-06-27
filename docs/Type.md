# 类型

**Devore Language**共有以下类型：

* bool：布尔值（true/false）
* number：
    * int：整数
    * float：浮点数
* list：列表
* macro：宏
* procedure：过程
* string：字符串
* symbol：符号
* table：表
* word：字面量
* export: 模块导出表

## export?

* 作用：判断是否为export类型
* 参数数量：1
* 参数作用：内容
* 参数类型：token
* 返回值：是否为export类型
* 返回类型：bool
* 示例：(export? 3)

## bool?

* 作用：判断是否为bool类型
* 参数数量：1
* 参数作用：内容
* 参数类型：token
* 返回值：是否为bool类型
* 返回类型：bool
* 示例：(bool? 3)

## float?

* 作用：判断是否为float类型
* 参数数量：1
* 参数作用：内容
* 参数类型：token
* 返回值：是否为float类型
* 返回类型：bool
* 示例：(float? 3)

## int?

* 作用：判断是否为int类型
* 参数数量：1
* 参数作用：内容
* 参数类型：token
* 返回值：是否为int类型
* 返回类型：bool
* 示例：(int? 3)

## list?

* 作用：判断是否为list类型
* 参数数量：1
* 参数作用：内容
* 参数类型：token
* 返回值：是否为list类型
* 返回类型：bool
* 示例：(list? 3)

## macro?

* 作用：判断是否为macro类型
* 参数数量：1
* 参数作用：内容
* 参数类型：token
* 返回值：是否为macro类型
* 返回类型：bool
* 示例：(macro? 3)

## number?

* 作用：判断是否为number类型
* 参数数量：1
* 参数作用：内容
* 参数类型：token
* 返回值：是否为number类型
* 返回类型：bool
* 示例：(number? 3)

## procedure?

* 作用：判断是否为procedure类型
* 参数数量：1
* 参数作用：内容
* 参数类型：token
* 返回值：是否为procedure类型
* 返回类型：bool
* 示例：(procedure? 3)

## string?

* 作用：判断是否为string类型
* 参数数量：1
* 参数作用：内容
* 参数类型：token
* 返回值：是否为string类型
* 返回类型：bool
* 示例：(string? 3)

## symbol?

* 作用：判断是否为symbol类型
* 参数数量：1
* 参数作用：内容
* 参数类型：token
* 返回值：是否为symbol类型
* 返回类型：bool
* 示例：(symbol? 3)

## table?

* 作用：判断是否为table类型
* 参数数量：1
* 参数作用：内容
* 参数类型：token
* 返回值：是否为table类型
* 返回类型：bool
* 示例：(table? 3)

## word?

* 作用：判断是否为table类型
* 参数数量：1
* 参数作用：内容
* 参数类型：token
* 返回值：是否为table类型
* 返回类型：bool
* 示例：(word? 3)

## type

* 作用：获取类型
* 参数数量：1
* 参数作用：内容
* 参数类型：token
* 返回值：类型
* 返回类型：string
* 示例：(type 3)
