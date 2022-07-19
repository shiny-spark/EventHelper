package ru.sparkcraft.eventhelper.activators.objects;

import ru.sparkcraft.eventhelper.EventHelper;
import ru.sparkcraft.eventhelper.activators.Activator;
import ru.sparkcraft.eventhelper.activators.ActivatorType;
import ru.sparkcraft.eventhelper.activators.EventProcessor;
import ru.sparkcraft.eventhelper.activators.EventType;

import java.util.Objects;

public class Region extends Activator {

    private final String regionName;

    public Region(EventHelper plugin, String owner, String name, String regionName) {
        super(plugin, owner, ActivatorType.REGION, name);
        this.regionName = regionName;
        saveToFile(plugin, this);
    }

    public String getRegionName() {
        return regionName;
    }

    private void saveToFile(EventHelper plugin, Activator activator) {
        plugin.getData().set(activator.getOwner() + "." + activator.getName() + ".regionName", getRegionName());
        plugin.saveData();
    }

    @Override
    public boolean addEventProcessor(EventProcessor eventProcessor) {
        if (eventProcessor.getEventType() == EventType.ENTER ||
                eventProcessor.getEventType() == EventType.LEAVE) {
            getEventProcessors().add(eventProcessor);
            return true;
        }
        return false;
    }
}
