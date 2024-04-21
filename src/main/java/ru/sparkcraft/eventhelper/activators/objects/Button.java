package ru.sparkcraft.eventhelper.activators.objects;

import org.bukkit.Location;
import ru.sparkcraft.eventhelper.activators.*;

public class Button extends Activator {

    public Button(String owner, String name, Location location) {
        super(owner, ActivatorType.BUTTON, name, location);
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
