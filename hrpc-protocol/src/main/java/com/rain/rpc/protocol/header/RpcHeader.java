package com.rain.rpc.protocol.header;

import java.io.Serializable;

/**
 * RPC协议头部信息
 * 包含RPC通信所需的基本元数据信息
 * <p>
 * 协议格式:
 * <pre>
 * +---------------------------------------------------------------+
 * | 魔数 2byte | 报文类型 1byte | 状态 1byte |     消息 ID 8byte      |
 * +---------------------------------------------------------------+
 * |           序列化类型 16byte      |        数据长度 4byte          |
 * +---------------------------------------------------------------+
 * </pre>
 */
public class RpcHeader implements Serializable {
    private static final long serialVersionUID = 643172623893625070L;

    /**
     * 魔数，用于快速验证是否为有效的RPC协议报文
     */
    private short magic;
    
    /**
     * 消息类型，标识当前报文的类型（如请求、响应、心跳等）
     */
    private byte messageType;
    
    /**
     * 状态码，表示当前报文的状态
     */
    private byte status;
    
    /**
     * 请求ID，用于匹配请求和响应
     */
    private long requestId;
    
    /**
     * 序列化类型，标识消息体采用的序列化方式
     */
    private String serializationType;
    
    /**
     * 消息体长度，单位字节
     */
    private int messageLength;

    public short getMagic() {
        return magic;
    }

    public void setMagic(short magic) {
        this.magic = magic;
    }

    public byte getMessageType() {
        return messageType;
    }

    public void setMessageType(byte messageType) {
        this.messageType = messageType;
    }

    public byte getStatus() {
        return status;
    }

    public void setStatus(byte status) {
        this.status = status;
    }

    public long getRequestId() {
        return requestId;
    }

    public void setRequestId(long requestId) {
        this.requestId = requestId;
    }

    public String getSerializationType() {
        return serializationType;
    }

    public void setSerializationType(String serializationType) {
        this.serializationType = serializationType;
    }

    public int getMessageLength() {
        return messageLength;
    }

    public void setMessageLength(int messageLength) {
        this.messageLength = messageLength;
    }
}