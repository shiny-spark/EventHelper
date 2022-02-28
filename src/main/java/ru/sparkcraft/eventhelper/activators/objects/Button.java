package ru.sparkcraft.eventhelper.activators.objects;

import org.bukkit.Location;
import ru.sparkcraft.eventhelper.activators.Activator;
import ru.sparkcraft.eventhelper.activators.EventProcessor;
import ru.sparkcraft.eventhelper.activators.EventType;
import ru.sparkcraft.eventhelper.activators.HaveLocation;

public class Button extends Activator implements HaveLocation {

    private final Location location;

    public Button(String owner, String name, Location location) {
        super(owner, name);
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
