package com.rain.rpc.common.scanner.consumer;

import com.rain.rpc.annotation.RpcReference;
import com.rain.rpc.common.scanner.service.DemoService;

/**
 * 消费者业务服务实现类
 * 用于测试RPC框架的服务消费功能
 * 通过@RpcReference注解引用远程服务
 */
public class ConsumerBusinessServiceImpl implements ConsumerBusinessService {

    /**
     * 远程服务引用
     * 通过@RpcReference注解自动注入DemoService的远程代理实例
     */
    @RpcReference(registryType = "zookeeper", registryAddress = "127.0.0.1:2181", version = "1.0.0", group = "rain")
    private DemoService demoService;

}