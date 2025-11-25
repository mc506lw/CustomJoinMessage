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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DatabaseManager {
    private final CustomJoinMessage plugin;
    private Connection connection;
    private final String databaseType;
    private final SchedulerUtils schedulerUtils;

    public DatabaseManager(CustomJoinMessage plugin) {
        this.plugin = plugin;
        this.schedulerUtils = plugin.getSchedulerUtils();
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
                "suffix TEXT," +
                "quit_message TEXT," +
                "quit_prefix TEXT," +
                "quit_suffix TEXT" +
                ")";
        
        try (Statement statement = connection.createStatement()) {
            statement.execute(query);
        }
        
        // Check if we need to add the quit message columns (for existing databases)
        try {
            // Check if quit_message column exists
            if (databaseType.equalsIgnoreCase("mysql")) {
                try (ResultSet rs = connection.getMetaData().getColumns(null, null, tableName, "quit_message")) {
                    if (!rs.next()) {
                        // Column doesn't exist, add it
                        try (Statement alterStatement = connection.createStatement()) {
                            alterStatement.execute("ALTER TABLE " + tableName + " ADD COLUMN quit_message TEXT");
                            alterStatement.execute("ALTER TABLE " + tableName + " ADD COLUMN quit_prefix TEXT");
                            alterStatement.execute("ALTER TABLE " + tableName + " ADD COLUMN quit_suffix TEXT");
                            plugin.getLogger().info("Added quit message columns to existing database table");
                        }
                    }
                }
            } else {
                // For SQLite, we need to check the table info
                try (ResultSet rs = connection.getMetaData().getColumns(null, null, tableName, "quit_message")) {
                    if (!rs.next()) {
                        // Column doesn't exist, add it
                        try (Statement alterStatement = connection.createStatement()) {
                            alterStatement.execute("ALTER TABLE " + tableName + " ADD COLUMN quit_message TEXT");
                            alterStatement.execute("ALTER TABLE " + tableName + " ADD COLUMN quit_prefix TEXT");
                            alterStatement.execute("ALTER TABLE " + tableName + " ADD COLUMN quit_suffix TEXT");
                            plugin.getLogger().info("Added quit message columns to existing database table");
                        }
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Could not check for quit message columns: " + e.getMessage());
        }
    }

    public void setJoinMessage(String uuid, String username, String message) {
        schedulerUtils.runTaskAsynchronously(() -> {
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
        
        schedulerUtils.runTaskAsynchronously(() -> {
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
        
        schedulerUtils.runTaskAsynchronously(() -> {
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
        
        schedulerUtils.runTaskAsynchronously(() -> {
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
        schedulerUtils.runTaskAsynchronously(() -> {
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
    
    /**
     * 获取数据库连接
     * @return 数据库连接
     */
    public Connection getConnection() {
        return connection;
    }

    // Quit message methods
    public void setQuitMessage(String uuid, String username, String message) {
        schedulerUtils.runTaskAsynchronously(() -> {
            String tableName = databaseType.equalsIgnoreCase("mysql") ? 
                plugin.getConfigManager().getMySQLTable() : "joinmessages";
                
            String query = "INSERT OR REPLACE INTO " + tableName + " (uuid, username, quit_message) VALUES (?, ?, ?)";
            
            if (databaseType.equalsIgnoreCase("mysql")) {
                query = "INSERT INTO " + tableName + " (uuid, username, quit_message) VALUES (?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE username = VALUES(username), quit_message = VALUES(quit_message)";
            }
            
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, uuid);
                statement.setString(2, username);
                statement.setString(3, message);
                statement.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to set quit message: " + e.getMessage());
            }
        });
    }
    
    public void setQuitPrefix(String uuid, String username, String prefix) {
        schedulerUtils.runTaskAsynchronously(() -> {
            String tableName = databaseType.equalsIgnoreCase("mysql") ? 
                plugin.getConfigManager().getMySQLTable() : "joinmessages";
                
            String query = "INSERT OR REPLACE INTO " + tableName + " (uuid, username, quit_prefix) VALUES (?, ?, ?)";
            
            if (databaseType.equalsIgnoreCase("mysql")) {
                query = "INSERT INTO " + tableName + " (uuid, username, quit_prefix) VALUES (?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE username = VALUES(username), quit_prefix = VALUES(quit_prefix)";
            }
            
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, uuid);
                statement.setString(2, username);
                statement.setString(3, prefix);
                statement.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to set quit prefix: " + e.getMessage());
            }
        });
    }
    
    public void setQuitSuffix(String uuid, String username, String suffix) {
        schedulerUtils.runTaskAsynchronously(() -> {
            String tableName = databaseType.equalsIgnoreCase("mysql") ? 
                plugin.getConfigManager().getMySQLTable() : "joinmessages";
                
            String query = "INSERT OR REPLACE INTO " + tableName + " (uuid, username, quit_suffix) VALUES (?, ?, ?)";
            
            if (databaseType.equalsIgnoreCase("mysql")) {
                query = "INSERT INTO " + tableName + " (uuid, username, quit_suffix) VALUES (?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE username = VALUES(username), quit_suffix = VALUES(quit_suffix)";
            }
            
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, uuid);
                statement.setString(2, username);
                statement.setString(3, suffix);
                statement.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to set quit suffix: " + e.getMessage());
            }
        });
    }

    public CompletableFuture<String> getQuitMessage(String uuid) {
        CompletableFuture<String> future = new CompletableFuture<>();
        
        schedulerUtils.runTaskAsynchronously(() -> {
            String tableName = databaseType.equalsIgnoreCase("mysql") ? 
                plugin.getConfigManager().getMySQLTable() : "joinmessages";
                
            String query = "SELECT quit_message FROM " + tableName + " WHERE uuid = ?";
            
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, uuid);
                ResultSet resultSet = statement.executeQuery();
                
                if (resultSet.next()) {
                    future.complete(resultSet.getString("quit_message"));
                } else {
                    future.complete(null);
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to get quit message: " + e.getMessage());
                future.complete(null);
            }
        });
        
        return future;
    }

    public CompletableFuture<String> getQuitPrefix(String uuid) {
        CompletableFuture<String> future = new CompletableFuture<>();
        
        schedulerUtils.runTaskAsynchronously(() -> {
            String tableName = databaseType.equalsIgnoreCase("mysql") ? 
                plugin.getConfigManager().getMySQLTable() : "joinmessages";
                
            String query = "SELECT quit_prefix FROM " + tableName + " WHERE uuid = ?";
            
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, uuid);
                ResultSet resultSet = statement.executeQuery();
                
                if (resultSet.next()) {
                    future.complete(resultSet.getString("quit_prefix"));
                } else {
                    future.complete(null);
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to get quit prefix: " + e.getMessage());
                future.complete(null);
            }
        });
        
        return future;
    }

    public CompletableFuture<String> getQuitSuffix(String uuid) {
        CompletableFuture<String> future = new CompletableFuture<>();
        
        schedulerUtils.runTaskAsynchronously(() -> {
            String tableName = databaseType.equalsIgnoreCase("mysql") ? 
                plugin.getConfigManager().getMySQLTable() : "joinmessages";
                
            String query = "SELECT quit_suffix FROM " + tableName + " WHERE uuid = ?";
            
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, uuid);
                ResultSet resultSet = statement.executeQuery();
                
                if (resultSet.next()) {
                    future.complete(resultSet.getString("quit_suffix"));
                } else {
                    future.complete(null);
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to get quit suffix: " + e.getMessage());
                future.complete(null);
            }
        });
        
        return future;
    }

    public void removeQuitMessage(String uuid) {
        schedulerUtils.runTaskAsynchronously(() -> {
            String tableName = databaseType.equalsIgnoreCase("mysql") ? 
                plugin.getConfigManager().getMySQLTable() : "joinmessages";
                
            String query = "UPDATE " + tableName + " SET quit_message = NULL, quit_prefix = NULL, quit_suffix = NULL WHERE uuid = ?";
            
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, uuid);
                statement.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to remove quit message: " + e.getMessage());
            }
        });
    }
    
    /**
     * 检查玩家是否存在于数据库中
     * @param uuid 玩家的UUID
     * @return CompletableFuture<Boolean> 如果玩家存在返回true，否则返回false
     */
    public CompletableFuture<Boolean> playerExists(String uuid) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        schedulerUtils.runTaskAsynchronously(() -> {
            String tableName = databaseType.equalsIgnoreCase("mysql") ? 
                plugin.getConfigManager().getMySQLTable() : "joinmessages";
                
            String query = "SELECT uuid FROM " + tableName + " WHERE uuid = ?";
            
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, uuid);
                ResultSet resultSet = statement.executeQuery();
                
                future.complete(resultSet.next());
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to check if player exists: " + e.getMessage());
                future.complete(false);
            }
        });
        
        return future;
    }
    
    /**
     * 通过玩家名获取玩家的UUID
     * @param username 玩家名
     * @return CompletableFuture<String> 玩家的UUID，如果不存在则返回null
     */
    public CompletableFuture<String> getPlayerUuid(String username) {
        CompletableFuture<String> future = new CompletableFuture<>();
        
        schedulerUtils.runTaskAsynchronously(() -> {
            String tableName = databaseType.equalsIgnoreCase("mysql") ? 
                plugin.getConfigManager().getMySQLTable() : "joinmessages";
                
            String query = "SELECT uuid FROM " + tableName + " WHERE username = ?";
            
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, username);
                ResultSet resultSet = statement.executeQuery();
                
                if (resultSet.next()) {
                    future.complete(resultSet.getString("uuid"));
                } else {
                    future.complete(null);
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to get player UUID: " + e.getMessage());
                future.complete(null);
            }
        });
        
        return future;
    }
    
    /**
     * 获取数据库中所有玩家的UUID和用户名
     * @return CompletableFuture<List<PlayerInfo>> 包含所有玩家信息的列表
     */
    public CompletableFuture<List<PlayerInfo>> getAllPlayers() {
        CompletableFuture<List<PlayerInfo>> future = new CompletableFuture<>();
        
        schedulerUtils.runTaskAsynchronously(() -> {
            String tableName = databaseType.equalsIgnoreCase("mysql") ? 
                plugin.getConfigManager().getMySQLTable() : "joinmessages";
                
            String query = "SELECT uuid, username FROM " + tableName + " ORDER BY username";
            List<PlayerInfo> players = new ArrayList<>();
            
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                ResultSet resultSet = statement.executeQuery();
                
                while (resultSet.next()) {
                    String uuid = resultSet.getString("uuid");
                    String username = resultSet.getString("username");
                    players.add(new PlayerInfo(uuid, username));
                }
                
                future.complete(players);
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to get all players: " + e.getMessage());
                future.complete(players); // 返回空列表而不是null
            }
        });
        
        return future;
    }
    
    /**
     * 通过UUID删除玩家数据
     * @param uuid 玩家的UUID
     * @return CompletableFuture<Boolean> 删除成功返回true，否则返回false
     */
    public CompletableFuture<Boolean> deletePlayerByUuid(String uuid) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        schedulerUtils.runTaskAsynchronously(() -> {
            String tableName = databaseType.equalsIgnoreCase("mysql") ? 
                plugin.getConfigManager().getMySQLTable() : "joinmessages";
                
            String query = "DELETE FROM " + tableName + " WHERE uuid = ?";
            
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, uuid);
                int rowsAffected = statement.executeUpdate();
                future.complete(rowsAffected > 0);
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to delete player by UUID: " + e.getMessage());
                future.complete(false);
            }
        });
        
        return future;
    }
    
    /**
     * 玩家信息类，用于存储UUID和用户名
     */
    public static class PlayerInfo {
        private final String uuid;
        private final String username;
        
        public PlayerInfo(String uuid, String username) {
            this.uuid = uuid;
            this.username = username;
        }
        
        public String getUuid() {
            return uuid;
        }
        
        public String getUsername() {
            return username;
        }
    }
}