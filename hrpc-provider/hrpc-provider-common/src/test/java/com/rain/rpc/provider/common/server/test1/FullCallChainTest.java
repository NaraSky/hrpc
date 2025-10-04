package com.rain.rpc.provider.common.server.test1;

import com.rain.rpc.codec.RpcDecoder;
import com.rain.rpc.codec.RpcEncoder;
import com.rain.rpc.common.helper.RpcServiceHelper;
import com.rain.rpc.common.threadpool.ServerThreadPool;
import com.rain.rpc.constants.RpcConstants;
import com.rain.rpc.protocol.RpcProtocol;
import com.rain.rpc.protocol.enumeration.RpcStatus;
import com.rain.rpc.protocol.enumeration.RpcType;
import com.rain.rpc.protocol.header.RpcHeader;
import com.rain.rpc.protocol.header.RpcHeaderFactory;
import com.rain.rpc.protocol.request.RpcRequest;
import com.rain.rpc.protocol.response.RpcResponse;
import com.rain.rpc.provider.common.server.base.BaseServer;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.reflect.FastClass;
import org.springframework.cglib.reflect.FastMethod;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

/**
 * 完整调用链路测试类
 * 展示从客户端发起请求到服务端处理并返回响应的完整流程
 * 包含完整的Netty通信、编解码、服务端处理等环节
 */
