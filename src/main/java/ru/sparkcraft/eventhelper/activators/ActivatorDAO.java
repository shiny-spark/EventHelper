package ru.sparkcraft.eventhelper.activators;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import ru.sparkcraft.eventhelper.EventHelper;
import ru.sparkcraft.eventhelper.activators.objects.*;

import java.sql.*;
import java.util.logging.Level;

public class ActivatorDAO {

    private static ActivatorDAO instance;
    private final HikariDataSource dataSource;
    private final EventHelper plugin;

    private ActivatorDAO(EventHelper plugin) {
        this.plugin = plugin;

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(buildLink());
        config.setUsername(getParam("login"));
        config.setPassword(getParam("password"));
        config.setMinimumIdle(1);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        this.dataSource = new HikariDataSource(config);

        createDatabases();
    }

    public static ActivatorDAO getInstance() {
        if (instance == null) {
            instance = new ActivatorDAO(EventHelper.getInstance());
        }
        return instance;
    }

    public void closeConnection() {
        dataSource.close();
    }

    private @NotNull String buildLink() {
        return "jdbc:mysql://" + getParam("host") + ":" + getParam("port") + "/" + getParam("database");
    }

    private String getParam(String param) {
        return getDatabaseSection().getString(param);
    }

    private ConfigurationSection getDatabaseSection() {
        return plugin.getConfig().getConfigurationSection("databaseSettings");
    }

    void createDatabases() {
        try {
            String sql = "CREATE TABLE IF NOT EXISTS e_activator ( " +
                    "id INT(11) NOT NULL AUTO_INCREMENT PRIMARY KEY, " +
                    "activator_type ENUM('BUTTON','CHEST','DOOR','FUNCTION','LEVER','PLATE','REGION') NOT NULL, " +
                    "location VARCHAR(255) NULL, " +
                    "name VARCHAR(255) NOT NULL, " +
                    "owner VARCHAR(255) NOT NULL);";
            executeSql(sql);

            sql = "CREATE TABLE IF NOT EXISTS e_event_processor ( " +
                    "id INT(11) NOT NULL AUTO_INCREMENT PRIMARY KEY, " +
                    "event_type ENUM('CLOSE','ENTER','LEAVE','OFF','ON','OPEN','PUT','RUN','TAKE','USE') NOT NULL, " +
                    "activator_id INT(11) NOT NULL, " +
                    "FOREIGN KEY (activator_id) REFERENCES e_activator(id) ON DELETE CASCADE ON UPDATE CASCADE);";
            executeSql(sql);

            sql = "CREATE TABLE IF NOT EXISTS e_action ( " +
                    "id INT(11) NOT NULL AUTO_INCREMENT PRIMARY KEY, " +
                    "action_type ENUM('ANNOUNCE','COMMAND','CONDITION','DELAY','EFFECT','ELSE','ELSEIF','ENDIF','FLY','GIVE','HEALTH','IF','KILL','MESSAGE','META','TAKE','TP') NOT NULL, " +
                    "order_field INT(11) NOT NULL, " +
                    "value VARCHAR(1000) NULL, " +
                    "event_processor_id INT(11) NOT NULL, " +
                    "FOREIGN KEY (event_processor_id) REFERENCES e_event_processor(id) ON DELETE RESTRICT ON UPDATE RESTRICT);";
            executeSql(sql);

        } catch (SQLException e) {
            Bukkit.getLogger().severe(e.getMessage());
        }
    }

