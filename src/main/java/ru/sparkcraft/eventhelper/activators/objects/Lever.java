package ru.sparkcraft.eventhelper.activators.objects;

import org.bukkit.Location;
import ru.sparkcraft.eventhelper.activators.*;

public class Lever extends Activator {

    public Lever(String owner, String name, Location location) {
        super(owner, ActivatorType.LEVER, name, location);
    }

    @Override
    public boolean addEventProcessor(EventType eventType) {
        if (eventType == EventType.USE ||
                eventType == EventType.ON ||
                eventType == EventType.OFF) {
            putEventProcessor(eventType);
            return true;
        }
        return false;
    }
}
