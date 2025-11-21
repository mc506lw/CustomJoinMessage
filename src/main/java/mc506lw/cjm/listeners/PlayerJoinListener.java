package mc506lw.cjm.listeners;

import mc506lw.cjm.CustomJoinMessage;
import mc506lw.cjm.utils.MessageManager;
import mc506lw.cjm.utils.PlaceholderUtil;
import mc506lw.cjm.utils.SchedulerUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.concurrent.CompletableFuture;

public class PlayerJoinListener implements Listener {
    private final CustomJoinMessage plugin;
    private final MessageManager messageManager;

    public PlayerJoinListener(CustomJoinMessage plugin) {
        this.plugin = plugin;
        this.messageManager = plugin.getMessageManager();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();
        
        // Hide default join message if configured
        if (plugin.getConfigManager().shouldHideDefaultJoinMessage()) {
            event.setJoinMessage(null);
        }
        
        // Get custom join message asynchronously
        CompletableFuture<String> futureMessage = plugin.getDatabaseManager().getJoinMessage(player.getUniqueId().toString());
        
        // When the future is complete, process and broadcast the message
        futureMessage.thenAccept(customMessage -> {
            String message;
            
            if (plugin.getConfigManager().isPrefixSuffixMode()) {
                // Prefix-suffix mode
                if (customMessage != null && !customMessage.isEmpty()) {
                    // Use custom prefix and suffix from database
                    // Process all color codes including RGB formats
                    message = messageManager.processColors(customMessage);
                } else {
                    // Use default prefix and suffix
                    String prefix = plugin.getConfigManager().getDefaultJoinPrefix();
                    String suffix = plugin.getConfigManager().getDefaultJoinSuffix();
                    
                    // Format prefix and suffix with placeholders
                    prefix = messageManager.formatMessage(prefix, playerName);
                    suffix = messageManager.formatMessage(suffix, playerName);
                    
                    // Build the complete message with color reset before player name
                    message = prefix + "&r" + playerName + suffix;
                    // Process all color codes including RGB formats
                    message = messageManager.processColors(message);
                }
            } else {
                // Full mode (default)
                if (customMessage != null) {
                    // Use custom message
                    message = messageManager.formatMessage(customMessage, playerName);
                } else {
                    // Use default message
                    message = plugin.getConfigManager().getDefaultJoinMessage();
                    message = messageManager.formatMessage(message, playerName);
                }
                // Process all color codes including RGB formats
                message = messageManager.processColors(message);
            }
            
            // Replace placeholders using PlaceholderUtil
            message = PlaceholderUtil.replacePlaceholders(player, message);
            
            // Create a final copy of the message for use in lambda
            final String finalMessage = message;
            
            // Broadcast the message using the appropriate scheduler
            SchedulerUtils.runTask(plugin, () -> {
                plugin.getServer().broadcastMessage(finalMessage);
            });
        });
    }
}