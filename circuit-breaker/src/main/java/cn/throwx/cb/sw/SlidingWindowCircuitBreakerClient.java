package cn.throwx.cb.sw;

import cn.throwx.cb.CircuitBreakerResourceConf;
import cn.throwx.cb.CircuitBreakerResourceManager;

import java.util.Random;

/**
 * @author throwable
 * @version v1
 * @description
 * @since 2020/10/25 17:33
 */
public class SlidingWindowCircuitBreakerClient {

    public static void main(String[] args) throws Exception {
        CircuitBreakerResourceConf conf = new CircuitBreakerResourceConf();
        conf.setCoreSize(10);
        conf.setQueueSize(0);
        conf.setResourceName("SERVICE");
        conf.setTimeout(50);
        CircuitBreakerResourceManager.X.register(conf);
        Service service = new Service();
        SlidingWindowCircuitBreaker cb = new SlidingWindowCircuitBreaker("SERVICE", 50, 500);
        for (int i = 0; i < 10; i++) {
            int temp = i;
            String result = cb.call(() -> service.process(temp));
            System.out.println(String.format("返回结果:%s,number:%d", result, temp));
        }
        Thread.sleep(501L);
        cb.call(service::processSuccess);
        for (int i = 0; i < 3; i++) {
            int temp = i;
            String result = cb.call(() -> service.process(temp));
            System.out.println(String.format("返回结果:%s,number:%d", result, temp));
        }
        Thread.sleep(501L);
        cb.call(service::processSuccess);
        cb.call(service::processSuccess);
    }

    public static class Service {

        private final Random r = new Random();

        public String process(int i) {
            int sleep = r.nextInt(200);
            System.out.println(String.format("线程[%s]-进入process方法,number:%d,休眠%d毫秒",
                    Thread.currentThread().getName(), i, sleep));
            try {
                Thread.sleep(sleep);
            } catch (InterruptedException ignore) {

            }
            return String.valueOf(i);
        }

        public void processSuccess() {
            System.out.println(String.format("线程[%s]-调用processSuccess方法", Thread.currentThread().getName()));
        }
    }
}
