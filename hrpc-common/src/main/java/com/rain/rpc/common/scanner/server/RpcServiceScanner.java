package com.rain.rpc.common.scanner.server;

import com.rain.rpc.annotation.RpcService;
import com.rain.rpc.common.scanner.ClassScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RpcService注解扫描器
 * 用于扫描并处理标记了@RpcService注解的类
 */
public class RpcServiceScanner {
    private static final Logger LOGGER = LoggerFactory.getLogger(RpcServiceScanner.class);

    /**
     * 扫描指定包下的类，并筛选使用@RpcService注解标注的类
     * 
     * @param scanPackage 需要扫描的包名
     * @return 包含所有标记了@RpcService注解的类实例的映射
     * @throws Exception 扫描过程中可能抛出的异常
     */
    public static Map<String, Object> doScannerWithRpcServiceAnnotationFilterAndRegistryService(String scanPackage) throws Exception {
        Map<String, Object> handlerMap = new HashMap<>();
        List<String> classNameList = ClassScanner.getClassNameList(scanPackage);
        if (classNameList == null || classNameList.isEmpty()) {
            return handlerMap;
        }
        
        for (String className : classNameList) {
            try {
                Class<?> clazz = Class.forName(className);
                RpcService rpcService = clazz.getAnnotation(RpcService.class);
                if (rpcService != null) {
                    // 优先使用interfaceClass, interfaceClass的name为空，再使用interfaceClassName
                    // TODO 后续逻辑向注册中心注册服务元数据，同时向handlerMap中记录标注了RpcService注解的类实例
                    LOGGER.info("当前标注了@RpcService注解的类实例名称 ===> {}", clazz.getName());
                    LOGGER.info("@RpcService注解上标注的属性信息如下：");
                    LOGGER.info("interfaceClass ===> {}", rpcService.interfaceClass().getName());
                    LOGGER.info("interfaceClassName ===> {}", rpcService.interfaceClassName());
                    LOGGER.info("version ===> {}", rpcService.version());
                    LOGGER.info("group ===> {}", rpcService.group());
                }
            } catch (Exception e) {
                LOGGER.error("scan classes throws exception: ", e);
            }
        }
        return handlerMap;
    }

    /**
     * 获取服务名称
     * 
     * @param rpcService RpcService注解实例
     * @return 服务名称
     */
    private static String getServiceName(RpcService rpcService) {
        // 优先使用interfaceClass
        Class<?> clazz = rpcService.interfaceClass();
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