    private void executeSql(String sql) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.execute();
        }
    }

    public boolean saveActivator(@NotNull Activator activator) {
        String sql = "INSERT INTO `e_activator` (`name`, `owner`, `activator_type`, `location`) VALUES (?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE `name` = ?, `owner` = ?, `activator_type` = ?, `location` = ?;";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, activator.getName());
            preparedStatement.setString(2, activator.getOwner());
            preparedStatement.setString(3, activator.getType().name());
            preparedStatement.setString(4, activator instanceof Region region ? region.getRegionName() : locationToString(activator.getLocation()));

            preparedStatement.setString(5, activator.getName());
            preparedStatement.setString(6, activator.getOwner());
            preparedStatement.setString(7, activator.getType().name());
            preparedStatement.setString(8, activator instanceof Region region ? region.getRegionName() : locationToString(activator.getLocation()));
            preparedStatement.executeUpdate();

            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                activator.setId(generatedKeys.getInt(1));
            }
            return true;
        } catch (SQLException e) {
            Bukkit.getLogger().severe(e.getMessage());
            return false;
        }
    }

    public void saveEventProcessor(@NotNull EventProcessor eventProcessor) {
        String sql = "INSERT INTO `e_event_processor` (`activator_id`, `event_type`) VALUES (?, ?) " +
                "ON DUPLICATE KEY UPDATE `activator_id` = ?, `event_type` = ?;";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setInt(1, eventProcessor.getActivator().getId());
            preparedStatement.setString(2, eventProcessor.getEventType().name());

            preparedStatement.setInt(3, eventProcessor.getActivator().getId());
            preparedStatement.setString(4, eventProcessor.getEventType().name());
            preparedStatement.executeUpdate();

            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                eventProcessor.setId(generatedKeys.getInt(1));
            }
        } catch (SQLException e) {
            Bukkit.getLogger().severe(e.getMessage());
        }
    }

    public void saveAction(@NotNull Action action) {
        String sql = "INSERT INTO `e_action` (`event_processor_id`, `action_type`, `value`, `order_field`) VALUES (?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE `event_processor_id` = ?, `action_type` = ?, `value` = ?, `order_field` = ?;";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setInt(1, action.getEventProcessorId());
            preparedStatement.setString(2, action.getActionType().name());
            preparedStatement.setString(3, action.getValue());
            preparedStatement.setInt(4, action.getOrder());

            preparedStatement.setInt(5, action.getEventProcessorId());
            preparedStatement.setString(6, action.getActionType().name());
            preparedStatement.setString(7, action.getValue());
            preparedStatement.setInt(8, action.getOrder());
            preparedStatement.executeUpdate();

            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                action.setId(generatedKeys.getInt(1));
            }
        } catch (SQLException e) {
            Bukkit.getLogger().severe(e.getMessage());
        }
    }

    public void updateLocation(@NotNull Activator activator, int activatorId) {
        String sql = "UPDATE `e_activator` SET `location` = ? WHERE `id` = ?;";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            String location = activator.getLocation().getWorld().getName() + "," + activator.getLocation().getX() + "," + activator.getLocation().getY() + "," + activator.getLocation().getZ();
            preparedStatement.setString(1, location);
            preparedStatement.setInt(2, activatorId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            Bukkit.getLogger().severe(e.getMessage());
        }
    }

    public void updateRegion(String regionName, int activatorId) {
        String sql = "UPDATE `e_activator` SET `region_name` = ? WHERE `id` = ?;";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, regionName);
            preparedStatement.setInt(2, activatorId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            Bukkit.getLogger().severe(e.getMessage());
        }
    }

    public void loadData() {
        try (Connection connection = dataSource.getConnection()) {

            String sql = "SELECT * FROM `e_activator`;";
            try (PreparedStatement statement = connection.prepareStatement(sql);
                 ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    int activatorId = resultSet.getInt("id");
                    ActivatorType activatorType = ActivatorType.valueOf(resultSet.getString("activator_type"));
                    String owner = resultSet.getString("owner");
                    String name = resultSet.getString("name");
                    String locationString = resultSet.getString("location");

                    Activator activator = switch (activatorType) {
                        case BUTTON -> new Button(owner, name, parseLocation(locationString));
                        case CHEST -> new Chest(owner, name, parseLocation(locationString));
                        case DOOR -> new Door(owner, name, parseLocation(locationString));
                        case LEVER -> new Lever(owner, name, parseLocation(locationString));
                        case PLATE -> new Plate(owner, name, parseLocation(locationString));
                        case REGION -> new Region(owner, name, locationString);
                        case FUNCTION -> new Function(owner, name);
                    };

                    String sqlEventProcessors = "SELECT * FROM `e_event_processor` WHERE `activator_id` = ?;";
                    try (PreparedStatement statementEventProcessors = connection.prepareStatement(sqlEventProcessors)) {
                        statementEventProcessors.setInt(1, activatorId);
                        try (ResultSet resultSetEventProcessors = statementEventProcessors.executeQuery()) {
                            while (resultSetEventProcessors.next()) {
                                EventType eventType = EventType.valueOf(resultSetEventProcessors.getString("event_type"));
                                int eventProcessorId = resultSetEventProcessors.getInt("id");
                                EventProcessor eventProcessor = new EventProcessor(activator, eventType);

                                String sqlActions = "SELECT * FROM `e_action` WHERE `event_processor_id` = ? ORDER BY `order_field`;";
                                try (PreparedStatement statementActions = connection.prepareStatement(sqlActions)) {
                                    statementActions.setInt(1, eventProcessorId);
                                    try (ResultSet resultSetActions = statementActions.executeQuery()) {
                                        while (resultSetActions.next()) {
                                            ActionType actionType = ActionType.valueOf(resultSetActions.getString("action_type"));
                                            String value = resultSetActions.getString("value");
                                            int order = resultSetActions.getInt("order_field");
                                            Action action = new Action(eventProcessorId, actionType, value, order);
                                            eventProcessor.addAction(action);
                                        }
                                    }
                                }
                                activator.addEventProcessor(eventProcessor);
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Error loading activators from database", e);
        }
    }

    private @NotNull Location parseLocation(@NotNull String locationString) {
        String[] locArgs = locationString.split(",");
        World world = Bukkit.getWorld(locArgs[0]);
        double x = Double.parseDouble(locArgs[1]);
        double y = Double.parseDouble(locArgs[2]);
        double z = Double.parseDouble(locArgs[3]);
        return new Location(world, x, y, z);
    }

    private String locationToString(Location location) {
        return location != null ? location.getWorld().getName() + "," + location.getX() + "," + location.getY() + "," + location.getZ() : null;
    }
}
