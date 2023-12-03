package github.metalshark.cloudwatch.listeners;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerJoinListener implements Listener {

    private double count = 0;
    private double max = 0;

    public PlayerJoinListener init() {
        for (World world : Bukkit.getWorlds()) {
            count = world.getPlayers().size();
        }
        return this;
    }

    @EventHandler(priority=EventPriority.MONITOR)
    @SuppressWarnings("unused")
    public void onPlayerJoin(PlayerJoinEvent event) {
        count++;
        if (count > max) max = count;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    @SuppressWarnings("unused")
    public void onPlayerLeave(PlayerQuitEvent event) {
        count--;
    }

    public double getMaxAndReset() {
        final double prevMax = max;
        max = count;
        return prevMax;
    }

}
