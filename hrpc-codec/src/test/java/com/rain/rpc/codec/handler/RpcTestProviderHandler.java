package com.rain.rpc.codec.handler;

import com.alibaba.fastjson.JSONObject;
import com.rain.rpc.protocol.RpcProtocol;
import com.rain.rpc.protocol.enumeration.RpcType;
import com.rain.rpc.protocol.header.RpcHeader;
import com.rain.rpc.protocol.request.RpcRequest;
import com.rain.rpc.protocol.response.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RPC测试服务提供者处理器
 * 负责处理来自客户端的请求，并返回相应的响应
 * 继承SimpleChannelInboundHandler以处理RpcProtocol<RpcRequest>类型的消息
 */
public class RpcTestProviderHandler extends SimpleChannelInboundHandler<RpcProtocol<RpcRequest>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RpcTestProviderHandler.class);

    /**
     * 处理从客户端接收到的请求数据
     * 
     * @param channelHandlerContext ChannelHandlerContext上下文
     * @param requestProtocol 从客户端接收到的请求协议对象
     * @throws Exception 处理过程中可能抛出的异常
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcProtocol<RpcRequest> requestProtocol) throws Exception {
        // 记录接收到的请求数据
        LOGGER.info("Received request data from client ===>>> {}", JSONObject.toJSONString(requestProtocol));
        
        // 获取请求头部和体部
        RpcHeader requestHeader = requestProtocol.getHeader();
        RpcRequest request = requestProtocol.getBody();
        
        // 打印请求详情
        LOGGER.info("Processing request: class={}, method={}, params={}", request.getClassName(), request.getMethodName(), request.getParameters());
        
        // 构造响应协议对象
        RpcProtocol<RpcResponse> responseProtocol = new RpcProtocol<>();
        
        // 复用请求头部，并修改消息类型为响应
        requestHeader.setMessageType((byte) RpcType.RESPONSE.getType());
        responseProtocol.setHeader(requestHeader);
        
        // 构造响应体
        RpcResponse response = new RpcResponse();
        response.setResult("Hello " + (request.getParameters().length > 0 ? request.getParameters()[0] : "world") + "! Response from RPC test provider.");
        response.setAsync(request.getAsync());
        response.setOneway(request.getOneway());
        responseProtocol.setBody(response);
        
        // 记录发送的响应数据
        LOGGER.info("Sending response data to client ===>>> {}", JSONObject.toJSONString(responseProtocol));
        
        // 发送响应数据到客户端
        channelHandlerContext.writeAndFlush(responseProtocol);
        
        LOGGER.info("Response sent successfully for request ID: {}", requestHeader.getRequestId());
    }
    
    /**
     * 异常处理方法
     * 当处理过程中发生异常时调用此方法
     * 
     * @param ctx ChannelHandlerContext上下文
     * @param cause 异常对象
     * @throws Exception 处理过程中可能抛出的异常
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error("Exception occurred in RPC test provider handler", cause);
        // 关闭连接
        ctx.close();
    }
    
    /**
     * 当连接不可用时调用此方法
     * 
     * @param ctx ChannelHandlerContext上下文
     * @throws Exception 处理过程中可能抛出的异常
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LOGGER.warn("Channel inactive. A client connection has been lost.");
        super.channelInactive(ctx);
    }
}