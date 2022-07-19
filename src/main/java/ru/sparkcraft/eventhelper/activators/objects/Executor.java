package ru.sparkcraft.eventhelper.activators.objects;

import ru.sparkcraft.eventhelper.EventHelper;
import ru.sparkcraft.eventhelper.activators.Activator;
import ru.sparkcraft.eventhelper.activators.ActivatorType;
import ru.sparkcraft.eventhelper.activators.EventProcessor;
import ru.sparkcraft.eventhelper.activators.EventType;

public class Executor extends Activator {

    public Executor(EventHelper plugin, String owner, String name) {
        super(plugin, owner, ActivatorType.EXECUTOR, name);
        saveToFile(plugin, this);
    }

    private void saveToFile(EventHelper plugin, Activator activator) {
        plugin.getData().createSection(activator.getOwner() + "." + activator.getName());
        plugin.saveData();
    }

    @Override
    public boolean addEventProcessor(EventProcessor eventProcessor) {
        if (eventProcessor.getEventType() == EventType.RUN) {
            getEventProcessors().add(eventProcessor);
            return true;
        }
        return false;
    }
}
