package com.rain.rpc.provider.common.server.base;

import com.rain.rpc.codec.RpcDecoder;
import com.rain.rpc.codec.RpcEncoder;
import com.rain.rpc.provider.common.handler.RpcProviderHandler;
import com.rain.rpc.provider.common.server.api.Server;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * RPC服务器基础实现类
 * 基于Netty实现的RPC服务器，提供基本的服务启动和网络通信功能
 */
public class BaseServer implements Server {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseServer.class);
    
    /**
     * 服务器主机地址
     */
    protected String host = "127.0.0.1";
    
    /**
     * 服务器端口号
     */
    protected int port = 27110;
    
    /**
     * 服务处理器映射表
     * 存储服务名称与服务实例的映射关系
     */
    public Map<String, Object> handlerMap = new HashMap<>();

    public BaseServer(String serverAddress) {
        // 检查服务器地址是否有效，避免空指针异常
        if (!StringUtils.isEmpty(serverAddress)) {
            String[] serverArray = serverAddress.split(":");
            this.host = serverArray[0];
            this.port = Integer.parseInt(serverArray[1]);
        }
    }

    /**
     * 启动Netty服务器
     * 初始化Netty的主从EventLoopGroup，配置ServerBootstrap并绑定端口
     * 这是服务端的核心方法，负责整个服务的启动流程
     */
    @Override
    public void startNettyServer() {
        // 主线程组，用于处理服务器端接收客户端连接
        // 默认线程数为CPU核心数*2，适用于处理大量并发连接的场景
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        // 从线程组，用于处理与已连接客户端的交互
        // 负责处理已建立连接的客户端的读写操作
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        
        try {
            // 创建服务端启动引导类
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    // 指定服务端通道类型为NIO模式
                    .channel(NioServerSocketChannel.class)
                    // 配置客户端连接的处理管道
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline()
                                    // TODO 预留编解码，需要实现自定义协议
                                    // 当前使用String编解码器仅用于测试，后续需要替换为自定义协议编解码器
                                    .addLast(new RpcDecoder())
                                    .addLast(new RpcEncoder())
                                    // 添加RPC处理器，负责处理客户端请求
                                    .addLast(new RpcProviderHandler(handlerMap));
                        }
                    })
                    // 设置TCP参数，连接缓冲池队列大小为128
                    // 控制等待连接的最大队列长度，防止连接过多导致服务崩溃
                    .option(ChannelOption.SO_BACKLOG, 128)
                    // 启用TCP Keep-Alive机制
                    // 检测连接是否有效，及时释放无效连接资源
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
                    
            // 绑定端口并同步等待，确保服务成功启动
            ChannelFuture future = bootstrap.bind(host, port).sync();
            LOGGER.info("Server started on {}:{}", host, port);
            // 等待服务端通道关闭，保持服务持续运行
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            LOGGER.error("Server start error", e);
        } finally {
            // 优雅关闭线程组，释放相关资源
            // shutdownGracefully方法会等待所有任务执行完毕再关闭
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}