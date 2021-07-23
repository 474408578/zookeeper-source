package com.xschen.zk.advanced.order;

import com.xschen.zk.advanced.common.Constants;
import org.apache.zookeeper.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 2. 如何通过 zookeeper 实现分布式锁
 * todo: 用户支付订单
 * @author xschen
 */
public class PayOrder {

    private static final String timeOutOrderLockPrefix = "/timeOutOrderLock";

    public void payOrder(Long orderId) {
        CountDownLatch latch = new CountDownLatch(1);
        try {
            ZooKeeper zk = new ZooKeeper(Constants.connString, 5000, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    latch.countDown();
                }
            });
            latch.await();
            System.out.println(System.currentTimeMillis() / 1000 + " PayOrder zookeeper 连接建立成功");

            String payStatus = doPayOrder(zk, orderId);
            System.out.println(System.currentTimeMillis() / 1000 + " 订单：" + orderId + " 支付状态：" + payStatus);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String doPayOrder(ZooKeeper zk, Long orderId) {
        String lockPath = timeOutOrderLockPrefix + "/" + orderId;
        try {
            zk.create(lockPath, "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
            TimeUnit.SECONDS.sleep(3);
            zk.delete(lockPath, -1);
            System.out.println(System.currentTimeMillis() / 1000 + " PayOrder 订单"+orderId+" 支付成功");
            return "success";
        } catch (KeeperException.NodeExistsException e) {
            System.out.println(System.currentTimeMillis() / 1000 + " PayOrder 订单：" + orderId + " 锁已存在，返回用户操作失败，请稍后重试");
            return "failed";
        } catch (Exception e) {
            /**
             * 其实还有其他的状态，比如连接断开，会话过期等等
             * 本次课程主要是让大家明白 master 选举的原理。
             * 我们只关心主要流程，其他的异常情况就不做处理了
             */
            System.out.println(System.currentTimeMillis() / 1000 + " PayOrder startPay 异常状态： "+e.getMessage());
            return "error";
        }
    }
}
