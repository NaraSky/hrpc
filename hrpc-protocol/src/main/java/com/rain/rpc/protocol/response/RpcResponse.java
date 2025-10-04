package com.rain.rpc.protocol.response;

import com.rain.rpc.protocol.base.RpcMessage;

/**
 * RPC响应数据载荷类
 * 包含远程调用的响应结果或错误信息
 */
public class RpcResponse extends RpcMessage {

    private static final long serialVersionUID = 8235332761117785772L;

    /**
     * 错误信息，如果调用成功则为null
     */
    private String error;
    
    /**
     * 调用结果，如果调用失败则为null
     */
    private Object result;

    public boolean isError() {
        return error != null;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }
}