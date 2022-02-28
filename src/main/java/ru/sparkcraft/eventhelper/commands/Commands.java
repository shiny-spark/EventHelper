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
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import ru.sparkcraft.eventhelper.activators.ActionType;
import ru.sparkcraft.eventhelper.activators.Activator;
import ru.sparkcraft.eventhelper.activators.EventProcessor;
import ru.sparkcraft.eventhelper.activators.EventType;
import ru.sparkcraft.eventhelper.activators.objects.Button;
import ru.sparkcraft.eventhelper.activators.objects.Lever;
import ru.sparkcraft.eventhelper.activators.objects.Plate;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Commands implements CommandExecutor {

    private final JavaPlugin plugin;
    private final Map<CommandSender, Activator> selectedActivators = new HashMap<>();

    private static final String NOT_FOUND = "Активатор с таким именем не найден.";
    private static final String NOT_CAN = "Данный тип действия недоступен для этого типа активатора.";
    private static final String CREATED = "Активатор создан под именем: ";
    private static final String ALREADY_EXISTS = "Активатор с таким именем уже существует.";

    public Commands(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            help(sender);
        } else if (sender.hasPermission("e.commands")) {
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

    private void add(CommandSender sender, String[] args, int index) {
        if (args.length > index + 2) {
            Activator activator = selectedActivators.get(sender);
            if (activator != null) {
                try {
                    EventType eventType = EventType.valueOf(args[index].toUpperCase(Locale.ROOT));
                    ActionType actionType = ActionType.valueOf(args[index + 1].toUpperCase(Locale.ROOT));
                    String finalValue = null;

                    if (!(actionType == ActionType.KILL || actionType == ActionType.HEALTH || actionType == ActionType.FLY)) {

                        StringBuilder value = new StringBuilder();
                        for (int i = index + 2; i < args.length; i++) {
                            value.append(args[i]).append(" ");
                        }
                        finalValue = value.toString().trim();
                    }

                    if (actionType == ActionType.TP) {
                        if (args.length == index + 5 || args.length == index + 6) {
                            try {
                                Integer.parseInt(args[index + 2]);
                                Integer.parseInt(args[index + 3]);
                                Integer.parseInt(args[index + 4]);
                                World world = Bukkit.getWorld(args[index + 5]);
                                if (world == null) {
                                    sender.sendMessage("Неверное название мира.");
                                    return;
                                }
                            } catch (NumberFormatException e) {
                                sender.sendMessage("Неверно указаны координаты.");
                                return;
                            } catch (ArrayIndexOutOfBoundsException e) {
                                finalValue = finalValue + " " + ((Player) sender).getWorld().getName();
                            }
                        } else {
                            sender.sendMessage("Недостаточно аргументов.");
                            return;
                        }
                    }

                    if (activator.addEventProcessor(new EventProcessor(activator, eventType))) {
                        activator.getEventProcessor(eventType).addAction(actionType, finalValue);
                        sender.sendMessage("Добавлено действие " + actionType + " к событию " + eventType + " активатора " + activator.getName());
                    } else {
                        sender.sendMessage(NOT_CAN);
                    }

                } catch (IllegalArgumentException e) {
                    sender.sendMessage("Неверные аргументы команды.");
                }
            } else {
                sender.sendMessage("Сначала выберите активатор.");
            }
        } else {
            sender.sendMessage("Недостаточно аргументов.");
        }
    }

    private void create(CommandSender sender, String[] args) {
        if (args.length == 2) {
            Player player = (Player) sender;
            Block block = player.getTargetBlock(5);
            String activatorName = args[1];

            if (block != null) {
                if (block.getType().name().endsWith("_BUTTON")) {
                    createActivator(sender, activatorName, block.getLocation(), Button.class);
                } else if (block.getType().name().endsWith("_PLATE")) {
                    createActivator(sender, activatorName, block.getLocation(), Plate.class);
                } else if (block.getType() == Material.LEVER) {
                    createActivator(sender, activatorName, block.getLocation(), Lever.class);
                } else {
                    player.sendMessage("Данный тип блока не может быть активатором.");
                }
            } else {
                sender.sendMessage("Блок, на который вы смотрите, слишком далеко.");
            }
        }
    }

    private void createActivator(CommandSender sender, String activatorName, Location location, Class<?> activatorClass) {
        String playerName = sender.getName();
        if (Activator.getActivator(playerName, activatorName) == null) {
            sender.sendMessage(CREATED + activatorName);
            if (activatorClass == Button.class) {
                selectActivator(sender, new Button(playerName, activatorName, location));
            } else if (activatorClass == Plate.class) {
                selectActivator(sender, new Plate(playerName, activatorName, location));
            } else if (activatorClass == Lever.class) {
                selectActivator(sender, new Lever(playerName, activatorName, location));
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
                        eventProcessor.getActionsQueue().remove(index);
                        sender.sendMessage("Действие под индексом " + index + " успешно удалено в активаторе " + activator.getName());
                    }
                } catch (IndexOutOfBoundsException e) {
                    sender.sendMessage("Индекс выходит за диапазон значений.");
                } catch (IllegalArgumentException e) {
                    sender.sendMessage("Указаны неверные аргументы команды.");
                }
            } else {
                sender.sendMessage("Сначала выберите активатор.");
            }
        }
    }

    private void remove(CommandSender sender, String[] args) {
        if (args.length == 2) {
            if (Activator.removeActivator(sender.getName(), args[0])) {
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
                sender.sendMessage("Сначала выберите активатор.");
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
            if (activator != null) {
                printActivatorInfo(sender, activator);
            } else {
                sender.sendMessage("Выберите активатор или укажите имя.");
            }
        }
    }

    private void printActivatorInfo(CommandSender sender, Activator activator) {
        if (!activator.getEventProcessors().isEmpty()) {
            sender.sendMessage("Активатор: " + activator.getName());
            for (EventProcessor eventProcessor : activator.getEventProcessors()) {
                sender.sendMessage(" " + eventProcessor.getEventType().name() + ":");
                int index = 0;
                for (EventProcessor.Action action : eventProcessor.getActionsQueue()) {
                    sender.sendMessage("  - " + (index++) + ". " + action.getActionType().name() + " " + action.getValue());
                }
            }
        } else {
            sender.sendMessage("Активатор еще не настроен.");
        }
    }

    private void help(CommandSender sender) {
        List<String> messages = plugin.getConfig().getStringList("helpMessages");
        for (String message : messages) {
            sender.sendMessage(message);
        }
    }
}