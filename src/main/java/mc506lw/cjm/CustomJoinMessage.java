package mc506lw.cjm;

import mc506lw.cjm.commands.CjmCommand;
import mc506lw.cjm.commands.SetJoinCommand;
import mc506lw.cjm.commands.SetQuitCommand;
import mc506lw.cjm.database.DatabaseManager;
import mc506lw.cjm.database.DatabaseVersionManager;
import mc506lw.cjm.expansions.CustomJoinMessageExpansion;
import mc506lw.cjm.listeners.PlayerJoinListener;
import mc506lw.cjm.listeners.PlayerQuitListener;
import mc506lw.cjm.utils.ConfigManager;
import mc506lw.cjm.utils.MessageManager;
import mc506lw.cjm.utils.MessageLengthUtil;
import mc506lw.cjm.utils.PermissionUtils;
import mc506lw.cjm.utils.PlaceholderUtil;
import mc506lw.cjm.utils.SchedulerUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class CustomJoinMessage extends JavaPlugin {

    private static CustomJoinMessage instance;
    private DatabaseManager databaseManager;
    private ConfigManager configManager;
    private MessageManager messageManager;
    private PermissionUtils permissionUtils;
    private SchedulerUtils schedulerUtils;
    private PlaceholderUtil placeholderUtil;
    private MessageLengthUtil messageLengthUtil;

    @Override
    public void onEnable() {
        instance = this;

        // Initialize managers
        configManager = new ConfigManager(this);
        messageManager = new MessageManager(this);
        permissionUtils = new PermissionUtils(this);
        schedulerUtils = new SchedulerUtils(this);
        placeholderUtil = new PlaceholderUtil(this);
        messageLengthUtil = new MessageLengthUtil();
        databaseManager = new DatabaseManager(this);

        // Initialize database
        databaseManager.initialize();

        // Initialize database version manager
        DatabaseVersionManager databaseVersionManager = new DatabaseVersionManager(this, databaseManager);
        databaseVersionManager.initialize();

        // Register commands
        getCommand("setjoin").setExecutor(new SetJoinCommand(this));
        getCommand("setquit").setExecutor(new SetQuitCommand(this));
        getCommand("cjm").setExecutor(new CjmCommand(this));

        // Register events
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);

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

    public PermissionUtils getPermissionUtils() {
        return permissionUtils;
    }

    public SchedulerUtils getSchedulerUtils() {
        return schedulerUtils;
    }
    
    public PlaceholderUtil getPlaceholderUtil() {
        return placeholderUtil;
    }
    
    public MessageLengthUtil getMessageLengthUtil() {
        return messageLengthUtil;
    }
}