package com.rain.rpc.protocol;

import com.rain.rpc.protocol.header.RpcHeader;
import com.rain.rpc.protocol.header.RpcHeaderFactory;
import com.rain.rpc.protocol.request.RpcRequest;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * RPC协议测试类
 * 用于测试RPC协议相关的功能
 */
public class MessageTest {
    
    /**
     * 测试获取RPC协议对象的功能
     * 验证创建的RPC协议对象是否包含正确的头部和请求体信息
     */
    @Test
    public void testGetRpcProtocol() {
        // 获取RPC协议对象
        RpcProtocol<RpcRequest> protocol = getRpcProtocol();
        
        // 验证协议对象不为null
        assertNotNull(protocol, "RPC协议对象不应为null");
        
        // 验证协议头部
        RpcHeader header = protocol.getHeader();
        assertNotNull(header, "协议头部不应为null");
        assertEquals("jdk", header.getSerializationType(), "序列化类型应为jdk");
        
        // 验证协议请求体
        RpcRequest request = protocol.getBody();
        assertNotNull(request, "请求体不应为null");
        assertFalse(request.isOneway(), "应不是单向调用");
        assertFalse(request.isAsync(), "应不是异步调用");
        assertEquals("com.raine.rpc.protocol.RpcProtocol", request.getClassName(), "类名应正确");
        assertEquals("hello", request.getMethodName(), "方法名应正确");
        assertEquals("rain", request.getGroup(), "组名应正确");
        assertEquals("1.0.0", request.getVersion(), "版本号应正确");
        
        // 验证参数
        Object[] parameters = request.getParameters();
        assertNotNull(parameters, "参数数组不应为null");
        assertEquals(1, parameters.length, "应有一个参数");
        assertEquals("rain", parameters[0], "参数值应正确");
        
        // 验证参数类型
        Class<?>[] parameterTypes = request.getParameterTypes();
        assertNotNull(parameterTypes, "参数类型数组不应为null");
        assertEquals(1, parameterTypes.length, "应有一个参数类型");
        assertEquals(String.class, parameterTypes[0], "参数类型应正确");
    }
    
    /**
     * 创建RPC协议对象的工厂方法
     * 用于生成测试用的RPC协议对象
     *
     * @return RPC协议对象
     */
    public static RpcProtocol<RpcRequest> getRpcProtocol(){
        RpcHeader header = RpcHeaderFactory.getRequestHeader("jdk");
        RpcRequest body = new RpcRequest();
        body.setOneway(false);
        body.setAsync(false);
        body.setClassName("com.raine.rpc.protocol.RpcProtocol");
        body.setMethodName("hello");
        body.setGroup("rain");
        body.setParameters(new Object[]{"rain"});
        body.setParameterTypes(new Class[]{String.class});
        body.setVersion("1.0.0");
        RpcProtocol<RpcRequest> protocol = new RpcProtocol<>();
        protocol.setBody(body);
        protocol.setHeader(header);
        return protocol;
    }
}