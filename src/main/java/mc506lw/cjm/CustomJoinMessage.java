package mc506lw.cjm;

import mc506lw.cjm.commands.CjmCommand;
import mc506lw.cjm.commands.SetJoinCommand;
import mc506lw.cjm.database.DatabaseManager;
import mc506lw.cjm.expansions.CustomJoinMessageExpansion;
import mc506lw.cjm.listeners.PlayerJoinListener;
import mc506lw.cjm.utils.ConfigManager;
import mc506lw.cjm.utils.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class CustomJoinMessage extends JavaPlugin {

    private static CustomJoinMessage instance;
    private DatabaseManager databaseManager;
    private ConfigManager configManager;
    private MessageManager messageManager;

    @Override
    public void onEnable() {
        instance = this;

        // Initialize managers
        configManager = new ConfigManager(this);
        messageManager = new MessageManager(this);
        databaseManager = new DatabaseManager(this);

        // Initialize database
        databaseManager.initialize();

        // Register commands
        getCommand("setjoin").setExecutor(new SetJoinCommand(this));
        getCommand("cjm").setExecutor(new CjmCommand(this));

        // Register events
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);

        // Register PlaceholderAPI expansion if PlaceholderAPI is available
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new CustomJoinMessageExpansion(this).register();
            getLogger().info("CustomJoinMessage PlaceholderAPI expansion has been registered!");
        }

        getLogger().info("CustomJoinMessage has been enabled!");
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.close();
        }
        getLogger().info("CustomJoinMessage has been disabled!");
    }

    public static CustomJoinMessage getInstance() {
        return instance;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }
}