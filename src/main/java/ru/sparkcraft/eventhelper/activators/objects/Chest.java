package ru.sparkcraft.eventhelper.activators.objects;

import org.bukkit.Location;
import ru.sparkcraft.eventhelper.EventHelper;
import ru.sparkcraft.eventhelper.activators.Activator;
import ru.sparkcraft.eventhelper.activators.ActivatorType;
import ru.sparkcraft.eventhelper.activators.EventProcessor;

public class Chest extends Activator {
    public Chest(EventHelper plugin, String owner, ActivatorType type, String name, Location location) {
        super(plugin, owner, type, name);
    }

    @Override
    public boolean addEventProcessor(EventProcessor eventProcessor) {
        return false;
    }
    // open, close, put, take
}
