package com.rain.rpc.protocol.base;

import java.io.Serializable;

/**
 * RPC消息基础类，提供通用的消息属性
 * 用于标识消息是否为单向调用或异步调用
 */
public class RpcMessage implements Serializable {
    private static final long serialVersionUID = -8384819512072649351L;

    /**
     * 是否为单向调用（不需要响应）
     */
    private boolean oneway;
    
    /**
     * 是否为异步调用
     */
    private boolean async;

    public boolean getOneway() {
        return oneway;
    }

    public void setOneway(boolean oneway) {
        this.oneway = oneway;
    }

    public boolean getAsync() {
        return async;
    }

    public void setAsync(boolean async) {
        this.async = async;
    }
}