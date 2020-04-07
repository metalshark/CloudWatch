package github.metalshark.cloudwatch.runnables;

import com.sun.management.UnixOperatingSystemMXBean;
import github.metalshark.cloudwatch.CloudWatch;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.*;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

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
        final double heapUsedSize = heapSize - heapFreeSize;

        final double threadCount = ManagementFactory.getThreadMXBean().getThreadCount();

        double openFileDescriptors = 0;
        double maxFileDescriptors = 0;
        double totalPhysicalMemorySize = 0;
        double freePhysicalMemorySize = 0;
        double usedPhysicalMemorySize = 0;
        double processCpuLoad = 0;
        double systemCpuLoad = 0;
        final OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
        if (os instanceof UnixOperatingSystemMXBean) {
            final UnixOperatingSystemMXBean unixOs = (UnixOperatingSystemMXBean) os;
            openFileDescriptors = unixOs.getOpenFileDescriptorCount();
            maxFileDescriptors = unixOs.getMaxFileDescriptorCount();
            totalPhysicalMemorySize = unixOs.getTotalPhysicalMemorySize();
            freePhysicalMemorySize = unixOs.getFreePhysicalMemorySize();
            usedPhysicalMemorySize = totalPhysicalMemorySize - freePhysicalMemorySize;
            processCpuLoad = unixOs.getProcessCpuLoad() * 100;
            systemCpuLoad = unixOs.getSystemCpuLoad() * 100;
        }

        final Dimension dimension = CloudWatch.getDimension();

        try (final CloudWatchClient cw = CloudWatchClient.builder().build()) {
            final MetricDatum garbageCollectionsMetric = MetricDatum
                .builder()
                .metricName("GarbageCollections")
                .unit(StandardUnit.COUNT)
                .value(garbageCollections)
                .dimensions(dimension)
                .build();
            final MetricDatum garbageCollectionTimeMetric = MetricDatum
                .builder()
                .metricName("GarbageCollectionTime")
                .unit(StandardUnit.MILLISECONDS)
                .value(garbageCollectionTime)
                .dimensions(dimension)
                .build();
            final MetricDatum heapSizeMetric = MetricDatum
                .builder()
                .metricName("HeapSize")
                .unit(StandardUnit.BYTES)
                .value(heapSize)
                .dimensions(dimension)
                .build();
            final MetricDatum heapMaxSizeMetric = MetricDatum
                .builder()
                .metricName("HeapMaxSize")
                .unit(StandardUnit.BYTES)
                .value(heapMaxSize)
                .dimensions(dimension)
                .build();
            final MetricDatum heapFreeSizeMetric = MetricDatum
                .builder()
                .metricName("HeapFreeSize")
                .unit(StandardUnit.BYTES)
                .value(heapFreeSize)
                .dimensions(dimension)
                .build();
            final MetricDatum heapUsedSizeMetric = MetricDatum
                .builder()
                .metricName("HeapUsedSize")
                .unit(StandardUnit.BYTES)
                .value(heapUsedSize)
                .dimensions(dimension)
                .build();
            final MetricDatum threadCountMetric = MetricDatum
                .builder()
                .metricName("Threads")
                .unit(StandardUnit.BYTES)
                .value(threadCount)
                .dimensions(dimension)
                .build();
            final MetricDatum openFileDescriptorsMetric = MetricDatum
                .builder()
                .metricName("OpenFileDescriptors")
                .unit(StandardUnit.COUNT)
                .value(openFileDescriptors)
                .dimensions(dimension)
                .build();
            final MetricDatum maxFileDescriptorsMetric = MetricDatum
                .builder()
                .metricName("MaxFileDescriptors")
                .unit(StandardUnit.COUNT)
                .value(maxFileDescriptors)
                .dimensions(dimension)
                .build();
            final MetricDatum totalPhysicalMemorySizeMetric = MetricDatum
                .builder()
                .metricName("TotalPhysicalMemorySize")
                .unit(StandardUnit.BYTES)
                .value(totalPhysicalMemorySize)
                .dimensions(dimension)
                .build();
            final MetricDatum freePhysicalMemorySizeMetric = MetricDatum
                .builder()
                .metricName("FreePhysicalMemorySize")
                .unit(StandardUnit.BYTES)
                .value(freePhysicalMemorySize)
                .dimensions(dimension)
                .build();
            final MetricDatum usedPhysicalMemorySizeMetric = MetricDatum
                .builder()
                .metricName("UsedPhysicalMemorySize")
                .unit(StandardUnit.BYTES)
                .value(usedPhysicalMemorySize)
                .dimensions(dimension)
                .build();
            final MetricDatum processCpuLoadMetric = MetricDatum
                .builder()
                .metricName("ProcessCpuLoad%")
                .unit(StandardUnit.PERCENT)
                .value(processCpuLoad)
                .dimensions(dimension)
                .build();
            final MetricDatum systemCpuLoadMetric = MetricDatum
                .builder()
                .metricName("SystemCpuLoad%")
                .unit(StandardUnit.PERCENT)
                .value(systemCpuLoad)
                .dimensions(dimension)
                .build();

            final MetricDatum[] metrics = new MetricDatum[]{
                garbageCollectionsMetric,
                garbageCollectionTimeMetric,
                heapSizeMetric,
                heapMaxSizeMetric,
                heapFreeSizeMetric,
                heapUsedSizeMetric,
                threadCountMetric,
                openFileDescriptorsMetric,
                maxFileDescriptorsMetric,
                totalPhysicalMemorySizeMetric,
                freePhysicalMemorySizeMetric,
                usedPhysicalMemorySizeMetric,
                processCpuLoadMetric,
                systemCpuLoadMetric
            };

            final PutMetricDataRequest request = PutMetricDataRequest
                .builder()
                .namespace("Java")
                .metricData(metrics)
                .build();

            final PutMetricDataResponse result = cw.putMetricData(request);
        } catch (CloudWatchException e) {
            CloudWatch.getPlugin().getLogger().severe(e.awsErrorDetails().errorMessage());
        }
    }

}
