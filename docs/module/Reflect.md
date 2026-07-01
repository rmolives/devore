# Java反射

需要导入模块：reflect

## 类型

### 新增类型

* java-object: Java对象包装

### java-object?

* 作用：判断是否为java-object类型
* 参数数量：1
* 参数作用：内容
* 参数类型：token
* 返回值：是否为java-object类型
* 返回类型：bool
* 示例：(java-object? (reflect-class "java.lang.String"))

## reflect-class

* 作用：加载Java类
* 参数数量：1
* 参数作用：完整类名
* 参数类型：string
* 返回值：Java Class包装对象
* 返回类型：java-object
* 示例：(reflect-class "java.lang.StringBuilder")

## reflect-class-name

* 作用：获取Java类名
* 参数数量：1
* 参数作用：类名或Java Class包装对象
* 参数类型：string|java-object
* 返回值：完整类名
* 返回类型：string
* 示例：(reflect-class-name (reflect-class "java.lang.String"))

## reflect-new

* 作用：调用Java构造方法创建对象
* 参数数量：无穷（>= 1）
* 参数作用：类名或Java Class包装对象，构造方法参数<*>
* 参数类型：string|java-object, token<*>
* 返回值：Java对象包装对象
* 返回类型：java-object
* 示例：(reflect-new "java.lang.StringBuilder" "hello")

## reflect-call

* 作用：调用Java实例方法；目标为Class包装对象或类名时调用静态方法
* 参数数量：无穷（>= 2）
* 参数作用：Java对象或类，方法名，方法参数<*>
* 参数类型：java-object|string, string, token<*>
* 返回值：方法返回值的Java对象包装对象
* 返回类型：java-object
* 示例：(reflect-call (reflect-new "java.lang.StringBuilder" "hello") "toString")

## reflect-static-call

* 作用：调用Java静态方法
* 参数数量：无穷（>= 2）
* 参数作用：类名或Java Class包装对象，方法名，方法参数<*>
* 参数类型：string|java-object, string, token<*>
* 返回值：方法返回值的Java对象包装对象
* 返回类型：java-object
* 示例：(reflect-static-call "java.lang.Math" "max" 3 7)

## java->devore

* 作用：将Java对象包装对象转换为Devore值
* 参数数量：1
* 参数作用：Java对象包装对象
* 参数类型：java-object
* 返回值：转换后的Devore值。支持nil、string、bool、number、array、Iterable、Map递归转换
* 返回类型：token
* 示例：(java->devore (reflect-static-call "java.lang.Math" "max" 3 7))
