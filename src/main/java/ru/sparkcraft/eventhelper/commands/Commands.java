package ru.sparkcraft.eventhelper.commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.sparkcraft.eventhelper.EventHelper;
import ru.sparkcraft.eventhelper.activators.*;
import ru.sparkcraft.eventhelper.activators.objects.*;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Commands implements CommandExecutor {

    private final EventHelper plugin;
    private final Map<CommandSender, Activator> selectedActivators = new HashMap<>();

    private static final String NOT_FOUND = "Активатор с таким именем не найден.";
    private static final String NOT_CAN = "Данный тип действия недоступен для этого типа активатора.";
    private static final String CREATED = "Активатор создан под именем: ";
    private static final String ALREADY_EXISTS = "Активатор с таким именем уже существует.";
    private static final String NEED_TO_SELECT = "Сначала выберите активатор.";
    private static final String INVALID_ARGUMENTS = "Неверные аргументы команды.";

    public Commands(EventHelper plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player && sender.hasPermission("e.commands")) {
            if (args.length == 0) {
                help(sender);
            } else {
                switch (args[0]) {
                    case "create" -> create(sender, args);
                    case "add" -> add(sender, args, 1);
                    case "list" -> list(sender);
                    case "delete" -> delete(sender, args);
                    case "clear" -> clear(sender, args);
                    case "remove" -> remove(sender, args);
                    case "select" -> select(sender, args);
                    case "info" -> info(sender, args);
                    default -> add(sender, args, 0);
                }
            }
            return false;
        }
        return false;
    }

    private void defaultCase(CommandSender sender, String[] args) {
        if (args.length == 2) {
            if (isActivatorSelected(sender)) {
                try {
                    EventType eventType = EventType.valueOf(args[0].toUpperCase(Locale.ROOT));
                    printEventProcessorInfo(sender, selectedActivators.get(sender).getEventProcessor(eventType));
                } catch (IllegalArgumentException e) {
                    sender.sendMessage(INVALID_ARGUMENTS);
                }
            }
        } else {
            help(sender);
        }
    }

    private void add(CommandSender sender, String[] args, int index) {
        try {
            Activator activator = selectedActivators.get(sender);
            EventType eventType = EventType.valueOf(args[index].toUpperCase(Locale.ROOT));
            ActionType actionType = ActionType.valueOf(args[index + 1].toUpperCase(Locale.ROOT));
            boolean noArgumentsNeeded = actionType == ActionType.KILL || actionType == ActionType.HEALTH;

            if (activator != null) {

                if (args.length > index + 1 && noArgumentsNeeded) {
                    addAction(activator, eventType, actionType, sender, null);
                    return;
                }

                if (args.length > index + 2 && actionType == ActionType.FLY) {
                    if (args[index + 2].equalsIgnoreCase("on") || args[index + 2].equalsIgnoreCase("off")) {
                        addAction(activator, eventType, actionType, sender, args[index + 2]);
                    } else {
                        sender.sendMessage(INVALID_ARGUMENTS);
                    }
                    return;
                }

                if (args.length > index + 2 && !noArgumentsNeeded) {

                    StringBuilder value = new StringBuilder();
                    for (int i = index + 2; i < args.length; i++) {
                        value.append(args[i]).append(" ");
                    }
                    String finalValue = value.toString().trim();

                    if (actionType == ActionType.TP &&
                            (args.length == index + 5 || args.length == index + 6)) {
                        try {
                            Double.parseDouble(args[index + 2]);
                            Double.parseDouble(args[index + 3]);
                            Double.parseDouble(args[index + 4]);
                            World world = Bukkit.getWorld(args[index + 5]);
                            if (world == null) {
                                sender.sendMessage("Неверное название мира.");
                                return;
                            }
                        } catch (NumberFormatException e) {
                            sender.sendMessage("Неверно указаны координаты.");
                            return;
                        } catch (ArrayIndexOutOfBoundsException e) {
                            // Если не указан мир в команде, берем мир, в котором находится игрок
                            finalValue = finalValue + " " + ((Player) sender).getWorld().getName();
                        }
                    } else {
                        sender.sendMessage("Недостаточно аргументов.");
                        return;
                    }
                    addAction(activator, eventType, actionType, sender, finalValue);
                } else {
                    sender.sendMessage("Недостаточно аргументов.");
                }
            } else {
                sender.sendMessage(NEED_TO_SELECT);
            }
        } catch (
                IllegalArgumentException e) {
            sender.sendMessage(INVALID_ARGUMENTS);
        }
    }

    private void addAction(Activator activator, EventType eventType, ActionType actionType, CommandSender sender, String value) {
        if (activator.addEventProcessor(new EventProcessor(activator, eventType, plugin))) {
            activator.getEventProcessor(eventType).addAction(actionType, value);
            sender.sendMessage("Добавлено действие " + actionType + " к событию " + eventType + " активатора " + activator.getName());
        } else {
            sender.sendMessage(NOT_CAN);
        }
    }

    private void create(CommandSender sender, String[] args) {
        if (args.length == 2) {
            Player player = (Player) sender;
            Block block = player.getTargetBlock(null, 5);
            String activatorName = args[1];
            if (block.getType().name().endsWith("_BUTTON")) {
                createActivator(sender, activatorName, block.getLocation(), ActivatorType.BUTTON);
            } else if (block.getType().name().endsWith("_PLATE")) {
                createActivator(sender, activatorName, block.getLocation(), ActivatorType.PLATE);
            } else if (block.getType().name().endsWith("_DOOR")) {
                createActivator(sender, activatorName, Door.getTop(block), ActivatorType.DOOR);
            } else if (block.getType() == Material.LEVER) {
                createActivator(sender, activatorName, block.getLocation(), ActivatorType.LEVER);
            } else {
                player.sendMessage("Данный тип блока не может быть активатором.");
            }
        } else if (args.length == 4 && args[2].equals("region")) {
            String activatorName = args[1];
            String regionName = args[3];
            createActivator(sender, activatorName, ActivatorType.REGION, regionName);
        }
    }

    private void createActivator(CommandSender sender, String activatorName, Location location, ActivatorType activatorType) {
        String owner = sender.getName();
        if (Activator.getActivator(owner, activatorName) == null) {
            sender.sendMessage(CREATED + activatorName);
            switch (activatorType) {
                case BUTTON -> selectActivator(sender, new Button(plugin, owner, activatorType, activatorName, location));
                case CHEST -> selectActivator(sender, new Chest(plugin, owner, activatorType, activatorName, location));
                case DOOR -> selectActivator(sender, new Door(plugin, owner, activatorType, activatorName, location));
                case LEVER -> selectActivator(sender, new Lever(plugin, owner, activatorType, activatorName, location));
                case PLATE -> selectActivator(sender, new Plate(plugin, owner, activatorType, activatorName, location));
                default -> throw new IllegalStateException("Unexpected value: " + activatorType);
            }
        } else {
            sender.sendMessage(ALREADY_EXISTS);
        }
    }

    private void createActivator(CommandSender sender, String activatorName, ActivatorType activatorType, String regionName) {
        String owner = sender.getName();
        if (Activator.getActivator(owner, activatorName) == null) {
            sender.sendMessage(CREATED + activatorName);
            if (activatorType == ActivatorType.REGION) {
                selectActivator(sender, new Region(plugin, owner, activatorType, activatorName, regionName));
            }
        } else {
            sender.sendMessage(ALREADY_EXISTS);
        }
    }

    private void list(CommandSender sender) {
        int index = 0;
        try {
            if (!Activator.getActivators(sender.getName()).isEmpty()) {
                sender.sendMessage("Список активаторов: ");
                for (Activator activator : Activator.getActivators(sender.getName())) {
                    sender.sendMessage(" - " + (index++) + ". " + activator.getName());
                }
            } else {
                sender.sendMessage("Список активаторов пуст.");
            }
        } catch (NullPointerException e) {
            sender.sendMessage("Список активаторов пуст.");
        }
    }

    private void delete(CommandSender sender, String[] args) {
        if (args.length == 3) {
            Activator activator = selectedActivators.get(sender);
            if (activator != null) {
                try {
                    EventType eventType = EventType.valueOf(args[1].toUpperCase(Locale.ROOT));
                    int index = Integer.parseInt(args[2]);
                    EventProcessor eventProcessor = activator.getEventProcessor(eventType);
                    if (eventProcessor != null) {
                        eventProcessor.deleteAction(index);
                        sender.sendMessage("Действие под индексом " + index + " успешно удалено в активаторе " + activator.getName());
                    }
                } catch (IndexOutOfBoundsException e) {
                    sender.sendMessage("Индекс выходит за диапазон значений.");
                } catch (IllegalArgumentException e) {
                    sender.sendMessage(INVALID_ARGUMENTS);
                }
            } else {
                sender.sendMessage(NEED_TO_SELECT);
            }
        }
    }

    private void remove(CommandSender sender, String[] args) {
        if (args.length == 2) {
            if (Activator.removeActivator(plugin, sender.getName(), args[1])) {
                sender.sendMessage("Активатор успешно удален.");
            } else {
                sender.sendMessage(NOT_FOUND);
            }
        }
    }

    private void clear(CommandSender sender, String[] args) {
        if (args.length == 2) {
            Activator activator = selectedActivators.get(sender);
            if (activator != null) {
                try {
                    EventType eventType = EventType.valueOf(args[1].toUpperCase(Locale.ROOT));
                    if (activator.removeEventProcessor(eventType)) {
                        sender.sendMessage("Указанный тип события удален.");
                    } else {
                        sender.sendMessage("Указаный тип события не найден в активаторе.");
                    }
                } catch (IllegalArgumentException e) {
                    sender.sendMessage("Указан неверный тип события активатора.");
                }
            } else {
                sender.sendMessage(NEED_TO_SELECT);
            }
        }
    }

    private void select(CommandSender sender, String[] args) {
        if (args.length == 2) {
            Activator activator = Activator.getActivator(sender.getName(), args[1]);
            selectActivator(sender, activator);
        }
    }

    private void selectActivator(CommandSender sender, Activator activator) {
        if (activator != null) {
            selectedActivators.put(sender, activator);
            sender.sendMessage("Активатор " + activator.getName() + " выбран для редактирования.");
        } else {
            sender.sendMessage(NOT_FOUND);
        }
    }

    private void info(CommandSender sender, String[] args) {
        if (args.length == 2) {
            Activator activator = Activator.getActivator(sender.getName(), args[1]);
            if (activator != null) {
                printActivatorInfo(sender, activator);
            } else {
                sender.sendMessage(NOT_FOUND);
            }
        } else if (args.length == 1) {
            Activator activator = selectedActivators.get(sender);
            if (isActivatorSelected(sender)) {
                printActivatorInfo(sender, activator);
            } else {
                sender.sendMessage("Выберите активатор или укажите имя.");
            }
        }
    }

    private boolean isActivatorSelected(CommandSender sender) {
        Activator activator = selectedActivators.get(sender);
        if (activator != null) {
            return true;
        } else {
            sender.sendMessage("Выберите активатор или укажите имя.");
            return false;
        }
    }

    private void printActivatorInfo(CommandSender sender, Activator activator) {
        if (!activator.getEventProcessors().isEmpty()) {
            sender.sendMessage("Активатор: " + activator.getName());
            for (EventProcessor eventProcessor : activator.getEventProcessors()) {
                printEventProcessorInfo(sender, eventProcessor);
            }
        } else {
            sender.sendMessage("Активатор еще не настроен.");
        }
    }

    private void printEventProcessorInfo(CommandSender sender, EventProcessor eventProcessor) {
        sender.sendMessage(" " + eventProcessor.getEventType().name() + ":");
        int index = 0;
        for (EventProcessor.Action action : eventProcessor.getActionsQueue()) {
            String result = action.getValue() == null ? action.getActionType().name() : action.getActionType().name() + ": " + action.getValue();
            sender.sendMessage("  - " + (index++) + ". " + result);
        }
    }

    private void help(CommandSender sender) {
        List<String> messages = plugin.getConfig().getStringList("helpMessages");
        for (String message : messages) {
            sender.sendMessage(message);
        }
    }
}