package ru.sparkcraft.eventhelper;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import ru.sparkcraft.eventhelper.activators.*;
import ru.sparkcraft.eventhelper.commands.Commands;
import ru.sparkcraft.eventhelper.listeners.EventListener;

import static org.bukkit.Bukkit.getPluginManager;

public class EventHelper extends JavaPlugin {

    private static EventHelper plugin;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        plugin = this;

        PluginCommand command = getCommand("e");
        if (command != null) {
            command.setExecutor(new Commands(this));
        }

        EventListener eventListener = new EventListener();
        getPluginManager().registerEvents(eventListener, this);

        ActionProcessor.init(this);

        ActivatorDAO.getInstance().loadDate();
    }

    @Override
    public void onDisable() {
        ActivatorDAO.getInstance().closeConnection();
    }

    public static EventHelper getInstance() {
        return plugin;
    }
}
