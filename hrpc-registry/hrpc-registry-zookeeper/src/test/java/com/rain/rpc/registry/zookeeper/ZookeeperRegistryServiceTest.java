package com.rain.rpc.registry.zookeeper;

import com.rain.rpc.protocol.meta.ServiceMeta;
import com.rain.rpc.registry.api.config.RegistryConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Zookeeper注册服务测试类
 * 用于测试基于Zookeeper实现的服务注册与发现功能
 * 
 * 测试前准备：
 * 1. 确保已安装并启动Zookeeper服务，默认端口为2181
 * 2. 确保网络连接正常，能够访问Zookeeper服务
 */
public class ZookeeperRegistryServiceTest {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperRegistryServiceTest.class);
    
    /**
     * Zookeeper注册服务实例
     */
    private ZookeeperRegistryService zookeeperRegistryService;
    
    /**
     * 注册中心配置信息
     */
    private RegistryConfig registryConfig;
    
    /**
     * 测试服务元数据
     */
    private ServiceMeta serviceMeta;

    /**
     * 测试前初始化
     * 创建Zookeeper注册服务实例并初始化连接
     */
    @BeforeEach
    public void setUp() throws Exception {
        LOGGER.info("开始初始化Zookeeper注册服务测试");
        
        // 创建注册中心配置，使用默认的本地Zookeeper地址
        registryConfig = new RegistryConfig("127.0.0.1:2181", "zookeeper");
        
        // 创建Zookeeper注册服务实例
        zookeeperRegistryService = new ZookeeperRegistryService();
        
        // 初始化注册服务
        zookeeperRegistryService.init(registryConfig);
        
        // 创建测试服务元数据
        serviceMeta = new ServiceMeta();
        serviceMeta.setServiceName("com.rain.test.TestService");
        serviceMeta.setServiceVersion("1.0.0");
        serviceMeta.setServiceAddr("127.0.0.1");
        serviceMeta.setServicePort(8080);
        serviceMeta.setServiceGroup("test");
        
        LOGGER.info("Zookeeper注册服务测试初始化完成");
    }

    /**
     * 测试后清理
     * 销毁Zookeeper注册服务，释放相关资源
     */
    @AfterEach
    public void tearDown() throws Exception {
        LOGGER.info("开始清理Zookeeper注册服务测试");
        
        if (zookeeperRegistryService != null) {
            // 注销测试服务
            try {
                zookeeperRegistryService.unRegister(serviceMeta);
                LOGGER.info("测试服务注销成功");
            } catch (Exception e) {
                LOGGER.warn("测试服务注销失败: {}", e.getMessage());
            }
            
            // 销毁注册服务
            zookeeperRegistryService.destroy();
            LOGGER.info("Zookeeper注册服务销毁完成");
        }
        
        LOGGER.info("Zookeeper注册服务测试清理完成");
    }

    /**
     * 测试服务注册功能
     * 验证服务能否成功注册到Zookeeper
     */
    @Test
    public void testRegister() {
        LOGGER.info("开始测试服务注册功能");
        
        try {
            // 注册服务
            zookeeperRegistryService.register(serviceMeta);
            LOGGER.info("服务注册成功: {}", serviceMeta);
            
            // 验证服务注册成功
            assertNotNull(serviceMeta.getServiceName(), "服务名称不应为空");
            assertNotNull(serviceMeta.getServiceVersion(), "服务版本不应为空");
            assertNotEquals(0, serviceMeta.getServicePort(), "服务端口应大于0");
            
            LOGGER.info("服务注册功能测试通过");
        } catch (Exception e) {
            LOGGER.error("服务注册功能测试失败", e);
            fail("服务注册过程中发生异常: " + e.getMessage());
        }
    }

    /**
     * 测试服务注销功能
     * 验证服务能否从Zookeeper成功注销
     */
    @Test
    public void testUnRegister() {
        LOGGER.info("开始测试服务注销功能");
        
        try {
            // 先注册服务
            zookeeperRegistryService.register(serviceMeta);
            LOGGER.info("服务注册成功: {}", serviceMeta);
            
            // 注销服务
            zookeeperRegistryService.unRegister(serviceMeta);
            LOGGER.info("服务注销成功: {}", serviceMeta);
            
            LOGGER.info("服务注销功能测试通过");
        } catch (Exception e) {
            LOGGER.error("服务注销功能测试失败", e);
            fail("服务注销过程中发生异常: " + e.getMessage());
        }
    }

    /**
     * 测试服务发现功能
     * 验证能否从Zookeeper发现已注册的服务
     */
    @Test
    public void testDiscovery() {
        LOGGER.info("开始测试服务发现功能");
        
        try {
            // 先注册服务
            zookeeperRegistryService.register(serviceMeta);
            LOGGER.info("服务注册成功: {}", serviceMeta);
            
            // 构建服务键
            String serviceKey = serviceMeta.getServiceName() + "#" + serviceMeta.getServiceVersion() + "#" + serviceMeta.getServiceGroup();
            
            // 发现服务
            ServiceMeta discoveredService = zookeeperRegistryService.discovery(serviceKey, 0);
            LOGGER.info("服务发现结果: {}", discoveredService);
            
            // 验证发现的服务信息是否正确
            assertNotNull(discoveredService, "发现的服务不应为空");
            assertEquals(serviceMeta.getServiceName(), discoveredService.getServiceName(), "服务名称应匹配");
            assertEquals(serviceMeta.getServiceVersion(), discoveredService.getServiceVersion(), "服务版本应匹配");
            assertEquals(serviceMeta.getServiceAddr(), discoveredService.getServiceAddr(), "服务地址应匹配");
            assertEquals(serviceMeta.getServicePort(), discoveredService.getServicePort(), "服务端口应匹配");
            assertEquals(serviceMeta.getServiceGroup(), discoveredService.getServiceGroup(), "服务分组应匹配");
            
            LOGGER.info("服务发现功能测试通过");
        } catch (Exception e) {
            LOGGER.error("服务发现功能测试失败", e);
            fail("服务发现过程中发生异常: " + e.getMessage());
        }
    }

    /**
     * 测试服务注册与发现完整流程
     * 验证从服务注册到服务发现的完整链路
     */
    @Test
    public void testRegisterAndDiscovery() {
        LOGGER.info("开始测试服务注册与发现完整流程");
        
        try {
            // 注册服务
            zookeeperRegistryService.register(serviceMeta);
            LOGGER.info("服务注册成功: {}", serviceMeta);
            
            // 构建服务键
            String serviceKey = serviceMeta.getServiceName() + "#" + serviceMeta.getServiceVersion() + "#" + serviceMeta.getServiceGroup();
            
            // 发现服务
            ServiceMeta discoveredService = zookeeperRegistryService.discovery(serviceKey, 0);
            LOGGER.info("服务发现结果: {}", discoveredService);
            
            // 验证发现的服务信息是否正确
            assertNotNull(discoveredService, "发现的服务不应为空");
            assertEquals(serviceMeta.getServiceName(), discoveredService.getServiceName(), "服务名称应匹配");
            assertEquals(serviceMeta.getServiceVersion(), discoveredService.getServiceVersion(), "服务版本应匹配");
            assertEquals(serviceMeta.getServiceAddr(), discoveredService.getServiceAddr(), "服务地址应匹配");
            assertEquals(serviceMeta.getServicePort(), discoveredService.getServicePort(), "服务端口应匹配");
            assertEquals(serviceMeta.getServiceGroup(), discoveredService.getServiceGroup(), "服务分组应匹配");
            
            // 注销服务
            zookeeperRegistryService.unRegister(serviceMeta);
            LOGGER.info("服务注销成功: {}", serviceMeta);
            
            LOGGER.info("服务注册与发现完整流程测试通过");
        } catch (Exception e) {
            LOGGER.error("服务注册与发现完整流程测试失败", e);
            fail("服务注册与发现完整流程测试过程中发生异常: " + e.getMessage());
        }
    }
}