package com.rain.rpc.proxy.api;

import com.rain.rpc.proxy.api.config.ProxyConfig;

/**
 * 代理工厂接口
 * 定义创建代理对象的标准接口
 * 
 * 设计说明：
 * 1. 提供获取代理对象的统一接口
 * 2. 提供默认的初始化方法，允许子类选择性实现
 */
public interface ProxyFactory {

    /**
     * 获取指定类的代理对象
     * 
     * @param clazz 需要代理的类
     * @param <T> 泛型类型
     * @return 代理对象
     */
    <T> T getProxy(Class<T> clazz);

    /**
     * 默认初始化方法
     * 
     * @param proxyConfig 代理配置
     * @param <T> 泛型类型
     */
    default <T> void init(ProxyConfig<T> proxyConfig){}
}
