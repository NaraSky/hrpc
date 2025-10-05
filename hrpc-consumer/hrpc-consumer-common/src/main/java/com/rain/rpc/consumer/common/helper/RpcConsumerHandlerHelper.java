package com.rain.rpc.consumer.common.helper;

import com.rain.rpc.consumer.common.handler.RpcConsumerHandler;
import com.rain.rpc.protocol.meta.ServiceMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RPC消费者处理器助手类
 * 
 * 负责管理RpcConsumerHandler实例的缓存，避免重复创建连接，提高性能
 */
public class RpcConsumerHandlerHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(RpcConsumerHandlerHelper.class);
    private static Map<String, RpcConsumerHandler> rpcConsumerHandlerMap;

    static {
        rpcConsumerHandlerMap = new ConcurrentHashMap<>();
    }

    /**
     * 根据服务元数据生成缓存键
     * 
     * @param key 服务元数据
     * @return 缓存键，格式为"地址_端口"
     */
    private static String getKey(ServiceMeta key) {
        return key.getServiceAddr().concat("_").concat(String.valueOf(key.getServicePort()));
    }

    /**
     * 将RpcConsumerHandler实例放入缓存
     * 
     * @param key 服务元数据
     * @param value RpcConsumerHandler实例
     */
    public static void put(ServiceMeta key, RpcConsumerHandler value) {
        String cacheKey = getKey(key);
        LOGGER.debug("Adding RpcConsumerHandler to cache with key: {}", cacheKey);
        rpcConsumerHandlerMap.put(cacheKey, value);
    }

    /**
     * 从缓存中获取RpcConsumerHandler实例
     * 
     * @param key 服务元数据
     * @return RpcConsumerHandler实例，如果不存在则返回null
     */
    public static RpcConsumerHandler get(ServiceMeta key) {
        String cacheKey = getKey(key);
        RpcConsumerHandler handler = rpcConsumerHandlerMap.get(cacheKey);
        LOGGER.debug("Retrieving RpcConsumerHandler from cache with key: {}, found: {}", cacheKey, handler != null);
        return handler;
    }

    /**
     * 关闭并清理所有RpcConsumerHandler实例
     * 
     * 遍历缓存中的所有RpcConsumerHandler实例，逐个关闭连接，并清空缓存
     */
    public static void closeRpcClientHandler() {
        Collection<RpcConsumerHandler> rpcClientHandlers = rpcConsumerHandlerMap.values();
        if (rpcClientHandlers != null) {
            LOGGER.info("Closing {} RpcConsumerHandler instances", rpcClientHandlers.size());
            rpcClientHandlers.stream().forEach((rpcClientHandler) -> {
                rpcClientHandler.close();
            });
        }
        rpcConsumerHandlerMap.clear();
        LOGGER.info("Cleared RpcConsumerHandler cache");
    }
}
