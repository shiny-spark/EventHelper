package ru.sparkcraft.eventhelper.activators;

import org.bukkit.Location;
import ru.sparkcraft.eventhelper.EventHelper;

public interface HaveLocation {

    Location getLocation();

    default void saveToFile(EventHelper plugin, Activator activator) {
        plugin.getData().set(activator.getOwner() + "." + activator.getName() + ".location", this.getLocation().toString());
        plugin.saveData();
    }
}
