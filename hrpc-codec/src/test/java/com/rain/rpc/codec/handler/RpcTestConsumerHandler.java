package com.rain.rpc.codec.handler;

import com.alibaba.fastjson.JSONObject;
import com.rain.rpc.protocol.RpcProtocol;
import com.rain.rpc.protocol.header.RpcHeaderFactory;
import com.rain.rpc.protocol.request.RpcRequest;
import com.rain.rpc.protocol.response.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RPC测试消费者处理器
 * 负责处理与服务端的通信，包括发送请求和接收响应
 * 继承SimpleChannelInboundHandler以处理RpcProtocol<RpcResponse>类型的消息
 */
public class RpcTestConsumerHandler extends SimpleChannelInboundHandler<RpcProtocol<RpcResponse>> {
    private final Logger logger = LoggerFactory.getLogger(RpcTestConsumerHandler.class);

    /**
     * 当通道激活时调用此方法（连接建立成功后）
     * 在此方法中构造并发送测试请求数据到服务端
     * 
     * @param ctx ChannelHandlerContext上下文
     * @throws Exception 处理过程中可能抛出的异常
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("Channel active, starting to send test data...");
        
        // 构造RPC协议对象
        RpcProtocol<RpcRequest> protocol = new RpcProtocol<>();
        
        // 设置协议头部，使用请求类型头部，序列化方式为JDK
        protocol.setHeader(RpcHeaderFactory.getRequestHeader("jdk"));
        
        // 构造请求体
        RpcRequest request = new RpcRequest();
        request.setClassName("cin.rain.rpc.test.DemoService"); // 模拟服务类名
        request.setGroup("rain"); // 服务分组
        request.setMethodName("hello"); // 调用方法名
        request.setParameters(new Object[]{"rain"}); // 方法参数
        request.setParameterTypes(new Class[]{String.class}); // 参数类型
        request.setVersion("1.0.0"); // 服务版本
        request.setAsync(false); // 是否异步调用
        request.setOneway(false); // 是否单向调用
        
        // 设置协议体
        protocol.setBody(request);
        
        // 记录发送的数据
        logger.info("Sending request data to server ===>>> {}", JSONObject.toJSONString(protocol));
        
        // 发送数据到服务端
        ctx.writeAndFlush(protocol);
        
        logger.info("Test data sent successfully");
    }

    /**
     * 处理从服务端接收到的响应数据
     * 
     * @param channelHandlerContext ChannelHandlerContext上下文
     * @param protocol 从服务端接收到的响应协议对象
     * @throws Exception 处理过程中可能抛出的异常
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcProtocol<RpcResponse> protocol) throws Exception {
        // 记录接收到的响应数据
        logger.info("Received response data from server ===>>> {}", JSONObject.toJSONString(protocol));
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
        logger.error("Exception occurred in RPC test consumer handler", cause);
        // 关闭连接
        ctx.close();
    }
}