package com.rain.rpc.proxy.api.consumer;

import com.rain.rpc.protocol.RpcProtocol;
import com.rain.rpc.protocol.request.RpcRequest;
import com.rain.rpc.proxy.api.future.RPCFuture;
import com.rain.rpc.registry.api.RegistryService;

/**
 * 消费者接口
 * 
 * 定义了消费者发送RPC请求的标准接口，负责将RPC协议对象发送到远程服务提供者
 */
public interface Consumer {

    /**
     * 消费者发送 request 请求
     * 
     * @param protocol RPC协议对象，包含请求头和请求体
     * @param registryService 注册服务，用于服务发现
     * @return RPCFuture对象，用于获取异步结果
     * @throws Exception 连接或处理异常
     */
    RPCFuture sendRequest(RpcProtocol<RpcRequest> protocol, RegistryService registryService) throws Exception;
}
