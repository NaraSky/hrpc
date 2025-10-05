package com.rain.rpc.consumer;

import com.rain.rpc.common.exception.RegistryException;
import com.rain.rpc.consumer.common.RpcConsumer;
import com.rain.rpc.proxy.api.ProxyFactory;
import com.rain.rpc.proxy.api.async.IAsyncObjectProxy;
import com.rain.rpc.proxy.api.config.ProxyConfig;
import com.rain.rpc.proxy.api.object.ObjectProxy;
import com.rain.rpc.proxy.jdk.JdkProxyFactory;
import com.rain.rpc.registry.api.RegistryService;
import com.rain.rpc.registry.api.config.RegistryConfig;
import com.rain.rpc.registry.zookeeper.ZookeeperRegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * RPC客户端
 * 提供创建代理对象和管理连接的统一入口
 * <p>
 * 设计说明：
 * 1. 封装代理对象的创建过程
 * 2. 提供同步和异步两种代理创建方式
 * 3. 管理RPC消费者的生命周期
 */
public class RpcClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcClient.class);

    /**
     * 注册服务
     */
    private RegistryService registryService;
    /**
     * 服务版本
     */
    private String serviceVersion;
    /**
     * 服务分组
     */
    private String serviceGroup;
    /**
     * 序列化类型
     */
    private String serializationType;
    /**
     * 超时时间（毫秒）
     */
    private long timeout;

    /**
     * 是否异步调用
     */
    private boolean async;

    /**
     * 是否单向调用
     */
    private boolean oneway;

    /**
     * 构造函数
     * 
     * @param registryAddress 注册中心地址
     * @param registryType 注册中心类型
     * @param serviceVersion 服务版本
     * @param serviceGroup 服务分组
     * @param serializationType 序列化类型
     * @param timeout 超时时间（毫秒）
     * @param async 是否异步调用
     * @param oneway 是否单向调用
     */
    public RpcClient(String registryAddress, String registryType, String serviceVersion, String serviceGroup, String serializationType, long timeout, boolean async, boolean oneway) {
        this.serviceVersion = serviceVersion;
        this.timeout = timeout;
        this.serviceGroup = serviceGroup;
        this.serializationType = serializationType;
        this.async = async;
        this.oneway = oneway;
        this.registryService = this.getRegistryService(registryAddress, registryType);
        LOGGER.info("RpcClient initialized with registryType: {}, serviceVersion: {}, serviceGroup: {}", registryType, serviceVersion, serviceGroup);
    }

    private RegistryService getRegistryService(String registryAddress, String registryType) {
        if (StringUtils.isEmpty(registryType)) {
            throw new IllegalArgumentException("registry type is null");
        }
        //TODO 后续SPI扩展
        RegistryService registryService = new ZookeeperRegistryService();
        try {
            registryService.init(new RegistryConfig(registryAddress, registryType));
            LOGGER.info("Registry service initialized successfully with address: {} and type: {}", registryAddress, registryType);
        } catch (Exception e) {
            LOGGER.error("RpcClient init registry service throws exception, address: {}, type: {}", registryAddress, registryType, e);
            throw new RegistryException(e.getMessage(), e);
        }
        return registryService;
    }

    /**
     * 创建同步代理对象
     * 
     * @param interfaceClass 接口类
     * @param <T> 泛型类型
     * @return 代理对象
     */
    public <T> T create(Class<T> interfaceClass) {
        ProxyFactory proxyFactory = new JdkProxyFactory<T>();
        proxyFactory.init(new ProxyConfig(interfaceClass, serviceVersion, serviceGroup, serializationType, timeout, registryService, RpcConsumer.getInstance(), async, oneway));
        LOGGER.debug("Creating sync proxy for interface: {}", interfaceClass.getName());
        return proxyFactory.getProxy(interfaceClass);
    }

    /**
     * 创建异步代理对象
     * 
     * @param interfaceClass 接口类
     * @param <T> 泛型类型
     * @return 异步代理对象
     */
    public <T> IAsyncObjectProxy createAsync(Class<T> interfaceClass) {
        LOGGER.debug("Creating async proxy for interface: {}", interfaceClass.getName());
        return new ObjectProxy<T>(interfaceClass, serviceVersion, serviceGroup, serializationType, timeout, registryService, RpcConsumer.getInstance(), async, oneway);
    }

    /**
     * 关闭RPC客户端，释放资源
     */
    public void shutdown() {
        LOGGER.info("Shutting down RpcClient");
        RpcConsumer.getInstance().close();
    }
}
