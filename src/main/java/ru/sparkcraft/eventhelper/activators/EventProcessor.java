package ru.sparkcraft.eventhelper.activators;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import ru.sparkcraft.eventhelper.EventHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EventProcessor {

    private final EventHelper plugin = EventHelper.getInstance();

    public class Action {
        private final ActionType actionType;
        private final String value;

        private Action(ActionType actionType, String value) {
            this.actionType = actionType;
            this.value = value;

            actionsQueue.add(this);

            plugin.getData().set(activator.getOwner() + "." + activator.getName() + ".eventType." + eventType.name() + "." + actionType, value);
            plugin.saveData();
        }

        public ActionType getActionType() {
            return actionType;
        }

        public String getValue() {
            return value;
        }
    }

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
        for (Action action : actionsQueue) {
            switch (action.actionType) {
                case COMMAND:
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                            action.value.replace("%player%", player.getName()));
                    break;
                case MESSAGE:
                    player.sendMessage(action.value.replace("%player%", player.getName()));
                    break;
                case TP:
                    String[] args = action.value.split(" ");
                    player.teleport(new Location(Bukkit.getWorld(args[3]), Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2])));
                    break;
                case EFFECT:
                    // player.addPotionEffect(new PotionEffect());
                    break;
                case KILL:
                    player.setHealth(0);
                    break;
                case HEALTH:
                    player.setHealth(player.getMaxHealth());
                    break;
                case GIVE:
                    break;
                case TAKE:
                    break;
                case ANNOUNCE:
                    Bukkit.broadcast();
                    //todo
                    break;
                case FLY:
                    break;
                case TAG:
                    break;
            }
        }
    }

    public void addAction(ActionType actionType, String value) {
        new Action(actionType, value);
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


