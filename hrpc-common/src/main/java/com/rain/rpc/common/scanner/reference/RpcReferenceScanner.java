package com.rain.rpc.common.scanner.reference;

import com.rain.rpc.annotation.RpcReference;
import com.rain.rpc.common.scanner.ClassScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * RpcReference注解扫描器
 * 用于扫描并处理标记了@RpcReference注解的字段
 */
public class RpcReferenceScanner {
    private static final Logger LOGGER = LoggerFactory.getLogger(RpcReferenceScanner.class);

    /**
     * 扫描指定包下的类，并筛选使用@RpcReference注解标注的字段
     * 
     * @param scanPackage 需要扫描的包名
     * @return 包含所有标记了@RpcReference注解的字段相关信息的映射
     * @throws Exception 扫描过程中可能抛出的异常
     */
    public static Map<String, Object> doScannerWithRpcReferenceAnnotationFilter(String scanPackage) throws Exception {
        Map<String, Object> handlerMap = new HashMap<>();
        List<String> classNameList = ClassScanner.getClassNameList(scanPackage);
        if (classNameList == null || classNameList.isEmpty()) {
            return handlerMap;
        }
        
        for (String className : classNameList) {
            try {
                Class<?> clazz = Class.forName(className);
                Field[] declaredFields = clazz.getDeclaredFields();
                for (Field field : declaredFields) {
                    RpcReference rpcReference = field.getAnnotation(RpcReference.class);
                    if (rpcReference != null) {
                        // TODO 处理后续逻辑，将@RpcReference注解标注的接口引用代理对象，放入全局缓存中
                        LOGGER.info("当前标注了@RpcReference注解的字段名称 ===> {}", field.getName());
                        LOGGER.info("@RpcReference注解上标注的属性信息如下：");
                        LOGGER.info("version ===> {}", rpcReference.version());
                        LOGGER.info("group ===> {}", rpcReference.group());
                        LOGGER.info("registryType ===> {}", rpcReference.registryType());
                        LOGGER.info("registryAddress ===> {}", rpcReference.registryAddress());
                    }
                }
            } catch (Exception e) {
                LOGGER.error("scan classes throws exception: ", e);
            }
        }
        return handlerMap;
    }
}