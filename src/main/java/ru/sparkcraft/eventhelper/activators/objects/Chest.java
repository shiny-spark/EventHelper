package ru.sparkcraft.eventhelper.activators.objects;

import org.bukkit.Location;
import ru.sparkcraft.eventhelper.EventHelper;
import ru.sparkcraft.eventhelper.activators.Activator;
import ru.sparkcraft.eventhelper.activators.ActivatorType;
import ru.sparkcraft.eventhelper.activators.EventProcessor;
import ru.sparkcraft.eventhelper.activators.HaveLocation;

public class Chest extends Activator implements HaveLocation {

    private Location location;

    public Chest(EventHelper plugin, String owner, ActivatorType type, String name, Location location) {
        super(plugin, owner, type, name);
        this.location = location;
        saveToFile(plugin, this);
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public boolean addEventProcessor(EventProcessor eventProcessor) {
        return false;
    }

    // open, close, put, take
}
