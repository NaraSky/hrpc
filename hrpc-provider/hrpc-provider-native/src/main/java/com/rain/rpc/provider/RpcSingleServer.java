package com.rain.rpc.provider;

import com.rain.rpc.common.scanner.server.RpcServiceScanner;
import com.rain.rpc.provider.common.server.base.BaseServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 单机版RPC服务器实现
 * 继承自BaseServer，增加了服务自动扫描和注册功能
 * 适用于单一节点部署的场景
 */
public class RpcSingleServer extends BaseServer {

    private final Logger logger = LoggerFactory.getLogger(RpcSingleServer.class);

    /**
     * 构造函数，初始化服务器地址并自动扫描注册服务
     * @param serverAddress 服务器地址，格式为 host:port
     * @param scanPackage 服务扫描包路径，用于自动发现和注册带有@RpcService注解的服务
     */
    public RpcSingleServer(String serverAddress, String scanPackage, String reflectType) {
        // 调用父类构造方法初始化服务器地址
        super(serverAddress, reflectType);
        try {
            // 扫描指定包路径下带有@RpcService注解的类，并注册为服务
            // 实现了服务的自动发现和注册，简化了服务配置过程
            this.handlerMap = RpcServiceScanner.doScannerWithRpcServiceAnnotationFilterAndRegistryService(scanPackage);
        } catch (Exception e) {
            logger.error("RPC Server init error", e);
        }
    }
}