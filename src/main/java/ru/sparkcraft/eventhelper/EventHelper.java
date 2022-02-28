package ru.sparkcraft.eventhelper;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import ru.sparkcraft.eventhelper.commands.Commands;
import ru.sparkcraft.eventhelper.listeners.EventListener;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import static org.bukkit.Bukkit.getPluginManager;

public final class EventHelper extends JavaPlugin {

    private static EventHelper plugin;
    private FileConfiguration data;
    private File dataFile;


    public EventHelper() {
        super();
        plugin = this;
    }

    public static EventHelper getInstance() {
        return plugin;
    }

    @Override
    public void onEnable() {

        PluginCommand command = getCommand("e");
        if (command != null) {
            command.setExecutor(new Commands(this));
        }

        EventListener eventListener = new EventListener();
        getPluginManager().registerEvents(eventListener, this);

        createDataFile();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public FileConfiguration getData() {
        return this.data;
    }

    private void createDataFile() {
        dataFile = new File(getDataFolder(), "activators.yml");

        if (!dataFile.exists()) {
            dataFile.getParentFile().mkdirs();
            saveResource("activators.yml", false);
        }

        data = new YamlConfiguration();
        try {
            data.load(dataFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public void saveData() {
        try {
            getData().save(dataFile);
        } catch (IOException e) {
            Bukkit.getLogger().log(Level.SEVERE, String.format("Could not save config to %s", dataFile), e);
        }
    }
}
