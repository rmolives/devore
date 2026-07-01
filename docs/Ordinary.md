# 普通过程

## 普通

### bound?

* 作用：判断特定symbol是否已绑定
* 参数数量：1
* 参数作用：特定symbol
* 参数类型：symbol
* 返回值：bool
* 返回类型：bool
* 示例：(bound? test)

### error

* 作用：抛出参数拼接的结果
* 参数数量：无穷（>= 1）
* 参数作用：错误内容<\*>
* 参数类型：token<\*>
* 返回值：nil
* 返回类型：word
* 示例：(error "error")

### try

* 作用：捕捉运行错误
* 参数数量：无穷（>= 2）
* 参数作用：body<\*>、\[catch key handler<\*>\]
* 参数类型：token<\*>、\[catch symbol token<\*>\]
* 返回值：正常时返回 body 最后一个表达式；报错时返回 handler 最后一个表达式
* 返回类型：token
* 示例：(try (/ 1 0) \[catch err 0\])

### def

* 作用：建立新绑定（非过程），建立于最后一个的运算结果
* 参数数量：无穷（>= 2）
* 参数作用：key、value<\*>
* 参数类型：symbol、token<\*>
* 返回值：nil
* 返回类型：word
* 示例：(def a 3)

### def

* 作用：建立新绑定（过程）
* 参数数量：无穷（>= 2）
* 参数作用：(key param<\*>)、value<\*>
* 参数类型：symbol<\*>、token<\*>
* 返回值：nil
* 返回类型：word
* 示例：(def (add a b) (+ a b))

### undef

* 作用：删除绑定
* 参数数量：无穷（>= 1）
* 参数作用：key
* 参数类型：symbol
* 返回值：nil
* 返回类型：word
* 示例：(undef a)

### set!

* 作用：重设绑定（非过程），建立于最后一个的运算结果
* 参数数量：无穷（>= 2）
* 参数作用：key、value<\*>
* 参数类型：symbol、token<\*>
* 返回值：nil
* 返回类型：word
* 示例：(set! a 3)

### set!

* 作用：重设绑定（过程）
* 参数数量：无穷（>= 2）
* 参数作用：(key param<\*>)、value<\*>
* 参数类型：symbol<\*>、token<\*>
* 返回值：nil
* 返回类型：word
* 示例：(set! (add a b) (+ a b))

### def-macro

* 作用：建立宏，把value<\*>内的和param<\*>相同的symbol替换成传入的内容
* 参数数量：无穷（>= 2）
* 参数作用：(key param<\*>)、value<\*>
* 参数类型：symbol、token<\*>
* 返回值：nil
* 返回类型：word
* 示例：(def-macro (infix a op b) (op a b))

### set-macro!

* 作用：重设宏，把value<\*>内的和param<\*>相同的symbol替换成传入的内容
* 参数数量：无穷（>= 2）
* 参数作用：(key param<\*>)、value<\*>
* 参数类型：symbol、token<\*>
* 返回值：nil
* 返回类型：word
* 示例：(set-macro! (infix a op b) (op a b))

### lambda

* 作用：Lambda
* 参数数量：无穷（>= 2）
* 参数作用：(param<\*>)、value<\*>
* 参数类型：symbol<\*>、token<\*>
* 返回值：procedure
* 返回类型：procedure
* 示例：(lambda (a b) (+ a b))

### let

* 作用：局部变量绑定（可互相引用）
* 参数数量：无穷（>= 2）
* 参数作用：((key value)<\*>)、value<\*>
* 参数类型：((symbol token)<\*>)、token<\*>
* 返回值：最后一个的运算结果
* 返回类型：token
* 示例：(let ((a 1) (b (+ a 1))) (+ a b))
* 示例：(let () (+ 1 2))

### apply

* 作用：参数应用于过程
* 参数数量：无穷（>= 1）
* 参数作用：procedure、arg<\*>
* 参数类型：procedure、token<\*>
* 返回值：运算结果
* 返回类型：token
* 示例：(apply + 1 2)

