package ru.sparkcraft.eventhelper.activators.objects;

import org.bukkit.Location;
import ru.sparkcraft.eventhelper.EventHelper;
import ru.sparkcraft.eventhelper.activators.*;

import java.util.Objects;

public class Plate extends Activator implements HaveLocation {

    private final Location location;

    public Plate(EventHelper plugin, String owner, String name, Location location) {
        super(plugin, owner, ActivatorType.PLATE, name);
        this.location = location;
        saveToFile(plugin, this);
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public boolean addEventProcessor(EventProcessor eventProcessor) {
        if (eventProcessor.getEventType() == EventType.USE) {
            getEventProcessors().add(eventProcessor);
            return true;
        }
        return false;
    }
}
