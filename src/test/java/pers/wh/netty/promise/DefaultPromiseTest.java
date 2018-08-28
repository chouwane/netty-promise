package pers.wh.netty.promise;

import org.junit.Test;
import pers.wh.netty.promise.impl.DefaultPromise;

import java.util.concurrent.*;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * @author wanghui
 * @date 2018/8/28 12:14
 */
public class DefaultPromiseTest {


    @Test(expected = CancellationException.class)
    public void testCancellationExceptionIsThrownWhenBlockingGet() throws ExecutionException, InterruptedException {
        DefaultPromise<Void> promise = new DefaultPromise<Void>();
        promise.cancel(false);
        promise.get();
    }

    @Test(expected = CancellationException.class)
    public void testCancellationExceptionIsThrownWhenBlockingGetWithTimeout() throws InterruptedException,
            ExecutionException, TimeoutException {
        final Promise<Void> promise = new DefaultPromise<Void>();
        promise.cancel(false);
        promise.get(1, TimeUnit.SECONDS);
    }

    @Test
    public void testListenerNotifyOrder() throws Exception {
        try {
            final BlockingQueue<FutureListener<Void>> listeners = new LinkedBlockingQueue<FutureListener<Void>>();
            int runs = 100000;

            for (int i = 0; i < runs; i++) {
                final Promise<Void> promise = new DefaultPromise<Void>();
                final FutureListener<Void> listener1 = new FutureListener<Void>() {
                    @Override
                    public void operationComplete(Future<Void> future) throws Exception {
                        listeners.add(this);
                    }
                };
                final FutureListener<Void> listener2 = new FutureListener<Void>() {
                    @Override
                    public void operationComplete(Future<Void> future) throws Exception {
                        listeners.add(this);
                    }
                };
                final FutureListener<Void> listener4 = new FutureListener<Void>() {
                    @Override
                    public void operationComplete(Future<Void> future) throws Exception {
                        listeners.add(this);
                    }
                };
                final FutureListener<Void> listener3 = new FutureListener<Void>() {
                    @Override
                    public void operationComplete(Future<Void> future) throws Exception {
                        listeners.add(this);
                        future.addListener(listener4);
                    }
                };

                promise.setSuccess(null);

                promise.addListener(listener1).addListener(listener2).addListener(listener3);

                assertSame("Fail 1 during run " + i + " / " + runs, listener1, listeners.take());
                assertSame("Fail 2 during run " + i + " / " + runs, listener2, listeners.take());
                assertSame("Fail 3 during run " + i + " / " + runs, listener3, listeners.take());
                assertSame("Fail 4 during run " + i + " / " + runs, listener4, listeners.take());
                assertTrue("Fail during run " + i + " / " + runs, listeners.isEmpty());
            }
        }finally {

        }
    }

    @Test(timeout = 2000)
    public void testPromiseListenerAddWhenCompleteFailure() throws Exception {
        testPromiseListenerAddWhenComplete(fakeException());
    }


    private static void testPromiseListenerAddWhenComplete(Throwable cause) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final Promise<Void> promise = new DefaultPromise<Void>();
        promise.addListener(new FutureListener<Void>() {
            @Override
            public void operationComplete(Future<Void> future) throws Exception {
                promise.addListener(new FutureListener<Void>() {
                    @Override
                    public void operationComplete(Future<Void> future) throws Exception {
                        latch.countDown();
                        System.out.println(future);
                    }
                });
            }
        });
        if (cause == null) {
            promise.setSuccess(null);
        } else {
            promise.setFailure(cause);
        }
        latch.await();
    }

    private static RuntimeException fakeException() {
        return new RuntimeException("fake exception");
    }
}
