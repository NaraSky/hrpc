package com.rain.rpc.registry.zookeeper;

import com.rain.rpc.common.helper.RpcServiceHelper;
import com.rain.rpc.protocol.meta.ServiceMeta;
import com.rain.rpc.registry.api.RegistryService;
import com.rain.rpc.registry.api.config.RegistryConfig;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;
import org.checkerframework.checker.units.qual.C;

import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 * 基于Zookeeper实现的注册服务
 * 使用Apache Curator框架操作Zookeeper，实现服务的注册、发现和注销功能
 */
public class ZookeeperRegistryService implements RegistryService {
    /**
     * 重试策略基础睡眠时间（毫秒）
     */
    public static final int BASE_SLEEP_TIME_MS = 1000;
    
    /**
     * 最大重试次数
     */
    public static final int MAX_RETRIES = 3;
    
    /**
     * Zookeeper中服务注册的基础路径
     */
    public static final String ZK_BASE_PATH = "/rain_rpc";

    /**
     * 服务发现组件，用于服务的注册与发现
     */
    private ServiceDiscovery<ServiceMeta> serviceDiscovery;

    /**
     * 初始化Zookeeper注册服务
     * 创建Curator客户端并启动服务发现组件
     * 
     * @param registryConfig 注册中心配置信息
     * @throws Exception 初始化过程中可能抛出的异常
     */
    @Override
    public void init(RegistryConfig registryConfig) throws Exception {
        // 创建Curator客户端，使用指数退避重试策略
        CuratorFramework client = CuratorFrameworkFactory.newClient(registryConfig.getRegistryAddr(), new ExponentialBackoffRetry(BASE_SLEEP_TIME_MS, MAX_RETRIES));
        client.start();
        
        // 创建JSON序列化器，用于序列化服务元数据
        JsonInstanceSerializer serializer = new JsonInstanceSerializer<>(ServiceMeta.class);
        
        // 构建并启动服务发现组件
        this.serviceDiscovery = ServiceDiscoveryBuilder.builder(ServiceMeta.class)
                .client(client)
                .serializer(serializer)
                .basePath(ZK_BASE_PATH)
                .build();
        this.serviceDiscovery.start();
    }

    /**
     * 注册服务到Zookeeper
     * 
     * @param serviceMeta 服务元数据信息
     * @throws Exception 注册过程中可能抛出的异常
     */
    @Override
    public void register(ServiceMeta serviceMeta) throws Exception {
        // 构建服务实例对象
        ServiceInstance<ServiceMeta> serviceInstance = ServiceInstance
                .<ServiceMeta>builder()
                .name(RpcServiceHelper.buildServiceKey(serviceMeta.getServiceName(), serviceMeta.getServiceVersion(), serviceMeta.getServiceGroup()))
                .address(serviceMeta.getServiceAddr())
                .port(serviceMeta.getServicePort())
                .payload(serviceMeta)
                .build();
        
        // 注册服务实例
        serviceDiscovery.registerService(serviceInstance);
    }

    /**
     * 从Zookeeper注销服务
     * 
     * @param serviceMeta 服务元数据信息
     * @throws Exception 注销过程中可能抛出的异常
     */
    @Override
    public void unRegister(ServiceMeta serviceMeta) throws Exception {
        // 构建服务实例对象
        ServiceInstance<ServiceMeta> serviceInstance = ServiceInstance
                .<ServiceMeta>builder()
                .name(serviceMeta.getServiceName())
                .address(serviceMeta.getServiceAddr())
                .port(serviceMeta.getServicePort())
                .payload(serviceMeta)
                .build();
        
        // 注销服务实例
        serviceDiscovery.unregisterService(serviceInstance);
    }

    /**
     * 服务发现
     * 根据服务名称查找可用的服务实例，并通过负载均衡策略选择一个实例
     * 
     * @param serviceName 服务名称
     * @param invokerHashCode 调用方哈希码，用于负载均衡策略
     * @return 服务元数据信息
     * @throws Exception 发现服务过程中可能抛出的异常
     */
    @Override
    public ServiceMeta discovery(String serviceName, int invokerHashCode) throws Exception {
        // 查询所有可用的服务实例
        Collection<ServiceInstance<ServiceMeta>> serviceInstances = serviceDiscovery.queryForInstances(serviceName);
        
        // 通过负载均衡策略选择一个服务实例
        ServiceInstance<ServiceMeta> instance = this.selectOneServiceInstance((List<ServiceInstance<ServiceMeta>>) serviceInstances);
        
        // 返回选中的服务实例的元数据
        if (instance != null) {
            return instance.getPayload();
        }
        return null;
    }

    /**
     * 负载均衡策略：随机选择一个服务实例
     * 
     * @param serviceInstances 服务实例列表
     * @return 选中的服务实例
     */
    private ServiceInstance<ServiceMeta> selectOneServiceInstance(List<ServiceInstance<ServiceMeta>> serviceInstances){
        // 检查服务实例列表是否为空
        if (serviceInstances == null || serviceInstances.isEmpty()){
            return null;
        }
        
        // 随机选择一个服务实例
        Random random = new Random();
        int index = random.nextInt(serviceInstances.size());
        return serviceInstances.get(index);
    }

    /**
     * 销毁Zookeeper注册服务
     * 关闭服务发现组件，释放相关资源
     * 
     * @throws Exception 销毁过程中可能抛出的异常
     */
    @Override
    public void destroy() throws Exception {
        serviceDiscovery.close();
    }
}