package com.rain.rpc.provider.common.server.api;

/**
 * 服务接口
 * 定义服务器的基本操作
 */
public interface Server {

    /**
     * 启动Netty服务
     * 该方法负责初始化并启动Netty服务器
     */
    void startNettyServer();
}