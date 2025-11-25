package mc506lw.cjm.listeners;

import mc506lw.cjm.CustomJoinMessage;
import mc506lw.cjm.utils.MessageManager;
import mc506lw.cjm.utils.PermissionUtils;
import mc506lw.cjm.utils.PlaceholderUtil;
import mc506lw.cjm.utils.SchedulerUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.concurrent.CompletableFuture;

public class PlayerQuitListener implements Listener {
    private final CustomJoinMessage plugin;
    private final MessageManager messageManager;
    private final PermissionUtils permissionUtils;
    private final SchedulerUtils schedulerUtils;
    private final PlaceholderUtil placeholderUtil;

    public PlayerQuitListener(CustomJoinMessage plugin) {
        this.plugin = plugin;
        this.messageManager = plugin.getMessageManager();
        this.permissionUtils = plugin.getPermissionUtils();
        this.schedulerUtils = plugin.getSchedulerUtils();
        this.placeholderUtil = plugin.getPlaceholderUtil();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();
        
        // Hide default quit message if configured
        if (plugin.getConfigManager().shouldHideDefaultQuitMessage()) {
            event.setQuitMessage(null);
        }
        
        // Get custom quit message asynchronously
        CompletableFuture<String> futureMessage = plugin.getDatabaseManager().getQuitMessage(player.getUniqueId().toString());
        
        // When the future is complete, process and broadcast the message
        futureMessage.thenAccept(customMessage -> {
            String message;
            
            // Check if player has a custom permission group for quit messages
            String permissionGroup = permissionUtils.getHighestPriorityPermissionGroup(player, "quit");
            
            if (plugin.getConfigManager().isPrefixSuffixMode()) {
                // Prefix-suffix mode
                if (customMessage != null && !customMessage.isEmpty()) {
                    // Use custom prefix and suffix from database
                    // Process all color codes including RGB formats
                    message = messageManager.processQuitMessageColors(customMessage, player);
                } else if (permissionGroup != null) {
                    // Use permission group prefix and suffix
                    String prefix = permissionUtils.getGroupQuitPrefix(permissionGroup);
                    String suffix = permissionUtils.getGroupQuitSuffix(permissionGroup);
                    
                    // Format prefix and suffix with placeholders
                    prefix = messageManager.formatMessage(prefix, playerName);
                    suffix = messageManager.formatMessage(suffix, playerName);
                    
                    // Build the complete message with color reset before player name
                    message = prefix + "&r" + playerName + suffix;
                    // Process all color codes including RGB formats
                    message = messageManager.processQuitMessageColors(message, player);
                } else {
                    // Use default prefix and suffix
                    String prefix = plugin.getConfigManager().getDefaultQuitPrefix();
                    String suffix = plugin.getConfigManager().getDefaultQuitSuffix();
                    
                    // Format prefix and suffix with placeholders
                    prefix = messageManager.formatMessage(prefix, playerName);
                    suffix = messageManager.formatMessage(suffix, playerName);
                    
                    // Build the complete message with color reset before player name
                    message = prefix + "&r" + playerName + suffix;
                    // Process all color codes including RGB formats
                    message = messageManager.processQuitMessageColors(message, player);
                }
            } else {
                // Full mode (default)
                if (customMessage != null) {
                    // Use custom message
                    message = messageManager.formatMessage(customMessage, playerName);
                } else if (permissionGroup != null) {
                    // Use permission group message
                    message = permissionUtils.getGroupQuitMessage(permissionGroup);
                    message = messageManager.formatMessage(message, playerName);
                } else {
                    // Use default message
                    message = plugin.getConfigManager().getDefaultQuitMessage();
                    message = messageManager.formatMessage(message, playerName);
                }
                // Process all color codes including RGB formats
                message = messageManager.processQuitMessageColors(message, player);
            }
            
            // Replace placeholders using PlaceholderUtil
            message = placeholderUtil.replacePlaceholders(player, message);
            
            // Create a final copy of the message for use in lambda
            final String finalMessage = message;
            
            // Broadcast the message using the appropriate scheduler
            schedulerUtils.runTask(() -> {
                plugin.getServer().broadcastMessage(finalMessage);
            });
        });
    }
}