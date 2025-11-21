package mc506lw.cjm.database;

import mc506lw.cjm.CustomJoinMessage;
import mc506lw.cjm.utils.ConfigManager;
import mc506lw.cjm.utils.SchedulerUtils;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.CompletableFuture;

public class DatabaseManager {
    private final CustomJoinMessage plugin;
    private Connection connection;
    private final String databaseType;

    public DatabaseManager(CustomJoinMessage plugin) {
        this.plugin = plugin;
        this.databaseType = plugin.getConfigManager().getDatabaseType();
    }

    public void initialize() {
        try {
            if (databaseType.equalsIgnoreCase("sqlite")) {
                initializeSQLite();
            } else if (databaseType.equalsIgnoreCase("mysql")) {
                initializeMySQL();
            } else {
                plugin.getLogger().warning("Unknown database type: " + databaseType + ". Using SQLite instead.");
                initializeSQLite();
            }
            createTable();
            plugin.getLogger().info("Database connection established successfully.");
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to initialize database: " + e.getMessage());
            plugin.getServer().getPluginManager().disablePlugin(plugin);
        }
    }

    private void initializeSQLite() throws SQLException {
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        
        String dbFile = plugin.getConfigManager().getSQLiteFile();
        connection = DriverManager.getConnection("jdbc:sqlite:" + new File(dataFolder, dbFile).getAbsolutePath());
    }

    private void initializeMySQL() throws SQLException {
        ConfigManager config = plugin.getConfigManager();
        String url = "jdbc:mysql://" + config.getMySQLHost() + ":" + config.getMySQLPort() + "/" + config.getMySQLDatabase();
        connection = DriverManager.getConnection(url, config.getMySQLUsername(), config.getMySQLPassword());
    }

    private void createTable() throws SQLException {
        String tableName = databaseType.equalsIgnoreCase("mysql") ? 
            plugin.getConfigManager().getMySQLTable() : "joinmessages";
            
        String query = "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                "uuid VARCHAR(36) PRIMARY KEY," +
                "username VARCHAR(16)," +
                "message TEXT," +
                "prefix TEXT," +
                "suffix TEXT" +
                ")";
        
        try (Statement statement = connection.createStatement()) {
            statement.execute(query);
        }
    }

    public void setJoinMessage(String uuid, String username, String message) {
        SchedulerUtils.runTaskAsynchronously(plugin, () -> {
            String tableName = databaseType.equalsIgnoreCase("mysql") ? 
                plugin.getConfigManager().getMySQLTable() : "joinmessages";
                
            String query = "INSERT OR REPLACE INTO " + tableName + " (uuid, username, message) VALUES (?, ?, ?)";
            
            if (databaseType.equalsIgnoreCase("mysql")) {
                query = "INSERT INTO " + tableName + " (uuid, username, message) VALUES (?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE username = VALUES(username), message = VALUES(message)";
            }
            
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, uuid);
                statement.setString(2, username);
                statement.setString(3, message);
                statement.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to set join message: " + e.getMessage());
            }
        });
    }

    public CompletableFuture<String> getJoinMessage(String uuid) {
        CompletableFuture<String> future = new CompletableFuture<>();
        
        SchedulerUtils.runTaskAsynchronously(plugin, () -> {
            String tableName = databaseType.equalsIgnoreCase("mysql") ? 
                plugin.getConfigManager().getMySQLTable() : "joinmessages";
                
            String query = "SELECT message FROM " + tableName + " WHERE uuid = ?";
            
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, uuid);
                ResultSet resultSet = statement.executeQuery();
                
                if (resultSet.next()) {
                    future.complete(resultSet.getString("message"));
                } else {
                    future.complete(null);
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to get join message: " + e.getMessage());
                future.complete(null);
            }
        });
        
        return future;
    }

    public CompletableFuture<String> getJoinPrefix(String uuid) {
        CompletableFuture<String> future = new CompletableFuture<>();
        
        SchedulerUtils.runTaskAsynchronously(plugin, () -> {
            String tableName = databaseType.equalsIgnoreCase("mysql") ? 
                plugin.getConfigManager().getMySQLTable() : "joinmessages";
                
            String query = "SELECT prefix FROM " + tableName + " WHERE uuid = ?";
            
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, uuid);
                ResultSet resultSet = statement.executeQuery();
                
                if (resultSet.next()) {
                    future.complete(resultSet.getString("prefix"));
                } else {
                    future.complete(null);
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to get join prefix: " + e.getMessage());
                future.complete(null);
            }
        });
        
        return future;
    }

    public CompletableFuture<String> getJoinSuffix(String uuid) {
        CompletableFuture<String> future = new CompletableFuture<>();
        
        SchedulerUtils.runTaskAsynchronously(plugin, () -> {
            String tableName = databaseType.equalsIgnoreCase("mysql") ? 
                plugin.getConfigManager().getMySQLTable() : "joinmessages";
                
            String query = "SELECT suffix FROM " + tableName + " WHERE uuid = ?";
            
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, uuid);
                ResultSet resultSet = statement.executeQuery();
                
                if (resultSet.next()) {
                    future.complete(resultSet.getString("suffix"));
                } else {
                    future.complete(null);
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to get join suffix: " + e.getMessage());
                future.complete(null);
            }
        });
        
        return future;
    }

    public void removeJoinMessage(String uuid) {
        SchedulerUtils.runTaskAsynchronously(plugin, () -> {
            String tableName = databaseType.equalsIgnoreCase("mysql") ? 
                plugin.getConfigManager().getMySQLTable() : "joinmessages";
                
            String query = "DELETE FROM " + tableName + " WHERE uuid = ?";
            
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, uuid);
                statement.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to remove join message: " + e.getMessage());
            }
        });
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to close database connection: " + e.getMessage());
        }
    }
}