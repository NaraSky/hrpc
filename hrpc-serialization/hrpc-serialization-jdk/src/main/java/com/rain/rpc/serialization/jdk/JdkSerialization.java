package com.rain.rpc.serialization.jdk;

import com.rain.rpc.common.exception.SerializerException;
import com.rain.rpc.serialization.api.Serialization;

import java.io.*;

/**
 * 基于JDK原生序列化的实现类
 * 使用Java内置的对象流进行序列化和反序列化操作
 * 注意：JDK原生序列化性能相对较低，且序列化后的数据较大，生产环境建议使用其他高性能序列化框架如Protobuf、Kryo等
 */
public class JdkSerialization implements Serialization {
    
    /**
     * 将对象序列化为字节数组
     * 通过对象输出流将对象写入到字节数组输出流中，然后返回字节数组
     *
     * @param obj 待序列化的对象，不能为null
     * @param <T> 对象类型
     * @return 序列化后的字节数组
     * @throws SerializerException 当对象为null或序列化过程中发生IO异常时抛出
     */
    @Override
    public <T> byte[] serialize(T obj) {
        // 防止NPE，确保待序列化的对象不为null
        if (obj == null) {
            throw new SerializerException("serialize object is null");
        }
        
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
            
            // 使用对象输出流将对象写入到字节数组输出流中
            objectOutputStream.writeObject(obj);
            // 返回序列化后的字节数组
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            // 序列化过程中发生IO异常，包装为SerializerException抛出
            throw new SerializerException(e.getMessage(), e);
        }
    }

    /**
     * 将字节数组反序列化为指定类型的对象
     * 通过对象输入流从字节数组输入流中读取对象
     *
     * @param data 待反序列化的字节数组，不能为null
     * @param clazz 目标对象的类类型
     * @param <T> 对象类型
     * @return 反序列化后的对象
     * @throws SerializerException 当字节数组为null或反序列化过程中发生IO异常或ClassNotFoundException时抛出
     */
    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        // 防止NPE，确保待反序列化的字节数组不为null
        if (data == null) {
            throw new SerializerException("deserialize data is null");
        }
        
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
             ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)) {
            
            // 使用对象输入流从字节数组输入流中读取对象，并强制转换为目标类型
            return (T) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            // 反序列化过程中发生IO异常或找不到对应类时，包装为SerializerException抛出
            throw new SerializerException(e.getMessage(), e);
        }
    }
}