package com.rain.rpc.codec;

import com.rain.rpc.common.utils.SerializationUtils;
import com.rain.rpc.protocol.RpcProtocol;
import com.rain.rpc.protocol.header.RpcHeader;
import com.rain.rpc.serialization.api.Serialization;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.nio.charset.StandardCharsets;

/**
 * RPC协议编码器
 * 继承Netty的MessageToByteEncoder，负责将RpcProtocol对象编码为字节流
 * 编码格式：魔数(2字节) + 消息类型(1字节) + 状态(1字节) + 请求ID(8字节) + 序列化类型(16字节) + 数据长度(4字节) + 数据(N字节)
 *
 * @author rain
 * @since 1.0.0
 */
public class RpcEncoder extends MessageToByteEncoder<RpcProtocol<Object>> implements RpcCodec {
    
    /**
     * 将RpcProtocol对象编码为字节流
     * 按照RPC协议格式依次写入魔数、消息类型、状态、请求ID、序列化类型、数据长度和数据体
     *
     * @param channelHandlerContext ChannelHandlerContext上下文
     * @param msg 待编码的RpcProtocol消息对象
     * @param byteBuf 用于写入编码后数据的ByteBuf
     * @throws Exception 编码过程中可能抛出的异常
     */
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, RpcProtocol<Object> msg, ByteBuf byteBuf) throws Exception {
        // 获取协议头部信息
        RpcHeader header = msg.getHeader();
        
        // 写入魔数，用于验证数据包的合法性
        byteBuf.writeShort(header.getMagic());
        
        // 写入消息类型，标识是请求、响应还是心跳等
        byteBuf.writeByte(header.getMessageType());
        
        // 写入状态码，用于标识请求或响应的状态
        byteBuf.writeByte(header.getStatus());
        
        // 写入请求ID，用于匹配请求和响应
        byteBuf.writeLong(header.getRequestId());
        
        // 获取序列化类型
        String serializationType = header.getSerializationType();
        
        // TODO Serialization是扩展点，当前默认使用JDK序列化
        Serialization serialization = getJdkSerialization();
        
        // 写入序列化类型，固定长度16字节，不足部分用"0"填充
        byteBuf.writeBytes(SerializationUtils.paddingString(serializationType).getBytes(StandardCharsets.UTF_8));
        
        // 序列化消息体
        byte[] data = serialization.serialize(msg.getBody());
        
        // 写入数据体长度
        byteBuf.writeInt(data.length);
        
        // 写入序列化后的数据体
        byteBuf.writeBytes(data);
    }
}