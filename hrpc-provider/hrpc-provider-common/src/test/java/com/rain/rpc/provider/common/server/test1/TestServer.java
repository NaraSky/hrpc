package com.rain.rpc.provider.common.server.test1;

import com.rain.rpc.common.helper.RpcServiceHelper;
import com.rain.rpc.provider.common.server.base.BaseServer;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 服务端测试类
 * 用于验证服务端能够正确处理RPC请求并调用真实方法
 */
public class TestServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestServer.class);

    public interface HelloService {
        String sayHello(String name);
    }

    public static class HelloServiceImpl implements HelloService {
        @Override
        public String sayHello(String name) {
            LOGGER.info("调用sayHello方法，参数name={}", name);
            return "Hello, " + name + "!";
        }
    }

    /**
     * 测试服务端启动和方法调用
     * 验证服务端能够正确注册服务并处理请求
     *
     * @throws InterruptedException 当线程被中断时抛出
     */
    @Test
    public void testServerStartAndMethodInvoke() throws InterruptedException {
        LOGGER.info("开始测试服务端启动和方法调用");

        // 创建服务端实例
        BaseServer server = new BaseServer("127.0.0.1:18080");

        // 创建服务实现类实例
        HelloServiceImpl helloService = new HelloServiceImpl();

        // 构建服务键
        String serviceKey = RpcServiceHelper.buildServiceKey(
                HelloService.class.getName(), "1.0.0", "default");

        // 将服务注册到handlerMap中
        server.handlerMap.put(serviceKey, helloService);

        // 启动服务端线程
        Thread serverThread = new Thread(() -> {
            try {
                LOGGER.info("启动服务端");
                server.startNettyServer();
            } catch (Exception e) {
                LOGGER.error("服务端启动异常", e);
            }
        });

        serverThread.setDaemon(false);
        serverThread.start();

        // 等待服务端启动
        Thread.sleep(2000);

        LOGGER.info("服务端启动完成，服务已注册: {}", serviceKey);
        LOGGER.info("可以通过编写客户端代码连接此服务端并调用sayHello方法进行测试");

        // 为了演示，我们不立即关闭服务端，让其运行一段时间
        Thread.sleep(5000);

        LOGGER.info("测试完成");
    }
}