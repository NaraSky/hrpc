package com.rain.rpc.registry.api;

import com.rain.rpc.protocol.meta.ServiceMeta;
import com.rain.rpc.registry.api.config.RegistryConfig;

/**
 * RPC注册服务接口
 * 定义了服务注册与发现的核心方法，用于服务的注册、注销和发现
 * 支持多种注册中心实现，如Zookeeper、Nacos等
 */
public interface RegistryService {

    /**
     * 注册服务元数据到注册中心
     * 
     * @param serviceMeta 服务元数据信息
     * @throws Exception 注册过程中可能抛出的异常
     */
    void register(ServiceMeta serviceMeta) throws Exception;

    /**
     * 从注册中心注销服务
     * 
     * @param serviceMeta 服务元数据信息
     * @throws Exception 注销过程中可能抛出的异常
     */
    void unRegister(ServiceMeta serviceMeta) throws Exception;

    /**
     * 服务发现
     * 根据服务名称和调用方哈希码查找可用的服务实例
     * 
     * @param serviceName 服务名称
     * @param invokerHashCode 调用方哈希码，用于负载均衡策略
     * @return 服务元数据信息
     * @throws Exception 发现服务过程中可能抛出的异常
     */
    ServiceMeta discovery(String serviceName, int invokerHashCode) throws Exception;

    /**
     * 销毁注册服务
     * 释放注册中心连接等资源
     * 
     * @throws Exception 销毁过程中可能抛出的异常
     */
    void destroy() throws Exception;

    /**
     * 初始化注册服务
     * 根据配置信息初始化注册中心客户端
     * 
     * @param registryConfig 注册中心配置信息
     * @throws Exception 初始化过程中可能抛出的异常
     */
    default void init(RegistryConfig registryConfig) throws Exception {
    }
}