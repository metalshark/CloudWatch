package github.metalshark.cloudwatch;

import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClientBuilder;
import github.metalshark.cloudwatch.runnables.JavaStatisticsRunnable;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CloudWatch extends JavaPlugin {

    private ScheduledExecutorService javaStatisticsExecutor;
    @Getter private AmazonCloudWatch cw;

    @Override
    public void onEnable() {
        cw = AmazonCloudWatchClientBuilder.defaultClient();

        javaStatisticsExecutor = Executors.newSingleThreadScheduledExecutor();
        javaStatisticsExecutor.scheduleAtFixedRate(new JavaStatisticsRunnable(), 0, 1, TimeUnit.MINUTES);
    }

    @Override
    public void onDisable() {
        javaStatisticsExecutor.shutdown();

        cw.shutdown();
    }

    public static CloudWatch getPlugin() {
        return getPlugin(CloudWatch.class);
    }

}
