# 网络信息

需要导入模块：net

## dns-lookup

* 作用：查询域名或主机名对应的IP地址
* 参数数量：1
* 参数作用：域名或主机名
* 参数类型：string
* 返回值：IP地址列表
* 返回类型：list
* 示例：(dns-lookup "example.com")

## dns-reverse-lookup

* 作用：反向查询IP地址对应的主机名
* 参数数量：1
* 参数作用：IP地址
* 参数类型：string
* 返回值：主机名
* 返回类型：string
* 示例：(dns-reverse-lookup "8.8.8.8")

## hostname

* 作用：获取本机主机名
* 参数数量：0
* 参数作用：
* 参数类型：
* 返回值：主机名
* 返回类型：string
* 示例：(hostname)

## ip

* 作用：获取本机默认IP地址
* 参数数量：0
* 参数作用：
* 参数类型：
* 返回值：IP地址
* 返回类型：string
* 示例：(ip)

## ips

* 作用：获取全部网络接口的IP地址
* 参数数量：0
* 参数作用：
* 参数类型：
* 返回值：IP地址列表
* 返回类型：list
* 示例：(ips)

## ips

* 作用：获取指定网络接口的IP地址
* 参数数量：1
* 参数作用：网络接口名称
* 参数类型：string
* 返回值：IP地址列表
* 返回类型：list
* 示例：(ips "eth0")

## mac

* 作用：获取默认网络接口的MAC地址
* 参数数量：0
* 参数作用：
* 参数类型：
* 返回值：MAC地址，不存在时返回nil
* 返回类型：string|word
* 示例：(mac)

## mac

* 作用：获取指定网络接口的MAC地址
* 参数数量：1
* 参数作用：网络接口名称
* 参数类型：string
* 返回值：MAC地址，不存在时返回nil
* 返回类型：string|word
* 示例：(mac "eth0")

## network-interface

* 作用：获取指定网络接口信息
* 参数数量：1
* 参数作用：网络接口名称
* 参数类型：string
* 返回值：网络接口信息表，包含name、display-name、mac、ips、up?、loopback?、virtual?、point-to-point?、multicast?、mtu
* 返回类型：table
* 示例：(network-interface "eth0")

## network-interfaces

* 作用：获取全部网络接口信息
* 参数数量：0
* 参数作用：
* 参数类型：
* 返回值：网络接口信息表列表
* 返回类型：list
* 示例：(network-interfaces)
