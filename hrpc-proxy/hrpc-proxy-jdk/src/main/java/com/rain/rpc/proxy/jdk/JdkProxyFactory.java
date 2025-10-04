package com.rain.rpc.proxy.jdk;

import com.rain.rpc.proxy.api.BaseProxyFactory;
import com.rain.rpc.proxy.api.ProxyFactory;

import java.lang.reflect.Proxy;

/**
 * JDK代理工厂实现
 * 基于JDK动态代理实现的代理工厂
 * 
 * 设计说明：
 * 1. 继承BaseProxyFactory，复用通用的代理创建逻辑
 * 2. 使用JDK动态代理创建代理对象
 */
public class JdkProxyFactory<T> extends BaseProxyFactory<T> implements ProxyFactory {
    /**
     * 获取指定类的代理对象
     * 使用JDK动态代理创建代理对象
     * 
     * @param clazz 需要代理的类
     * @param <T> 泛型类型
     * @return 代理对象
     */
    @Override
    public <T> T getProxy(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(
                clazz.getClassLoader(),
                new Class<?>[]{clazz},
                objectProxy
        );
    }
}
