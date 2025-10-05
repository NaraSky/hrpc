package com.rain.rpc.provider;

import com.rain.rpc.provider.common.scanner.RpcServiceScanner;
import com.rain.rpc.provider.common.server.base.BaseServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 单机版RPC服务器实现
 * 继承自BaseServer，增加了服务自动扫描和注册功能
 * 适用于单一节点部署的场景，自动扫描指定包下的服务并注册
 */
public class RpcSingleServer extends BaseServer {

    private final Logger logger = LoggerFactory.getLogger(RpcSingleServer.class);

    /**
     * 构造函数，初始化单机版RPC服务器
     * 在父类基础上增加了服务自动扫描和注册功能
     * 
     * @param serverAddress 服务器地址，格式为 host:port
     * @param registryAddress 注册中心地址
     * @param registryType 注册中心类型
     * @param scanPackage 需要扫描的服务包路径
     * @param reflectType 反射类型
     */
    public RpcSingleServer(String serverAddress, String registryAddress, String registryType, String scanPackage, String reflectType) {
        //调用父类构造方法，初始化服务器基础配置
        super(serverAddress, registryAddress, registryType, reflectType);
        try {
            // 扫描指定包下的RPC服务，并注册到注册中心
            this.handlerMap = RpcServiceScanner.doScannerWithRpcServiceAnnotationFilterAndRegistryService(this.host, this.port, scanPackage, registryService);
        } catch (Exception e) {
            logger.error("RPC Server init error", e);
        }
    }
}