package github.metalshark.cloudwatch;

import github.metalshark.cloudwatch.runnables.JavaStatisticsRunnable;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CloudWatch extends JavaPlugin {

    private ScheduledExecutorService javaStatisticsExecutor;

    @Override
    public void onEnable() {
        javaStatisticsExecutor = Executors.newSingleThreadScheduledExecutor();
        javaStatisticsExecutor.scheduleAtFixedRate(new JavaStatisticsRunnable(), 0, 1, TimeUnit.MINUTES);
    }

    @Override
    public void onDisable() {
        javaStatisticsExecutor.shutdown();
    }

    public static CloudWatch getPlugin() {
        return getPlugin(CloudWatch.class);
    }

}
