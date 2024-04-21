package ru.sparkcraft.eventhelper.activators;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.MetaNode;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class ActionProcessor {

    private static JavaPlugin plugin;

    private ActionProcessor() {
        throw new IllegalStateException("Utility class");
    }

    public static void run(List<Action> actionsQueue, Player player) {
        processQueue(actionsQueue, player, 0);
    }

    private static void processQueue(List<Action> actionsQueue, Player player, int index) {
        if (!actionsQueue.isEmpty()) {
            Action act;
            final boolean[] pauseExecution = {false};
            final boolean[] ifConditionMet = {false};
            do {
                act = actionsQueue.get(index++);
                if (act.getActionType() != ActionType.DELAY) {
                    Action action = act;
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (pauseExecution[0] && action.getActionType() != ActionType.ELSE && action.getActionType() != ActionType.ENDIF && action.getActionType() != ActionType.ELSEIF) {
                                return;
                            }

                            String[] args = new String[0];
                            if (action.getValue() != null) {
                                args = action.getValue().split(" ");
                            }
                            switch (action.getActionType()) {
                                case COMMAND -> {
                                    assert action.getValue() != null;
                                    runCommand(action.getValue(), player.getName());
                                }
                                case MESSAGE -> {
                                    Audience pl = player;
                                    assert action.getValue() != null;
                                    String message = action.getValue().replace("%player%", player.getName());
                                    var mm = MiniMessage.miniMessage();
                                    Component parsed = mm.deserialize(message);
                                    pl.sendMessage(parsed);
                                }
                                case TP -> {
                                    World w = Bukkit.getWorld(args[3]);
                                    double x = Double.parseDouble(args[0]);
                                    double y = Double.parseDouble(args[1]);
                                    double z = Double.parseDouble(args[2]);
                                    player.teleport(new Location(w, x, y, z));
                                }
                                case EFFECT -> {
                                    // player.addPotionEffect(new PotionEffect());
                                }
                                case KILL -> {
                                    player.setHealth(0);
                                }
                                case HEALTH -> {
                                    player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
                                }
                                case GIVE -> {
                                    Material material = Material.valueOf(args[0].toUpperCase());
                                    ItemStack itemStack = new ItemStack(material, Integer.parseInt(args[1]));
                                    player.getInventory().addItem(itemStack);
                                }
                                case TAKE -> {
                                    Material material = Material.valueOf(args[0].toUpperCase());
                                    ItemStack itemStack = new ItemStack(material, Integer.parseInt(args[1]));
                                    player.getInventory().removeItem(itemStack);
                                }
                                case ANNOUNCE -> {
                                    for (Player ply : Bukkit.getOnlinePlayers()) {
                                        Audience pl = ply;
                                        assert action.getValue() != null;
                                        String message = action.getValue().replace("%player%", player.getName());
                                        var mm = MiniMessage.miniMessage();
                                        Component parsed = mm.deserialize(message);
                                        pl.sendMessage(parsed);
                                    }
                                }
                                case FLY -> {
                                    assert action.getValue() != null;
                                    if (action.getValue().equalsIgnoreCase("on")) {
                                        player.setAllowFlight(true);
                                        player.setFlying(true);
                                    } else if (action.getValue().equalsIgnoreCase("off")) {
                                        player.setAllowFlight(false);
                                        player.setFlying(false);
                                    }
                                }
                                case META -> {
                                    String metaAction = args[0];
                                    String key = args[1].toLowerCase();
                                    String value;
                                    LuckPerms luckPerms = LuckPermsProvider.get();
                                    User user = luckPerms.getPlayerAdapter(Player.class).getUser(player);

                                    switch (metaAction) {
                                        case "set" -> {
                                            if (args.length > 2) {
                                                value = args[2];
                                                MetaNode node = MetaNode.builder(key, value).build();
                                                user.data().clear(NodeType.META.predicate(mn -> mn.getMetaKey().equals(key)));
                                                user.data().add(node);
                                                luckPerms.getUserManager().saveUser(user);
                                            }
                                        }
                                        case "unset" -> {
                                            user.data().clear(NodeType.META.predicate(mn -> mn.getMetaKey().equals(key)));
                                            luckPerms.getUserManager().saveUser(user);
                                        }
                                    }
                                }
                                case CONDITION -> {
                                    assert action.getValue() != null;
                                    String[] splitValue = action.getValue().split("\\?");
                                    if (splitValue.length > 1) {
                                        String condition = splitValue[0].trim();
                                        String[] commands = splitValue[1].split(":");
                                        String[] conditionArgs;

                                        if (condition.contains("==") || condition.contains("!=")) {
                                            boolean equals;
                                            if (condition.contains("==")) {
                                                conditionArgs = condition.split("==");
                                                equals = true;
                                            } else {
                                                conditionArgs = condition.split("!=");
                                                equals = false;
                                            }
                                            String key = conditionArgs[0].trim();
                                            String value = conditionArgs[1].trim();

                                            if (key.contains(":")) {
                                                String[] keyArgs = key.split(":");
                                                switch (keyArgs[0].trim()) {
                                                    case "meta" -> {
                                                        LuckPerms luckPerms = LuckPermsProvider.get();
                                                        CachedMetaData metaData = luckPerms.getPlayerAdapter(Player.class).getMetaData(player);
                                                        String playerName = player.getName();
                                                        boolean metaEquals = Objects.equals(metaData.getMetaValue(keyArgs[1].trim()), value);

                                                        if (equals) {
                                                            if (metaEquals) {
                                                                runCommand(commands[0], playerName);
                                                            } else {
                                                                if (commands.length > 1) {
                                                                    runCommand(commands[1], playerName);
                                                                }
                                                            }
                                                        } else {
                                                            if (!metaEquals) {
                                                                runCommand(commands[0], playerName);
                                                            } else {
                                                                if (commands.length > 1) {
                                                                    runCommand(commands[1], playerName);
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                case IF, ELSEIF -> {
                                    String condition = action.getValue();
                                    String[] conditionArgs;

                                    if (condition.contains("==") || condition.contains("!=")) {
                                        boolean equals;
                                        if (condition.contains("==")) {
                                            conditionArgs = condition.split("==");
                                            equals = true;
                                        } else {
                                            conditionArgs = condition.split("!=");
                                            equals = false;
                                        }
                                        String key = conditionArgs[0].trim();
                                        String value = conditionArgs[1].trim();

                                        if (key.contains(":")) {
                                            String[] keyArgs = key.split(":");
                                            switch (keyArgs[0].trim()) {
                                                case "meta" -> {
                                                    LuckPerms luckPerms = LuckPermsProvider.get();
                                                    CachedMetaData metaData = luckPerms.getPlayerAdapter(Player.class).getMetaData(player);
                                                    boolean metaEquals = Objects.equals(metaData.getMetaValue(keyArgs[1].trim()), value);

                                                    if (equals) {
                                                        if (metaEquals) {
                                                            pauseExecution[0] = true;
                                                            ifConditionMet[0] = true;
                                                        }
                                                    } else {
                                                        if (!metaEquals) {
                                                            pauseExecution[0] = true;
                                                            ifConditionMet[0] = true;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                case ELSE -> {
                                    pauseExecution[0] = !ifConditionMet[0];
                                }
                                case ENDIF -> {
                                    pauseExecution[0] = false;
                                }
                            }
                        }
                    }.runTaskLater(plugin, 5L);

                } else if (index < actionsQueue.size()) {
                    int finalIndex = index;
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            processQueue(actionsQueue, player, finalIndex);
                        }
                    }.runTaskLater(plugin, 20L * Long.parseLong(act.getValue()));
                }
            } while (act.getActionType() != ActionType.DELAY && index < actionsQueue.size());
        }
    }

    private static void runCommand(@NotNull String command, String playerName) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                command.replace("%player%", playerName));
    }

    public static void init(JavaPlugin plugin) {
        ActionProcessor.plugin = plugin;
    }
}
