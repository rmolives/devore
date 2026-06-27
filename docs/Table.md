# table
* 作用：创建表
* 参数数量：0
* 参数作用：
* 参数类型：
* 返回值：table
* 返回类型：table
* 示例：(table)

# table-put
* 作用：往表里添加元素（不改变原表，生成新表）
* 参数数量：3
* 参数作用：表、key、value
* 参数类型：table、token、token
* 返回值：更改过的表
* 返回类型：table
* 示例：(table-put (table) "a" 3)

# table-remove
* 作用：往表里删除元素（不改变原表，生成新表）
* 参数数量：2
* 参数作用：表、key
* 参数类型：table、token
* 返回值：更改过的表
* 返回类型：table
* 示例：(table-remove (table) "a")

# table-put!
* 作用：往表里添加元素（改变原表，不生成新表）
* 参数数量：3
* 参数作用：表、key、value
* 参数类型：table、token、token
* 返回值：更改过的表
* 返回类型：table
* 示例：(table-put! (table) "a" 3)

# table-remove!
* 作用：往表里删除元素（改变原表，不生成新表）
* 参数数量：2
* 参数作用：表、key
* 参数类型：table、token
* 返回值：更改过的表
* 返回类型：table
* 示例：(table-remove! (table) "a")

# table-clear!
* 作用：清空表
* 参数数量：1
* 参数作用：表
* 参数类型：table
* 返回值：更改过的表
* 返回类型：table
* 示例：(table-clear! a)

# length
* 作用：获取表长度
* 参数数量：1
* 参数作用：表
* 参数类型：table
* 返回值：长度
* 返回类型：int
* 示例：(length t)

# table-contains-key
* 作用：判断表是否包含特定key
* 参数数量：2
* 参数作用：表、元素
* 参数类型：table、token
* 返回值：bool
* 返回类型：bool
* 示例：(table-contains-key t "a")

# table-contains-value
* 作用：判断表是否包含特定value
* 参数数量：2
* 参数作用：表、元素
* 参数类型：table、token
* 返回值：bool
* 返回类型：bool
* 示例：(table-contains-value t "a")

# table-keys
* 作用：获取表的所有key
* 参数数量：1
* 参数作用：表
* 参数类型：table
* 返回值：所有key组成的列表
* 返回类型：token
* 示例：(table-keys t)
