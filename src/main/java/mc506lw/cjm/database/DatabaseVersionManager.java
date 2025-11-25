package mc506lw.cjm.database;

import mc506lw.cjm.CustomJoinMessage;
import mc506lw.cjm.utils.SchedulerUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

/**
 * 数据库版本管理器
 * 负责管理数据库版本升级和维护
 */
public class DatabaseVersionManager {
    private final CustomJoinMessage plugin;
    private final DatabaseManager databaseManager;
    private final SchedulerUtils schedulerUtils;
    
    // 当前数据库版本
    private static final int CURRENT_DB_VERSION = 1;
    
    public DatabaseVersionManager(CustomJoinMessage plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
        this.schedulerUtils = plugin.getSchedulerUtils();
    }
    
    /**
     * 初始化数据库版本管理
     * 检查数据库版本并执行必要的升级
     */
    public void initialize() {
        schedulerUtils.runTaskAsynchronously(() -> {
            try {
                // 创建版本表（如果不存在）
                createVersionTable();
                
                // 获取当前数据库版本
                int currentVersion = getCurrentDatabaseVersion();
                
                // 执行版本升级
                if (currentVersion < CURRENT_DB_VERSION) {
                    plugin.getLogger().info("检测到数据库版本需要升级，当前版本: " + currentVersion + "，目标版本: " + CURRENT_DB_VERSION);
                    upgradeDatabase(currentVersion, CURRENT_DB_VERSION);
                } else {
                    plugin.getLogger().info("数据库版本已是最新: " + currentVersion);
                }
                
                // 执行自动维护
                performMaintenance();
                
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "数据库版本管理初始化失败", e);
            }
        });
    }
    
    /**
     * 创建版本表
     */
    private void createVersionTable() throws SQLException {
        String databaseType = plugin.getConfigManager().getDatabaseType();
        String tableName = databaseType.equalsIgnoreCase("mysql") ? 
            plugin.getConfigManager().getMySQLTable() + "_version" : "joinmessages_version";
            
        String query = "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                "id INTEGER PRIMARY KEY," +
                "version INTEGER NOT NULL," +
                "upgrade_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")";
        
        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(query)) {
            statement.execute();
        }
    }
    
    /**
     * 获取当前数据库版本
     */
    private int getCurrentDatabaseVersion() throws SQLException {
        String databaseType = plugin.getConfigManager().getDatabaseType();
        String tableName = databaseType.equalsIgnoreCase("mysql") ? 
            plugin.getConfigManager().getMySQLTable() + "_version" : "joinmessages_version";
            
        String query = "SELECT version FROM " + tableName + " ORDER BY id DESC LIMIT 1";
        
        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            
            if (resultSet.next()) {
                return resultSet.getInt("version");
            } else {
                // 如果没有版本记录，假设是版本0（未初始化）
                return 0;
            }
        }
    }
    
    /**
     * 更新数据库版本
     */
    private void updateDatabaseVersion(int newVersion) throws SQLException {
        String databaseType = plugin.getConfigManager().getDatabaseType();
        String tableName = databaseType.equalsIgnoreCase("mysql") ? 
            plugin.getConfigManager().getMySQLTable() + "_version" : "joinmessages_version";
            
        String query = "INSERT INTO " + tableName + " (id, version) VALUES (1, ?)";
        
        if (databaseType.equalsIgnoreCase("mysql")) {
            query = "INSERT INTO " + tableName + " (id, version) VALUES (1, ?) " +
                    "ON DUPLICATE KEY UPDATE version = VALUES(version), upgrade_date = CURRENT_TIMESTAMP";
        } else {
            // 对于SQLite，先尝试更新，如果失败则插入
            try {
                String updateQuery = "UPDATE " + tableName + " SET version = ? WHERE id = 1";
                try (PreparedStatement updateStatement = databaseManager.getConnection().prepareStatement(updateQuery)) {
                    updateStatement.setInt(1, newVersion);
                    updateStatement.executeUpdate();
                }
                return;
            } catch (SQLException e) {
                // 更新失败，执行插入
            }
        }
        
        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(query)) {
            statement.setInt(1, newVersion);
            statement.executeUpdate();
        }
    }
    
    /**
     * 升级数据库
     */
    private void upgradeDatabase(int fromVersion, int toVersion) throws SQLException {
        for (int version = fromVersion + 1; version <= toVersion; version++) {
            plugin.getLogger().info("正在升级数据库到版本: " + version);
            
            switch (version) {
                case 1:
                    // 版本1升级：确保quit_message列存在
                    upgradeToVersion1();
                    break;
                default:
                    plugin.getLogger().warning("未知的数据库版本: " + version);
                    break;
            }
            
            // 更新版本记录
            updateDatabaseVersion(version);
            plugin.getLogger().info("数据库升级到版本 " + version + " 完成");
        }
    }
    
    /**
     * 升级到版本1
     * 确保quit_message列存在
     */
    private void upgradeToVersion1() throws SQLException {
        String databaseType = plugin.getConfigManager().getDatabaseType();
        String tableName = databaseType.equalsIgnoreCase("mysql") ? 
            plugin.getConfigManager().getMySQLTable() : "joinmessages";
            
        // 检查quit_message列是否存在
        try (ResultSet rs = databaseManager.getConnection().getMetaData().getColumns(null, null, tableName, "quit_message")) {
            if (!rs.next()) {
                // 列不存在，添加它
                try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(
                        "ALTER TABLE " + tableName + " ADD COLUMN quit_message TEXT")) {
                    statement.execute();
                }
                
                try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(
                        "ALTER TABLE " + tableName + " ADD COLUMN quit_prefix TEXT")) {
                    statement.execute();
                }
                
                try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(
                        "ALTER TABLE " + tableName + " ADD COLUMN quit_suffix TEXT")) {
                    statement.execute();
                }
                
                plugin.getLogger().info("已添加退出消息相关列到数据库表");
            }
        }
    }
    
    /**
     * 执行数据库维护
     */
    private void performMaintenance() {
        try {
            // 检查是否有孤立记录（没有有效UUID的记录）
            cleanupOrphanedRecords();
            
            // 检查是否有重复记录
            cleanupDuplicateRecords();
            
            plugin.getLogger().info("数据库维护完成");
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "数据库维护过程中出现问题", e);
        }
    }
    
    /**
     * 清理孤立记录
     */
    private void cleanupOrphanedRecords() throws SQLException {
        String databaseType = plugin.getConfigManager().getDatabaseType();
        String tableName = databaseType.equalsIgnoreCase("mysql") ? 
            plugin.getConfigManager().getMySQLTable() : "joinmessages";
            
        String query = "DELETE FROM " + tableName + " WHERE uuid IS NULL OR uuid = ''";
        
        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(query)) {
            int deletedRows = statement.executeUpdate();
            if (deletedRows > 0) {
                plugin.getLogger().info("清理了 " + deletedRows + " 条孤立记录");
            }
        }
    }
    
    /**
     * 清理重复记录
     */
    private void cleanupDuplicateRecords() throws SQLException {
        String databaseType = plugin.getConfigManager().getDatabaseType();
        String tableName = databaseType.equalsIgnoreCase("mysql") ? 
            plugin.getConfigManager().getMySQLTable() : "joinmessages";
            
        // 查找重复的UUID
        String findDuplicatesQuery = "SELECT uuid, COUNT(*) as count FROM " + tableName + 
                " GROUP BY uuid HAVING count > 1";
        
        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(findDuplicatesQuery);
             ResultSet resultSet = statement.executeQuery()) {
            
            int duplicatesCount = 0;
            while (resultSet.next()) {
                String uuid = resultSet.getString("uuid");
                int count = resultSet.getInt("count");
                
                // 保留最新的记录，删除其他记录
                String deleteDuplicatesQuery;
                if (databaseType.equalsIgnoreCase("mysql")) {
                    deleteDuplicatesQuery = "DELETE FROM " + tableName + " WHERE uuid = ? AND id NOT IN (" +
                            "SELECT id FROM (SELECT id FROM " + tableName + " WHERE uuid = ? ORDER BY id DESC LIMIT 1) AS temp)";
                } else {
                    // SQLite不支持子查询中的LIMIT，使用不同的方法
                    deleteDuplicatesQuery = "DELETE FROM " + tableName + " WHERE uuid = ? AND rowid NOT IN (" +
                            "SELECT rowid FROM " + tableName + " WHERE uuid = ? ORDER BY rowid DESC LIMIT 1)";
                }
                
                try (PreparedStatement deleteStatement = databaseManager.getConnection().prepareStatement(deleteDuplicatesQuery)) {
                    deleteStatement.setString(1, uuid);
                    deleteStatement.setString(2, uuid);
                    int deletedRows = deleteStatement.executeUpdate();
                    duplicatesCount += deletedRows;
                }
            }
            
            if (duplicatesCount > 0) {
                plugin.getLogger().info("清理了 " + duplicatesCount + " 条重复记录");
            }
        }
    }
    
    /**
     * 重置单个玩家的数据库记录
     * @param playerName 玩家名称
     * @return CompletableFuture<Boolean> 如果重置成功返回true，否则返回false
     */
    public CompletableFuture<Boolean> resetPlayerData(String playerName) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        schedulerUtils.runTaskAsynchronously(() -> {
            try {
                // 通过玩家名获取UUID
                databaseManager.getPlayerUuid(playerName).thenAccept(uuid -> {
                    if (uuid == null) {
                        future.complete(false);
                        return;
                    }
                    
                    // 重置玩家数据
                    try {
                        String databaseType = plugin.getConfigManager().getDatabaseType();
                        String tableName = databaseType.equalsIgnoreCase("mysql") ? 
                            plugin.getConfigManager().getMySQLTable() : "joinmessages";
                            
                        String query = "DELETE FROM " + tableName + " WHERE uuid = ?";
                        
                        try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(query)) {
                            statement.setString(1, uuid);
                            int deletedRows = statement.executeUpdate();
                            future.complete(deletedRows > 0);
                        }
                    } catch (SQLException e) {
                        plugin.getLogger().log(Level.SEVERE, "重置玩家数据失败", e);
                        future.complete(false);
                    }
                });
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "重置玩家数据过程中出现异常", e);
                future.complete(false);
            }
        });
        
        return future;
    }
    
    /**
     * 强制通过玩家名重置玩家的数据库记录（不依赖UUID）
     * @param playerName 玩家名称
     * @return 如果重置成功返回true，否则返回false
     */
    public boolean forceResetPlayerDataByName(String playerName) {
        try {
            String databaseType = plugin.getConfigManager().getDatabaseType();
            String tableName = databaseType.equalsIgnoreCase("mysql") ? 
                plugin.getConfigManager().getMySQLTable() : "joinmessages";
                
            // 尝试通过username字段删除记录
            String query = "DELETE FROM " + tableName + " WHERE username = ?";
            
            try (PreparedStatement statement = databaseManager.getConnection().prepareStatement(query)) {
                statement.setString(1, playerName);
                int deletedRows = statement.executeUpdate();
                plugin.getLogger().info("强制删除了玩家 " + playerName + " 的 " + deletedRows + " 条记录");
                return deletedRows > 0;
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "强制重置玩家数据失败", e);
            return false;
        }
    }
}