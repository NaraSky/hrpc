package com.rain.rpc.codec;

import com.rain.rpc.codec.handler.RpcTestProviderHandler;
import com.rain.rpc.codec.RpcDecoder;
import com.rain.rpc.codec.RpcEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RPC测试服务提供者
 * 用于测试RPC编解码功能的服务端程序
 * 通过Netty监听客户端连接并处理请求
 */
public class RpcTestProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(RpcTestProvider.class);
    
    // 默认监听的地址和端口
    private static final String DEFAULT_HOST = "127.0.0.1";
    private static final int DEFAULT_PORT = 27880;

    public static void main(String[] args) {
        LOGGER.info("Starting RPC test provider server");
        
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup(4);
        
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            
            // 配置ServerBootstrap
            bootstrap.group(bossGroup, workerGroup)
                    // 指定服务端通道类型为NioServerSocketChannel
                    .channel(NioServerSocketChannel.class)
                    // 设置TCP连接缓冲池队列大小
                    .option(ChannelOption.SO_BACKLOG, 128)
                    // 启用TCP Keep-Alive机制
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    // 设置客户端连接的处理管道
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline()
                                    // 添加RPC解码器，负责将字节流解码为RpcProtocol对象
                                    .addLast("rpc-decoder", new RpcDecoder())
                                    // 添加RPC编码器，负责将RpcProtocol对象编码为字节流
                                    .addLast("rpc-encoder", new RpcEncoder())
                                    // 添加RPC测试服务提供者处理器，负责处理业务逻辑
                                    .addLast("rpc-provider-handler", new RpcTestProviderHandler());
                        }
                    });
            
            // 绑定端口并同步等待，确保服务成功启动
            LOGGER.info("Binding server to {}:{}", DEFAULT_HOST, DEFAULT_PORT);
            ChannelFuture channelFuture = bootstrap.bind(DEFAULT_HOST, DEFAULT_PORT).sync();
            
            LOGGER.info("RPC test provider server started successfully");
            
            // 等待服务端通道关闭，保持服务持续运行
            channelFuture.channel().closeFuture().sync();
            LOGGER.info("Server channel closed");
            
        } catch (Exception e) {
            LOGGER.error("Error occurred in RPC test provider", e);
        } finally {
            // 优雅关闭事件循环组
            LOGGER.info("Shutting down event loop groups");
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            LOGGER.info("Event loop groups shutdown completed");
        }
    }
}