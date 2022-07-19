package ru.sparkcraft.eventhelper.activators.objects;

import org.bukkit.Location;
import ru.sparkcraft.eventhelper.EventHelper;
import ru.sparkcraft.eventhelper.activators.*;

import java.util.Objects;

public class Lever extends Activator implements HaveLocation {

    private final Location location;

    public Lever(EventHelper plugin, String owner, String name, Location location) {
        super(plugin, owner, ActivatorType.LEVER, name);
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
                eventProcessor.getEventType() == EventType.ON ||
                eventProcessor.getEventType() == EventType.OFF) {
            getEventProcessors().add(eventProcessor);
            return true;
        }
        return false;
    }
}
