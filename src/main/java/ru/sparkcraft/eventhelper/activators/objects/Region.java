package ru.sparkcraft.eventhelper.activators.objects;

import ru.sparkcraft.eventhelper.activators.*;

public class Region extends Activator {

    private final String regionName;

    public Region(String owner, String name, String regionName) {
        super(owner, ActivatorType.REGION, name, null);
        this.regionName = regionName;
    }

    public String getRegionName() {
        return regionName;
    }

    @Override
    public boolean addEventProcessor(EventType eventType) {
        if (eventType == EventType.ENTER ||
                eventType == EventType.LEAVE) {
            putEventProcessor(eventType);
            return true;
        }
        return false;
    }
}