public class FullCallChainTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(FullCallChainTest.class);
    
    /**
     * 测试服务接口
     */
    public interface CalculatorService {
        int add(int a, int b);
        String sayHello(String name);
    }
    
    /**
     * 测试服务实现类
     */
    public static class CalculatorServiceImpl implements CalculatorService {
        @Override
        public int add(int a, int b) {
            LOGGER.info("执行加法运算: {} + {} = {}", a, b, a + b);
            return a + b;
        }
        
        @Override
        public String sayHello(String name) {
            LOGGER.info("执行sayHello方法: Hello, {}!", name);
            return "Hello, " + name + "!";
        }
    }
    
    /**
     * 自定义服务端处理器，用于处理客户端请求
     */
    private static class TestServerHandler extends SimpleChannelInboundHandler<RpcProtocol<RpcRequest>> {
        private final Map<String, Object> handlerMap;
        private final String reflectType;
        
        public TestServerHandler(String reflectType, Map<String, Object> handlerMap) {
            this.reflectType = reflectType;
            this.handlerMap = handlerMap;
        }
        
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, RpcProtocol<RpcRequest> requestProtocol) throws Exception {
            // 使用线程池异步处理请求
            ServerThreadPool.submit(() -> {
                RpcHeader requestHeader = requestProtocol.getHeader();
                requestHeader.setMessageType((byte) RpcType.RESPONSE.getType());
                
                RpcRequest request = requestProtocol.getBody();
                RpcProtocol<RpcResponse> responseProtocol = new RpcProtocol<>();
                RpcResponse response = new RpcResponse();
                
                try {
                    // 处理请求
                    Object result = handle(request);
                    response.setResult(result);
                    response.setAsync(request.getAsync());
                    response.setOneway(request.getOneway());
                    requestHeader.setStatus((byte) RpcStatus.SUCCESS.getCode());
                    
                    LOGGER.info("方法 {} 执行成功，结果: {}", request.getMethodName(), result);
                } catch (Throwable t) {
                    response.setError(t.toString());
                    requestHeader.setStatus((byte) RpcStatus.FAIL.getCode());
                    LOGGER.error("执行方法 {} 时发生异常", request.getMethodName(), t);
                }
                
                responseProtocol.setHeader(requestHeader);
                responseProtocol.setBody(response);
                
                ctx.writeAndFlush(responseProtocol).addListener(ChannelFutureListener.CLOSE);
            });
        }
        
        private Object handle(RpcRequest request) throws Throwable {
            String serviceKey = RpcServiceHelper.buildServiceKey(
                request.getClassName(), request.getVersion(), request.getGroup());
            
            Object serviceInstance = handlerMap.get(serviceKey);
            if (serviceInstance == null) {
                String errorMsg = String.format("服务不存在: %s:%s", 
                    request.getClassName(), request.getMethodName());
                LOGGER.error(errorMsg);
                throw new RuntimeException(errorMsg);
            }
            
            Class<?> serviceClass = serviceInstance.getClass();
            String methodName = request.getMethodName();
            Class<?>[] parameterTypes = request.getParameterTypes();
            Object[] parameters = request.getParameters();
            
            return invokeMethod(serviceInstance, serviceClass, methodName, parameterTypes, parameters);
        }
        
        private Object invokeMethod(Object serviceInstance, Class<?> serviceClass, String methodName, 
                                  Class<?>[] parameterTypes, Object[] parameters) throws Throwable {
            switch (this.reflectType) {
                case RpcConstants.REFLECT_TYPE_JDK -> {
                    return this.invokeJDKMethod(serviceInstance, serviceClass, methodName, parameterTypes, parameters);
                }
                case RpcConstants.REFLECT_TYPE_CGLIB -> {
                    return this.invokeCGLIBMethod(serviceInstance, serviceClass, methodName, parameterTypes, parameters);
                }
                default -> throw new RuntimeException("不支持的反射类型: " + this.reflectType);
            }
        }
        
        private Object invokeCGLIBMethod(Object serviceInstance, Class<?> serviceClass, String methodName, 
                                       Class<?>[] parameterTypes, Object[] parameters) throws Throwable {
            LOGGER.info("使用CGLIB方式调用方法: {}", methodName);
            FastClass serviceFastClass = FastClass.create(serviceClass);
            FastMethod serviceFastMethod = serviceFastClass.getMethod(methodName, parameterTypes);
            return serviceFastMethod.invoke(serviceInstance, parameters);
        }
        
        private Object invokeJDKMethod(Object serviceInstance, Class<?> serviceClass, String methodName, 
                                     Class<?>[] parameterTypes, Object[] parameters) throws Throwable {
            LOGGER.info("使用JDK反射方式调用方法: {}", methodName);
            Method method = serviceClass.getMethod(methodName, parameterTypes);
            method.setAccessible(true);
            return method.invoke(serviceInstance, parameters);
        }
        
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            LOGGER.error("服务端处理器发生异常", cause);
            ctx.close();
        }
    }
    
    /**
     * 自定义客户端处理器，用于处理服务端响应
     */
    private static class TestClientHandler extends SimpleChannelInboundHandler<RpcProtocol<RpcResponse>> {
        private final CompletableFuture<RpcResponse> responseFuture;
        
        public TestClientHandler(CompletableFuture<RpcResponse> responseFuture) {
            this.responseFuture = responseFuture;
        }
        
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, RpcProtocol<RpcResponse> responseProtocol) {
            LOGGER.info("客户端接收到服务端响应: {}", responseProtocol);
            responseFuture.complete(responseProtocol.getBody());
        }
        
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            LOGGER.error("客户端处理器发生异常", cause);
            responseFuture.completeExceptionally(cause);
            ctx.close();
        }
    }
    
    /**
     * 测试完整调用链路
     * 展示从客户端发起请求到服务端处理并返回响应的完整流程
     * 
     * @throws Exception 测试过程中可能抛出的异常
     */
    @Test
    public void testFullCallChain() throws Exception {
        LOGGER.info("开始测试完整调用链路");
        
        // 1. 启动服务端
        int port = 18081;
        Map<String, Object> handlerMap = new HashMap<>();
        CalculatorServiceImpl calculatorService = new CalculatorServiceImpl();
        
        String serviceKey = RpcServiceHelper.buildServiceKey(
            CalculatorService.class.getName(), "1.0.0", "default");
        handlerMap.put(serviceKey, calculatorService);
        
        CountDownLatch serverStartLatch = new CountDownLatch(1);
        Thread serverThread = new Thread(() -> {
            try {
                startServer(port, handlerMap, serverStartLatch);
            } catch (Exception e) {
                LOGGER.error("服务端启动失败", e);
            }
        });
        serverThread.setDaemon(false);
        serverThread.start();
        
        // 等待服务端启动完成
        serverStartLatch.await();
        LOGGER.info("服务端启动完成，监听端口: {}", port);
        
        try {
            // 2. 启动客户端并发送请求
            testClientCall(port);
        } finally {
            // 3. 关闭资源
            ServerThreadPool.shutdown();
        }
        
        LOGGER.info("完整调用链路测试完成");
    }
    
    /**
     * 启动服务端
     * 
     * @param port 服务端监听端口
     * @param handlerMap 服务处理器映射
     * @param startLatch 启动完成信号
     * @throws InterruptedException 线程中断异常
     */
    private void startServer(int port, Map<String, Object> handlerMap, CountDownLatch startLatch) throws InterruptedException {
        LOGGER.info("开始启动服务端，监听端口: {}", port);
        
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup(4);
        
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline()
                                    .addLast(new RpcDecoder())     // 添加解码器
                                    .addLast(new RpcEncoder())     // 添加编码器
                                    .addLast(new TestServerHandler("cglib", handlerMap)); // 添加服务处理器
                        }
                    });
            
            ChannelFuture future = bootstrap.bind(port).sync();
            startLatch.countDown(); // 通知服务端启动完成
            LOGGER.info("服务端启动成功，等待客户端连接...");
            
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            LOGGER.error("服务端运行中断", e);
            throw e;
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
    
    /**
     * 客户端调用测试
     * 
     * @param port 服务端端口
     * @throws Exception 测试过程中可能抛出的异常
     */
    private void testClientCall(int port) throws Exception {
        LOGGER.info("开始客户端调用测试");
        
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            // 准备响应Future
            final CompletableFuture<RpcResponse> responseFuture = new CompletableFuture<>();
            
            // 配置客户端
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline()
                                    .addLast(new RpcDecoder())  // 添加解码器
                                    .addLast(new RpcEncoder())  // 添加编码器
                                    .addLast(new TestClientHandler(responseFuture)); // 添加客户端处理器
                        }
                    });
            
            // 连接到服务端
            ChannelFuture future = bootstrap.connect("127.0.0.1", port).sync();
            LOGGER.info("客户端连接服务端成功 {}:{}", "127.0.0.1", port);
            
            // 发送加法请求
            sendAddRequest(future, responseFuture);
            
            // 发送sayHello请求
            final CompletableFuture<RpcResponse> sayHelloResponseFuture = new CompletableFuture<>();
            sendSayHelloRequest(future, sayHelloResponseFuture);
            
            // 等待一段时间观察结果
            Thread.sleep(2000);
        } finally {
            group.shutdownGracefully();
        }
    }
    
    /**
     * 发送加法请求
     * 
     * @param future 连接Future
     * @param responseFuture 响应Future
     * @throws Exception 异常
     */
    private void sendAddRequest(ChannelFuture future, CompletableFuture<RpcResponse> responseFuture) throws Exception {
        LOGGER.info("发送加法请求");
        
        // 构造RPC请求协议
        RpcProtocol<RpcRequest> requestProtocol = new RpcProtocol<>();
        requestProtocol.setHeader(RpcHeaderFactory.getRequestHeader("jdk"));
        
        // 构造请求体
        RpcRequest request = new RpcRequest();
        request.setClassName(CalculatorService.class.getName());
        request.setMethodName("add");
        request.setParameterTypes(new Class[]{int.class, int.class});
        request.setParameters(new Object[]{10, 20});
        request.setVersion("1.0.0");
        request.setGroup("default");
        requestProtocol.setBody(request);
        
        LOGGER.info("发送请求: {}", request);
        
        // 发送请求
        future.channel().writeAndFlush(requestProtocol);
        
        // 等待并处理响应
        RpcResponse response = responseFuture.get();
        LOGGER.info("接收到加法响应结果: {}", response.getResult());
        
        if (response.getResult() != null && response.getResult().equals(30)) {
            LOGGER.info("加法调用测试成功");
        } else {
            LOGGER.error("加法调用测试失败，期望结果不匹配");
        }
    }
    
    /**
     * 发送sayHello请求
     * 
     * @param future 连接Future
     * @param responseFuture 响应Future
     * @throws Exception 异常
     */
    private void sendSayHelloRequest(ChannelFuture future, CompletableFuture<RpcResponse> responseFuture) throws Exception {
        LOGGER.info("发送sayHello请求");
        
        // 构造RPC请求协议
        RpcProtocol<RpcRequest> requestProtocol = new RpcProtocol<>();
        requestProtocol.setHeader(RpcHeaderFactory.getRequestHeader("jdk"));
        
        // 构造请求体
        RpcRequest request = new RpcRequest();
        request.setClassName(CalculatorService.class.getName());
        request.setMethodName("sayHello");
        request.setParameterTypes(new Class[]{String.class});
        request.setParameters(new Object[]{"张三"});
        request.setVersion("1.0.0");
        request.setGroup("default");
        requestProtocol.setBody(request);
        
        LOGGER.info("发送请求: {}", request);
        
        // 发送请求
        future.channel().writeAndFlush(requestProtocol);
        
        // 等待并处理响应
        RpcResponse response = responseFuture.get();
        LOGGER.info("接收到sayHello响应结果: {}", response.getResult());
        
        if (response.getResult() != null && response.getResult().equals("Hello, 张三!")) {
            LOGGER.info("sayHello调用测试成功");
        } else {
            LOGGER.error("sayHello调用测试失败，期望结果不匹配");
        }
    }
}