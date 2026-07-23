# 安全限制

需要导入模块：security

安全限制用于禁止当前环境及其子环境执行指定类型的敏感操作。可选限制值为：`"file"`、`"exec"`、`"security"`、`"thread"`。
其中 `"security"` 用于禁止设置或清空安全限制，但不影响查询安全限制。

## security

* 作用：设置当前环境的安全限制
* 参数数量：无穷（>= 0）
* 参数作用：安全限制<*>
* 参数类型：string<*>
* 返回值：nil
* 返回类型：word
* 示例：(security "file")

## security-clear!

* 作用：清空当前环境的安全限制
* 参数数量：0
* 参数作用：
* 参数类型：
* 返回值：nil
* 返回类型：word
* 示例：(security-clear!)

## security-remove!

* 作用：删除当前环境直接设置的指定安全限制；只能删除本层环境添加的限制，父环境继承的限制不能在子环境删除
* 参数数量：无穷（>= 1）
* 参数作用：安全限制<*>
* 参数类型：string<*>
* 返回值：nil
* 返回类型：word
* 示例：(security-remove! "file" "net")

## security-restrictions

* 作用：获取当前环境及父环境合并后的安全限制
* 参数数量：0
* 参数作用：
* 参数类型：
* 返回值：安全限制列表
* 返回类型：list
* 示例：(security-restrictions)

## security-restrict?

* 作用：判断当前环境是否生效指定安全限制
* 参数数量：1
* 参数作用：安全限制
* 参数类型：string
* 返回值：是否限制
* 返回类型：bool
* 示例：(security-restrict? "file")
