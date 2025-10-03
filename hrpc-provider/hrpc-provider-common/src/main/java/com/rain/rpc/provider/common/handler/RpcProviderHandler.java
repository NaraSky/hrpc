package com.rain.rpc.provider.common.handler;

import com.rain.rpc.common.helper.RpcServiceHelper;
import com.rain.rpc.common.threadpool.ServerThreadPool;
import com.rain.rpc.protocol.RpcProtocol;
import com.rain.rpc.protocol.enumeration.RpcStatus;
import com.rain.rpc.protocol.enumeration.RpcType;
import com.rain.rpc.protocol.header.RpcHeader;
import com.rain.rpc.protocol.request.RpcRequest;
import com.rain.rpc.protocol.response.RpcResponse;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * RPC服务提供者处理器
 * 作为Netty管道中的一个处理节点，负责接收客户端请求并调用对应的服务实现
 * 继承SimpleChannelInboundHandler以处理RpcProtocol<RpcRequest>类型的消息
 */
public class RpcProviderHandler extends SimpleChannelInboundHandler<RpcProtocol<RpcRequest>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RpcProviderHandler.class);

    /**
     * 服务处理器映射表
     * key为服务名称，value为服务实例
     */
    private final Map<String, Object> handlerMap;

    public RpcProviderHandler(Map<String, Object> handlerMap) {
        this.handlerMap = handlerMap;
    }

    /**
     * 处理接收到的RPC请求
     * 当Netty管道中接收到一个完整的RpcProtocol<RpcRequest>消息时，该方法会被调用
     * 使用线程池处理请求，避免阻塞IO线程
     * 
     * @param ctx             ChannelHandlerContext上下文
     * @param requestProtocol 接收到的RPC请求协议对象
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcProtocol<RpcRequest> requestProtocol) throws Exception {
        // 使用线程池异步处理请求，避免阻塞Netty的IO线程
        // IO线程需要快速处理网络事件，业务逻辑应放在其他线程中处理
        ServerThreadPool.submit(() -> {
            // 获取请求协议的头部信息，后续需要修改头部信息作为响应返回
            RpcHeader requestHeader = requestProtocol.getHeader();
            // 设置消息类型为响应类型，表明这是对请求的响应
            requestHeader.setMessageType((byte) RpcType.RESPONSE.getType());
            // 获取请求体，包含具体的方法调用信息
            RpcRequest request = requestProtocol.getBody();
            
            // 创建响应协议对象
            RpcProtocol<RpcResponse> responseProtocol = new RpcProtocol<>();
            // 创建响应体
            RpcResponse response = new RpcResponse();
            
            try {
                // 调用handle方法处理请求，获取执行结果
                Object result = handle(request);
                // 设置响应结果
                response.setResult(result);
                // 保留请求中的异步标记
                response.setAsync(request.getAsync());
                // 保留请求中的单向调用标记
                response.setOneway(request.getOneway());
                // 设置响应状态为成功
                requestHeader.setStatus((byte) RpcStatus.SUCCESS.getCode());
                
                LOGGER.info("RPC method {} executed successfully", request.getMethodName());
            } catch (Throwable t) {
                // 捕获处理过程中的异常，设置错误信息
                response.setError(t.toString());
                // 设置响应状态为失败
                requestHeader.setStatus((byte) RpcStatus.FAIL.getCode());
                
                LOGGER.error("Error occurred while executing RPC method: {}", request.getMethodName(), t);
            }
            
            // 设置响应协议的头部和体
            responseProtocol.setHeader(requestHeader);
            responseProtocol.setBody(response);
            
            // 将响应写回客户端并刷新
            ctx.writeAndFlush(responseProtocol).addListener(ChannelFutureListener.CLOSE);
        });
    }

    /**
     * 处理RPC请求，根据请求信息调用对应的服务方法
     * 
     * @param request RPC请求对象
     * @return 方法调用结果
     * @throws Throwable 方法调用过程中可能抛出的异常
     */
    private Object handle(RpcRequest request) throws Throwable {
        // 根据类名、版本号和服务组构建服务唯一标识
        // 通过服务标识可以在handlerMap中快速查找对应的服务实例
        String serviceKey = RpcServiceHelper.buildServiceKey(request.getClassName(), request.getVersion(), request.getGroup());
        
        // 根据服务标识从handlerMap中获取服务实例
        Object serviceInstance = handlerMap.get(serviceKey);
        
        // 如果找不到对应的服务实例，则抛出异常
        if (serviceInstance == null) {
            String errorMsg = String.format("Service not exist: %s:%s", request.getClassName(), request.getMethodName());
            LOGGER.error(errorMsg);
            throw new RuntimeException(errorMsg);
        }

        // 获取服务实例的类信息
        Class<?> serviceClass = serviceInstance.getClass();
        // 获取要调用的方法名
        String methodName = request.getMethodName();
        // 获取方法参数类型数组
        Class<?>[] parameterTypes = request.getParameterTypes();
        // 获取方法参数值数组
        Object[] parameters = request.getParameters();

        // 记录调试信息
        if (parameterTypes != null && parameterTypes.length > 0) {
            LOGGER.debug("Method {} has {} parameter types:", methodName, parameterTypes.length);
            for (int i = 0; i < parameterTypes.length; ++i) {
                LOGGER.debug("Parameter {}: {}", i, parameterTypes[i].getName());
            }
        }

        // 记录调试信息
        if (parameters != null && parameters.length > 0) {
            LOGGER.debug("Method {} has {} parameters:", methodName, parameters.length);
            for (int i = 0; i < parameters.length; ++i) {
                LOGGER.debug("Parameter {}: {}", i, parameters[i]);
            }
        }
        
        // 调用具体的方法并返回结果
        return invokeMethod(serviceInstance, serviceClass, methodName, parameterTypes, parameters);
    }

    /**
     * 通过反射调用具体的服务方法
     * 目前使用JDK动态代理方式
     * todo 未来扩展其他代理方式
     * 
     * @param serviceInstance 服务实例
     * @param serviceClass    服务类
     * @param methodName      方法名
     * @param parameterTypes  参数类型数组
     * @param parameters      参数值数组
     * @return 方法调用结果
     * @throws Throwable 方法调用过程中可能抛出的异常
     */
    private Object invokeMethod(Object serviceInstance, Class<?> serviceClass, String methodName, Class<?>[] parameterTypes, Object[] parameters) throws Throwable {
        // 通过反射获取要调用的方法
        Method method = serviceClass.getMethod(methodName, parameterTypes);
        // 设置方法可访问，可以调用私有方法
        method.setAccessible(true);
        // 执行方法调用并返回结果
        return method.invoke(serviceInstance, parameters);
    }

    /**
     * 异常处理方法
     * 当处理请求过程中发生异常时，记录错误日志并关闭连接
     *
     * @param ctx   ChannelHandlerContext上下文
     * @param cause 异常对象
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOGGER.error("Error occurred in RPC provider handler", cause);
        // 关闭连接以释放资源
        ctx.close();
    }
}