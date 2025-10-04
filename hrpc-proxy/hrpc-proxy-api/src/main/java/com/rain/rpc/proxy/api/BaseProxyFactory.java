package com.rain.rpc.proxy.api;

import com.rain.rpc.proxy.api.config.ProxyConfig;
import com.rain.rpc.proxy.api.object.ObjectProxy;

/**
 * 抽象代理工厂基类
 * 提供通用的代理创建功能
 * 
 * 设计说明：
 * 1. 实现ProxyFactory接口，提供基本的代理创建逻辑
 * 2. 使用模板方法模式，将具体的代理创建交给子类实现
 * 3. 提供统一的初始化方法
 */
public abstract class BaseProxyFactory<T> implements ProxyFactory {

    /**
     * 对象代理实例，用于处理代理对象的方法调用
     */
    protected ObjectProxy<T> objectProxy;

    /**
     * 初始化代理工厂
     * 
     * @param proxyConfig 代理配置
     * @param <T> 泛型类型
     */
    @Override
    public <T> void init(ProxyConfig<T> proxyConfig) {
        this.objectProxy = new ObjectProxy(
                proxyConfig.getClazz(),
                proxyConfig.getServiceVersion(),
                proxyConfig.getServiceGroup(),
                proxyConfig.getSerializationType(),
                proxyConfig.getTimeout(),
                proxyConfig.getConsumer(),
                proxyConfig.getAsync(),
                proxyConfig.getOneway());
    }
}
