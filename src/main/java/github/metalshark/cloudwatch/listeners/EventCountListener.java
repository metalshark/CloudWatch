package github.metalshark.cloudwatch.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class EventCountListener<BukkitEvent> implements Listener {

    public double count = 0;

    @EventHandler(priority=EventPriority.MONITOR)
    @SuppressWarnings("unused")
    public void onEvent(BukkitEvent event) {
        count++;
    }

}
