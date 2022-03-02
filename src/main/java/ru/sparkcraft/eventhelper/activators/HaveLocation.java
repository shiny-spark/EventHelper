package ru.sparkcraft.eventhelper.activators;

import org.bukkit.Location;
import ru.sparkcraft.eventhelper.EventHelper;

public interface HaveLocation {

    Location getLocation();

    default void saveToFile(EventHelper plugin, Activator activator) {
        HaveLocation a = (HaveLocation) activator;
        String loc = a.getLocation().getWorld().getName() + "," + a.getLocation().getBlockX() + "," + a.getLocation().getBlockY() + "," + a.getLocation().getBlockZ();
        plugin.getData().set(activator.getOwner() + "." + activator.getName() + ".location", loc);
        plugin.saveData();
    }
}