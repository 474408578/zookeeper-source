package com.xschen.zk.advanced.timedtask;

import com.xschen.zk.advanced.common.Constants;
import org.apache.zookeeper.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 1. 如何通过 zookeeper 实现 master 选举
 * 通过Zookeeper实现分布式定时任务执行的选举：
 *      选举一台机器作为master，master可以执行任务
 * @author xschen
 */

public class TimedTask {

    /**
     * create /timedTask
     */
    private static final String lockPath = "/timedTask/lock";
    private String machineName;


    public TimedTask(String machineName) {
        this.machineName = machineName;
    }

    public void go() {
        CountDownLatch latch = new CountDownLatch(1);
        try {
            ZooKeeper zk = new ZooKeeper(Constants.connString, 5000, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    latch.countDown();
                }
            });

            latch.await();
            System.out.println(System.currentTimeMillis() / 1000 + ": " + machineName +" zookeeper 连接建立成功 " + zk);

            toBeMaster(zk, machineName);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void toBeMaster(ZooKeeper zk, String machineName) {
        /**
         * 1、尝试去创建 /timedTask/lock 临时无序节点
         *      如果创建成功，则可以执行定时任务
         *      如果创建失败，则监听 （wacher） /timedTask/lock 节点
         * 2、如果收到节点被删除的通知，就重新执行步骤 1
         */
        zk.create(lockPath, "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.EPHEMERAL, new AsyncCallback.StringCallback() {
                    @Override
                    public void processResult(int rc, String path, Object ctx, String name) {
                        if (rc == KeeperException.Code.OK.intValue()) {
                            System.out.println(System.currentTimeMillis() / 1000 + ": " + machineName+" 创建 lock 节点成功，成为 master，定时任务由我来执行");
                            try {
                                TimeUnit.SECONDS.sleep(3);
                                zk.delete(lockPath, -1);
                                System.out.println(System.currentTimeMillis() / 1000 + ":" + machineName+" 宕机");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        else if (rc == KeeperException.Code.NODEEXISTS.intValue()) {
                            System.out.println(System.currentTimeMillis() / 1000 + ": " + machineName+" 等待");
                            try {
                                zk.exists(lockPath, new Watcher() {
                                    @Override
                                    public void process(WatchedEvent event) {
                                        if (event.getType() == Event.EventType.NodeDeleted) {
                                            toBeMaster(zk, machineName);
                                        }
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        else {
                            /**
                             * 其实还有其他的状态，比如连接断开，会话过期等等
                             * 本次课程主要是让大家明白 master 选举的原理。
                             * 我们只关心主要流程，其他的异常情况就不做处理了
                             */
                            System.out.println(System.currentTimeMillis() / 1000 + ": " + machineName+" toBeAMaster 异常状态" + rc);
                        }
                    }
                },"ctx_data");
    }
}
