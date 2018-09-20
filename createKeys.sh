#!/usr/bin/env bash

# 生成netty服务端证书
keytool -genkey -alias server -keysize 2048 -validity 365 -keyalg RSA -dname "CN=localhost" -keypass server -storepass server -keystore server.jks

# 生成netty客户端证书
keytool -genkey -alias client1 -keysize 2048 -validity 365 -keyalg RSA -dname "CN=localhost" -keypass client1 -storepass client1 -keystore client1.jks
keytool -genkey -alias client2 -keysize 2048 -validity 365 -keyalg RSA -dname "CN=localhost" -keypass client2 -storepass client2 -keystore client2.jks
keytool -genkey -alias client3 -keysize 2048 -validity 365 -keyalg RSA -dname "CN=localhost" -keypass client3 -storepass client3 -keystore client3.jks

# 生成自签名
keytool -export -alias server -keystore server.jks -storepass server -file server.cer
keytool -export -alias client1 -keystore client1.jks -storepass client1 -file client1.cer
keytool -export -alias client2 -keystore client2.jks -storepass client2 -file client2.cer
keytool -export -alias client3 -keystore client3.jks -storepass client3 -file client3.cer

# 将服务端证书导入到客户端证书仓库中
keytool -import -trustcacerts -alias server -file server.cer -storepass client1 -keystore client1.jks
keytool -import -trustcacerts -alias server -file server.cer -storepass client2 -keystore client2.jks
keytool -import -trustcacerts -alias server -file server.cer -storepass client3 -keystore client3.jks

keytool -import -trustcacerts -alias client1 -file client1.cer -storepass server -keystore server.jks
keytool -import -trustcacerts -alias client1 -file client1.cer -storepass client2 -keystore client2.jks
keytool -import -trustcacerts -alias client1 -file client1.cer -storepass client3 -keystore client3.jks

keytool -import -trustcacerts -alias client2 -file client2.cer -storepass server -keystore server.jks
keytool -import -trustcacerts -alias client2 -file client2.cer -storepass client1 -keystore client1.jks
keytool -import -trustcacerts -alias client2 -file client2.cer -storepass client3 -keystore client3.jks

keytool -import -trustcacerts -alias client3 -file client3.cer -storepass server -keystore server.jks
keytool -import -trustcacerts -alias client3 -file client3.cer -storepass client1 -keystore client1.jks
keytool -import -trustcacerts -alias client3 -file client3.cer -storepass client2 -keystore client2.jks

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
# 注意: CN=localhost请改为正确域名