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

    @Getter
    private double count = 0;

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
    }

    @EventHandler(priority=EventPriority.MONITOR)
    @SuppressWarnings("unused")
    public void onChunkUnload(ChunkUnloadEvent event) {
        count--;
    }

}
