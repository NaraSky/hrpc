package com.rain.rpc.consumer.common.handler;

import com.alibaba.fastjson2.JSONObject;
import com.rain.rpc.consumer.common.context.RpcContext;
import com.rain.rpc.protocol.RpcProtocol;
import com.rain.rpc.protocol.header.RpcHeader;
import com.rain.rpc.protocol.request.RpcRequest;
import com.rain.rpc.protocol.response.RpcResponse;
import com.rain.rpc.proxy.api.future.RPCFuture;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RPC消费者处理器
 * 处理与RPC服务提供者的通信，包括发送请求和接收响应
 * 
 * 设计说明：
 * 1. 继承SimpleChannelInboundHandler以处理入站消息
 * 2. 使用pendingRPC映射表维护请求ID与RPCFuture的对应关系
 * 3. 支持同步、异步和单向调用三种模式
 */
public class RpcConsumerHandler extends SimpleChannelInboundHandler<RpcProtocol<RpcResponse>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RpcConsumerHandler.class);
    // 当前连接的Channel实例
    private volatile Channel channel;
    // 远程服务地址
    private SocketAddress remotePeer;

    // 存储请求ID与RPCFuture的映射关系，用于异步处理响应
    // 使用ConcurrentHashMap保证线程安全
    private final Map<Long, RPCFuture> pendingRPC = new ConcurrentHashMap<>();

    /**
     * 获取当前连接的Channel
     *
     * @return 当前连接的Channel实例
     */
    public Channel getChannel() {
        return channel;
    }

    /**
     * 获取远程服务地址
     *
     * @return 远程服务地址
     */
    public SocketAddress getRemotePeer() {
        return remotePeer;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        this.remotePeer = this.channel.remoteAddress();
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        this.channel = ctx.channel();
    }

    /**
     * 处理从服务提供者接收到的响应数据
     * 
     * @param channelHandlerContext channel处理器上下文
     * @param responseProtocol 响应协议数据
     * @throws Exception 处理异常
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcProtocol<RpcResponse> responseProtocol) throws Exception {
        if (responseProtocol == null) {
            return;
        }
        LOGGER.info("服务消费者接收到的数据===>>>{}", JSONObject.toJSONString(responseProtocol));
        RpcHeader header = responseProtocol.getHeader();
        // 获取请求ID，用于匹配对应的RPCFuture
        long requestId = header.getRequestId();
        // 从待处理RPC映射中移除并获取对应的RPCFuture
        RPCFuture rpcFuture = pendingRPC.remove(requestId);
        if (rpcFuture != null) {
            // 完成RPC调用，设置响应结果
            rpcFuture.done(responseProtocol);
        }
    }

    /**
     * 服务消费者向服务提供者发送请求
     *
     * @param requestProtocol RPC请求协议
     * @param async 是否异步调用
     * @param oneway 是否单向调用（不需要响应）
     * @return RPCFuture对象，用于获取异步结果
     */
    public RPCFuture sendRequest(RpcProtocol<RpcRequest> requestProtocol, boolean async, boolean oneway) {
        LOGGER.info("服务消费者发送的数据===>>>{}", JSONObject.toJSONString(requestProtocol));
        // 根据调用方式选择不同的发送策略：单向、异步或同步
        return oneway ? this.sendRequestOneway(requestProtocol) : async ? sendRequestAsync(requestProtocol) : this.sendRequestSync(requestProtocol);
    }

    /**
     * 同步发送请求
     *
     * @param protocol RPC请求协议
     * @return RPCFuture对象，用于获取响应结果
     */
    private RPCFuture sendRequestSync(RpcProtocol<RpcRequest> protocol) {
        RPCFuture rpcFuture = this.getRpcFuture(protocol);
        channel.writeAndFlush(protocol);
        return rpcFuture;
    }

    /**
     * 异步发送请求
     *
     * @param protocol RPC请求协议
     * @return null（异步调用通过上下文获取结果）
     */
    private RPCFuture sendRequestAsync(RpcProtocol<RpcRequest> protocol) {
        RPCFuture rpcFuture = this.getRpcFuture(protocol);
        // 如果是异步调用，则将RPCFuture放入RpcContext供外部获取
        RpcContext.getContext().setRPCFuture(rpcFuture);
        channel.writeAndFlush(protocol);
        return null;
    }

    /**
     * 单向发送请求（不需要响应）
     *
     * @param protocol RPC请求协议
     * @return null（单向调用不关心响应）
     */
    private RPCFuture sendRequestOneway(RpcProtocol<RpcRequest> protocol) {
        channel.writeAndFlush(protocol);
        return null;
    }

    /**
     * 创建并注册RPCFuture
     *
     * @param protocol RPC请求协议
     * @return 新创建的RPCFuture对象
     */
    private RPCFuture getRpcFuture(RpcProtocol<RpcRequest> protocol) {
        RPCFuture rpcFuture = new RPCFuture(protocol);
        RpcHeader header = protocol.getHeader();
        // 获取请求ID，用于匹配请求和响应
        long requestId = header.getRequestId();
        pendingRPC.put(requestId, rpcFuture);
        return rpcFuture;
    }

    /**
     * 关闭连接
     */
    public void close() {
        channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }
}