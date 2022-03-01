package ru.sparkcraft.eventhelper.activators.objects;

import org.bukkit.Location;
import ru.sparkcraft.eventhelper.EventHelper;
import ru.sparkcraft.eventhelper.activators.*;

public class Button extends Activator implements HaveLocation {

    private final Location location;

    public Button(EventHelper plugin, String owner, ActivatorType type, String name, Location location) {
        super(plugin, owner, type, name);
        this.location = location;
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
