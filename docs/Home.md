欢迎使用**Devore Language**，是一门**Java**实现的**Lisp方言**，本wiki将讲解**Devore Language**的内置过程的用法等。

**Devore Language**采用**S-表达式**，其中\[\]和\(\)可以互换，但需要互相匹配。

语法：(operator operand1 operand2 ...)

例如：
* (+ 3 (- 4 5))是+(3, -(4, 5))也就是3 + (4 - 5)
* (a (b c))是a(b(c))

# 字面量
* 真：true
* 假：false
* null：nil

# 注释
使用`;xxx`进行注释。
