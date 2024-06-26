package ru.sparkcraft.eventhelper.commands;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.sparkcraft.eventhelper.EventHelper;
import ru.sparkcraft.eventhelper.activators.*;
import ru.sparkcraft.eventhelper.activators.objects.*;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class Commands implements TabExecutor {

    private final String[] COMMANDS = {"create", "add", "list", "delete", "clear", "remove", "select", "info", "execute", "reload"};
    private final String[] META_SUB_COMMANDS = {"set", "unset"};
    private final EventHelper plugin;
    private final Map<CommandSender, Activator> selectedActivators = new HashMap<>();

    private static final String NOT_FOUND = "Активатор с таким именем не найден.";
    private static final String NOT_CAN = "Данный тип действия недоступен для этого типа активатора.";
    private static final String ACTIVATOR_CREATED = "Активатор создан под именем: ";
    private static final String FUNCTION_CREATED = "Функция создана под именем: ";
    private static final String ACTIVATOR_ALREADY_EXISTS = "Активатор с таким именем уже существует.";
    private static final String NEED_TO_SELECT = "Сначала выберите активатор.";
    private static final String NEED_TO_SELECT_OR_NAME = "Выберите активатор или укажите имя.";
    private static final String INVALID_ARGUMENTS = "Неверные аргументы команды.";
    private static final String NOT_ENOUGH_ARGUMENTS = "Недостаточно аргументов.";

    public Commands(EventHelper plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender.hasPermission("e.commands")) {
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
                    case "execute" -> execute(sender, args);
                    case "reload" -> reload(sender);
                    default -> add(sender, args, 0);
                }
            }
            return true;
        }
        sender.sendMessage("У вас нет прав.");
        return false;
    }

    private void add(CommandSender sender, String @NotNull [] args, int index) {
        try {
            Activator activator = selectedActivators.get(sender);
            EventType eventType = EventType.valueOf(args[index].toUpperCase(Locale.ROOT));
            ActionType actionType = ActionType.valueOf(args[index + 1].toUpperCase(Locale.ROOT));
            boolean noArgumentsNeeded = actionType == ActionType.KILL || actionType == ActionType.HEALTH ||
                    actionType == ActionType.ELSE || actionType == ActionType.ENDIF;

            if (activator != null) {

                if (args.length > index + 1 && noArgumentsNeeded) {
                    addAction(activator, eventType, actionType, sender, null);
                    return;
                }

                if (args.length > index + 2 && actionType == ActionType.FLY) {
                    String value = args[index + 2];
                    if (value.equalsIgnoreCase("on") || value.equalsIgnoreCase("off")) {
                        addAction(activator, eventType, actionType, sender, value);
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

                    if (actionType == ActionType.TP) {
                        if (args.length == index + 5 || args.length == index + 6) {
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
                            sender.sendMessage(NOT_ENOUGH_ARGUMENTS);
                            return;
                        }
                    }

                    if (actionType == ActionType.META) {
                        String metaAction = args[index + 2];
                        switch (metaAction) {
                            case "set" -> {
                                if (args.length < index + 5) {
                                    sender.sendMessage(NOT_ENOUGH_ARGUMENTS);
                                    return;
                                }
                            }
                            case "unset" -> {
                                if (args.length < index + 4) {
                                    sender.sendMessage(NOT_ENOUGH_ARGUMENTS);
                                    return;
                                }
                            }
                            default -> {
                                sender.sendMessage("Неизвестное действие для META: " + metaAction);
                                return;
                            }
                        }
                    }

                    addAction(activator, eventType, actionType, sender, finalValue);
                } else {
                    sender.sendMessage(NOT_ENOUGH_ARGUMENTS);
                }
            } else {
                sender.sendMessage(NEED_TO_SELECT);
            }
        } catch (
                IllegalArgumentException e) {
            sender.sendMessage(INVALID_ARGUMENTS);
        }
    }

    private void addAction(@NotNull Activator activator, EventType eventType, ActionType actionType, CommandSender sender, String value) {
        if (activator.addEventProcessor(eventType)) {
            activator.getEventProcessor(eventType).addAction(actionType, value);
            String message = "Добавлено действие " + actionType +
                    (activator instanceof Function function ? " к функции " + function.getName() : " к событию " + eventType + " активатора " + activator.getName());
            sender.sendMessage(message);
        } else {
            sender.sendMessage(NOT_CAN);
        }
    }

    private void create(CommandSender sender, String @NotNull [] args) {
        if (args.length == 2) {
            Player player = (Player) sender;
            Block block = player.getTargetBlock(null, 5);
            String activatorName = args[1];
            if (Tag.BUTTONS.isTagged(block.getType())) {
                createActivator(sender, activatorName, block.getLocation(), ActivatorType.BUTTON);
            } else if (Tag.PRESSURE_PLATES.isTagged(block.getType())) {
                createActivator(sender, activatorName, block.getLocation(), ActivatorType.PLATE);
            } else if (Tag.DOORS.isTagged(block.getType())) {
                createActivator(sender, activatorName, Door.getTop(block), ActivatorType.DOOR);
            } else if (block.getType() == Material.LEVER) {
                createActivator(sender, activatorName, block.getLocation(), ActivatorType.LEVER);
            } else {
                player.sendMessage("Данный тип блока не может быть активатором.");
            }

        } else if (args.length == 3 && args[2].equalsIgnoreCase("FUNCTION")) {
            String activatorName = args[1];
            createFunction(sender, activatorName);

        } else if (args.length == 4 && args[2].equalsIgnoreCase("REGION")) {
            String activatorName = args[1];
            String regionName = args[3];
            createRegionActivator(sender, activatorName, regionName);

        } else {
            sender.sendMessage("Неверные атрибуты команды");
        }
    }

    private void createActivator(@NotNull CommandSender sender, String activatorName, Location location, ActivatorType activatorType) {
        String owner = sender.getName();
        if (checkAndCreateActivator(sender, owner, activatorName, () -> createActivatorByType(owner, activatorName, location, activatorType))) {
            sender.sendMessage(ACTIVATOR_CREATED + activatorName);
        }
    }

    private void createRegionActivator(@NotNull CommandSender sender, String activatorName, String regionName) {
        String owner = sender.getName();
        if (checkAndCreateActivator(sender, owner, activatorName, () -> new Region(owner, activatorName, regionName))) {
            sender.sendMessage(ACTIVATOR_CREATED + activatorName);
        }
    }

    private void createFunction(@NotNull CommandSender sender, String functionName) {
        String owner = sender.getName();
        if (checkAndCreateActivator(sender, owner, functionName, () -> new Function(owner, functionName))) {
            sender.sendMessage(FUNCTION_CREATED + functionName);
        }
    }

    private boolean checkAndCreateActivator(CommandSender sender, String owner, String name, Supplier<Activator> activatorSupplier) {
        if (Activator.getActivator(owner, name) == null) {
            Activator activator = activatorSupplier.get();
            return ActivatorDAO.getInstance().saveActivator(activator);
        } else {
            sender.sendMessage(ACTIVATOR_ALREADY_EXISTS);
            return false;
        }
    }

    private @NotNull Activator createActivatorByType(String owner, String activatorName, Location location, @NotNull ActivatorType activatorType) {
        return switch (activatorType) {
            case BUTTON -> new Button(owner, activatorName, location);
            case CHEST -> new Chest(owner, activatorName, location);
            case DOOR -> new Door(owner, activatorName, location);
            case LEVER -> new Lever(owner, activatorName, location);
            case PLATE -> new Plate(owner, activatorName, location);
            default -> throw new IllegalStateException("Unexpected value: " + activatorType);
        };
    }

    private void list(CommandSender sender) {
        int index = 0;
        try {
            if (!Activator.getActivators(sender.getName()).isEmpty()) {
                for (Activator activator : Activator.getActivators(sender.getName())) {
                    sender.sendMessage(" - " + (index++) + ". " + activator.getName() + " (" + activator.getType() + ")");
                }
            } else {
                sender.sendMessage("Список пуст.");
            }
        } catch (NullPointerException e) {
            sender.sendMessage("Список пуст.");
        }
    }

    private void delete(CommandSender sender, String @NotNull [] args) {
        if (args.length == 3) {
            Activator activator = selectedActivators.get(sender);
            if (activator != null) {
                try {
                    EventType eventType = EventType.valueOf(args[1].toUpperCase(Locale.ROOT));
                    int index = Integer.parseInt(args[2]);
                    EventProcessor eventProcessor = activator.getEventProcessor(eventType);
                    if (eventProcessor != null) {
                        eventProcessor.deleteAction(index);
                        sender.sendMessage("Действие под индексом " + index + " успешно удалено в " + activator.getName());
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

    private void remove(CommandSender sender, String @NotNull [] args) {
        if (args.length == 2) {
            if (Activator.removeActivator(sender.getName(), args[1])) {
                sender.sendMessage("Активатор успешно удален.");
            } else {
                sender.sendMessage(NOT_FOUND);
            }
        }
    }

    private void clear(CommandSender sender, String @NotNull [] args) {
        Activator activator = selectedActivators.get(sender);
        if (activator != null) {
            if (args.length == 1 && activator instanceof Function) {
                if (activator.removeEventProcessor(EventType.RUN)) {
                    sender.sendMessage("Функция очищена.");
                } else {
                    sender.sendMessage("Не удалось очистить функцию.");
                }
            }
            if (args.length == 2) {
                try {
                    EventType eventType = EventType.valueOf(args[1].toUpperCase(Locale.ROOT));
                    if (activator.removeEventProcessor(eventType)) {
                        sender.sendMessage("Указанный тип события удален.");
                    } else {
                        sender.sendMessage("Указаный тип события не найден.");
                    }
                } catch (IllegalArgumentException e) {
                    sender.sendMessage("Указан неверный тип события.");
                }
            }
        } else {
            sender.sendMessage(NEED_TO_SELECT);
        }
    }

    private void select(CommandSender sender, String @NotNull [] args) {
        if (args.length == 2) {
            Activator activator = Activator.getActivator(sender.getName(), args[1]);
            selectActivator(sender, activator);
        }
    }

    private void selectActivator(CommandSender sender, Activator activator) {
        if (activator != null) {
            selectedActivators.put(sender, activator);
            if (activator instanceof Function function) {
                sender.sendMessage("Функция " + function.getName() + " выбрана для редактирования.");
            } else {
                sender.sendMessage("Активатор " + activator.getName() + " выбран для редактирования.");
            }
        } else {
            sender.sendMessage(NOT_FOUND);
        }
    }

    private void info(CommandSender sender, String @NotNull [] args) {
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
                sender.sendMessage(NEED_TO_SELECT_OR_NAME);
            }
        }
    }

    private void execute(CommandSender sender, String @NotNull [] args) {
        if (args.length == 3) {
            Activator activator = Activator.getFunctionActivator(args[1]);
            if (activator != null) {
                EventProcessor eventProcessor = activator.getEventProcessor(EventType.RUN);
                if (eventProcessor != null) {
                    Player player = Bukkit.getPlayer(args[2]);
                    if (player != null) {
                        eventProcessor.run(player);
                    }
                }
            } else {
                sender.sendMessage(NOT_FOUND);
            }
        }
    }

    private void reload(@NotNull CommandSender sender) {
        Activator.reloadActivators();
        sender.sendMessage("Данные перезагружены!");
    }

    private boolean isActivatorSelected(CommandSender sender) {
        Activator activator = selectedActivators.get(sender);
        return activator != null;
    }

    private void printActivatorInfo(CommandSender sender, @NotNull Activator activator) {
        if (!activator.getEventProcessors().isEmpty()) {
            sender.sendMessage("Активатор: " + activator.getName());
            for (EventProcessor eventProcessor : activator.getEventProcessors().values()) {
                printEventProcessorInfo(sender, eventProcessor);
            }
        } else {
            sender.sendMessage("Активатор еще не настроен.");
        }
    }

    private void printEventProcessorInfo(@NotNull CommandSender sender, @NotNull EventProcessor eventProcessor) {
        sender.sendMessage(" " + eventProcessor.getEventType().name() + ":");
        int index = 0;
        for (Action action : eventProcessor.getActionsQueue()) {
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

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (sender.hasPermission("e.commands")) {
            final List<String> completions = new ArrayList<>();

            switch (args.length) {
                case 1 -> {
                    if (isActivatorSelected(sender)) {
                        StringUtil.copyPartialMatches(args[0],
                                Stream.of(EventType.values())
                                        .map(EventType::name)
                                        .toList(),
                                completions);
                    }
                    StringUtil.copyPartialMatches(args[0], Arrays.asList(COMMANDS), completions);
                }
                case 2 -> {
                    switch (args[0]) {
                        case "create":
                            return Collections.singletonList("<имя>");

                        case "clear", "add", "delete":
                            StringUtil.copyPartialMatches(args[1],
                                    Arrays.stream(EventType.values())
                                            .map(EventType::name)
                                            .toList(),
                                    completions);
                            break;

                        case "remove", "select", "info":
                            StringUtil.copyPartialMatches(args[1],
                                    Activator.getActivators(sender.getName())
                                            .stream()
                                            .map(Activator::getName)
                                            .toList(),
                                    completions);
                            break;

                        default:
                            break;
                    }
                    if (Arrays.stream(EventType.values())
                            .map(EventType::name)
                            .toList()
                            .contains(args[0].toUpperCase()) && isActivatorSelected(sender)) {
                        StringUtil.copyPartialMatches(args[1],
                                Stream.of(ActionType.values())
                                        .map(ActionType::name)
                                        .toList(),
                                completions);
                    }
                }
                case 3 -> {
                    switch (args[0]) {
                        case "create":
                            StringUtil.copyPartialMatches(args[2], Arrays.asList("REGION", "FUNCTION"), completions);
                            break;

                        case "add":
                            StringUtil.copyPartialMatches(args[2],
                                    Stream.of(ActionType.values())
                                            .map(ActionType::name)
                                            .toList(),
                                    completions);
                            break;

                        case "delete":
                            return Collections.singletonList("<индекс действия>");

                        default:
                            break;
                    }
                    if (args[1].equalsIgnoreCase("META")) {
                        StringUtil.copyPartialMatches(args[2], Arrays.asList(META_SUB_COMMANDS), completions);
                    }
                    if (args[1].equalsIgnoreCase("CONDITION")) {
                        return Arrays.asList("условие ? команда1 : команда2", "условие ? команда");
                    }
                    if (args[1].equalsIgnoreCase("IF")) {
                        return Collections.singletonList("условие");
                    }
                }
                case 4 -> {
                    if (args[1].equalsIgnoreCase("META")) {
                        if (args[2].equalsIgnoreCase("set")) {
                            return Collections.singletonList("<ключ> <значение>");
                        } else if (args[2].equalsIgnoreCase("unset")) {
                            return Collections.singletonList("<ключ>");
                        }
                    }
                    if (args[2].equalsIgnoreCase("REGION")) {
                        return Collections.singletonList("<название региона>");
                    }
                }
            }
            return completions;
        }
        return null;
    }
}