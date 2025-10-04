package com.rain.rpc.proxy.api.future;

import com.rain.rpc.common.threadpool.ClientThreadPool;
import com.rain.rpc.protocol.RpcProtocol;
import com.rain.rpc.protocol.request.RpcRequest;
import com.rain.rpc.protocol.response.RpcResponse;
import com.rain.rpc.proxy.api.callback.AsyncRPCCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.ReentrantLock;

/**
 * RPC Future实现
 * 基于CompletableFuture扩展，用于处理RPC异步调用结果
 */
public class RPCFuture extends CompletableFuture<Object> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RPCFuture.class);

    // 同步器，用于控制异步操作的完成状态
    private Sync sync;
    // 请求协议
    private RpcProtocol<RpcRequest> requestProtocol;
    // 响应协议
    private RpcProtocol<RpcResponse> responseProtocol;
    // 请求开始时间，用于性能监控
    private long startTime;
    // 响应时间阈值（毫秒），超过该值会输出警告日志
    private long responseTimeThreshold = 5000;

    private List<AsyncRPCCallback> pendingCallbacks = new ArrayList<AsyncRPCCallback>();
    private ReentrantLock lock = new ReentrantLock();

    public RPCFuture(RpcProtocol<RpcRequest> requestProtocol) {
        this.sync = new Sync();
        this.requestProtocol = requestProtocol;
        this.startTime = System.currentTimeMillis();
    }

    @Override
    public boolean isDone() {
        return sync.isDone();
    }

    /**
     * 获取RPC调用结果（阻塞直到结果返回）
     *
     * @return RPC调用结果
     * @throws InterruptedException 中断异常
     * @throws ExecutionException 执行异常
     */
    @Override
    public Object get() throws InterruptedException, ExecutionException {
        // 等待操作完成
        sync.acquire(-1);
        if (this.responseProtocol != null) {
            return this.responseProtocol.getBody().getResult();
        } else {
            return null;
        }
    }

    /**
     * 获取RPC调用结果（带超时时间）
     *
     * @param timeout 超时时间
     * @param unit 时间单位
     * @return RPC调用结果
     * @throws InterruptedException 中断异常
     * @throws ExecutionException 执行异常
     * @throws TimeoutException 超时异常
     */
    @Override
    public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        // 在指定时间内等待操作完成
        boolean success = sync.tryAcquireNanos(-1, unit.toNanos(timeout));
        if (success) {
            if (this.responseProtocol != null) {
                return this.responseProtocol.getBody().getResult();
            } else {
                return null;
            }
        } else {
            // 超时抛出异常，并提供详细的调试信息
            throw new RuntimeException("Timeout exception. Request id: " + this.requestProtocol.getHeader().getRequestId()
                    + ". Request class name: " + this.requestProtocol.getBody().getClassName()
                    + ". Request method: " + this.requestProtocol.getBody().getMethodName());
        }
    }

    @Override
    public boolean isCancelled() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        throw new UnsupportedOperationException();
    }

    /**
     * 标记RPC调用完成，并设置响应结果
     *
     * @param responseProtocol 响应协议
     */
    public void done(RpcProtocol<RpcResponse> responseProtocol) {
        this.responseProtocol = responseProtocol;
        // 释放同步锁，标记操作完成
        sync.release(1);
        invokeCallbacks();
        // 性能监控：检查响应时间是否超过阈值
        long responseTime = System.currentTimeMillis() - startTime;
        if (responseTime > this.responseTimeThreshold) {
            LOGGER.warn("Service response time is too slow. Request id = " + responseProtocol.getHeader().getRequestId() + ". Response Time = " + responseTime + "ms");
        }
    }

    private void invokeCallbacks() {
        lock.lock();
        try {
            for (final AsyncRPCCallback callback : pendingCallbacks) {
                runCallback(callback);
            }
        } finally {
            lock.unlock();
        }
    }

    public RPCFuture addCallback(AsyncRPCCallback callback) {
        lock.lock();
        try {
            if (isDone()) {
                runCallback(callback);
            } else {
                this.pendingCallbacks.add(callback);
            }
        } finally {
            lock.unlock();
        }
        return this;
    }

    private void runCallback(final AsyncRPCCallback callback) {
        final RpcResponse res = this.responseProtocol.getBody();
        ClientThreadPool.submit(() -> {
            if (!res.isError()) {
                callback.onSuccess(res.getResult());
            } else {
                callback.onException(new RuntimeException("Response error", new Throwable(res.getError())));
            }
        });
    }

    /**
     * 内部同步器实现
     * 基于AbstractQueuedSynchronizer实现RPC调用的同步控制
     */
    static class Sync extends AbstractQueuedSynchronizer {
        private static final long serialVersionUID = 9092188010933422653L;

        // 完成状态
        private final int done = 1;
        // 等待状态
        private final int pending = 0;

        @Override
        protected boolean tryAcquire(int arg) {
            return getState() == done;
        }

        @Override
        protected boolean tryRelease(int arg) {
            if (getState() == pending) {
                if (compareAndSetState(pending, done)) {
                    return true;
                }
            }
            return false;
        }

        protected boolean isDone() {
            getState();
            return getState() == done;
        }
    }
}