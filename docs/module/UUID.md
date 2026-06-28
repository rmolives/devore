# UUID

需要导入模块：uuid

## uuid

* 作用：生成随机UUID
* 参数数量：0
* 参数作用：
* 参数类型：
* 返回值：标准UUID字符串
* 返回类型：string
* 示例：(uuid)

## uuid-simple

* 作用：生成无连字符的随机UUID
* 参数数量：0
* 参数作用：
* 参数类型：
* 返回值：无连字符UUID字符串
* 返回类型：string
* 示例：(uuid-simple)

## uuid-parse

* 作用：解析标准或无连字符UUID并返回标准UUID字符串
* 参数数量：1
* 参数作用：标准或无连字符UUID字符串
* 参数类型：string
* 返回值：标准UUID字符串
* 返回类型：string
* 示例：(uuid-parse "550e8400-e29b-41d4-a716-446655440000")

## uuid-simple

* 作用：解析标准或无连字符UUID并返回无连字符UUID字符串
* 参数数量：1
* 参数作用：标准或无连字符UUID字符串
* 参数类型：string
* 返回值：无连字符UUID字符串
* 返回类型：string
* 示例：(uuid-simple "550e8400-e29b-41d4-a716-446655440000")
