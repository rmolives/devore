需要导入模块：os

# os-name
* 作用：获取操作系统名称
* 参数数量：0
* 参数作用：
* 参数类型：
* 返回值：操作系统名称
* 返回类型：string
* 示例：(os-name)

# os-arch
* 作用：获取操作系统架构
* 参数数量：0
* 参数作用：
* 参数类型：
* 返回值：操作系统架构
* 返回类型：string
* 示例：(os-arch)

# os-version
* 作用：获取操作系统版本
* 参数数量：0
* 参数作用：
* 参数类型：
* 返回值：操作系统版本
* 返回类型：string
* 示例：(os-version)

# os-user-name
* 作用：获取当前用户名称
* 参数数量：0
* 参数作用：
* 参数类型：
* 返回值：当前用户名称
* 返回类型：string
* 示例：(os-user-name)

# os-user-home
* 作用：获取当前用户主目录
* 参数数量：0
* 参数作用：
* 参数类型：
* 返回值：当前用户主目录
* 返回类型：string
* 示例：(os-user-home)

# os-current-dir
* 作用：获取当前工作目录
* 参数数量：0
* 参数作用：
* 参数类型：
* 返回值：当前工作目录
* 返回类型：string
* 示例：(os-current-dir)

# os-line-separator
* 作用：获取当前系统的换行符
* 参数数量：0
* 参数作用：无
* 参数类型：无
* 返回值：换行符
* 返回类型：string
* 示例：(os-line-separator)

# os-available-processors
* 作用：获取JVM可用的处理器数量
* 参数数量：0
* 参数作用：
* 参数类型：
* 返回值：处理器数量
* 返回类型：int
* 示例：(os-available-processors)

# os-free-memory
* 作用：获取JVM当前空闲内存字节数
* 参数数量：0
* 参数作用：
* 参数类型：
* 返回值：空闲内存字节数
* 返回类型：int
* 示例：(os-free-memory)

# os-total-memory
* 作用：获取JVM当前总内存字节数
* 参数数量：0
* 参数作用：
* 参数类型：
* 返回值：总内存字节数
* 返回类型：int
* 示例：(os-total-memory)

# os-max-memory
* 作用：获取JVM最大可用内存字节数
* 参数数量：0
* 参数作用：
* 参数类型：
* 返回值：最大内存字节数
* 返回类型：int
* 示例：(os-max-memory)

# os-process-id
* 作用：获取当前JVM进程ID
* 参数数量：0
* 参数作用：
* 参数类型：
* 返回值：进程ID，获取失败时返回-1
* 返回类型：int
* 示例：(os-process-id)

# os-env
* 作用：获取指定名称的环境变量
* 参数数量：1
* 参数作用：环境变量名称
* 参数类型：string
* 返回值：环境变量值，不存在时返回nil
* 返回类型：string|word
* 示例：(os-env "PATH")

# os-envs
* 作用：获取全部环境变量
* 参数数量：0
* 参数作用：无
* 参数类型：无
* 返回值：环境变量表，key为变量名，value为变量值
* 返回类型：table
* 示例：(os-envs)

# os-property
* 作用：获取指定名称的Java系统属性
* 参数数量：1
* 参数作用：Java系统属性名称
* 参数类型：string
* 返回值：Java系统属性值，不存在时返回nil
* 返回类型：string|word
* 示例：(os-property "java.version")

# os-properties
* 作用：获取全部Java系统属性
* 参数数量：0
* 参数作用：无
* 参数类型：无
* 返回值：Java系统属性表，key为属性名，value为属性值
* 返回类型：table
* 示例：(os-properties)

# os-exec
* 作用：执行操作系统命令
* 参数数量：1
* 参数作用：命令
* 参数类型：string|list
* 返回值：命令执行结果表，包含exit-code、stdout、stderr
* 返回类型：table
* 示例：(os-exec "echo abc")

# os-exec
* 作用：在指定工作目录执行操作系统命令
* 参数数量：2
* 参数作用：命令、工作目录
* 参数类型：[string|list] string
* 返回值：命令执行结果表，包含exit-code、stdout、stderr
* 返回类型：table
* 示例：(os-exec "pwd" "/tmp")
