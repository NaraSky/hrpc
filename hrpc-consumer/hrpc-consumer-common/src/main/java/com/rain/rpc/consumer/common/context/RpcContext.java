package com.rain.rpc.consumer.common.context;

import com.rain.rpc.proxy.api.future.RPCFuture;

/**
 * RPC上下文
 * 提供线程本地存储功能，用于在同一线程中传递RPC调用相关信息
 * 
 * 设计说明：
 * 1. 使用单例模式确保全局唯一实例
 * 2. 使用InheritableThreadLocal确保子线程可以继承父线程的RPCFuture
 * 3. 提供线程安全的RPCFuture存储和获取方法
 */
public class RpcContext {

    /**
     * 私有构造函数，防止外部实例化
     */
    private RpcContext(){
    }

    private static final RpcContext INSTANCE = new RpcContext();

    /**
     * 存放RPCFuture的InheritableThreadLocal
     * 使用InheritableThreadLocal确保子线程可以继承父线程的RPCFuture
     */
    private static final InheritableThreadLocal<RPCFuture> RPC_FUTURE_THREAD_LOCAL = new InheritableThreadLocal<>();

    /**
     * 获取RpcContext单例实例
     * 
     * @return RpcContext单例
     */
    public static RpcContext getContext(){
        return INSTANCE;
    }

    /**
     * 在线程本地存储中设置RPCFuture
     * 
     * @param rpcFuture RPCFuture实例
     */
    public void setRPCFuture(RPCFuture rpcFuture){
        RPC_FUTURE_THREAD_LOCAL.set(rpcFuture);
    }

    /**
     * 从线程本地存储中获取RPCFuture
     * 
     * @return RPCFuture实例
     */
    public RPCFuture getRPCFuture(){
        return RPC_FUTURE_THREAD_LOCAL.get();
    }

    /**
     * 清除线程本地存储中的RPCFuture
     */
    public void removeRPCFuture(){
        RPC_FUTURE_THREAD_LOCAL.remove();
    }
}