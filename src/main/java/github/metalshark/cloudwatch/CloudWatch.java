package github.metalshark.cloudwatch;

import github.metalshark.cloudwatch.listeners.*;
import github.metalshark.cloudwatch.runnables.JavaStatisticsRunnable;
import github.metalshark.cloudwatch.runnables.MinecraftStatisticsRunnable;
import github.metalshark.cloudwatch.runnables.TickRunnable;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import software.amazon.awssdk.regions.internal.util.EC2MetadataUtils;
import software.amazon.awssdk.services.cloudwatch.model.Dimension;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CloudWatch extends JavaPlugin {

    private Listener playerJoinListener = new PlayerJoinListener();

    @Getter private final static Map<String, EventCountListener> eventCountListeners = new ConcurrentHashMap<>();

    private ScheduledExecutorService javaStatisticsExecutor;
    private ScheduledExecutorService minecraftStatisticsExecutor;

    @Getter private final static Dimension dimension = Dimension
        .builder()
        .name("Per-Instance Metrics")
        .value(EC2MetadataUtils.getInstanceId())
        .build();

    @Override
    public void onEnable() {
        final PluginManager pluginManager = Bukkit.getPluginManager();

        pluginManager.registerEvents(playerJoinListener, this);

        eventCountListeners.put("ChunksLoaded", new ChunkLoadListener());
        eventCountListeners.put("ChunksPopulated", new ChunkPopulateListener());
        eventCountListeners.put("ChunksUnloaded", new ChunkUnloadListener());
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

        javaStatisticsExecutor = Executors.newSingleThreadScheduledExecutor();
        javaStatisticsExecutor.scheduleAtFixedRate(new JavaStatisticsRunnable(), 0, 1, TimeUnit.MINUTES);

        minecraftStatisticsExecutor = Executors.newSingleThreadScheduledExecutor();
        minecraftStatisticsExecutor.scheduleAtFixedRate(new MinecraftStatisticsRunnable(), 0, 1, TimeUnit.MINUTES);

        Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(this, new TickRunnable(), 1, 1);
    }

    @Override
    public void onDisable() {
        PlayerJoinEvent.getHandlerList().unregister(playerJoinListener);

        for (Map.Entry<String, EventCountListener> entry : eventCountListeners.entrySet()) {
            final Listener listener = entry.getValue();
            HandlerList.unregisterAll(listener);
        }

        javaStatisticsExecutor.shutdown();
        minecraftStatisticsExecutor.shutdown();
    }

    public static CloudWatch getPlugin() {
        return getPlugin(CloudWatch.class);
    }

}
