package ru.sparkcraft.eventhelper.activators.objects;

import org.bukkit.Location;
import ru.sparkcraft.eventhelper.EventHelper;
import ru.sparkcraft.eventhelper.activators.*;

import java.util.Objects;

public class Chest extends Activator implements HaveLocation {

    private final Location location;

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
        if (eventProcessor.getEventType() == EventType.OPEN ||
                eventProcessor.getEventType() == EventType.CLOSE ||
                eventProcessor.getEventType() == EventType.PUT ||
                eventProcessor.getEventType() == EventType.TAKE) {
            getEventProcessors().add(eventProcessor);
            return true;
        }
        return false;
    }
}
