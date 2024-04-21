package ru.sparkcraft.eventhelper.activators;

import org.bukkit.entity.Player;

import java.util.*;

public class EventProcessor {

    private int id;
    private final List<Action> actionsQueue = new ArrayList<>();
    private final Activator activator;
    private final EventType eventType;

    public EventProcessor(Activator activator, EventType eventType) {
        this.activator = activator;
        this.eventType = eventType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EventProcessor that = (EventProcessor) o;
        return eventType == that.eventType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventType);
    }

    public void run(Player player) {
        ActionProcessor.run(actionsQueue, player);
    }

    public void addAction(ActionType actionType, String value) {
        Action action = new Action(this.id, actionType, value, getActionsQueue().size());
        ActivatorDAO.getInstance().saveAction(action);
        getActionsQueue().add(action);
    }

    public void addAction(Action action) {
        getActionsQueue().add(action);
    }

    public void deleteAction(int index) {
        getActionsQueue().remove(index);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Activator getActivator() {
        return activator;
    }

    public EventType getEventType() {
        return eventType;
    }

    public List<Action> getActionsQueue() {
        return actionsQueue;
    }
}