package com.rain.rpc.common.scanner;

import com.rain.rpc.common.scanner.reference.RpcReferenceScanner;
import com.rain.rpc.common.scanner.server.RpcServiceScanner;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * 类扫描器测试类
 * 用于测试RPC框架中各类扫描器的功能
 */
public class ClassScannerTest {

    /**
     * 测试基础类名列表扫描功能
     * 验证ClassScanner能够正确扫描指定包下的所有类
     *
     * @throws Exception 扫描过程中可能抛出的异常
     */
    @Test
    public void testScannerClassNameList() throws Exception {
        List<String> classNameList = ClassScanner.getClassNameList("com.rain.rpc.common.scanner");
        classNameList.forEach(System.out::println);
    }

    /**
     * 测试RpcService注解扫描功能
     * 验证RpcServiceScanner能够正确识别并处理标记了@RpcService注解的类
     *
     * @throws Exception 扫描过程中可能抛出的异常
     */
    @Test
    public void testScannerClassNameListByRpcService() throws Exception {
        RpcServiceScanner.doScannerWithRpcServiceAnnotationFilterAndRegistryService("com.rain.rpc.common.scanner");
    }

    /**
     * 测试RpcReference注解扫描功能
     * 验证RpcReferenceScanner能够正确识别并处理包含@RpcReference注解字段的类
     *
     * @throws Exception 扫描过程中可能抛出的异常
     */
    @Test
    public void testScannerClassNameListByRpcReference() throws Exception {
        RpcReferenceScanner.doScannerWithRpcReferenceAnnotationFilter("com.rain.rpc.common.scanner");
    }

}