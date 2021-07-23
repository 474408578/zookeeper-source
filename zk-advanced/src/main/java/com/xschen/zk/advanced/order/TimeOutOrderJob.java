package com.xschen.zk.advanced.order;

import com.xschen.zk.advanced.common.Constants;
import org.apache.zookeeper.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 2. 如何通过 zookeeper 实现分布式锁
 * todo：扫描超时订单，并将订单状态改为超时关闭
 * @author xschen
 */
public class TimeOutOrderJob {
    private static final String timeOutOrderLockPrefix = "/timeOutOrderLock";

    public void updateTimeOutOrder() {
        CountDownLatch latch = new CountDownLatch(1);
        try {
            ZooKeeper zk = new ZooKeeper(Constants.connString, 5000, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    latch.countDown();
                }
            });
            latch.await();
            System.out.println(System.currentTimeMillis() / 1000 + " TimeOutOrderJob zookeeper 连接建立成功");
            doUpdateTimeOutOrder(zk);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 1. 找出30分钟没有付款的订单
     * 2. 处理每一条订单的时候，加上分布式锁。尝试创建/timeOutOrderLock/{OrderId} 节点
     *      如果创建成功，则进行业务逻辑处理
     *      如果创建失败，则跳过该笔订单，不做处理
     * 3. 修改需要 “修改状态” 的订单
     * @param zk
     */
    private void doUpdateTimeOutOrder(ZooKeeper zk) {
        List<Order> orderList = new ArrayList<>();
        orderList.add(new Order(1L, "NOT_PAY"));
        orderList.add(new Order(2L, "NOT_PAY"));
        Iterator<Order> iterator = orderList.iterator();
        while (iterator.hasNext()) {
            Order order = iterator.next();
            String lockPath = timeOutOrderLockPrefix + "/" + order.getId();
            zk.create(lockPath, "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL, new AsyncCallback.StringCallback() {
                @Override
                public void processResult(int rc, String path, Object ctx, String name) {
                    if (rc == KeeperException.Code.OK.intValue()) {
                        System.out.println(System.currentTimeMillis() / 1000 + " TimeOutOrderJob 开始执行业务逻辑处理 orderId: " + order.getId());
                        try {
                            TimeUnit.SECONDS.sleep(3);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        System.out.println(System.currentTimeMillis() / 1000 + " TimeOutOrderJob 订单处理完毕 orderId: " + order.getId());
                    } else if (rc == KeeperException.Code.NODEEXISTS.intValue()) {
                        System.out.println(System.currentTimeMillis() / 1000 + " TimeOutOrderJob 跳过订单 orderId: " + order.getId());
                        iterator.remove();
                    } else {
                        /**
                         * 其实还有其他的状态，比如连接断开，会话过期等等
                         * 本次课程主要是让大家明白 master 选举的原理。
                         * 我们只关心主要流程，其他的异常情况就不做处理了
                         */
                        System.out.println(System.currentTimeMillis() / 1000 + " TimeOutOrderJob 异常状态：" + rc);
                    }
                }
            }, "callback_data");
        }

    }

}