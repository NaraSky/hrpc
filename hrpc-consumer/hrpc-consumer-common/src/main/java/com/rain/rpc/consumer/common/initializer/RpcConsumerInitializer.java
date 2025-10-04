package com.rain.rpc.consumer.common.initializer;

import com.rain.rpc.codec.RpcDecoder;
import com.rain.rpc.codec.RpcEncoder;
import com.rain.rpc.consumer.common.handler.RpcConsumerHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

/**
 * RPC消费者通道初始化器
 * 负责初始化SocketChannel的处理管道，添加编解码器和业务处理器
 * 
 * 设计说明：
 * 1. 继承ChannelInitializer实现通道初始化
 * 2. 按顺序添加编码器、解码器和业务处理器
 * 3. 编解码器负责处理网络传输中的序列化和反序列化
 */
public class RpcConsumerInitializer extends ChannelInitializer<SocketChannel> {
    /**
     * 初始化通道，配置处理链
     * 
     * @param socketChannel Socket通道
     * @throws Exception 初始化异常
     */
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        // 添加RPC消息编码器，负责将Java对象编码为字节流
        pipeline.addLast(new RpcEncoder());
        // 添加RPC消息解码器，负责将字节流解码为Java对象
        pipeline.addLast(new RpcDecoder());
        // 添加RPC消费者处理器，处理具体的业务逻辑
        pipeline.addLast(new RpcConsumerHandler());
    }
}