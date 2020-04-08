package github.metalshark.cloudwatch.listeners;

import lombok.Getter;
import org.bukkit.event.Listener;

public class EventCountListener implements Listener {

    protected double count = 0;

    public double getCountAndReset() {
        final double oldCount = count;
        count = 0;
        return oldCount;
    }

}
