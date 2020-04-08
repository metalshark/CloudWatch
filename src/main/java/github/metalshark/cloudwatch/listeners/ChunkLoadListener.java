package github.metalshark.cloudwatch.listeners;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

public class ChunkLoadListener implements Listener {

    private double count = 0;
    private double max = 0;

    public ChunkLoadListener init() {
        for (World world : Bukkit.getWorlds()) {
            count = world.getLoadedChunks().length;
        }
        return this;
    }

    @EventHandler(priority=EventPriority.MONITOR)
    @SuppressWarnings("unused")
    public void onChunkLoad(ChunkLoadEvent event) {
        count++;
        if (count > max) max = count;
    }

    @EventHandler(priority=EventPriority.MONITOR)
    @SuppressWarnings("unused")
    public void onChunkUnload(ChunkUnloadEvent event) {
        count--;
    }

    public double getMaxAndReset() {
        final double prevMax = max;
        max = count;
        return prevMax;
    }

}
