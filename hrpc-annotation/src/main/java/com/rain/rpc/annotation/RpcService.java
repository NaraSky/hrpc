package com.rain.rpc.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 服务提供者注解，标注在服务实现类上，表明该类需要被RPC框架管理和暴露为远程服务
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface RpcService {
    // Class 本质上表示运行时的类和接口的信息，可以获取类的所有信息，包括方法、字段、构造函数等
    Class<?> interfaceClass() default void.class;

    String interfaceClassName() default "";

    String version() default "1.0.0";

    String group() default "";
}
