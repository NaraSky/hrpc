package com.rain.rpc.provider.common.server.test1;

import com.rain.rpc.codec.RpcDecoder;
import com.rain.rpc.codec.RpcEncoder;
import com.rain.rpc.protocol.RpcProtocol;
import com.rain.rpc.protocol.header.RpcHeaderFactory;
import com.rain.rpc.protocol.request.RpcRequest;
import com.rain.rpc.protocol.response.RpcResponse;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

/**
 * 测试客户端类
 * 用于连接测试服务端并验证RPC方法调用功能
 */
public class TestClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestClient.class);
    private CompletableFuture<RpcResponse> responseFuture = new CompletableFuture<>();

    /**
     * 测试客户端处理器
     * 处理从服务端返回的响应数据
     */
    private class TestClientHandler extends SimpleChannelInboundHandler<RpcProtocol<RpcResponse>> {
        /**
         * 处理从服务端接收到的响应数据
         *
         * @param ctx             ChannelHandlerContext上下文
         * @param responseProtocol 从服务端接收到的响应协议对象
         */
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, RpcProtocol<RpcResponse> responseProtocol) {
            LOGGER.info("接收到服务端响应: {}", responseProtocol);
            responseFuture.complete(responseProtocol.getBody());
        }

        /**
         * 异常处理方法
         *
         * @param ctx   ChannelHandlerContext上下文
         * @param cause 异常对象
         */
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            LOGGER.error("客户端发生异常", cause);
            responseFuture.completeExceptionally(cause);
            ctx.close();
        }
    }

    /**
     * 测试客户端连接和服务调用
     * 验证客户端能够成功连接服务端并调用远程方法
     *
     * @throws Exception 当发生异常时抛出
     */
    @Test
    public void testClientConnectAndCall() throws Exception {
        LOGGER.info("开始测试客户端连接和服务调用");

        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline()
                                    .addLast(new RpcDecoder())
                                    .addLast(new RpcEncoder())
                                    .addLast(new TestClientHandler());
                        }
                    });

            // 连接到服务端
            ChannelFuture future = bootstrap.connect("127.0.0.1", 18080).sync();
            LOGGER.info("客户端已连接到服务端");

            // 构造RPC请求
            RpcProtocol<RpcRequest> requestProtocol = new RpcProtocol<>();
            requestProtocol.setHeader(RpcHeaderFactory.getRequestHeader("jdk"));

            RpcRequest request = new RpcRequest();
            request.setClassName("com.rain.rpc.provider.common.server.test1.TestServer$HelloService");
            request.setMethodName("sayHello");
            request.setParameterTypes(new Class[]{String.class});
            request.setParameters(new Object[]{"RPC测试"});
            request.setVersion("1.0.0");
            request.setGroup("default");

            requestProtocol.setBody(request);

            LOGGER.info("发送请求: {}", request);

            // 发送请求到服务端
            future.channel().writeAndFlush(requestProtocol);

            // 等待响应
            RpcResponse response = responseFuture.get();
            LOGGER.info("接收到响应结果: {}", response.getResult());

            // 验证结果
            if (response.getResult() != null && response.getResult().equals("Hello, RPC测试!")) {
                LOGGER.info("RPC调用测试成功");
            } else {
                LOGGER.error("RPC调用测试失败，期望结果不匹配");
            }

            // 等待一段时间以便观察日志
            Thread.sleep(2000);
        } finally {
            group.shutdownGracefully();
        }
    }
}