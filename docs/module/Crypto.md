# 加密

需要导入模块：crypto

## AES

### aes-key

* 作用：生成AES密钥
* 参数数量：1
* 参数作用：密钥位数
* 参数类型：int
* 返回值：binary
* 返回类型：list
* 示例：(aes-key 128)

### aes-encrypt

* 作用：aes加密，默认采用AES/CBC/PKCS5Padding
* 参数数量：3
* 参数作用：data、密钥、iv
* 参数类型：\[string|list\]、\[string|list\]、\[string|list\]
* 返回值：binary
* 返回类型：list
* 示例：(aes-encrypt data key iv)

### aes-encrypt

* 作用：aes加密，采用特定AES_TRANSFORMATION
* 参数数量：4
* 参数作用：data、密钥、iv、AES_TRANSFORMATION
* 参数类型：\[string|list\]、\[string|list\]、\[string|list\]、string
* 返回值：binary
* 返回类型：list
* 示例：(aes-encrypt data key iv "AES/CBC/PKCS5Padding")

### aes-decrypt

* 作用：aes解密，默认采用AES/CBC/PKCS5Padding
* 参数数量：3
* 参数作用：data、密钥、iv
* 参数类型：\[string|list\]、\[string|list\]、\[string|list\]
* 返回值：binary
* 返回类型：list
* 示例：(aes-decrypt data key iv)

### aes-decrypt

* 作用：aes解密，采用特定AES_TRANSFORMATION
* 参数数量：4
* 参数作用：data、密钥、iv、AES_TRANSFORMATION
* 参数类型：\[string|list\]、\[string|list\]、\[string|list\]、string
* 返回值：binary
* 返回类型：list
* 示例：(aes-decrypt data key iv "AES/CBC/PKCS5Padding")

## RSA

### rsa-keypair

* 作用：生成rsa的公钥和私钥
* 参数数量：1
* 参数作用：密钥位数，不能小于1024
* 参数类型：int
* 返回值：{"public"=\<public\>, "private"=\<private\>}
* 返回类型：table
* 示例：(rsa-keypair 2048)

### rsa-encrypt

* 作用：rsa加密，默认采用RSA/ECB/OAEPWithSHA-256AndMGF1Padding
* 参数数量：2
* 参数作用：data、公钥
* 参数类型：\[string|list\]、string
* 返回值：binary
* 返回类型：list
* 示例：(rsa-encrypt data public-key)

### rsa-encrypt

* 作用：rsa加密，采用特定RSA_TRANSFORMATION
* 参数数量：3
* 参数作用：data、公钥、RSA_TRANSFORMATION
* 参数类型：\[string|list\]、string、string
* 返回值：binary
* 返回类型：list
* 示例：(rsa-encrypt data public-key "RSA/ECB/OAEPWithSHA-256AndMGF1Padding")

### rsa-decrypt

* 作用：rsa解密，默认采用RSA/ECB/OAEPWithSHA-256AndMGF1Padding
* 参数数量：2
* 参数作用：data、私钥
* 参数类型：\[string|list\]、string
* 返回值：binary
* 返回类型：list
* 示例：(rsa-decrypt data private-key)

### rsa-decrypt

* 作用：rsa解密，采用特定RSA_TRANSFORMATION
* 参数数量：3
* 参数作用：data、私钥、RSA_TRANSFORMATION
* 参数类型：\[string|list\]、string、string
* 返回值：binary
* 返回类型：list
* 示例：(rsa-decrypt data private-key "RSA/ECB/OAEPWithSHA")

### rsa-sign

* 作用：RSA签名，默认采用SHA256withRSA
* 参数数量：2
* 参数作用：data、privateKey
* 参数类型：\[string|list\]、string
* 返回值：binary
* 返回类型：list
* 示例：(rsa-sigh xxx xxx)

### rsa-sign

* 作用：RSA签名，采用特定RSA_SIGNATURE
* 参数数量：3
* 参数作用：data、privateKey、RSA_SIGNATURE
* 参数类型：\[string|list\]、string、string
* 返回值：binary
* 返回类型：list
* 示例：(rsa-sigh xxx xxx "SHA256withRSA")

### rsa-verify

* 作用：RSA验证，默认采用SHA256withRSA
* 参数数量：3
* 参数作用：data、signed、publicKey
* 参数类型：\[string|list\]、\[string|list\]、string
* 返回值：bool
* 返回类型：bool
* 示例：(rsa-verify xxx xxx xxx)

### rsa-verify

* 作用：RSA验证，采用特定RSA_SIGNATURE
* 参数数量：4
* 参数作用：data、signed、publicKey、RSA_SIGNATURE
* 参数类型：\[string|list\]、\[string|list\]、string、string
* 返回值：bool
* 返回类型：bool
* 示例：(rsa-verify xxx xxx xxx "SHA256withRSA")

## ECDSA

### ecdsa-keypair

* 作用：生成ecdsa的公钥和私钥，默认采用secp256r1
* 参数数量：0
* 参数作用：
* 参数类型：
* 返回值：{"public"=\<public\>, "private"=\<private\>}
* 返回类型：table
* 示例：(ecdsa-keypair)

### ecdsa-keypair

* 作用：生成ecdsa的公钥和私钥，采用特定EC_CURVE
* 参数数量：1
* 参数作用：EC_CURVE
* 参数类型：string
* 返回值：{"public"=\<public\>, "private"=\<private\>}
* 返回类型：table
* 示例：(ecdsa-keypair "secp256r1")

### ecdsa-sign

* 作用：ECDSA签名，默认采用SHA256withECDSA
* 参数数量：2
* 参数作用：data、privateKey
* 参数类型：\[string|list\]、string
* 返回值：binary
* 返回类型：list
* 示例：(ecdsa-sigh xxx xxx)

### ecdsa-sign

* 作用：ECDSA签名，采用特定ECDSA_SIGNATURE
* 参数数量：3
* 参数作用：data、privateKey、ECDSA_SIGNATURE
* 参数类型：\[string|list\]、string、string
* 返回值：binary
* 返回类型：list
* 示例：(ecdsa-sigh xxx xxx "SHA256withECDSA")

### ecdsa-verify

* 作用：ECDSA验证，默认采用SHA256withECDSA
* 参数数量：3
* 参数作用：data、signed、publicKey
* 参数类型：\[string|list\]、\[string|list\]、string
* 返回值：bool
* 返回类型：bool
* 示例：(ecdsa-verify xxx xxx xxx)

### ecdsa-verify

* 作用：ECDSA验证，采用特定ECDSA_SIGNATURE
* 参数数量：4
* 参数作用：data、signed、publicKey、ECDSA_SIGNATURE
* 参数类型：\[string|list\]、\[string|list\]、string、string
* 返回值：bool
* 返回类型：bool
* 示例：(ecdsa-verify xxx xxx xxx "SHA256withECDSA")
