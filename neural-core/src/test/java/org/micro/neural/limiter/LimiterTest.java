package org.micro.neural.limiter;

import java.time.Duration;
import java.util.Random;

import org.micro.neural.common.URL;
import org.micro.neural.OriginalCall;
import org.micro.neural.config.store.StorePool;

/**
 * @author lry
 **/
public class LimiterTest {
    public static void main(String[] args) throws Throwable {
        String application = "gateway";
        Limiter limiter = new Limiter();

        //query order
        LimiterConfig config1 = new LimiterConfig();
        config1.setApplication(application);
        config1.setGroup("order");
        config1.setResource("queryOrder");
        config1.setName("查询订单");
        config1.setRequestPermit(3);
        config1.setRequestInterval(Duration.ofSeconds(1));
        limiter.addConfig(config1);

        //insert order
        LimiterConfig config2 = new LimiterConfig();
        config2.setApplication(application);
        config2.setGroup("order");
        config2.setResource("insertOrder");
        config2.setName("新增订单");
        limiter.addConfig(config2);

        //delete order
        LimiterConfig config3 = new LimiterConfig();
        config3.setApplication(application);
        config3.setGroup("order");
        config3.setResource("deleteOrder");
        config3.setName("删除订单");
        limiter.addConfig(config3);

        limiter.initialize(URL.valueOf("redis://localhost:6379?minIdle=2"));

        for (int i = 0; i < 100000; i++) {
            Object result = limiter.call(config1.identity(), new OriginalCall() {
                @Override
                public Object call() throws Throwable {
                    Thread.sleep(new Random().nextInt(100) + 20);
                    return "ok";
                }

                @Override
                public Object fallback() throws Throwable {
                    return "fallback";
                }
            });
            System.out.println(i + ": " + result);
            if (i == 50) {
                config1.setRateTimeout(3000L);
                System.out.println("1发布配置");
                StorePool.getInstance().publish("limiter", config1);
                System.out.println("2发布配置");
            }
        }
    }
}
