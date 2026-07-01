# 文档

欢迎使用**Devore Language**，是一门**Java**实现的**Lisp方言**，本wiki将讲解**Devore Language**的内置过程的用法等。

**Devore Language**采用**S-表达式**，其中\[\]和\(\)可以互换，但需要互相匹配。

语法：(operator operand1 operand2 ...)

例如：

* (+ 3 (- 4 5))是+(3, -(4, 5))也就是3 + (4 - 5)
* (a (b c))是a(b(c))

## 字面量

* 真：true
* 假：false
* null：nil

## 注释

使用`;xxx`进行注释。

## 基础

* [普通过程](Ordinary.md)
* [类型](Type.md)
* [模块](module/Module.md)

## 数据处理

* [字符串](String.md)
* [列表](List.md)
* [表](Table.md)
* [数学](Math.md)
* [JSON](module/Json.md)
* [CSV](module/Csv.md)
* [XML](module/Xml.md)
* [HTML](module/Html.md)
* [二进制](module/Binary.md)
* [Base64](module/Base64.md)
* [哈希](module/Hash.md)
* [签名](module/Sign.md)
* [加密](module/Crypto.md)
* [UUID](module/UUID.md)
* [Properties](module/Properties.md)
* [正则表达式](module/Regex.md)
* [ZIP](module/Zip.md)

## 系统与工具

* [文件](module/File.md)
* [HTTP](module/Http.md)
* [网络信息](module/Net.md)
* [TCP](module/TCP.md)
* [UDP](module/UDP.md)
* [操作系统](module/OS.md)
* [Java反射](module/Reflect.md)
* [安全限制](module/Security.md)
* [线程](module/Thread.md)
* [时间](module/Time.md)
