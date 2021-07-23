package com.xschen.zk.advanced.services;

import com.xschen.zk.advanced.common.Constants;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.util.concurrent.CountDownLatch;

/**
 * 3. 如何通过zookeeper实现服务注册中心
 * @author xschen
 */
public class UserService {
    private static final String userServicePrefix = "/services/user";
    private static final String orderServicePrefix = "/services/order";

    public void userService(String ip) {
        CountDownLatch latch = new CountDownLatch(1);
        try {
            ZooKeeper zk = new ZooKeeper(Constants.connString, 5000, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    latch.countDown();
                }
            });
            latch.await();
            System.out.println(System.currentTimeMillis() / 1000 + " UserService: " + ip + " 连接建立成功");

            // 把自己注册到服务注册中心
            startRegister(zk, ip);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startRegister(ZooKeeper zk, String ip) {

    }
}
