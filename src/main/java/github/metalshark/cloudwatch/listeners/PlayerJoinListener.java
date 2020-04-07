package github.metalshark.cloudwatch.listeners;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    public static double maxOnlinePlayers = 0;

    @EventHandler(priority=EventPriority.MONITOR)
    @SuppressWarnings("unused")
    public void onPlayerJoin(PlayerJoinEvent event) {
        final double onlinePlayers = Bukkit.getOnlinePlayers().size();
        if (onlinePlayers > maxOnlinePlayers) maxOnlinePlayers = onlinePlayers;
    }

}
