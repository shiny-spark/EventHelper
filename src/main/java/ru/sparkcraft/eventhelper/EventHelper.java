package ru.sparkcraft.eventhelper;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import ru.sparkcraft.eventhelper.activators.*;
import ru.sparkcraft.eventhelper.activators.objects.Button;
import ru.sparkcraft.eventhelper.activators.objects.Lever;
import ru.sparkcraft.eventhelper.commands.Commands;
import ru.sparkcraft.eventhelper.listeners.EventListener;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import static org.bukkit.Bukkit.getPluginManager;

public final class EventHelper extends JavaPlugin {

    private FileConfiguration data;
    private File dataFile;

    @Override
    public void onEnable() {

        PluginCommand command = getCommand("e");
        if (command != null) {
            command.setExecutor(new Commands(this));
        }

        EventListener eventListener = new EventListener();
        getPluginManager().registerEvents(eventListener, this);

        createDataFile();
        loadData();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
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

    public FileConfiguration getData() {
        return this.data;
    }

    public void saveData() {
        try {
            getData().save(dataFile);
        } catch (IOException e) {
            Bukkit.getLogger().log(Level.SEVERE, String.format("Could not save config to %s", dataFile), e);
        }
    }

    private void loadData() {
        for (String nick : getData().getKeys(false)) {

            for (String activatorName : getData().getConfigurationSection(nick).getKeys(false)) {

                String[] locArgs = getData().getString(nick + "." + activatorName + ".location").split(",");
                World w = Bukkit.getWorld(locArgs[0]);
                double x = Double.parseDouble(locArgs[1]);
                double y = Double.parseDouble(locArgs[2]);
                double z = Double.parseDouble(locArgs[3]);
                Location location = new Location(w, x, y, z);

                switch (ActivatorType.valueOf(getData().getString(nick + "." + activatorName + ".type"))) {
                    case LEVER -> {
                        Activator activator = new Lever(this, nick, ActivatorType.LEVER, activatorName, location);

                        for (String eventType : getData().getConfigurationSection(nick + "." + activatorName + ".eventType").getKeys(false)) {
                            if (activator.addEventProcessor(new EventProcessor(activator, EventType.valueOf(eventType), this))) {
                                for (String action : getData().getStringList(nick + "." + activatorName + ".eventType." + eventType)) {
                                    String[] act = action.split(":");
                                    ActionType actionType = ActionType.valueOf(act[0]);
                                    activator.getEventProcessor(EventType.valueOf(eventType))
                                            .addAction(actionType, act.length > 1 ? act[1] : null);
                                }
                            }
                        }
                    }
                    case CHEST -> {
                    }
                    case DOOR -> {
                    }
                    case BUTTON -> {
                        new Button(this, nick, ActivatorType.BUTTON, activatorName, location);
                    }
                    case PLATE -> {
                    }
                    case REGION -> {
                    }
                }
            }
        }
    }
}
