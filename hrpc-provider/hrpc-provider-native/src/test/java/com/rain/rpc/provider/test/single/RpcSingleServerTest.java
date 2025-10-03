package com.rain.rpc.provider.test.single;

import com.rain.rpc.provider.RpcSingleServer;
import org.junit.jupiter.api.Test;

public class RpcSingleServerTest {

    @Test
    public void startRpcSingleServer(){
        RpcSingleServer singleServer = new RpcSingleServer("127.0.0.1:27880", "com.rain.rpc.provider.test", "cglib");
        singleServer.startNettyServer();
    }
}
