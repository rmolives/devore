需要导入模块：thread

# 类型

## 新增类型
* thread: 线程
* lock: 锁

## thread?
* 作用：判断是否为thread类型
* 参数数量：1
* 参数作用：内容
* 参数类型：token
* 返回值：是否为thread类型
* 返回类型：bool
* 示例：(thread? 3)

## lock?
* 作用：判断是否为lock类型
* 参数数量：1
* 参数作用：内容
* 参数类型：token
* 返回值：是否为lock类型
* 返回类型：bool
* 示例：(lock? 3)

# 线程

## thread
* 作用：创建线程
* 参数数量：无穷（>= 1）
* 参数作用：token<*>
* 参数类型：token<*>
* 返回值：线程
* 返回类型：thread
* 示例：(thread (while true (print (time))))

## thread-start
* 作用：启动线程
* 参数数量：无穷（>= 1）
* 参数作用：thread<*>
* 参数类型：thread<*>
* 返回值：nil
* 返回类型：word
* 示例：(thread-start a b c)

## thread-interrupt!
* 作用：尝试结束线程
* 参数数量：1
* 参数作用：thread
* 参数类型：thread
* 返回值：nil
* 返回类型：word
* 示例：(thread-interrupt! a)

## join
* 作用：等待一个 thread 线程执行结束，并返回线程体最后一个表达式的返回值
* 参数数量：1
* 参数作用：thread
* 参数类型：thread
* 返回值：线程
* 返回类型：token
* 示例：(join a)

## join
* 作用：等待一个 thread 线程执行结束（最多等特定毫秒），并返回线程体最后一个表达式的返回值
* 参数数量：2
* 参数作用：thread、特定毫秒
* 参数类型：thread、int
* 返回值：如果超时则返回nil，否则返回线程体最后一个表达式的返回值
* 返回类型：token
* 示例：(join a 1000)

## thread-alive?
* 作用：判断一个线程现在是否还在运行
* 参数数量：1
* 参数作用：thread
* 参数类型：thread
* 返回值：bool
* 返回类型：bool
* 示例：(thread-alive? a)

## current-thread
* 作用：当前正在执行这段代码的线程
* 参数数量：0
* 参数作用：
* 参数类型：
* 返回值：thread
* 返回类型：thread
* 示例：(current-thread a)

# 锁

## lock
* 作用：创建新锁
* 参数数量：0
* 参数作用：
* 参数类型：
* 返回值：lock
* 返回类型：lock
* 示例：(lock)

## lock!
* 作用：如果锁空闲，当前线程立刻拿到锁；如果锁已经被别的线程持有，当前线程会阻塞等待，直到拿到锁为止
* 参数数量：1
* 参数作用：锁
* 参数类型：lock
* 返回值：nil
* 返回类型：word
* 示例：(lock! a)

## unlock!
* 作用：释放锁
* 参数数量：1
* 参数作用：锁
* 参数类型：lock
* 返回值：nil
* 返回类型：word
* 示例：(unlock! a)

## try-lock!
* 作用：尝试获取锁
* 参数数量：1
* 参数作用：锁
* 参数类型：lock
* 返回值：是否获取到
* 返回类型：bool
* 示例：(try-lock! a)

## try-lock!
* 作用：尝试获取锁（最多等特定毫秒）
* 参数数量：2
* 参数作用：锁、特定毫秒
* 参数类型：lock、int
* 返回值：是否获取到
* 返回类型：bool
* 示例：(try-lock! a 1000)

## locked?
* 作用：判断一把锁现在是否被任意线程持有
* 参数数量：1
* 参数作用：锁
* 参数类型：lock
* 返回值：bool
* 返回类型：bool
* 示例：(locked? a)

## held-by-current-thread?
* 作用：判断“当前线程”是否持有这把锁
* 参数数量：1
* 参数作用：锁
* 参数类型：lock
* 返回值：bool
* 返回类型：bool
* 示例：(held-by-current-thread? a)

## with-lock
* 作用：带自动释放的加锁代码块、返回最后一个表达式的结果
* 参数数量：无穷（>= 2）
* 参数作用：锁、token<*>
* 参数类型：lock、token<*>
* 返回值：token
* 返回类型：token
* 示例：(with-lock l (set! n (+ n 1))))
