package org.dromara.milvus.plus.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

public class IdWorkerUtils {

    // 时间起始标记点，作为基准，一般取系统的最近时间（一旦确定不能变动）
    private static final long twepoch = 1288834974657L;

    // 机器标识位数
    private static final long workerIdBits = 5L;
    // 数据中心标识位数
    private static final long datacenterIdBits = 5L;

    // 机器ID最大值
    private static final long maxWorkerId = -1L ^ (-1L << workerIdBits);
    // 数据中心ID最大值
    private static final long maxDatacenterId = -1L ^ (-1L << datacenterIdBits);

    // 毫秒内自增位
    private static final long sequenceBits = 12L;

    // 机器ID偏左移12位
    private static final long workerIdShift = sequenceBits;
    // 数据中心ID左移17位
    private static final long datacenterIdShift = sequenceBits + workerIdBits;
    // 时间毫秒左移22位
    private static final long timestampLeftShift = sequenceBits + workerIdBits + datacenterIdBits;

    private static final long sequenceMask = -1L ^ (-1L << sequenceBits);
    /* 上次生产id时间戳 */
    private static long lastTimestamp = -1L;
    // 0，并发控制
    private static long sequence = 0L;

    // 数据标识id部分
    private static long datacenterId;
    // 机器ID
    private static long workerId;

    static {
        datacenterId = getDatacenterId(maxDatacenterId);
        workerId = getMaxWorkerId();
    }

    /**
     * 获取下一个ID
     *
     * @return the next ID
     */
    public static synchronized long nextId() {
        long timestamp = timeGen();
        if (timestamp < lastTimestamp) {
            throw new RuntimeException(String.format(
                "Clock moved backwards. Refusing to generate id for %d milliseconds", lastTimestamp - timestamp));
        }

        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & sequenceMask;
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }
        lastTimestamp = timestamp;
        // ID偏移组合生成最终的ID，并返回ID
        long nextId = ((timestamp - twepoch) << timestampLeftShift)
            | (datacenterId << datacenterIdShift)
            | (workerId << workerIdShift) | sequence;
        return nextId;
    }

    private static long tilNextMillis(final long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    private static long timeGen() {
        return System.currentTimeMillis();
    }

    /**
     * <p>
     * 获取 maxWorkerId
     * </p>
     */
    private static long getMaxWorkerId() {
        long maxWorkerId = 0L;
        try {
            List<NetworkInterface> networkInterfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface networkInterface : networkInterfaces) {
                if (networkInterface.getName().contains("eth") || networkInterface.getName().contains("wlan")) {
                    byte[] macBytes = networkInterface.getHardwareAddress();
                    if (macBytes != null) {
                        maxWorkerId |= ((macBytes[macBytes.length - 1] & 0xff) | ((macBytes[macBytes.length - 2] & 0xff) << 8)) & 0xffff;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (maxWorkerId >> 10) & 0x3ff;
    }

    /**
     * <p>
     * 数据标识id部分
     * </p>
     */
    private static long getDatacenterId(long maxDatacenterId) {
        long id = 0L;
        try {
            InetAddress ip = InetAddress.getLocalHost();
            NetworkInterface network = NetworkInterface.getByInetAddress(ip);
            if (network == null) {
                id = 1L;
            } else {
                byte[] mac = network.getHardwareAddress();
                id = ((0x000000FF & (long) mac[mac.length - 1])
                    | (0x0000FF00 & (((long) mac[mac.length - 2]) << 8))) >> 6;
                id = id % (maxDatacenterId + 1);
            }
        } catch (Exception e) {
            System.out.println(" getDatacenterId: " + e.getMessage());
        }
        return id;
    }
}