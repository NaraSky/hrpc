package com.rain.rpc.serialization.api;

/**
 * 序列化接口，定义了序列化和反序列化的方法规范
 * 该接口作为RPC框架的序列化SPI扩展点，允许实现不同的序列化方式
 */
public interface Serialization {

    /**
     * 将对象序列化为字节数组
     * 
     * @param obj 待序列化的对象
     * @param <T> 对象类型
     * @return 序列化后的字节数组
     */
    <T> byte[] serialize(T obj);

    /**
     * 将字节数组反序列化为指定类型的对象
     * 
     * @param data 待反序列化的字节数组
     * @param clazz 目标对象的类类型
     * @param <T> 对象类型
     * @return 反序列化后的对象
     */
    <T> T deserialize(byte[] data, Class<T> clazz);
}