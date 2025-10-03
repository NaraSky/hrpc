package com.rain.rpc.protocol.enumeration;

/**
 * RPC消息类型枚举
 * 定义了系统中所有可能的RPC消息类型
 */
public enum RpcType {
    /**
     * 请求消息类型
     */
    REQUEST(1),
    
    /**
     * 响应消息类型
     */
    RESPONSE(2),
    
    /**
     * 心跳消息类型
     */
    HEARTBEAT(3);

    /**
     * 类型值
     */
    private final int type;

    RpcType(int type) {
        this.type = type;
    }

    public static RpcType findByType(int type) {
        for (RpcType rpcType : RpcType.values()) {
            if (rpcType.type == type) {
                return rpcType;
            }
        }
        return null;
    }

    public int getType() {
        return type;
    }
}