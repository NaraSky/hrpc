package com.rain.rpc.protocol.request;

import com.rain.rpc.protocol.base.RpcMessage;

/**
 * RPC请求数据载荷类
 * 包含远程调用所需的所有信息
 */
public class RpcRequest extends RpcMessage {

    private static final long serialVersionUID = 7604945741428075409L;

    /**
     * 服务接口类名
     */
    private String className;
    
    /**
     * 调用的方法名
     */
    private String methodName;
    
    /**
     * 方法参数类型数组
     */
    private Class<?>[] parameterTypes;
    
    /**
     * 方法参数值数组
     */
    private Object[] parameters;
    
    /**
     * 服务版本号
     */
    private String version;
    
    /**
     * 服务分组
     */
    private String group;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(Class<?>[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public Object[] getParameters() {
        return parameters;
    }

    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }
}