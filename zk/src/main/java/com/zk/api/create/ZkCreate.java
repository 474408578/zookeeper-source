package com.zk.api.create;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.junit.After;
import org.junit.Before;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * @author xschen
 */


public class ZkCreate {
    ZooKeeper zooKeeper = null;

    @Before
    public void getConnection() throws IOException, InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        zooKeeper = new ZooKeeper("119.45.56.227:2181", 5000, new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                if (event.getState() == Event.KeeperState.SyncConnected) {
                    System.out.println("Connection created successfully");
                    countDownLatch.countDown();
                }
            }
        });

        countDownLatch.await();
    }


    @After
    public void closeConnection() throws InterruptedException {
        if (zooKeeper != null) {
            zooKeeper.close();
            System.out.println("Connection closed successfully");
        }
    }
}
