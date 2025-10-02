package com.rain.rpc.provider.common.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * RPC服务提供者处理器
 * 处理来自客户端的请求，并返回相应的响应
 * 作为Netty管道中的一个处理节点，负责接收客户端请求并调用对应的服务实现
 */
public class RpcProviderHandler extends SimpleChannelInboundHandler<Object> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RpcProviderHandler.class);

    /**
     * 服务处理器映射表
     * key为服务名称，value为服务实例
     */
    private final Map<String, Object> handlerMap;

    public RpcProviderHandler(Map<String, Object> handlerMap) {
        this.handlerMap = handlerMap;
    }

    /**
     * 处理接收到的消息
     * 此方法会打印接收到的数据和服务处理器映射表中的内容
     * 在实际RPC调用中，这里应该解析请求并调用对应的服务方法
     *
     * @param ctx ChannelHandlerContext上下文，用于与客户端进行通信
     * @param msg 接收到的消息对象，应为具体的RPC请求对象
     * @throws Exception 处理过程中可能抛出的异常
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        LOGGER.info("RPC provider received data: {}", msg);
        LOGGER.info("Handler map contents:");
        
        // 遍历并打印handlerMap中的所有条目，用于调试和监控
        handlerMap.forEach((key, value) -> LOGGER.info("{} => {}", key, value));
        
        // 将消息原样写回客户端，仅用于测试
        // 在实际RPC框架中，这里应该解析请求、调用服务方法并返回结果
        ctx.writeAndFlush(msg);
    }
}