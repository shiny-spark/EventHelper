package ru.sparkcraft.eventhelper.activators.objects;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.Bisected;
import ru.sparkcraft.eventhelper.EventHelper;
import ru.sparkcraft.eventhelper.activators.*;

import java.util.Objects;

public class Door extends Activator implements HaveLocation {

    private final Location location;

    public Door(EventHelper plugin, String owner, String name, Location location) {
        super(plugin, owner, ActivatorType.DOOR, name);
        this.location = location;
        saveToFile(plugin, this);
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public boolean addEventProcessor(EventProcessor eventProcessor) {
        if (eventProcessor.getEventType() == EventType.USE ||
                eventProcessor.getEventType() == EventType.OPEN ||
                eventProcessor.getEventType() == EventType.CLOSE) {
            getEventProcessors().add(eventProcessor);
            return true;
        }
        return false;
    }

    public static Location getTop(Block block) {
        Bisected.Half half = ((Bisected)block.getBlockData()).getHalf();
        if (half == Bisected.Half.BOTTOM) {
            return new Location(block.getWorld(), block.getX(), block.getY() + 1.0, block.getZ());
        } else {
            return block.getLocation();
        }
    }
}
