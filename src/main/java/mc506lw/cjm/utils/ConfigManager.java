package mc506lw.cjm.utils;

import mc506lw.cjm.CustomJoinMessage;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ConfigManager {
    private final CustomJoinMessage plugin;
    private FileConfiguration config;
    private FileConfiguration messages;
    private static final String CURRENT_CONFIG_VERSION = "1.1.0";
    private String lastBackupFile;
    private String lastMessagesBackupFile;

    public ConfigManager(CustomJoinMessage plugin) {
        this.plugin = plugin;
        saveDefaultConfig();
        saveDefaultMessages();
        checkAndUpdateConfig();
        checkAndUpdateMessages();
        this.config = plugin.getConfig();
        this.messages = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "messages.yml"));
    }

    private void saveDefaultConfig() {
        plugin.saveDefaultConfig();
    }

    private void saveDefaultMessages() {
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
    }

    private void checkAndUpdateConfig() {
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        FileConfiguration existingConfig = YamlConfiguration.loadConfiguration(configFile);
        
        // Check if config-version exists and matches
        if (existingConfig.contains("config-version")) {
            String existingVersion = existingConfig.getString("config-version");
            
            if (!CURRENT_CONFIG_VERSION.equals(existingVersion)) {
                // Version mismatch, backup and update config
                backupConfig(configFile);
                updateConfig(configFile);
                
                // Log the update
                plugin.getLogger().info("配置文件已从版本 " + existingVersion + " 更新到 " + CURRENT_CONFIG_VERSION);
                plugin.getLogger().info("旧配置文件已备份为 config_backup_" + getTimestamp() + ".yml");
            }
        } else {
            // No version info, assume old version
            backupConfig(configFile);
            updateConfig(configFile);
            
            plugin.getLogger().info("检测到旧版本配置文件，已更新到版本 " + CURRENT_CONFIG_VERSION);
            plugin.getLogger().info("旧配置文件已备份为 config_backup_" + getTimestamp() + ".yml");
        }
    }

    private void backupConfig(File configFile) {
        try {
            String timestamp = getTimestamp();
            lastBackupFile = "config_backup_" + timestamp + ".yml";
            File backupFile = new File(plugin.getDataFolder(), lastBackupFile);
            Files.copy(configFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            plugin.getLogger().warning("无法备份配置文件: " + e.getMessage());
        }
    }

    private void backupMessages(File messagesFile) {
        try {
            String timestamp = getTimestamp();
            lastMessagesBackupFile = "messages_backup_" + timestamp + ".yml";
            File backupFile = new File(plugin.getDataFolder(), lastMessagesBackupFile);
            Files.copy(messagesFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            plugin.getLogger().warning("无法备份消息文件: " + e.getMessage());
        }
    }

    private void updateConfig(File configFile) {
        // Save the new config from resources
        plugin.saveResource("config.yml", true);
    }

    private void updateMessages(File messagesFile) {
        // Save the new messages from resources
        plugin.saveResource("messages.yml", true);
    }

    private String getTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        return sdf.format(new Date());
    }

    private void checkAndUpdateMessages() {
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        FileConfiguration existingMessages = YamlConfiguration.loadConfiguration(messagesFile);
        
        // Check if the messages file has all required keys
        boolean needsUpdate = false;
        
        // Get the default messages from resources
        FileConfiguration defaultMessages = null;
        try (InputStream defaultStream = plugin.getResource("messages.yml")) {
            if (defaultStream != null) {
                defaultMessages = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defaultStream, StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            plugin.getLogger().warning("无法加载默认消息文件: " + e.getMessage());
            // Reset the backup file variable since we can't determine if an update is needed
            lastMessagesBackupFile = null;
            return;
        }
        
        if (defaultMessages == null) {
            plugin.getLogger().warning("无法找到默认消息文件");
            // Reset the backup file variable since we can't determine if an update is needed
            lastMessagesBackupFile = null;
            return;
        }
        
        // Check for missing keys in the messages section
        if (existingMessages.contains("messages")) {
            ConfigurationSection defaultMsgSection = defaultMessages.getConfigurationSection("messages");
            ConfigurationSection existingMsgSection = existingMessages.getConfigurationSection("messages");
            
            if (defaultMsgSection != null && existingMsgSection != null) {
                for (String key : defaultMsgSection.getKeys(false)) {
                    if (!existingMsgSection.contains(key)) {
                        needsUpdate = true;
                        break;
                    }
                }
            } else {
                needsUpdate = true;
            }
        } else {
            needsUpdate = true;
        }
        
        if (needsUpdate) {
            // Backup and update messages file
            backupMessages(messagesFile);
            updateMessages(messagesFile);
            
            // Log the update
            plugin.getLogger().info("检测到消息文件缺少必要内容，已自动更新到最新版本！");
            plugin.getLogger().info("原消息文件已备份为 messages_backup_" + getTimestamp() + ".yml");
        } else {
            // Reset the backup file variable if no update was needed
            lastMessagesBackupFile = null;
        }
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public FileConfiguration getMessages() {
        return messages;
    }

    public boolean reloadConfig() {
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        FileConfiguration existingConfig = YamlConfiguration.loadConfiguration(configFile);
        
        boolean configUpdated = false;
        boolean messagesUpdated = false;
        
        // Check if config file exists and has the required version key
        if (!configFile.exists() || !existingConfig.contains("config-version")) {
            // Config file doesn't exist or is missing version, update it
            backupConfig(configFile);
            updateConfig(configFile);
            configUpdated = true;
            
            // Log the update
            plugin.getLogger().info("配置文件版本过旧，已自动更新到最新版本！");
            plugin.getLogger().info("原配置文件已备份为 config_backup_" + getTimestamp() + ".yml");
        } else {
            // Reset the backup file variable if no update was needed
            lastBackupFile = null;
        }
        
        // Check and update messages file
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        FileConfiguration existingMessages = YamlConfiguration.loadConfiguration(messagesFile);
        
        // Get the default messages from resources
        FileConfiguration defaultMessages = null;
        try (InputStream defaultStream = plugin.getResource("messages.yml")) {
            if (defaultStream != null) {
                defaultMessages = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defaultStream, StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            plugin.getLogger().warning("无法加载默认消息文件: " + e.getMessage());
            // Reset the backup file variable since we can't determine if an update is needed
            lastMessagesBackupFile = null;
            return configUpdated;
        }
        
        if (defaultMessages == null) {
            plugin.getLogger().warning("无法找到默认消息文件");
            // Reset the backup file variable since we can't determine if an update is needed
            lastMessagesBackupFile = null;
            return configUpdated;
        }
        
        // Check for missing keys in the messages section
        if (defaultMessages != null) {
            if (!existingMessages.contains("messages")) {
                messagesUpdated = true;
            } else {
                ConfigurationSection defaultMsgSection = defaultMessages.getConfigurationSection("messages");
                ConfigurationSection existingMsgSection = existingMessages.getConfigurationSection("messages");
                
                if (defaultMsgSection != null && existingMsgSection != null) {
                    for (String key : defaultMsgSection.getKeys(false)) {
                        if (!existingMsgSection.contains(key)) {
                            messagesUpdated = true;
                            break;
                        }
                    }
                } else {
                    messagesUpdated = true;
                }
            }
        } else {
            // If we can't load the default messages, we can't determine if an update is needed
            // So we'll assume no update is needed to avoid accidentally overwriting user's messages
            plugin.getLogger().warning("无法确定消息文件是否需要更新，跳过消息文件检查");
            // Reset the backup file variable since we can't determine if an update is needed
            lastMessagesBackupFile = null;
            return configUpdated;
        }
        
        if (messagesUpdated) {
            // Backup and update messages file
            backupMessages(messagesFile);
            updateMessages(messagesFile);
            
            // Log the update
            plugin.getLogger().info("检测到消息文件缺少必要内容，已自动更新到最新版本！");
            plugin.getLogger().info("原消息文件已备份为 messages_backup_" + getTimestamp() + ".yml");
        } else {
            // Reset the backup file variable if no update was needed
            lastMessagesBackupFile = null;
        }
        
        // Reload the configuration
        plugin.reloadConfig();
        messages = YamlConfiguration.loadConfiguration(messagesFile);
        
        return configUpdated || messagesUpdated;
    }

    public String getLastBackupFile() {
        return lastBackupFile;
    }

    public String getLastMessagesBackupFile() {
        return lastMessagesBackupFile;
    }

    public String getMessageMode() {
        return config.getString("message-mode", "full");
    }

    public boolean isFullMode() {
        return "full".equalsIgnoreCase(getMessageMode());
    }

    public boolean isPrefixSuffixMode() {
        return "prefix_suffix".equalsIgnoreCase(getMessageMode());
    }

    public String getDefaultJoinMessage() {
        return config.getString("default-join-message", "&e%player_name% 加入了服务器");
    }

    public String getDefaultJoinPrefix() {
        return config.getString("default-join-prefix", "&e欢迎 ");
    }

    public String getDefaultJoinSuffix() {
        return config.getString("default-join-suffix", " 加入服务器！");
    }

    public boolean shouldHideDefaultJoinMessage() {
        return config.getBoolean("hide-default-join-message", true);
    }

    public String getDatabaseType() {
        return config.getString("database.type", "sqlite");
    }

    public String getSQLiteFile() {
        return config.getString("database.sqlite.file", "joinmessages.db");
    }

    public String getMySQLHost() {
        return config.getString("database.mysql.host", "localhost");
    }

    public int getMySQLPort() {
        return config.getInt("database.mysql.port", 3306);
    }

    public String getMySQLDatabase() {
        return config.getString("database.mysql.database", "minecraft");
    }

    public String getMySQLUsername() {
        return config.getString("database.mysql.username", "root");
    }

    public String getMySQLPassword() {
        return config.getString("database.mysql.password", "password");
    }

    public String getMySQLTable() {
        return config.getString("database.mysql.table", "joinmessages");
    }

    public int getFullModeLengthLimit() {
        return config.getInt("length-limits.full-mode", 50);
    }

    public int getPrefixLengthLimit() {
        return config.getInt("length-limits.prefix", 20);
    }

    public int getSuffixLengthLimit() {
        return config.getInt("length-limits.suffix", 20);
    }

    public boolean isPlaceholdersEnabled() {
        return config.getBoolean("placeholders.enabled", true);
    }
}