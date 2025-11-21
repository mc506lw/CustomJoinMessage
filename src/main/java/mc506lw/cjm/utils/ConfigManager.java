package mc506lw.cjm.utils;

import mc506lw.cjm.CustomJoinMessage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class ConfigManager {
    private final CustomJoinMessage plugin;
    private FileConfiguration config;
    private FileConfiguration messages;

    public ConfigManager(CustomJoinMessage plugin) {
        this.plugin = plugin;
        saveDefaultConfig();
        saveDefaultMessages();
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

    public FileConfiguration getConfig() {
        return config;
    }

    public FileConfiguration getMessages() {
        return messages;
    }

    public void reloadConfig() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
        
        try {
            messages = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "messages.yml"));
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to reload messages.yml: " + e.getMessage());
        }
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