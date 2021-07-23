package com.xschen.zk.advanced.order;

import java.util.concurrent.TimeUnit;

/**
 * @author xschen
 */
public class Main {

    public static void main(String[] args) throws InterruptedException {
        Thread payOrder = new Thread(() -> new PayOrder().payOrder(1L));
        Thread timeOutOrderJob = new Thread(() -> new TimeOutOrderJob().updateTimeOutOrder());

        /**
         * 两种情况需要测试：
         *      1. 用户先获取到锁
         *      2. 定时任务先获取到锁
         */

        /**
         * 1. 用户先获取到锁
         */
//        payOrder.start();
//        TimeUnit.SECONDS.sleep(1);
//        timeOutOrderJob.start();
        /**
         * 1627034339 PayOrder zookeeper 连接建立成功
         * 1627034340 TimeOutOrderJob zookeeper 连接建立成功
         * 1627034340 TimeOutOrderJob 跳过订单 orderId: 1
         * 1627034340 TimeOutOrderJob 开始执行业务逻辑处理 orderId: 2
         * 1627034342 PayOrder 订单1 支付成功
         * 1627034342 订单：1 支付状态：success
         * 1627034343 TimeOutOrderJob 订单处理完毕 orderId: 2
         */

        /**
         * 2. 定时任务先获取到锁
         */
        timeOutOrderJob.start();
        TimeUnit.SECONDS.sleep(1);
        payOrder.start();
        /**
         * 1627034427 TimeOutOrderJob zookeeper 连接建立成功
         * 1627034427 TimeOutOrderJob 开始执行业务逻辑处理 orderId: 1
         * 1627034428 PayOrder zookeeper 连接建立成功
         * 1627034428 PayOrder 订单：1 锁已存在，返回用户操作失败，请稍后重试
         * 1627034428 订单：1 支付状态：failed
         * 1627034430 TimeOutOrderJob 订单处理完毕 orderId: 1
         * 1627034430 TimeOutOrderJob 开始执行业务逻辑处理 orderId: 2
         * 1627034433 TimeOutOrderJob 订单处理完毕 orderId: 2
         */

        TimeUnit.SECONDS.sleep(10);
    }
}
