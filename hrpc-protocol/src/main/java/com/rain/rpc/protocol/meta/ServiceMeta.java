package com.rain.rpc.protocol.meta;

import java.io.Serializable;

/**
 * 服务元数据，注册到注册中心的元数据信息
 * 包含服务的基本信息，如名称、版本、地址、端口和分组等
 */
public class ServiceMeta implements Serializable {

    /**
     * 服务名称
     */
    private String serviceName;
    
    /**
     * 服务版本
     */
    private String serviceVersion;
    
    /**
     * 服务地址
     */
    private String serviceAddr;
    
    /**
     * 服务端口
     */
    private int servicePort;
    
    /**
     * 服务分组
     */
    private String serviceGroup;

    public ServiceMeta() {
    }

    public ServiceMeta(String serviceName, String serviceVersion, String serviceAddr, int servicePort, String serviceGroup) {
        this.serviceName = serviceName;
        this.serviceVersion = serviceVersion;
        this.serviceAddr = serviceAddr;
        this.servicePort = servicePort;
        this.serviceGroup = serviceGroup;
    }

    /**
     * 获取服务名称
     * 
     * @return 服务名称
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * 设置服务名称
     * 
     * @param serviceName 服务名称
     */
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    /**
     * 获取服务版本
     * 
     * @return 服务版本
     */
    public String getServiceVersion() {
        return serviceVersion;
    }

    /**
     * 设置服务版本
     * 
     * @param serviceVersion 服务版本
     */
    public void setServiceVersion(String serviceVersion) {
        this.serviceVersion = serviceVersion;
    }

    /**
     * 获取服务地址
     * 
     * @return 服务地址
     */
    public String getServiceAddr() {
        return serviceAddr;
    }

    /**
     * 设置服务地址
     * 
     * @param serviceAddr 服务地址
     */
    public void setServiceAddr(String serviceAddr) {
        this.serviceAddr = serviceAddr;
    }

    /**
     * 获取服务端口
     * 
     * @return 服务端口
     */
    public int getServicePort() {
        return servicePort;
    }

    /**
     * 设置服务端口
     * 
     * @param servicePort 服务端口
     */
    public void setServicePort(int servicePort) {
        this.servicePort = servicePort;
    }

    /**
     * 获取服务分组
     * 
     * @return 服务分组
     */
    public String getServiceGroup() {
        return serviceGroup;
    }

    /**
     * 设置服务分组
     * 
     * @param serviceGroup 服务分组
     */
    public void setServiceGroup(String serviceGroup) {
        this.serviceGroup = serviceGroup;
    }
}