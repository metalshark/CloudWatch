package github.metalshark.cloudwatch;

import github.metalshark.cloudwatch.listeners.*;
import github.metalshark.cloudwatch.runnables.JavaStatisticsRunnable;
import github.metalshark.cloudwatch.runnables.MinecraftStatisticsRunnable;
import github.metalshark.cloudwatch.runnables.TickRunnable;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkPopulateEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.StructureGrowEvent;
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

        eventCountListeners.put("ChunksLoaded", new EventCountListener<ChunkLoadEvent>());
        eventCountListeners.put("ChunksPopulated", new EventCountListener<ChunkPopulateEvent>());
        eventCountListeners.put("ChunksUnloaded", new EventCountListener<ChunkUnloadEvent>());
        eventCountListeners.put("CreaturesSpawned", new EventCountListener<CreatureSpawnEvent>());
        eventCountListeners.put("EntityDeaths", new EventCountListener<EntityDeathEvent>());
        eventCountListeners.put("InventoriesClosed", new EventCountListener<InventoryCloseEvent>());
        eventCountListeners.put("InventoriesOpened", new EventCountListener<InventoryOpenEvent>());
        eventCountListeners.put("InventoryClicks", new EventCountListener<InventoryClickEvent>());
        eventCountListeners.put("InventoryDrags", new EventCountListener<InventoryDragEvent>());
        eventCountListeners.put("ItemsDespawned", new EventCountListener<ItemSpawnEvent>());
        eventCountListeners.put("ItemsSpawned", new EventCountListener<ItemDespawnEvent>());
        eventCountListeners.put("PlayerDropItems", new EventCountListener<PlayerDropItemEvent>());
        eventCountListeners.put("PlayerExperienceChanges", new EventCountListener<PlayerExpChangeEvent>());
        eventCountListeners.put("PlayerInteractions", new EventCountListener<PlayerInteractEvent>());
        eventCountListeners.put("ProjectilesLaunched", new EventCountListener<ProjectileLaunchEvent>());
        eventCountListeners.put("StructuresGrown", new EventCountListener<StructureGrowEvent>());
        eventCountListeners.put("TradeSelects", new EventCountListener<TradeSelectEvent>());

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
