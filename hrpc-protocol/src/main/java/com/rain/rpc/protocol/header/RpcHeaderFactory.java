package com.rain.rpc.protocol.header;

import com.rain.rpc.common.id.IdFactory;
import com.rain.rpc.constants.RpcConstants;
import com.rain.rpc.protocol.enumeration.RpcType;

/**
 * RPC协议头部工厂类
 * 用于创建各种类型的RPC协议头部对象
 *
 * @author rain
 * @since 1.0.0
 */
public class RpcHeaderFactory {

    /**
     * 创建请求类型的RPC协议头部
     *
     * @param serializationType 序列化类型
     * @return 请求类型的RPC协议头部
     */
    public static RpcHeader getRequestHeader(String serializationType) {
        RpcHeader header = new RpcHeader();
        long requestId = IdFactory.getId();
        header.setMagic(RpcConstants.MAGIC);
        header.setRequestId(requestId);
        header.setMessageType((byte) RpcType.REQUEST.getType());
        header.setStatus((byte) 0x1);
        header.setSerializationType(serializationType);
        return header;
    }
}