package com.rain.rpc.proxy.api.consumer;

import com.rain.rpc.protocol.RpcProtocol;
import com.rain.rpc.protocol.request.RpcRequest;
import com.rain.rpc.proxy.api.future.RPCFuture;

public interface Consumer {

    /**
     * 消费者发送 request 请求
     */
    RPCFuture sendRequest(RpcProtocol<RpcRequest> protocol) throws Exception;
}
