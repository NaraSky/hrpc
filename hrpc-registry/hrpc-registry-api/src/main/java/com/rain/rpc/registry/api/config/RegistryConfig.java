package com.rain.rpc.registry.api.config;

import java.io.Serializable;

/**
 * 注册中心配置类
 * 用于配置注册中心的地址和类型信息
 */
public class RegistryConfig implements Serializable {
    /**
     * 注册中心地址
     */
    private String registryAddr;

    /**
     * 注册中心类型
     */
    private String registryType;
    
    public RegistryConfig(String registryAddr, String registryType) {
        this.registryAddr = registryAddr;
        this.registryType = registryType;
    }
    
    public String getRegistryAddr() {
        return registryAddr;
    }
    
    public void setRegistryAddr(String registryAddr) {
        this.registryAddr = registryAddr;
    }
    
    public String getRegistryType() {
        return registryType;
    }
    
    public void setRegistryType(String registryType) {
        this.registryType = registryType;
    }
}