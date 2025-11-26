package mc506lw.cjm.utils;

import mc506lw.cjm.CustomJoinMessage;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Utility class for handling placeholder replacement
 * 处理占位符替换的工具类
 */
public class PlaceholderUtil {
    
    private final CustomJoinMessage plugin;
    
    public PlaceholderUtil(CustomJoinMessage plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Replace placeholders in a message
     * 替换消息中的占位符
     * 
     * @param player The player to replace placeholders for
     * @param message The message to replace placeholders in
     * @return The message with placeholders replaced
     */
    public String replacePlaceholders(Player player, String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }
        
        // First replace %player_name% with the player's name
        String result = message.replace("%player_name%", player.getName());
        
        // If PlaceholderAPI is enabled, replace PlaceholderAPI placeholders
        if (plugin.getConfigManager().isPlaceholdersEnabled() 
                && Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            result = PlaceholderAPI.setPlaceholders(player, result);
        }
        
        return result;
    }
    
    /**
     * Replace placeholders in a message for an offline player
     * 为离线玩家替换消息中的占位符
     * 
     * @param playerName The name of the offline player
     * @param message The message to replace placeholders in
     * @return The message with placeholders replaced
     */
    public String replacePlaceholders(String playerName, String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }
        
        // Only replace basic placeholders for offline players
        String result = message.replace("%player_name%", playerName);
        result = result.replace("%display_name%", playerName);
        
        // Can't use PlaceholderAPI for offline players
        return result;
    }
    
    /**
     * Check if PlaceholderAPI is available and enabled
     * 检查PlaceholderAPI是否可用且已启用
     * 
     * @return True if PlaceholderAPI is available and enabled
     */
    public boolean isPlaceholderAPIEnabled() {
        return plugin.getConfigManager().isPlaceholdersEnabled() 
                && Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
    }
}