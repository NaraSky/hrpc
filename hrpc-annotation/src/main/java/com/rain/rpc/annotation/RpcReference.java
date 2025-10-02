package com.rain.rpc.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 服务引用注解，标注在服务调用方，表明该类需要被RPC框架管理，并调用远程服务
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface RpcReference {
    /**
     * 版本号
     */
    String version() default "1.0.0";

    /**
     * 服务注册中心类型，默认为zookeeper
     * todo 后续支持nacos，consul，etcd
     */
    String registryType() default "zookeeper";

    /**
     * 服务注册中心地址，默认为localhost:2181
     */
    String registryAddress() default "127.0.0.1:2181";

    /**
     * 服务负载均衡类型，默认基于ZK的一致性Hash
     */
    String loadBalanceType() default "zkconsistenthash";

    /**
     * 序列化类型，目前的类型包含：protostuff、kryo、json、jdk、hessian2、fst
     */
    String serializationType() default "protostuff";

    /**
     * 服务调用超时时间，默认5秒
     */
    long timeout() default 5000;

    /**
     * 是否异步调用，默认为false
     */
    boolean async() default false;

    /**
     * 是否单向调用，默认为false
     */
    boolean oneway() default false;

    /**
     * 代理的类型，jdk：jdk代理，javassist: javassist代理, cglib: cglib代理
     */
    String proxy() default "jdk";

    /**
     * 服务分组，默认为空
     */
    String group() default "";
}
