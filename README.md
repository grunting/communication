# communication

# 通信的工程

目标在于通过无状态服务器，实现终端在公网内的信息通信

## 模块
 - basic：实现基础加密、模型、接口类等
 - passageways：实现终端与服务端基层
 - service：为终端与服务端实现具体服务
 - 其他为测试内容，后续会删除

## 启动

- 包括服务端
修改配置文件（passageways.properties）
启动server主类（cn.gp.banana.Server.java），以启动服务端
- 包括客户端
修改配置文件（passageways.properties）
启动client主类（cn.gp.banana.Client.java），以启动客户端