### act

* 作用：列表应用于过程
* 参数数量：2
* 参数作用：procedure、arg<\*>
* 参数类型：procedure、list<\*>
* 返回值：运算结果
* 返回类型：token
* 示例：(act + (list 1 2))

## 布尔运算

### >

* 作用：大于
* 参数数量：2
* 参数作用：token、token
* 参数类型：token、token
* 返回值：比较结果
* 返回类型：bool
* 示例：(> 1 2)

### <

* 作用：小于
* 参数数量：2
* 参数作用：token、token
* 参数类型：token、token
* 返回值：比较结果
* 返回类型：bool
* 示例：(< 1 2)

### =

* 作用：等于
* 参数数量：2
* 参数作用：token、token
* 参数类型：token、token
* 返回值：比较结果
* 返回类型：bool
* 示例：(= 1 2)

### >=

* 作用：大于等于
* 参数数量：2
* 参数作用：token、token
* 参数类型：token、token
* 返回值：比较结果
* 返回类型：bool
* 示例：(>= 1 2)

### <=

* 作用：小于等于
* 参数数量：2
* 参数作用：token、token
* 参数类型：token、token
* 返回值：比较结果
* 返回类型：bool
* 示例：(<= 1 2)

### /=

* 作用：不等于
* 参数数量：2
* 参数作用：token、token
* 参数类型：token、token
* 返回值：比较结果
* 返回类型：bool
* 示例：(/= 1 2)

### and

* 作用：且
* 参数数量：无穷（>= 1）
* 参数作用：bool<\*>
* 参数类型：bool<\*>
* 返回值：结果
* 返回类型：bool
* 示例：(and (> a 3) (< a 5))

### or

* 作用：或
* 参数数量：无穷（>= 1）
* 参数作用：bool<\*>
* 参数类型：bool<\*>
* 返回值：结果
* 返回类型：bool
* 示例：(or (> a 3) (< a 5))

### not

* 作用：非
* 参数数量：1
* 参数作用：bool
* 参数类型：bool
* 返回值：结果
* 返回类型：bool
* 示例：(not (> a 3))

## IO

### error-print

* 作用：不换行输出参数拼接的结果（错误）
* 参数数量：无穷（>= 1）
* 参数作用：输出内容<\*>
* 参数类型：token<\*>
* 返回值：nil
* 返回类型：word
* 示例：(error-print "hello")

### error-println

* 作用：换行输出参数拼接的结果（错误）
* 参数数量：无穷（>= 1）
* 参数作用：输出内容<\*>
* 参数类型：token<\*>
* 返回值：nil
* 返回类型：word
* 示例：(error-println "hello")

### error-newline

* 作用：输出换行（错误）
* 参数数量：0
* 参数作用：
* 参数类型：
* 返回值：nil
* 返回类型：word
* 示例：(error-newline)

### print

* 作用：不换行输出参数拼接的结果
* 参数数量：无穷（>= 1）
* 参数作用：输出内容<\*>
* 参数类型：token<\*>
* 返回值：nil
* 返回类型：word
* 示例：(print "hello")

### println

* 作用：换行输出参数拼接的结果
* 参数数量：无穷（>= 1）
* 参数作用：输出内容<\*>
* 参数类型：token<\*>
* 返回值：nil
* 返回类型：word
* 示例：(println "hello")

### newline

* 作用：输出换行
* 参数数量：0
* 参数作用：
* 参数类型：
* 返回值：nil
* 返回类型：word
* 示例：(newline)

### read-line

* 作用：读入一行
* 参数数量：0
* 参数作用：
* 参数类型：
* 返回值：读入内容
* 返回类型：string
* 示例：(read-line)

### read-int

* 作用：读入一个整数
* 参数数量：0
* 参数作用：
* 参数类型：
* 返回值：读入内容
* 返回类型：int
* 示例：(read-int)

### read-float

