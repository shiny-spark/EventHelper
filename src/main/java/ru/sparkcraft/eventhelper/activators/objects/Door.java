package ru.sparkcraft.eventhelper.activators.objects;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.Bisected;
import org.jetbrains.annotations.NotNull;
import ru.sparkcraft.eventhelper.activators.*;

public class Door extends Activator {

    public Door(String owner, String name, Location location) {
        super(owner, ActivatorType.DOOR, name, location);
    }

    @Override
    public boolean addEventProcessor(EventType eventType) {
        if (eventType == EventType.USE ||
                eventType == EventType.OPEN ||
                eventType == EventType.CLOSE) {
            putEventProcessor(eventType);
            return true;
        }
        return false;
    }

    public static @NotNull Location getTop(@NotNull Block block) {
        Bisected.Half half = ((Bisected) block.getBlockData()).getHalf();
        if (half == Bisected.Half.BOTTOM) {
            return new Location(block.getWorld(), block.getX(), block.getY() + 1.0, block.getZ());
        } else {
            return block.getLocation();
        }
    }
}