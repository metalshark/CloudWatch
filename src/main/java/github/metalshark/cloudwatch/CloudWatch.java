package github.metalshark.cloudwatch;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import github.metalshark.cloudwatch.listeners.*;
import github.metalshark.cloudwatch.runnables.JavaStatisticsRunnable;
import github.metalshark.cloudwatch.runnables.MinecraftStatisticsRunnable;
import github.metalshark.cloudwatch.runnables.TickRunnable;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.regions.internal.util.EC2MetadataUtils;
import software.amazon.awssdk.services.cloudwatch.model.Dimension;

import java.util.Map;
import java.util.concurrent.*;

public class CloudWatch extends JavaPlugin {

    @Getter
    private ChunkLoadListener chunkLoadListener = new ChunkLoadListener();

    @Getter
    private PlayerJoinListener playerJoinListener = new PlayerJoinListener();

    @Getter
    private TickRunnable tickRunnable = new TickRunnable();

    @Getter
    private final static Map<String, EventCountListener> eventCountListeners = new ConcurrentHashMap<>();

    private final static ThreadFactory javaStatisticsThreadFactory = new ThreadFactoryBuilder()
        .setNameFormat("CloudWatch - Java Statistics")
        .build();
    private final static ThreadFactory minecraftStatisticsThreadFactory = new ThreadFactoryBuilder()
        .setNameFormat("CloudWatch - Minecraft Statistics")
        .build();

    private ScheduledExecutorService javaStatisticsExecutor;
    private ScheduledExecutorService minecraftStatisticsExecutor;

    @Getter
    private static Dimension dimension;

    @Override
    public void onEnable() {
        try {
            dimension = Dimension
                .builder()
                .name("Per-Instance Metrics")
                .value(EC2MetadataUtils.getInstanceId())
                .build();
        } catch (SdkClientException exception) {
            getLogger().warning("The CloudWatch plugin only works on EC2 instances.");
            this.setEnabled(false);
            return;
        }

        final PluginManager pluginManager = Bukkit.getPluginManager();

        pluginManager.registerEvents(chunkLoadListener.init(), this);
        pluginManager.registerEvents(playerJoinListener.init(), this);

        eventCountListeners.put("ChunksPopulated", new ChunkPopulateListener());
        eventCountListeners.put("CreaturesSpawned", new CreatureSpawnListener());
        eventCountListeners.put("EntityDeaths", new EntityDeathListener());
        eventCountListeners.put("InventoriesClosed", new InventoryCloseListener());
        eventCountListeners.put("InventoriesOpened", new InventoryOpenListener());
        eventCountListeners.put("InventoryClicks", new InventoryClickListener());
        eventCountListeners.put("InventoryDrags", new InventoryDragListener());
        eventCountListeners.put("ItemsDespawned", new ItemSpawnListener());
        eventCountListeners.put("ItemsSpawned", new ItemDespawnListener());
        eventCountListeners.put("PlayerDropItems", new PlayerDropItemListener());
        eventCountListeners.put("PlayerExperienceChanges", new PlayerExpChangeListener());
        eventCountListeners.put("PlayerInteractions", new PlayerInteractListener());
        eventCountListeners.put("ProjectilesLaunched", new ProjectileLaunchListener());
        eventCountListeners.put("StructuresGrown", new StructureGrowListener());
        eventCountListeners.put("TradesSelected", new TradeSelectListener());

        for (Map.Entry<String, EventCountListener> entry : eventCountListeners.entrySet()) {
            final EventCountListener listener = entry.getValue();
            pluginManager.registerEvents(listener, this);
        }

        javaStatisticsExecutor = Executors.newSingleThreadScheduledExecutor(javaStatisticsThreadFactory);
        javaStatisticsExecutor.scheduleAtFixedRate(new JavaStatisticsRunnable(), 0, 1, TimeUnit.MINUTES);

        minecraftStatisticsExecutor = Executors.newSingleThreadScheduledExecutor(minecraftStatisticsThreadFactory);
        minecraftStatisticsExecutor.scheduleAtFixedRate(new MinecraftStatisticsRunnable(), 0, 1, TimeUnit.MINUTES);

        Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(this, tickRunnable, 1, 1);
    }

    @Override
    public void onDisable() {
        ChunkLoadEvent.getHandlerList().unregister(chunkLoadListener);
        ChunkUnloadEvent.getHandlerList().unregister(chunkLoadListener);
        PlayerJoinEvent.getHandlerList().unregister(playerJoinListener);
        PlayerQuitEvent.getHandlerList().unregister(playerJoinListener);

        for (Map.Entry<String, EventCountListener> entry : eventCountListeners.entrySet()) {
            final Listener listener = entry.getValue();
            HandlerList.unregisterAll(listener);
        }

        if (javaStatisticsExecutor != null) javaStatisticsExecutor.shutdown();
        if (minecraftStatisticsExecutor != null) minecraftStatisticsExecutor.shutdown();
    }

    public static CloudWatch getPlugin() {
        return getPlugin(CloudWatch.class);
    }

}
