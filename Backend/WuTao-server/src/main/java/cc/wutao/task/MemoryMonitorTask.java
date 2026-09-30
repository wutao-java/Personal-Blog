package cc.wutao.task;

import cc.wutao.config.RateLimitConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.lang.management.BufferPoolMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.List;

/**
 * JVM 内存监控与释放任务
 * <p>
 * 每小时记录堆/非堆/直接内存使用情况；
 * 当堆使用率超过阈值时主动触发 System.gc() 促使 G1 归还内存给操作系统。
 * <p>
 * 注意：System.gc() 只是建议 JVM 做 GC，G1 会根据自身策略决定是否执行。
 * 真正的内存归还由 -XX:G1PeriodicGCInterval + -XX:MaxHeapFreeRatio 驱动，
 * 此任务作为补充保险。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MemoryMonitorTask {

    private static final double GC_TRIGGER_RATIO = 0.85;

    private final RateLimitConfiguration rateLimitConfiguration;

    private static final MemoryMXBean MEMORY_BEAN = ManagementFactory.getMemoryMXBean();
    private static final List<BufferPoolMXBean> BUFFER_POOLS = ManagementFactory.getPlatformMXBeans(BufferPoolMXBean.class);

    /**
     * 每小时执行一次：记录内存 + 清理本地缓存
     */
    @Scheduled(fixedRate = 60 * 60 * 1000, initialDelay = 5 * 60 * 1000)
    public void monitorAndRelease() {
        MemoryUsage heap = MEMORY_BEAN.getHeapMemoryUsage();
        MemoryUsage nonHeap = MEMORY_BEAN.getNonHeapMemoryUsage();

        long heapUsed = heap.getUsed();
        long heapMax = heap.getMax();
        double heapRatio = heapMax > 0 ? (double) heapUsed / heapMax : 0;

        long directUsed = BUFFER_POOLS.stream()
                .filter(b -> "direct".equals(b.getName()))
                .mapToLong(BufferPoolMXBean::getMemoryUsed)
                .sum();

        String heapPct = String.format("%.1f%%", heapRatio * 100);
        int bucketSize = getBucketCacheSize();

        log.info("内存监控 | 堆: {}/{}MB ({}) | 非堆: {}MB | 直接内存: {}MB | 限流桶: {}",
                toMB(heapUsed), toMB(heapMax), heapPct,
                toMB(nonHeap.getUsed()), toMB(directUsed), bucketSize);

        // 清理过期限流桶
        rateLimitConfiguration.cleanupIfNeeded(System.currentTimeMillis());

        // 堆使用率超过阈值时，建议 JVM 执行 GC
        if (heapRatio >= GC_TRIGGER_RATIO) {
            long before = heapUsed;
            log.warn("堆使用率 {} 超过阈值 {}，触发 GC", heapPct,
                    String.format("%.0f%%", GC_TRIGGER_RATIO * 100));
            System.gc();
            MemoryUsage afterUsage = MEMORY_BEAN.getHeapMemoryUsage();
            long after = afterUsage.getUsed();
            log.warn("GC 完成 | 前: {}MB | 后: {}MB | 释放: {}MB",
                    toMB(before), toMB(after), toMB(before - after));
        }
    }

    private long toMB(long bytes) {
        return bytes / 1024 / 1024;
    }

    private int getBucketCacheSize() {
        try {
            return rateLimitConfiguration.getBucketCache().size();
        } catch (Exception e) {
            return -1;
        }
    }
}
