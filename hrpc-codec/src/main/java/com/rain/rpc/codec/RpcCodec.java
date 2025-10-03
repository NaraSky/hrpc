package com.rain.rpc.codec;

import com.rain.rpc.serialization.api.Serialization;
import com.rain.rpc.serialization.jdk.JdkSerialization;

/**
 * RPC编解码器接口
 * 提供获取序列化实现的默认方法，作为编解码器的SPI扩展点
 * 当前默认使用JDK序列化实现，未来可扩展支持其他序列化方式
 */
public interface RpcCodec {
    
    /**
     * 获取JDK序列化实现的默认方法
     * 该方法作为SPI扩展点，方便后续替换为其他序列化实现
     * 
     * @return JDK序列化实现实例
     */
    default Serialization getJdkSerialization() {
        return new JdkSerialization();
    }
}