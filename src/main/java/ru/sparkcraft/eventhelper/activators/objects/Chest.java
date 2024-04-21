package ru.sparkcraft.eventhelper.activators.objects;

import org.bukkit.Location;
import ru.sparkcraft.eventhelper.activators.*;

public class Chest extends Activator {

    public Chest(String owner, String name, Location location) {
        super(owner, ActivatorType.CHEST, name, location);
    }

    @Override
    public boolean addEventProcessor(EventType eventType) {
        if (eventType == EventType.OPEN ||
                eventType == EventType.CLOSE ||
                eventType == EventType.PUT ||
                eventType == EventType.TAKE) {
            putEventProcessor(eventType);
            return true;
        }
        return false;
    }
}