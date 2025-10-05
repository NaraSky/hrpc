package com.rain.rpc.proxy.api.object;

import com.rain.rpc.protocol.RpcProtocol;
import com.rain.rpc.protocol.header.RpcHeaderFactory;
import com.rain.rpc.protocol.request.RpcRequest;
import com.rain.rpc.proxy.api.async.IAsyncObjectProxy;
import com.rain.rpc.proxy.api.consumer.Consumer;
import com.rain.rpc.proxy.api.future.RPCFuture;
import com.rain.rpc.registry.api.RegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * 对象代理类
 * 实现InvocationHandler接口，处理代理对象的方法调用
 * 同时实现IAsyncObjectProxy接口，支持异步调用
 * 
 * 设计说明：
 * 1. 实现InvocationHandler接口，处理同步方法调用
 * 2. 实现IAsyncObjectProxy接口，处理异步方法调用
 * 3. 封装RPC调用的通用逻辑
 */
public class ObjectProxy<T> implements IAsyncObjectProxy, InvocationHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectProxy.class);

    /**
     * 代理的类
     */
    private Class<?> clazz;
    /**
     * 服务版本
     */
    private String serviceVersion;
    /**
     * 服务分组
     */
    private String serviceGroup;
    /**
     * 超时时间（毫秒）
     */
    private long timeout = 15000;
    /**
     * 注册服务
     */
    private RegistryService registryService;
    /**
     * 消费者实例，用于发送请求
     */
    private Consumer consumer;
    /**
     * 序列化类型
     */
    private String serializationType;
    /**
     * 是否异步调用
     */
    private boolean async;
    /**
     * 是否单向调用（不需要响应）
     */
    private boolean oneWay;

    public ObjectProxy(Class<T> clazz) {
        this.clazz = clazz;
    }

    /**
     * 构造函数
     * 
     * @param clazz 代理的类
     * @param serviceVersion 服务版本
     * @param serviceGroup 服务分组
     * @param serializationType 序列化类型
     * @param timeout 超时时间
     * @param registryService 注册服务
     * @param consumer 消费者
     * @param async 是否异步调用
     * @param oneWay 是否单向调用
     */
    public ObjectProxy(
            Class<T> clazz,
            String serviceVersion,
            String serviceGroup,
            String serializationType,
            long timeout,
            RegistryService registryService,
            Consumer consumer,
            boolean async,
            boolean oneWay) {
        this.clazz = clazz;
        this.serviceVersion = serviceVersion;
        this.timeout = timeout;
        this.serviceGroup = serviceGroup;
        this.consumer = consumer;
        this.serializationType = serializationType;
        this.async = async;
        this.oneWay = oneWay;
        this.registryService = registryService;
    }


    /**
     * 处理代理对象的方法调用
     * 
     * @param proxy 代理对象
     * @param method 调用的方法
     * @param args 方法参数
     * @return 方法调用结果
     * @throws Throwable 异常
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 处理Object类的方法
        if (Object.class == method.getDeclaringClass()) {
            String name = method.getName();
            if ("equals".equals(name)) {
                return proxy == args[0];
            } else if ("hashCode".equals(name)) {
                return System.identityHashCode(proxy);
            } else if ("toString".equals(name)) {
                return proxy.getClass().getName() + "@" +
                        Integer.toHexString(System.identityHashCode(proxy)) +
                        ", with InvocationHandler " + this;
            } else {
                throw new IllegalStateException(String.valueOf(method));
            }
        }
        
        // 构建RPC请求协议
        RpcProtocol<RpcRequest> requestRpcProtocol = new RpcProtocol<RpcRequest>();
        requestRpcProtocol.setHeader(RpcHeaderFactory.getRequestHeader(serializationType));

        // 构建请求体
        RpcRequest request = new RpcRequest();
        request.setVersion(this.serviceVersion);
        request.setClassName(method.getDeclaringClass().getName());
        request.setMethodName(method.getName());
        request.setParameterTypes(method.getParameterTypes());
        request.setGroup(this.serviceGroup);
        request.setParameters(args);
        request.setAsync(async);
        request.setOneway(oneWay);
        requestRpcProtocol.setBody(request);

        // 记录调试日志
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Invoking method: {}#{}", method.getDeclaringClass().getName(), method.getName());

            if (method.getParameterTypes() != null && method.getParameterTypes().length > 0) {
                for (int i = 0; i < method.getParameterTypes().length; ++i) {
                    LOGGER.debug("Parameter type {}: {}", i, method.getParameterTypes()[i].getName());
                }
            }

            if (args != null && args.length > 0) {
                for (int i = 0; i < args.length; ++i) {
                    LOGGER.debug("Parameter value {}: {}", i, args[i]);
                }
            }
        }

        // 发送请求并获取结果
        RPCFuture rpcFuture = this.consumer.sendRequest(requestRpcProtocol, registryService);
        return rpcFuture == null ? null : timeout > 0 ? rpcFuture.get(timeout, TimeUnit.MILLISECONDS) : rpcFuture.get();
    }

    /**
     * 异步调用方法
     * 
     * @param funcName 方法名称
     * @param args 方法参数
     * @return RPCFuture对象
     */
    @Override
    public RPCFuture call(String funcName, Object... args) {
        RpcProtocol<RpcRequest> request = createRequest(this.clazz.getName(), funcName, args);
        RPCFuture rpcFuture = null;
        try {
            rpcFuture = this.consumer.sendRequest(request, registryService);
        } catch (Exception e) {
            LOGGER.error("Async call method {} throws exception", funcName, e);
        }
        return rpcFuture;
    }

    /**
     * 创建RPC请求
     * 
     * @param className 类名
     * @param methodName 方法名
     * @param args 参数
     * @return RpcProtocol<RpcRequest> 请求协议
     */
    private RpcProtocol<RpcRequest> createRequest(String className, String methodName, Object[] args) {

        RpcProtocol<RpcRequest> requestRpcProtocol = new RpcProtocol<RpcRequest>();

        requestRpcProtocol.setHeader(RpcHeaderFactory.getRequestHeader(serializationType));

        RpcRequest request = new RpcRequest();
        request.setClassName(className);
        request.setMethodName(methodName);
        request.setParameters(args);
        request.setVersion(this.serviceVersion);
        request.setGroup(this.serviceGroup);

        Class[] parameterTypes = new Class[args.length];
        // 获取正确的类类型
        for (int i = 0; i < args.length; i++) {
            parameterTypes[i] = getClassType(args[i]);
        }
        request.setParameterTypes(parameterTypes);
        requestRpcProtocol.setBody(request);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Creating request for method: {}#{}", className, methodName);
            for (int i = 0; i < parameterTypes.length; ++i) {
                LOGGER.debug("Parameter type {}: {}", i, parameterTypes[i].getName());
            }
            for (int i = 0; i < args.length; ++i) {
                LOGGER.debug("Parameter value {}: {}", i, args[i]);
            }
        }

        return requestRpcProtocol;
    }

    /**
     * 获取对象的类类型
     * 处理基本数据类型的包装类转换
     * 
     * @param obj 对象
     * @return 类类型
     */
    private Class<?> getClassType(Object obj) {
        Class<?> classType = obj.getClass();
        String typeName = classType.getName();
        switch (typeName) {
            case "java.lang.Integer":
                return Integer.TYPE;
            case "java.lang.Long":
                return Long.TYPE;
            case "java.lang.Float":
                return Float.TYPE;
            case "java.lang.Double":
                return Double.TYPE;
            case "java.lang.Character":
                return Character.TYPE;
            case "java.lang.Boolean":
                return Boolean.TYPE;
            case "java.lang.Short":
                return Short.TYPE;
            case "java.lang.Byte":
                return Byte.TYPE;
        }
        return classType;
    }
}
