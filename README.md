# HRPC - 高性能RPC框架

HRPC是一个基于Java开发的轻量级RPC框架，采用Netty作为通信层，支持多种序列化方式和动态代理技术。

## 项目结构

```
hrpc
├── hrpc-annotation      # 注解模块，包含服务提供者和消费者注解
├── hrpc-codec           # 编解码模块，处理RPC协议的编码和解码
├── hrpc-common          # 公共模块，包含工具类、异常类、扫描器等
├── hrpc-constants       # 常量模块，定义RPC框架中的各种常量
├── hrpc-protocol        # 协议模块，定义RPC通信协议和消息格式
├── hrpc-provider        # 服务提供者模块
│   ├── hrpc-provider-common  # 服务提供者公共实现
│   └── hrpc-provider-native  # 原生服务提供者实现
└── hrpc-serialization   # 序列化模块
    ├── hrpc-serialization-api    # 序列化接口定义
    └── hrpc-serialization-jdk    # JDK原生序列化实现
```

## 核心特性

### 1. 多种序列化支持
- JDK原生序列化
- 后续可扩展支持Protostuff、Kryo、Hessian等高性能序列化方式

### 2. 多种动态代理支持
- JDK动态代理
- Cglib动态代理
- Javassist动态代理

### 3. 自定义RPC协议
协议格式：
```
+---------------------------------------------------------------+
| 魔数 2byte | 报文类型 1byte | 状态 1byte |     消息 ID 8byte      |
+---------------------------------------------------------------+
|           序列化类型 16byte      |        数据长度 4byte          |
+---------------------------------------------------------------+
|                             数据                              |
+---------------------------------------------------------------+
```

### 4. 注解驱动
- `@RpcService`：标记服务提供者
- `@RpcReference`：标记服务消费者

### 5. Netty通信
基于Netty的高性能NIO通信框架，支持高并发连接

## 模块详解

### hrpc-annotation
定义了核心注解：
- `@RpcService`：用于标记服务提供者实现类
- `@RpcReference`：用于标记服务消费者引用字段

### hrpc-codec
包含RPC协议的编解码器：
- `RpcEncoder`：将RPC消息编码为字节流
- `RpcDecoder`：将字节流解码为RPC消息

### hrpc-common
公共工具模块：
- 类扫描器：扫描指定包下的类
- 序列化工具：处理序列化相关操作
- 线程池：统一的线程池管理
- ID生成器：生成唯一请求ID

### hrpc-constants
定义RPC框架中使用的常量：
- 协议头长度
- 魔数
- 版本号
- 序列化类型常量
- 注册中心类型常量

### hrpc-protocol
定义RPC协议相关类：
- `RpcProtocol`：RPC协议封装类
- `RpcHeader`：协议头
- `RpcRequest`：RPC请求
- `RpcResponse`：RPC响应
- `RpcType`：消息类型枚举
- `RpcStatus`：状态码枚举

### hrpc-provider
服务提供者实现：
- `BaseServer`：基础服务端实现
- `RpcProviderHandler`：服务端消息处理器
- `RpcSingleServer`：单机版服务端

### hrpc-serialization
序列化模块：
- `Serialization`：序列化接口
- `JdkSerialization`：JDK原生序列化实现

## 使用示例

### 1. 定义服务接口
```java
public interface HelloService {
    String sayHello(String name);
}
```

### 2. 实现服务提供者
```java
@RpcService(interfaceClass = HelloService.class, version = "1.0.0", group = "default")
public class HelloServiceImpl implements HelloService {
    @Override
    public String sayHello(String name) {
        return "Hello, " + name;
    }
}
```

### 3. 启动服务提供者
```java
public class Server {
    public static void main(String[] args) {
        BaseServer server = new BaseServer("127.0.0.1:27880", "jdk");
        // 注册服务...
        server.startNettyServer();
    }
}
```

### 4. 服务消费者引用
```java
public class Client {
    @RpcReference(version = "1.0.0", group = "default")
    private HelloService helloService;
    
    public void callRemoteService() {
        String result = helloService.sayHello("World");
        System.out.println(result);
    }
}
```

## 数据流转图

![数据流转图](data-flow.png)

## 核心类交互图

![核心类交互图](core-classes-interaction.png)

## 后续计划

1. 实现服务注册与发现（Zookeeper、Nacos等）
2. 添加更多序列化方式支持
3. 实现负载均衡策略
4. 添加服务监控和管理功能
5. 提供Spring Boot Starter简化使用

## 许可证

MIT License