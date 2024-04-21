package ru.sparkcraft.eventhelper.activators.objects;

import org.bukkit.Location;
import ru.sparkcraft.eventhelper.activators.*;

public class Plate extends Activator {

    public Plate(String owner, String name, Location location) {
        super(owner, ActivatorType.PLATE, name, location);
    }

    @Override
    public boolean addEventProcessor(EventType eventType) {
        if (eventType == EventType.USE) {
            putEventProcessor(eventType);
            return true;
        }
        return false;
    }
}
