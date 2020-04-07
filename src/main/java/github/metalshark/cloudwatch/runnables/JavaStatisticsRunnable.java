package github.metalshark.cloudwatch.runnables;

import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.model.*;
import github.metalshark.cloudwatch.CloudWatch;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryManagerMXBean;

public class JavaStatisticsRunnable implements Runnable {

    private static double prevTotalGarbageCollections = 0;
    private static double prevTotalGarbageCollectionTime = 0;

    public void run() {
        final boolean firstRun = (prevTotalGarbageCollections + prevTotalGarbageCollectionTime) == 0;

        double totalGarbageCollections = 0;
        double totalGarbageCollectionTime = 0;

        for(GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans()) {
            final long count = gc.getCollectionCount();

            if (count >= 0) {
                totalGarbageCollections += count;
            }

            final long time = gc.getCollectionTime();

            if (time >= 0) {
                totalGarbageCollectionTime += time;
            }
        }

        final double garbageCollections = totalGarbageCollections - prevTotalGarbageCollections;
        final double garbageCollectionTime = totalGarbageCollectionTime - prevTotalGarbageCollectionTime;

        prevTotalGarbageCollections = totalGarbageCollections;
        prevTotalGarbageCollectionTime = totalGarbageCollectionTime;

        if (firstRun) return;

        // Get current size of heap in bytes
        final double heapSize = Runtime.getRuntime().totalMemory();

        // Get maximum size of heap in bytes. The heap cannot grow beyond this size.// Any attempt will result in an OutOfMemoryException.
        final double heapMaxSize = Runtime.getRuntime().maxMemory();

        // Get amount of free memory within the heap in bytes. This size will increase // after garbage collection and decrease as new objects are created.
        final double heapFreeSize = Runtime.getRuntime().freeMemory();

        final double threadCount = ManagementFactory.getThreadMXBean().getThreadCount();

        final AmazonCloudWatch cw = CloudWatch.getPlugin().getCw();
        final MetricDatum garbageCollectionsMetric = new MetricDatum()
            .withMetricName("GarbageCollections")
            .withUnit(StandardUnit.Count)
            .withValue(garbageCollections);
        final MetricDatum garbageCollectionTimeMetric = new MetricDatum()
            .withMetricName("GarbageCollectionTime")
            .withUnit(StandardUnit.Milliseconds)
            .withValue(garbageCollectionTime);
        final MetricDatum heapSizeMetric = new MetricDatum()
            .withMetricName("HeapSize")
            .withUnit(StandardUnit.Bytes)
            .withValue(heapSize);
        final MetricDatum heapMaxSizeMetric = new MetricDatum()
            .withMetricName("HeapMaxSize")
            .withUnit(StandardUnit.Bytes)
            .withValue(heapMaxSize);
        final MetricDatum heapFreeSizeMetric = new MetricDatum()
            .withMetricName("HeapFreeSize")
            .withUnit(StandardUnit.Bytes)
            .withValue(heapFreeSize);
        final MetricDatum threadCountMetric = new MetricDatum()
            .withMetricName("Threads")
            .withUnit(StandardUnit.Count)
            .withValue(threadCount);
        final MetricDatum[] metrics = new MetricDatum[]{garbageCollectionsMetric, garbageCollectionTimeMetric, heapSizeMetric, heapMaxSizeMetric, heapFreeSizeMetric, threadCountMetric};
        final PutMetricDataRequest request = new PutMetricDataRequest()
            .withNamespace("Java")
            .withMetricData(metrics);
        try {
            final PutMetricDataResult result = cw.putMetricData(request);
        } catch (Exception e) {
            CloudWatch.getPlugin().getLogger().warning(e.getMessage());
        }
    }

}
