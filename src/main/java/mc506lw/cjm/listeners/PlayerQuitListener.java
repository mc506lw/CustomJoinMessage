 package mc506lw.cjm.listeners;

 import mc506lw.cjm.CustomJoinMessage;
 import mc506lw.cjm.utils.MessageManager;
 import mc506lw.cjm.utils.PermissionUtils;
 import mc506lw.cjm.utils.PlaceholderUtil;
 import mc506lw.cjm.utils.SchedulerUtils;
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerQuitEvent;

 import java.util.UUID;
 import java.util.concurrent.CompletableFuture;

 public class PlayerQuitListener implements Listener {
     private final CustomJoinMessage plugin;
     private final MessageManager messageManager;
     private final PermissionUtils permissionUtils;
     private final PlaceholderUtil placeholderUtil;
     private final SchedulerUtils schedulerUtils;
     
     public PlayerQuitListener(CustomJoinMessage plugin) {
         this.plugin = plugin;
         this.messageManager = plugin.getMessageManager();
         this.permissionUtils = plugin.getPermissionUtils();
         this.placeholderUtil = plugin.getPlaceholderUtil();
         this.schedulerUtils = plugin.getSchedulerUtils();
     }
     
     @EventHandler(priority = EventPriority.MONITOR)
     public void onPlayerQuit(PlayerQuitEvent event) {
         // Hide the default quit message
         event.setQuitMessage(null);
         
         // Get player information
         Player player = event.getPlayer();
         final String playerName = player.getName();
         final UUID playerUuid = player.getUniqueId();
         
         // Process quit message asynchronously to avoid blocking the main thread
         plugin.getSchedulerUtils().runTaskAsynchronously(() -> {
             // Get the quit message from database
             plugin.getDatabaseManager().getQuitMessage(playerUuid.toString()).thenAccept(quitMessage -> {
                 if (quitMessage != null && !quitMessage.isEmpty()) {
                     // Replace placeholders in the quit message using the offline player method
                     String finalMessage = placeholderUtil.replacePlaceholders(playerName, quitMessage);
                     
                     // Format the message with player name
                     finalMessage = messageManager.formatMessage(finalMessage, playerName);
                     
                     // Process color codes based on player permissions (using player name for offline players)
                     finalMessage = messageManager.processQuitMessageColors(finalMessage, playerName);
                     
                     // Broadcast the custom quit message on the main thread
                     final String broadcastMessage = finalMessage;
                     plugin.getSchedulerUtils().runTask(() -> {
                         Bukkit.broadcastMessage(broadcastMessage);
                     });
                 }
             }).exceptionally(throwable -> {
                 plugin.getLogger().warning("Failed to get quit message for player " + playerName + ": " + throwable.getMessage());
                 return null;
             });
         });
     }
 }