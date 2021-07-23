package com.xschen.zk.advanced.timedtask;

import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * @author xschen
 */
public class Main {

    public static void main(String[] args) throws InterruptedException {
        IntStream.rangeClosed(1, 5)
                .mapToObj(index -> "机器" + index)
                .map(TimedTask::new)
                .map(task -> new Runnable() {
                    @Override
                    public void run() {
                        task.go();
                    }
                })
                .map(Thread::new)
                .forEach(Thread::start);

        TimeUnit.SECONDS.sleep(30);
    }

    /**
     * 建立连接，并且机器4创建 lock 节点成功，这个客户端成为 master，其余机器只能等待
     * 1626940725: 机器4 zookeeper 连接建立成功 State:CONNECTED Timeout:5000 sessionid:0x2000b9d0128000b local:/127.0.0.1:49278 remoteserver:127.0.0.1/127.0.0.1:2182 lastZxid:0 xid:1 sent:1 recv:1 queuedpkts:0 pendingresp:0 queuedevents:0
     * 1626940725: 机器1 zookeeper 连接建立成功 State:CONNECTED Timeout:5000 sessionid:0x1000b9d011f000d local:/127.0.0.1:49280 remoteserver:127.0.0.1/127.0.0.1:2181 lastZxid:0 xid:1 sent:1 recv:1 queuedpkts:0 pendingresp:0 queuedevents:0
     * 1626940725: 机器5 zookeeper 连接建立成功 State:CONNECTED Timeout:5000 sessionid:0x3000b9d1228000c local:/127.0.0.1:49276 remoteserver:127.0.0.1/127.0.0.1:2183 lastZxid:0 xid:1 sent:1 recv:1 queuedpkts:0 pendingresp:0 queuedevents:0
     * 1626940725: 机器2 zookeeper 连接建立成功 State:CONNECTED Timeout:5000 sessionid:0x1000b9d011f000e local:/127.0.0.1:49277 remoteserver:127.0.0.1/127.0.0.1:2181 lastZxid:0 xid:1 sent:1 recv:1 queuedpkts:0 pendingresp:0 queuedevents:0
     * 1626940725: 机器3 zookeeper 连接建立成功 State:CONNECTED Timeout:5000 sessionid:0x1000b9d011f000f local:/127.0.0.1:49279 remoteserver:127.0.0.1/127.0.0.1:2181 lastZxid:0 xid:1 sent:1 recv:1 queuedpkts:0 pendingresp:0 queuedevents:0
     * 1626940725: 机器5 等待
     * 1626940725: 机器1 等待
     * 1626940725: 机器2 等待
     * 1626940725: 机器4 创建 lock 节点成功，成为 master，定时任务由我来执行
     * 1626940725: 机器3 等待
     *
     * 1626940728:机器4 宕机
     * 1626940728: 机器5 创建 lock 节点成功，成为 master，定时任务由我来执行
     * 1626940728: 机器2 等待
     * 1626940728: 机器1 等待
     * 1626940728: 机器3 等待
     *
     *
     * 1626940731:机器5 宕机
     * 1626940731: 机器2 等待
     * 1626940731: 机器3 等待
     * 1626940731: 机器1 创建 lock 节点成功，成为 master，定时任务由我来执行
     *
     *
     * 1626940734:机器1 宕机
     * 1626940734: 机器2 创建 lock 节点成功，成为 master，定时任务由我来执行
     * 1626940734: 机器3 等待
     *
     *
     * 1626940737:机器2 宕机
     * 1626940737: 机器3 创建 lock 节点成功，成为 master，定时任务由我来执行
     *
     * 1626940740:机器3 宕机
     */
}
