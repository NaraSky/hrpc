package com.rain.rpc.common.scanner.provider;

import com.rain.rpc.annotation.RpcService;
import com.rain.rpc.common.scanner.service.DemoService;

/**
 * 演示服务实现类
 * 用于测试RPC框架的服务提供功能
 * 通过@RpcService注解标记为RPC服务提供者
 */
@RpcService(interfaceClass = DemoService.class, interfaceClassName = "com.rain.rpc.common.scanner.service.DemoService", version = "1.0.0", group = "rain")
public class ProviderDemoServiceImpl implements DemoService {

}