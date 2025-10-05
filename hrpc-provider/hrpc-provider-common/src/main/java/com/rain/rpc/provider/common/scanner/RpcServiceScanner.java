package com.rain.rpc.provider.common.scanner;

import com.rain.rpc.annotation.RpcService;
import com.rain.rpc.common.helper.RpcServiceHelper;
import com.rain.rpc.protocol.meta.ServiceMeta;
import com.rain.rpc.registry.api.RegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.rain.rpc.common.scanner.ClassScanner.getClassNameList;

/**
 * RpcService注解扫描器
 * 用于扫描并处理标记了@RpcService注解的类
 */
public class RpcServiceScanner {
    private static final Logger LOGGER = LoggerFactory.getLogger(RpcServiceScanner.class);

    /**
     * 扫描指定包下带有@RpcService注解的类，并注册到注册中心
     * 
     * @param host 服务提供方主机地址
     * @param port 服务提供方端口号
     * @param scanPackage 需要扫描的包路径
     * @param registryService 注册中心服务实例
     * @return handlerMap 服务名称与服务实例的映射关系
     * @throws Exception 扫描或实例化过程中可能抛出的异常
     */
    public static Map<String, Object> doScannerWithRpcServiceAnnotationFilterAndRegistryService(String host, int port, String scanPackage, RegistryService registryService) throws Exception {
        Map<String, Object> handlerMap = new HashMap<>();
        List<String> classNameList = getClassNameList(scanPackage);
        if (classNameList == null || classNameList.isEmpty()) {
            return handlerMap;
        }
        classNameList.stream().forEach((className) -> {
            try {
                Class<?> clazz = Class.forName(className);
                RpcService rpcService = clazz.getAnnotation(RpcService.class);
                if (rpcService != null) {
                    //优先使用interfaceClass, interfaceClass的name为空，再使用interfaceClassName
                    ServiceMeta serviceMeta = new ServiceMeta(getServiceName(rpcService), rpcService.version(),  host, port, rpcService.group());
                    //将元数据注册到注册中心
                    registryService.register(serviceMeta);
                    handlerMap.put(RpcServiceHelper.buildServiceKey(serviceMeta.getServiceName(), serviceMeta.getServiceVersion(), serviceMeta.getServiceGroup()), clazz.newInstance());
                }
            } catch (Exception e) {
                LOGGER.error("scan classes throws exception: {}", e);
            }
        });
        return handlerMap;
    }

    /**
     * 获取服务名称
     * 优先使用@RpcService注解中指定的interfaceClass，如果未指定则使用interfaceClassName
     * 
     * @param rpcService RpcService注解实例
     * @return 服务名称
     */
    private static String getServiceName(RpcService rpcService) {
        //优先使用interfaceClass
        Class clazz = rpcService.interfaceClass();
        if (clazz == void.class) {
            return rpcService.interfaceClassName();
        }
        String serviceName = clazz.getName();
        if (serviceName == null || serviceName.trim().isEmpty()) {
            serviceName = rpcService.interfaceClassName();
        }
        return serviceName;
    }
}