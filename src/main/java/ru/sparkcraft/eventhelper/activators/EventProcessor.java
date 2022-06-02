package ru.sparkcraft.eventhelper.activators;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import ru.sparkcraft.eventhelper.EventHelper;

import java.util.*;

public class EventProcessor {

    public static class Action {
        private final ActionType actionType;
        private final String value;

        private Action(ActionType actionType, String value) {
            this.actionType = actionType;
            this.value = value;
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
    private final EventHelper plugin;

    public EventProcessor(Activator activator, EventType eventType, EventHelper plugin) {
        this.activator = activator;
        this.eventType = eventType;
        this.plugin = plugin;
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
        processQueue(player, 0);
    }

    public void processQueue(Player player, int index) {
        Action action;
        do {
            action = actionsQueue.get(index++);
            if (action.actionType != ActionType.DELAY) {

                switch (action.actionType) {
                    case COMMAND:
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                                action.value.replace("%player%", player.getName()));
                        break;
                    case MESSAGE:
                        Audience pl = (Audience) player;
                        String message = action.value.replace("%player%", player.getName());
                        var mm = MiniMessage.miniMessage();
                        Component parsed = mm.deserialize(message);
                        pl.sendMessage(parsed);
                        break;
                    case TP:
                        String[] args = action.value.split(" ");
                        World w = Bukkit.getWorld(args[3]);
                        double x = Double.parseDouble(args[0]);
                        double y = Double.parseDouble(args[1]);
                        double z = Double.parseDouble(args[2]);
                        player.teleport(new Location(w, x, y, z));
                        break;
                    case EFFECT:
                        // player.addPotionEffect(new PotionEffect());
                        break;
                    case KILL:
                        player.setHealth(0);
                        break;
                    case HEALTH:
                        player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
                        break;
                    case GIVE:
                        args = action.value.split(" ");
                        Material material = Material.valueOf(args[0].toUpperCase());
                        ItemStack itemStack = new ItemStack(material, Integer.parseInt(args[1]));
                        player.getInventory().addItem(itemStack);
                        break;
                    case TAKE:
                        args = action.value.split(" ");
                        material = Material.valueOf(args[0].toUpperCase());
                        itemStack = new ItemStack(material, Integer.parseInt(args[1]));
                        player.getInventory().removeItem(itemStack);
                        break;
                    case ANNOUNCE:
                        for (Player ply : Bukkit.getOnlinePlayers()) {
                            pl = (Audience) ply;
                            message = action.value.replace("%player%", player.getName());
                            mm = MiniMessage.miniMessage();
                            parsed = mm.deserialize(message);
                            pl.sendMessage(parsed);
                        }
                        break;
                    case FLY:
                        if (action.value.equalsIgnoreCase("on")) {
                            player.setAllowFlight(true);
                            player.setFlying(true);
                        } else if (action.value.equalsIgnoreCase("off")) {
                            player.setAllowFlight(false);
                            player.setFlying(false);
                        }
                        break;
                    case TAG:
                        break;
                }

            } else if (index < actionsQueue.size()) {
                int finalIndex = index;
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        processQueue(player, finalIndex);
                    }
                }.runTaskLater(plugin, 20L * Long.parseLong(action.value));
            }
        } while (action.actionType != ActionType.DELAY && index < actionsQueue.size());
    }

    public void addAction(ActionType actionType, String value) {
        getActionsQueue().add(new Action(actionType, value));
        saveToFile();
    }

    public void deleteAction(int index) {
        getActionsQueue().remove(index);
        saveToFile();
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

    private void saveToFile() {
        List<String> actions = new ArrayList<>();
        for (Action action : actionsQueue) {
            actions.add(action.value == null ? action.actionType.name() : action.actionType.name() + ":" + action.value);
        }
        plugin.getData().set(activator.getOwner() + "." + activator.getName() + ".eventType." + eventType.name(), actions);
        plugin.saveData();
    }
}