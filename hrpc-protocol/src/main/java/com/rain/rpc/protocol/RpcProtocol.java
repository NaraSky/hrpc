package com.rain.rpc.protocol;

import com.rain.rpc.protocol.header.RpcHeader;

import java.io.Serializable;

/**
 * RPC协议封装类
 * 包含协议头部和具体的数据载荷
 *
 * @param <T> 数据载荷类型
 */
public class RpcProtocol<T> implements Serializable {

    private static final long serialVersionUID = -6294017313161240184L;

    /**
     * 协议头部信息
     */
    private RpcHeader header;
    
    /**
     * 协议数据载荷
     */
    private T body;

    public RpcHeader getHeader() {
        return header;
    }

    public void setHeader(RpcHeader header) {
        this.header = header;
    }

    public T getBody() {
        return body;
    }

    public void setBody(T body) {
        this.body = body;
    }
}