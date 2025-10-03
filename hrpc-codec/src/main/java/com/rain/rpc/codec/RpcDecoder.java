package com.rain.rpc.codec;

import com.rain.rpc.common.exception.SerializerException;
import com.rain.rpc.common.utils.SerializationUtils;
import com.rain.rpc.constants.RpcConstants;
import com.rain.rpc.protocol.RpcProtocol;
import com.rain.rpc.protocol.enumeration.RpcType;
import com.rain.rpc.protocol.header.RpcHeader;
import com.rain.rpc.protocol.request.RpcRequest;
import com.rain.rpc.protocol.response.RpcResponse;
import com.rain.rpc.serialization.api.Serialization;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.CharsetUtil;

import java.util.List;

/**
 * RPC协议解码器
 * 继承Netty的ByteToMessageDecoder，负责将字节流解码为RpcProtocol对象
 * 解码格式：魔数(2字节) + 消息类型(1字节) + 状态(1字节) + 请求ID(8字节) + 序列化类型(16字节) + 数据长度(4字节) + 数据(N字节)
 *
 * @author rain
 * @since 1.0.0
 */
public class RpcDecoder extends ByteToMessageDecoder implements RpcCodec {
    
    /**
     * 将字节流解码为RpcProtocol对象
     * 按照RPC协议格式依次读取魔数、消息类型、状态、请求ID、序列化类型、数据长度和数据体，并构建对应的RpcProtocol对象
     *
     * @param channelHandlerContext ChannelHandlerContext上下文
     * @param byteBuf 待解码的字节流数据
     * @param out 解码后的对象列表
     * @throws Exception 解码过程中可能抛出的异常
     */
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> out) throws Exception {
        // 如果可读字节数小于协议头总长度，则直接返回，等待更多数据
        if (byteBuf.readableBytes() < RpcConstants.HEADER_TOTAL_LEN) {
            return;
        }
        
        // 标记当前读索引位置，用于后续可能的数据回退
        byteBuf.markReaderIndex();
        
        // 读取魔数，用于验证数据包的合法性
        short magic = byteBuf.readShort();
        if (magic != RpcConstants.MAGIC) {
            throw new SerializerException("magic number is illegal, " + magic);
        }
        
        // 读取消息类型
        byte messageType = byteBuf.readByte();
        
        // 读取状态码
        byte status = byteBuf.readByte();
        
        // 读取请求ID
        long requestId = byteBuf.readLong();
        
        // 读取序列化类型，固定长度16字节
        ByteBuf serializationTypeByteBuf = byteBuf.readBytes(SerializationUtils.MAX_SERIALIZATION_TYPE_COUNT);
        String serializationType = SerializationUtils.subString(serializationTypeByteBuf.toString(CharsetUtil.UTF_8));
        
        // 读取数据体长度
        int dataLength = byteBuf.readInt();
        
        // 如果可读字节数小于数据体长度，则回退读索引，等待更多数据
        if (byteBuf.readableBytes() < dataLength) {
            byteBuf.resetReaderIndex();
            return;
        }
        
        // 读取数据体
        byte[] data = new byte[dataLength];
        byteBuf.readBytes(data);
        
        // 根据消息类型获取对应的枚举值
        RpcType msgTypeEnum = RpcType.findByType(messageType);
        if (msgTypeEnum == null) {
            return;
        }
        
        // 构建协议头
        RpcHeader header = new RpcHeader();
        header.setMagic(magic);
        header.setStatus(status);
        header.setRequestId(requestId);
        header.setMessageType(messageType);
        header.setSerializationType(serializationType);
        header.setMessageLength(dataLength);
        
        // TODO Serialization是扩展点，当前默认使用JDK序列化
        Serialization serialization = getJdkSerialization();
        
        // 根据消息类型进行不同的处理
        switch (msgTypeEnum) {
            case REQUEST:
                // 反序列化请求数据
                RpcRequest request = serialization.deserialize(data, RpcRequest.class);
                if (request != null) {
                    // 构建请求协议对象
                    RpcProtocol<RpcRequest> protocol = new RpcProtocol<>();
                    protocol.setHeader(header);
                    protocol.setBody(request);
                    out.add(protocol);
                }
                break;
                
            case RESPONSE:
                // 反序列化响应数据
                RpcResponse response = serialization.deserialize(data, RpcResponse.class);
                if (response != null) {
                    // 构建响应协议对象
                    RpcProtocol<RpcResponse> protocol = new RpcProtocol<>();
                    protocol.setHeader(header);
                    protocol.setBody(response);
                    out.add(protocol);
                }
                break;
                
            case HEARTBEAT:
                // TODO 心跳处理逻辑待实现
                break;
        }
    }
}