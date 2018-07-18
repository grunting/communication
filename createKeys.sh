
# 生成netty服务端证书
keytool -genkey -alias securechat -keysize 2048 -validity 365 -keyalg RSA -dname "CN=localhost" -keypass sNetty -storepass sNetty -keystore sChat.jks

# 生成netty客户端证书
keytool -genkey -alias smcc -keysize 2048 -validity 365 -keyalg RSA -dname "CN=localhost" -keypass cNetty -storepass cNetty -keystore cChat.jks

# 生成服务端自签名
keytool -export -alias securechat -keystore sChat.jks -storepass sNetty -file sChat.cer

# 生成客户端自签名
keytool -export -alias smcc -keystore cChat.jks -storepass cNetty -file cChat.cer

# 将服务端证书导入到客户端证书仓库中
keytool -import -trustcacerts -alias securechat -file sChat.cer -storepass cNetty -keystore cChat.jks

# 将客户端证书导入到服务端证书仓库中
keytool -import -trustcacerts -alias smcc -file cChat.cer -storepass sNetty -keystore sChat.jks

# 查看证书
keytool -list -rfc -keystore sChat.jks

# 参数解释
# -keysize 2048 密钥长度2048位（这个长度的密钥目前可认为无法被暴力破解）
# -validity 365 证书有效期365天
# -keyalg RSA 使用RSA非对称加密算法
# -dname "CN=localhost" 设置Common Name为gornix.com，这是我的域名
# -keypass sNetty 密钥的访问密码为sNetty
# -storepass sNetty 密钥库的访问密码为sNetty（通常都设置一样，方便记）
# -keystore sChat.jks 指定生成的密钥库文件为gornix.jks