package ru.sparkcraft.eventhelper.activators.objects;

import ru.sparkcraft.eventhelper.activators.Activator;
import ru.sparkcraft.eventhelper.activators.ActivatorType;
import ru.sparkcraft.eventhelper.activators.EventType;

public class Function extends Activator {

    public Function(String owner, String name) {
        super(owner, ActivatorType.FUNCTION, name, null);
    }

    @Override
    public boolean addEventProcessor(EventType eventType) {
        if (eventType == EventType.RUN) {
            putEventProcessor(eventType);
            return true;
        }
        return false;
    }
}
