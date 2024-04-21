package ru.sparkcraft.eventhelper.activators;

public class Action {

    private int id;
    private final int eventProcessorId;
    private final ActionType actionType;
    private final String value;
    private final int order;

    public Action(int eventProcessorId, ActionType actionType, String value, int order) {
        this.eventProcessorId = eventProcessorId;
        this.actionType = actionType;
        this.value = value;
        this.order = order;
    }

    public int getEventProcessorId() {
        return eventProcessorId;
    }

    public int getOrder() {
        return order;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public String getValue() {
        return value;
    }
}