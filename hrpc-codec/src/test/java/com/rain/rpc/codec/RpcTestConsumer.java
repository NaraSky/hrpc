package com.rain.rpc.codec;

import com.rain.rpc.codec.init.RpcTestConsumerInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RPC测试消费者
 * 用于测试RPC编解码功能的客户端程序
 * 通过Netty建立与服务端的连接并发送测试数据
 */
public class RpcTestConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(RpcTestConsumer.class);
    
    // 默认连接的服务端地址和端口
    private static final String DEFAULT_HOST = "127.0.0.1";
    private static final int DEFAULT_PORT = 27880;

    public static void main(String[] args) {
        LOGGER.info("Starting RPC test consumer client");
        Bootstrap bootstrap = new Bootstrap();
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup(4);
        
        try {
            // 配置Bootstrap
            bootstrap.group(eventLoopGroup)
                    // 指定通道类型为NioSocketChannel
                    .channel(NioSocketChannel.class)
                    // 设置通道初始化器
                    .handler(new RpcTestConsumerInitializer());
            
            // 连接到服务端并同步等待连接完成
            LOGGER.info("Connecting to server {}:{}", DEFAULT_HOST, DEFAULT_PORT);
            ChannelFuture channelFuture = bootstrap.connect(DEFAULT_HOST, DEFAULT_PORT).sync();
            
            // 等待通道关闭
            channelFuture.channel().closeFuture().sync();
            LOGGER.info("Connection to server closed");
            
        } catch (Exception e) {
            LOGGER.error("Error occurred in RPC test consumer", e);
        } finally {
            // 优雅关闭事件循环组
            LOGGER.info("Shutting down event loop group");
            try {
                // 等待2秒确保资源释放
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.warn("Interrupted while waiting for shutdown", e);
            }
            eventLoopGroup.shutdownGracefully();
            LOGGER.info("Event loop group shutdown completed");
        }
    }
}