* 作用：读入一个浮点数
* 参数数量：0
* 参数作用：
* 参数类型：
* 返回值：读入内容
* 返回类型：float
* 示例：(read-float)

### read-bool

* 作用：读入一个布尔值
* 参数数量：0
* 参数作用：
* 参数类型：
* 返回值：读入内容
* 返回类型：bool
* 示例：(read-bool)

### read

* 作用：读入一个字符串
* 参数数量：0
* 参数作用：
* 参数类型：
* 返回值：读入内容
* 返回类型：string
* 示例：(read)

## 流程控制

### if

* 作用：判断参数，如果为true就执行并返回第一个参数的运算结果，否则返回nil
* 参数数量：2
* 参数作用：条件、true结果
* 参数类型：bool、token
* 返回值：结果
* 返回类型：token
* 示例：(if (> 2 3) 1)

### if

* 作用：判断参数，如果为true就执行并返回第一个参数的运算结果，否则执行并返回第二个参数的运算结果
* 参数数量：3
* 参数作用：条件、true结果、false结果
* 参数类型：bool、token、token
* 返回值：结果
* 返回类型：token
* 示例：(if (> 2 3) 1 2)

### when

* 作用：判断参数，如果为true就执行并返回最后一个的运算结果
* 参数数量：无穷（>= 2）
* 参数作用：条件、true结果<\*>
* 参数类型：bool、token<\*>
* 返回值：结果
* 返回类型：token
* 示例：(when (> 2 3) 1)

### unless

* 作用：判断参数，如果为false就执行并返回最后一个的运算结果
* 参数数量：无穷（>= 2）
* 参数作用：条件、false结果<\*>
* 参数类型：bool、token<\*>
* 返回值：结果
* 返回类型：token
* 示例：(unless (> 2 3) 1)

### cond

* 作用：多条件判断
* 参数数量：无穷（>= 1）
* 参数作用：(\[条件|else\] value<\*>)<\*>
* 参数类型：(\[bool|symbol\]<\*> token<\*>)<\*>
* 返回值：token
* 返回类型：token
* 示例：(cond ((>= 1 2) 1) (>= 2 3) 2) (else 4)))

### while

* 作用：while循环
* 参数数量：无穷（>= 2）
* 参数作用：条件、token<\*>
* 参数类型：bool、token<\*>
* 返回值：结果
* 返回类型：token
* 示例：(while (< a 3) (set! a (- a 1)))

## 控制

### exit

* 作用：结束程序
* 参数数量：1
* 参数作用：结束状态
* 参数类型：int
* 返回值：nil
* 返回类型：word
* 示例：(exit 0)

### sleep

* 作用：延迟运行
* 参数数量：1
* 参数作用：延迟运行时间
* 参数类型：int
* 返回值：nil
* 返回类型：word
* 示例：(sleep 1000)

## 杂项

### max

* 作用：获取最大值
* 参数数量：无穷（>= 1）
* 参数作用：需要比较的元素
* 参数类型：token<\*>
* 返回值：最大值
* 返回类型：token
* 示例：(max -1 1 2 3)

### min

* 作用：获取最小值
* 参数数量：无穷（>= 1）
* 参数作用：需要比较的元素
* 参数类型：token<\*>
* 返回值：最小值
* 返回类型：token
* 示例：(min -1 1 2 3)

### time

* 作用：获取当前时间戳
* 参数数量：0
* 参数作用：
* 参数类型：
* 返回值：time
* 返回类型：int
* 示例：(time)

### nil?

* 作用：判断是否为nil
* 参数数量：1
* 参数作用：内容
* 参数类型：token
* 返回值：是否为nil
* 返回类型：bool
* 示例：(nil? 3)

### begin

* 作用：执行多条并返回最后一个的运算结果
* 参数数量：无穷（>= 1）
* 参数作用：token<\*>
* 参数类型：token<\*>
* 返回值：结果
* 返回类型：token
* 示例：(begin (def a 3) (+ a 2))
