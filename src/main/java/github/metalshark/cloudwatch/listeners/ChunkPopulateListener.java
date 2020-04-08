package github.metalshark.cloudwatch.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.world.ChunkPopulateEvent;

public class ChunkPopulateListener extends EventCountListener {

    public double count = 0;

    @EventHandler(priority=EventPriority.MONITOR)
    @SuppressWarnings("unused")
    public void onEvent(ChunkPopulateEvent event) {
        count++;
    }

}
