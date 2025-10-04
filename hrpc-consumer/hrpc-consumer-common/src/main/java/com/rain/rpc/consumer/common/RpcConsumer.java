package com.rain.rpc.consumer.common;

import com.rain.rpc.common.threadpool.ClientThreadPool;
import com.rain.rpc.consumer.common.handler.RpcConsumerHandler;
import com.rain.rpc.consumer.common.initializer.RpcConsumerInitializer;
import com.rain.rpc.protocol.RpcProtocol;
import com.rain.rpc.protocol.request.RpcRequest;
import com.rain.rpc.proxy.api.consumer.Consumer;
import com.rain.rpc.proxy.api.future.RPCFuture;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RPC消费者
 * 负责管理与RPC服务提供者的连接，并发送RPC请求
 * 
 * 设计说明：
 * 1. 使用单例模式确保整个应用只有一个RpcConsumer实例
 * 2. 使用连接缓存避免重复创建连接，提高性能
 * 3. 基于Netty实现异步网络通信
 */
public class RpcConsumer implements Consumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcConsumer.class);
    // Netty客户端引导程序，用于配置和启动客户端
    private final Bootstrap bootstrap;
    // 事件循环组，处理IO操作，使用4个线程处理网络事件
    private final EventLoopGroup eventLoopGroup;

    // 使用双重检查锁定实现的单例模式
    private static volatile RpcConsumer instance;

    // 连接缓存，以"地址_端口"为key缓存RpcConsumerHandler，避免重复创建连接
    private static final Map<String, RpcConsumerHandler> CONNECTION_HANDLER_MAP = new ConcurrentHashMap<>();

    /**
     * 私有构造函数
     * 初始化Netty客户端配置
     */
    private RpcConsumer() {
        bootstrap = new Bootstrap();
        eventLoopGroup = new NioEventLoopGroup(4);
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new RpcConsumerInitializer());
    }

    public static RpcConsumer getInstance() {
        if (instance == null) {
            synchronized (RpcConsumer.class) {
                if (instance == null) {
                    instance = new RpcConsumer();
                }
            }
        }
        return instance;
    }

    public void close() {
        eventLoopGroup.shutdownGracefully();
        ClientThreadPool.shutdown();
    }

    /**
     * 发送RPC请求
     *
     * @param protocol RPC请求协议
     * @return RPCFuture对象，用于获取异步结果
     * @throws Exception 连接异常
     */
    @Override
    public RPCFuture sendRequest(RpcProtocol<RpcRequest> protocol) throws Exception {
        // TODO 暂时写死服务地址和端口，后续在引入注册中心时，从注册中心获取
        String serviceAddress = "127.0.0.1";
        int port = 27880;
        String connectionKey = serviceAddress.concat("_").concat(String.valueOf(port));
        RpcConsumerHandler handler = CONNECTION_HANDLER_MAP.get(connectionKey);
        // 缓存中无RpcConsumerHandler，需要创建新的连接
        if (handler == null) {
            handler = getRpcConsumerHandler(serviceAddress, port);
            CONNECTION_HANDLER_MAP.put(connectionKey, handler);
        } else if (!handler.getChannel().isActive()) {
            // 缓存中存在RpcConsumerHandler，但连接已失效，需要重新创建连接
            handler.close();
            handler = getRpcConsumerHandler(serviceAddress, port);
            CONNECTION_HANDLER_MAP.put(connectionKey, handler);
        }
        RpcRequest request = protocol.getBody();
        // 通过handler发送请求，并根据请求类型（同步/异步/单向）返回相应结果
        return handler.sendRequest(protocol, request.getAsync(), request.getOneway());
    }

    /**
     * 创建与RPC服务提供者的连接并返回RpcConsumerHandler
     *
     * @param serviceAddress 服务地址
     * @param port           服务端口
     * @return RpcConsumerHandler对象
     * @throws InterruptedException 连接中断异常
     */
    private RpcConsumerHandler getRpcConsumerHandler(String serviceAddress, int port) throws InterruptedException {
        // 建立连接并同步等待连接结果
        ChannelFuture channelFuture = bootstrap.connect(serviceAddress, port).sync();
        channelFuture.addListener((ChannelFutureListener) listener -> {
            if (channelFuture.isSuccess()) {
                LOGGER.info("connect rpc server {} on port {} success.", serviceAddress, port);
            } else {
                LOGGER.error("connect rpc server {} on port {} failed.", serviceAddress, port);
                channelFuture.cause().printStackTrace();
                eventLoopGroup.shutdownGracefully();
            }
        });
        // 从Channel的处理管道中获取RpcConsumerHandler
        return channelFuture.channel().pipeline().get(RpcConsumerHandler.class);
    }
}