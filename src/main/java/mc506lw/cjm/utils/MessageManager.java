package mc506lw.cjm.utils;

import mc506lw.cjm.CustomJoinMessage;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MessageManager {
    private final CustomJoinMessage plugin;
    private final PermissionUtils permissionUtils;
    private final PlaceholderUtil placeholderUtil;

    public MessageManager(CustomJoinMessage plugin) {
        this.plugin = plugin;
        this.permissionUtils = plugin.getPermissionUtils();
        this.placeholderUtil = plugin.getPlaceholderUtil();
    }

    /**
     * Process color codes in a message, supporting both standard and RGB formats
     * 处理消息中的颜色代码，支持标准和RGB格式
     * 
     * @param message The message to process
     * @return The message with color codes processed
     */
    public String processColors(String message) {
        if (message == null) return "";
        
        // Support for RGB colors (1.16+) - &#RRGGBB format
        message = message.replaceAll("&#([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])", "§x§$1§$2§$3§$4§$5§$6");
        
        // Translate standard color codes (&a, &b, etc.) and &x&r&r&g&g&b&b RGB format
        message = ChatColor.translateAlternateColorCodes('&', message);
        
        return message;
    }

    public String getMessage(String path) {
        String message = plugin.getConfigManager().getMessages().getString("messages." + path);
        return message != null ? processColors(message) : "";
    }

    public String getMessage(String path, String placeholder, String replacement) {
        return getMessage(path).replace(placeholder, replacement);
    }

    public void sendMessage(CommandSender sender, String path) {
        sender.sendMessage(getMessage(path));
    }

    public void sendMessage(CommandSender sender, String path, String placeholder, String replacement) {
        sender.sendMessage(getMessage(path, placeholder, replacement));
    }

    public String formatMessage(String message, String playerName) {
        // Replace &n with player name with color reset
        message = message.replace("&n", "&r" + playerName);

        // Replace %player_name% with player name
        message = message.replace("%player_name%", playerName);

        // Process all color codes
        message = processColors(message);

        return message;
    }
    
    /**
     * Format a message with placeholders for a specific player
     * 为特定玩家格式化带有占位符的消息
     * 
     * @param message The message to format
     * @param player The player to format the message for
     * @return The formatted message
     */
    public String formatMessageWithPlaceholders(String message, Player player) {
        // First do basic formatting
        message = formatMessage(message, player.getName());
        
        // Then replace PlaceholderAPI placeholders if enabled
        message = placeholderUtil.replacePlaceholders(player, message);
        
        return message;
    }
    
    /**
     * Process color codes in a join message based on player permissions
     * 根据玩家权限处理加入消息中的颜色代码
     * 
     * @param message The message to process
     * @param player The player to check permissions for
     * @return The message with color codes processed based on permissions
     */
    public String processJoinMessageColors(String message, Player player) {
        return permissionUtils.processJoinMessageColors(player, message);
    }
    
    /**
     * Process color codes in a quit message based on player permissions
     * 根据玩家权限处理退出消息中的颜色代码
     * 
     * @param message The message to process
     * @param player The player to check permissions for
     * @return The message with color codes processed based on permissions
     */
    public String processQuitMessageColors(String message, Player player) {
        return permissionUtils.processQuitMessageColors(player, message);
    }
}