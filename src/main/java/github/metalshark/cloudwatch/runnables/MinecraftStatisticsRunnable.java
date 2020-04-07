package github.metalshark.cloudwatch.runnables;

import github.metalshark.cloudwatch.CloudWatch;
import github.metalshark.cloudwatch.listeners.*;
import org.apache.commons.lang.ArrayUtils;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.*;

import java.util.HashSet;
import java.util.Set;

public class MinecraftStatisticsRunnable implements Runnable {

    @Override
    public void run() {
        final Dimension dimension = CloudWatch.getDimension();

        final double onlinePlayers = PlayerJoinListener.maxOnlinePlayers;
        PlayerJoinListener.maxOnlinePlayers = 0;
        final double maxTickTime = TickRunnable.maxElapsedMillis;
        TickRunnable.maxElapsedMillis = 0;
        final double ticksPerSecond = TickRunnable.numberOfTicks / 60;
        TickRunnable.numberOfTicks = 0;

        try (final CloudWatchClient cw = CloudWatchClient.builder().build()) {

            final MetricDatum onlinePlayersMetric = MetricDatum
                .builder()
                .metricName("OnlinePlayers")
                .unit(StandardUnit.COUNT)
                .value(onlinePlayers)
                .dimensions(dimension)
                .build();
            final MetricDatum maxTickTimeMetric = MetricDatum
                .builder()
                .metricName("MaxTickTime")
                .unit(StandardUnit.MILLISECONDS)
                .value(maxTickTime)
                .dimensions(dimension)
                .build();
            final MetricDatum ticksPerSecondMetric = MetricDatum
                .builder()
                .metricName("TicksPerSecond")
                .unit(StandardUnit.COUNT)
                .value(ticksPerSecond)
                .dimensions(dimension)
                .build();

            final Set<MetricDatum> metricDatumSet = new HashSet<>();
            metricDatumSet.add(maxTickTimeMetric);
            metricDatumSet.add(onlinePlayersMetric);
            metricDatumSet.add(ticksPerSecondMetric);

            CloudWatch
                .getEventCountListeners()
                .forEach((name, listener) -> {
                    final double count = listener.count;
                    listener.count = 0;
                    metricDatumSet.add(MetricDatum
                        .builder()
                        .metricName(name)
                        .unit(StandardUnit.COUNT)
                        .value(count)
                        .dimensions(dimension)
                        .build());
                });

            MetricDatum[] metrics = new MetricDatum[metricDatumSet.size()];
            metricDatumSet.toArray(metrics);

            final PutMetricDataRequest request = PutMetricDataRequest
                .builder()
                .namespace("Minecraft")
                .metricData(metrics)
                .build();

            final PutMetricDataResponse result = cw.putMetricData(request);
        } catch (CloudWatchException e) {
            CloudWatch.getPlugin().getLogger().severe(e.awsErrorDetails().errorMessage());
        }
    }

}
