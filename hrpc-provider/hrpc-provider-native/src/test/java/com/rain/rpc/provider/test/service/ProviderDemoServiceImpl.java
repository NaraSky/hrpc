package com.rain.rpc.provider.test.service;

import com.rain.rpc.annotation.RpcService;

@RpcService(interfaceClass = DemoService.class, interfaceClassName = "com.rain.rpc.provider.test.service.DemoService", version = "1.0.0", group = "rain")
public class ProviderDemoServiceImpl implements DemoService {

}
