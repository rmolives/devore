# \+\+
* 作用：拼接字符串
* 参数数量：无穷（>= 2）
* 参数作用：字符串
* 参数类型：string
* 返回值：拼接的字符串
* 返回类型：string
* 示例：(\+\+ "abc" "bcd")

# length
* 作用：获取字符串长度
* 参数数量：1
* 参数作用：字符串
* 参数类型：string
* 返回值：长度
* 返回类型：int
* 示例：(length "abc")

# ->string
* 作用：将输入转为字符串
* 参数数量：1
* 参数作用：内容
* 参数类型：token
* 返回值：字符串
* 返回类型：string
* 示例：(->string 123)

# string->number
* 作用：将字符串转换为number
* 参数数量：1
* 参数作用：内容
* 参数类型：string
* 返回值：number
* 返回类型：number
* 示例：(string->number "123.2")

# string->bool
* 作用：将字符串转换为bool
* 参数数量：1
* 参数作用：内容
* 参数类型：string
* 返回值：bool
* 返回类型：bool
* 示例：(string->bool "false")

# string->symbol
* 作用：将字符串转换为symbol
* 参数数量：1
* 参数作用：内容
* 参数类型：string
* 返回值：symbol
* 返回类型：symbol
* 示例：(string->symbol "a")

# string->list
* 作用：将字符串转换为list
* 参数数量：1
* 参数作用：内容
* 参数类型：string
* 返回值：list
* 返回类型：list
* 示例：(string->list "abc")

# string-index
* 作用：获取字符最早出现位置
* 参数数量：2
* 参数作用：字符串、定位字符串
* 参数类型：string、string
* 返回值：位置
* 返回类型：int
* 示例：(string-index "dabac" "a")

# string-index-last
* 作用：获取字符最后出现位置
* 参数数量：2
* 参数作用：字符串、定位字符串
* 参数类型：string、string
* 返回值：位置
* 返回类型：int
* 示例：(string-index-last "dabac" "a")

# string-contains
* 作用：字符串是否包含特定字符串
* 参数数量：2
* 参数作用：字符串、特定字符串
* 参数类型：string、string
* 返回值：bool
* 返回类型：bool
* 示例：(string-contains "dabac" "a")

# string-match
* 作用：字符串是否符合正则
* 参数数量：2
* 参数作用：字符串、正则
* 参数类型：string、string
* 返回值：bool
* 返回类型：bool
* 示例：(string-match "dabac" "a")

# string-starts-with
* 作用：字符串是否起始特定字符串
* 参数数量：2
* 参数作用：字符串、特定字符串
* 参数类型：string、string
* 返回值：bool
* 返回类型：bool
* 示例：(string-starts-with "dabac" "a")

# string-ends-with
* 作用：字符串是否结束特定字符串
* 参数数量：2
* 参数作用：字符串、特定字符串
* 参数类型：string、string
* 返回值：bool
* 返回类型：bool
* 示例：(string-ends-with "dabac" "a")

# string-get
* 作用：获取字符串特定下标的字符
* 参数数量：2
* 参数作用：字符串、下标
* 参数类型：string、int
* 返回值：特定下标的字符
* 返回类型：string
* 示例：(string-get "dabac" 2)

# string-sub
* 作用：截取字符串特定起始下标之后的字符
* 参数数量：2
* 参数作用：字符串、起始下标
* 参数类型：string、int
* 返回值：特定下标之后的字符
* 返回类型：string
* 示例：(string-sub "dabac" 2)

# string-sub
* 作用：截取字符串特定起始下标到结束下标之间的字符
* 参数数量：3
* 参数作用：字符串、起始下标、结束下标
* 参数类型：string、int、int
* 返回值：特定起始下标到结束下标之间的字符
* 返回类型：string
* 示例：(string-sub "dabac" 2 4)

# string-empty?
* 作用：字符串是否为空
* 参数数量：1
* 参数作用：字符串
* 参数类型：string
* 返回值：bool
* 返回类型：bool
* 示例：(string-empty? "dabac")

# string-replace
* 作用：替换字符串
* 参数数量：2
* 参数作用：原字符串、替换前、替换后
* 参数类型：string、string、string
* 返回值：替换后的内容
* 返回类型：string
* 示例：(string-replace "dabac" "a" "c")

# string-replace-regex
* 作用：替换字符串（正则）
* 参数数量：2
* 参数作用：原字符串、替换前、替换后
* 参数类型：string、string、string
* 返回值：替换后的内容
* 返回类型：string
* 示例：(string-replace-regex "dabac" "a" "c")

# string-split-regex
* 作用：分割字符串（正则）
* 参数数量：2
* 参数作用：原字符串、分割字符串
* 参数类型：string、string
* 返回值：list
* 返回类型：list
* 示例：(string-split-regex "dabac" "a")

# string-split
* 作用：分割字符串（正则）
* 参数数量：2
* 参数作用：原字符串、分割字符串
* 参数类型：string、string
* 返回值：list
* 返回类型：list
* 示例：(string-split "dabac" "a")

# string-trim
* 作用：删除两侧空白
* 参数数量：1
* 参数作用：原字符串
* 参数类型：string
* 返回值：string
* 返回类型：string
* 示例：(string-trim "dabac ")

# string-trim-left
* 作用：删除左侧空白
* 参数数量：1
* 参数作用：原字符串
* 参数类型：string
* 返回值：string
* 返回类型：string
* 示例：(string-trim-left "dabac ")

# string-trim-right
* 作用：删除右侧空白
* 参数数量：1
* 参数作用：原字符串
* 参数类型：string
* 返回值：string
* 返回类型：string
* 示例：(string-trim-right "dabac ")

# string-upper
* 作用：字符串转大写
* 参数数量：1
* 参数作用：原字符串
* 参数类型：string
* 返回值：string
* 返回类型：string
* 示例：(string-upper "dabac ")

# string-lower
* 作用：字符串转小写
* 参数数量：1
* 参数作用：原字符串
* 参数类型：string
* 返回值：string
* 返回类型：string
* 示例：(string-lower "AAA ")

# char->unicode
* 作用：将字符转换为unicode
* 参数数量：1
* 参数作用：内容
* 参数类型：string
* 返回值：unicode
* 返回类型：int
* 示例：(char->unicode "a")

# unicode->char
* 作用：将unicode转换为字符
* 参数数量：1
* 参数作用：unicode
* 参数类型：int
* 返回值：字符
* 返回类型：string
* 示例：(unicode->char 48)
