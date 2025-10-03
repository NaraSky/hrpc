package com.rain.rpc.codec.init;

import com.rain.rpc.codec.RpcDecoder;
import com.rain.rpc.codec.RpcEncoder;
import com.rain.rpc.codec.handler.RpcTestConsumerHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RPC测试消费者通道初始化器
 * 负责初始化SocketChannel的处理管道，添加编解码器和业务处理器
 */
public class RpcTestConsumerInitializer extends ChannelInitializer<SocketChannel> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RpcTestConsumerInitializer.class);

    /**
     * 初始化SocketChannel的处理管道
     * 按顺序添加编码器、解码器和业务处理器
     * 
     * @param socketChannel 待初始化的SocketChannel
     * @throws Exception 初始化过程中可能抛出的异常
     */
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        LOGGER.info("Initializing channel pipeline for RPC test consumer");
        
        // 获取通道的处理管道
        ChannelPipeline pipeline = socketChannel.pipeline();
        
        // 添加RPC编码器，负责将RpcProtocol对象编码为字节流
        pipeline.addLast("rpc-encoder", new RpcEncoder());
        
        // 添加RPC解码器，负责将字节流解码为RpcProtocol对象
        pipeline.addLast("rpc-decoder", new RpcDecoder());
        
        // 添加RPC测试消费者处理器，负责处理业务逻辑
        pipeline.addLast("rpc-consumer-handler", new RpcTestConsumerHandler());
        
        LOGGER.info("Channel pipeline initialized successfully");
    }
}