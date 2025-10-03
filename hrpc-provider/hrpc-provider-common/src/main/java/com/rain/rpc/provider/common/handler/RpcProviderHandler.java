package com.rain.rpc.provider.common.handler;

import com.rain.rpc.protocol.RpcProtocol;
import com.rain.rpc.protocol.enumeration.RpcType;
import com.rain.rpc.protocol.header.RpcHeader;
import com.rain.rpc.protocol.request.RpcRequest;
import com.rain.rpc.protocol.response.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * RPC服务提供者处理器
 * 作为Netty管道中的一个处理节点，负责接收客户端请求并调用对应的服务实现
 * 继承SimpleChannelInboundHandler以处理RpcProtocol<RpcRequest>类型的消息
 */
public class RpcProviderHandler extends SimpleChannelInboundHandler<RpcProtocol<RpcRequest>> {
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
     * 处理接收到的RPC请求消息
     * 此方法是Netty事件循环中的核心处理方法，负责解析请求并生成响应
     * 当前实现为测试版本，实际应用中应根据请求调用对应的服务方法
     *
     * @param ctx ChannelHandlerContext上下文，用于与客户端进行通信
     * @param requestProtocol 接收到的RPC请求协议对象
     * @throws Exception 处理过程中可能抛出的异常
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcProtocol<RpcRequest> requestProtocol) throws Exception {
        // 记录接收到的请求数据，用于调试和监控
        LOGGER.info("RPC provider received request: {}", requestProtocol);
        
        // 记录当前注册的服务列表，用于调试和监控
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Current registered services:");
            handlerMap.forEach((serviceName, serviceInstance) -> 
                LOGGER.debug("Service: {} => Instance: {}", serviceName, serviceInstance.getClass().getName()));
        }

        // 从请求协议中提取头部和请求体信息
        RpcHeader requestHeader = requestProtocol.getHeader();
        RpcRequest request = requestProtocol.getBody();
        
        // 设置响应头部信息
        requestHeader.setMessageType((byte) RpcType.RESPONSE.getType());
        
        // 构建响应协议数据
        RpcProtocol<RpcResponse> responseProtocol = new RpcProtocol<>();
        RpcResponse response = new RpcResponse();
        
        // 设置响应结果，当前为测试信息，实际应为服务调用结果
        response.setResult("数据交互成功");
        // 保持请求中的异步标记
        response.setAsync(request.getAsync());
        // 保持请求中的单向调用标记
        response.setOneway(request.getOneway());
        
        // 设置响应协议的头部和体部
        responseProtocol.setHeader(requestHeader);
        responseProtocol.setBody(response);
        
        // 将响应写入通道并刷新发送给客户端
        ctx.writeAndFlush(responseProtocol);
        
        // 记录响应发送日志
        LOGGER.info("RPC response sent successfully for request ID: {}", requestHeader.getRequestId());
    }
    
    /**
     * 异常处理方法
     * 当处理请求过程中发生异常时，记录错误日志并关闭连接
     * 
     * @param ctx ChannelHandlerContext上下文
     * @param cause 异常对象
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOGGER.error("Error occurred in RPC provider handler", cause);
        // 关闭连接以释放资源
        ctx.close();
    }
}