package mc506lw.cjm.commands;

import mc506lw.cjm.CustomJoinMessage;
import mc506lw.cjm.utils.MessageLengthUtil;
import mc506lw.cjm.utils.MessageManager;
import mc506lw.cjm.utils.PermissionUtils;
import mc506lw.cjm.utils.SchedulerUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SetJoinCommand implements CommandExecutor, TabCompleter {
    private final CustomJoinMessage plugin;
    private final MessageManager messageManager;

    public SetJoinCommand(CustomJoinMessage plugin) {
        this.plugin = plugin;
        this.messageManager = plugin.getMessageManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check basic permission
        if (!PermissionUtils.canUseBasic(sender)) {
            messageManager.sendMessage(sender, "no-permission");
            return true;
        }

        // Handle different command scenarios
        if (args.length == 0) {
            // View current join message
            if (!(sender instanceof Player)) {
                messageManager.sendMessage(sender, "console-not-allowed");
                return true;
            }

            Player player = (Player) sender;
            
            // Get join message asynchronously
            CompletableFuture<String> futureMessage = plugin.getDatabaseManager().getJoinMessage(player.getUniqueId().toString());
            
            // Process the result when it's available
            futureMessage.thenAccept(joinMessage -> {
                SchedulerUtils.runTask(plugin, () -> {
                    if (plugin.getConfigManager().isPrefixSuffixMode()) {
                        // In prefix-suffix mode, extract and show current prefix and suffix
                        messageManager.sendMessage(player, "current-mode", "%mode%", "prefix-suffix");
                        
                        if (joinMessage == null || joinMessage.isEmpty()) {
                            // No custom message, show defaults
                            String defaultPrefix = plugin.getConfigManager().getDefaultJoinPrefix();
                            String defaultSuffix = plugin.getConfigManager().getDefaultJoinSuffix();
                            messageManager.sendMessage(player, "current-prefix", "%prefix%", defaultPrefix);
                            messageManager.sendMessage(player, "current-suffix", "%suffix%", defaultSuffix);
                        } else {
                            // Extract prefix and suffix from the stored message
                            String playerNameWithReset = "&r" + player.getName();
                            int playerNameIndex = joinMessage.indexOf(playerNameWithReset);
                            
                            if (playerNameIndex != -1) {
                                String prefix = joinMessage.substring(0, playerNameIndex);
                                String suffix = joinMessage.substring(playerNameIndex + playerNameWithReset.length());
                                
                                messageManager.sendMessage(player, "current-prefix", "%prefix%", prefix.isEmpty() ? "(无)" : prefix);
                                messageManager.sendMessage(player, "current-suffix", "%suffix%", suffix.isEmpty() ? "(无)" : suffix);
                            } else {
                                // Fallback if format is unexpected
                                messageManager.sendMessage(player, "current-message", "%message%", joinMessage);
                            }
                        }
                    } else {
                        // In full mode, show the current message
                        if (joinMessage == null) {
                            messageManager.sendMessage(player, "no-custom-message");
                        } else {
                            String formattedMessage = messageManager.formatMessage(joinMessage, player.getName());
                            messageManager.sendMessage(player, "current-message", "%message%", formattedMessage);
                            // Show color permission info
                            String permissionLevel = PermissionUtils.getPermissionLevel(player);
                            messageManager.sendMessage(player, "permission-level", "%level%", permissionLevel);
                        }
                    }
                });
            });
            
            return true;
        }

        if (args[0].equalsIgnoreCase("reset")) {
            // Reset join message
            if (!(sender instanceof Player)) {
                messageManager.sendMessage(sender, "console-not-allowed");
                return true;
            }

            Player player = (Player) sender;
            plugin.getDatabaseManager().removeJoinMessage(player.getUniqueId().toString());
            messageManager.sendMessage(player, "join-message-cleared");
            return true;
        }

        if (args[0].equalsIgnoreCase("prefix")) {
            // Set prefix in prefix-suffix mode
            if (!plugin.getConfigManager().isPrefixSuffixMode()) {
                messageManager.sendMessage(sender, "not-prefix-suffix-mode");
                return true;
            }

            if (!(sender instanceof Player)) {
                messageManager.sendMessage(sender, "console-not-allowed");
                return true;
            }

            if (args.length < 2) {
                messageManager.sendMessage(sender, "prefix-usage");
                return true;
            }

            Player player = (Player) sender;
            StringBuilder prefix = new StringBuilder();
            for (int i = 1; i < args.length; i++) {
                if (i > 1) prefix.append(" ");
                prefix.append(args[i]);
            }
            
            // Check prefix length limit
            int prefixLengthLimit = plugin.getConfigManager().getPrefixLengthLimit();
            if (MessageLengthUtil.exceedsLengthLimit(prefix.toString(), prefixLengthLimit)) {
                messageManager.sendMessage(player, "prefix-too-long", "%limit%", String.valueOf(prefixLengthLimit));
                return true;
            }
            
            // Check color permissions and process message
            String processedPrefix = PermissionUtils.processMessageColors(player, prefix.toString());
            if (processedPrefix.isEmpty() && !prefix.toString().isEmpty()) {
                messageManager.sendMessage(player, "no-color-permission");
                return true;
            }

            // Get current message to extract suffix
            CompletableFuture<String> futureMessage = plugin.getDatabaseManager().getJoinMessage(player.getUniqueId().toString());
            
            futureMessage.thenAccept(currentMessage -> {
                SchedulerUtils.runTask(plugin, () -> {
                    String suffix = "";
                    
                    // If there's already a message, try to extract the suffix
                    if (currentMessage != null && !currentMessage.isEmpty()) {
                        // Check if the message contains the player name with color reset
                        String playerNameWithReset = "&r" + player.getName();
                        int playerNameIndex = currentMessage.indexOf(playerNameWithReset);
                        
                        if (playerNameIndex != -1) {
                            // Extract suffix after player name
                            suffix = currentMessage.substring(playerNameIndex + playerNameWithReset.length());
                        }
                    }
                    
                    // Combine prefix and suffix into a complete message
                    String completeMessage = prefix.toString() + "&r" + player.getName() + suffix;
                    
                    // Store the complete message in the database
                    plugin.getDatabaseManager().setJoinMessage(player.getUniqueId().toString(), player.getName(), completeMessage);
                    messageManager.sendMessage(player, "join-prefix-set");
                });
            });
            
            return true;
        }

        if (args[0].equalsIgnoreCase("suffix")) {
            // Set suffix in prefix-suffix mode
            if (!plugin.getConfigManager().isPrefixSuffixMode()) {
                messageManager.sendMessage(sender, "not-prefix-suffix-mode");
                return true;
            }

            if (!(sender instanceof Player)) {
                messageManager.sendMessage(sender, "console-not-allowed");
                return true;
            }

            if (args.length < 2) {
                messageManager.sendMessage(sender, "suffix-usage");
                return true;
            }

            Player player = (Player) sender;
            StringBuilder suffix = new StringBuilder();
            for (int i = 1; i < args.length; i++) {
                if (i > 1) suffix.append(" ");
                suffix.append(args[i]);
            }
            
            // Check suffix length limit
            int suffixLengthLimit = plugin.getConfigManager().getSuffixLengthLimit();
            if (MessageLengthUtil.exceedsLengthLimit(suffix.toString(), suffixLengthLimit)) {
                messageManager.sendMessage(player, "suffix-too-long", "%limit%", String.valueOf(suffixLengthLimit));
                return true;
            }
            
            // Check color permissions and process message
            String processedSuffix = PermissionUtils.processMessageColors(player, suffix.toString());
            if (processedSuffix.isEmpty() && !suffix.toString().isEmpty()) {
                messageManager.sendMessage(player, "no-color-permission");
                return true;
            }

            // Get current message to extract prefix
            CompletableFuture<String> futureMessage = plugin.getDatabaseManager().getJoinMessage(player.getUniqueId().toString());
            
            futureMessage.thenAccept(currentMessage -> {
                SchedulerUtils.runTask(plugin, () -> {
                    String prefix = "";
                    
                    // If there's already a message, try to extract the prefix
                    if (currentMessage != null && !currentMessage.isEmpty()) {
                        // Check if the message contains the player name with color reset
                        String playerNameWithReset = "&r" + player.getName();
                        int playerNameIndex = currentMessage.indexOf(playerNameWithReset);
                        
                        if (playerNameIndex != -1) {
                            // Extract prefix before player name
                            prefix = currentMessage.substring(0, playerNameIndex);
                        }
                    }
                    
                    // Combine prefix and suffix into a complete message
                    String completeMessage = prefix + "&r" + player.getName() + processedSuffix;
                    
                    // Store the complete message in the database
                    plugin.getDatabaseManager().setJoinMessage(player.getUniqueId().toString(), player.getName(), completeMessage);
                    messageManager.sendMessage(player, "join-suffix-set");
                });
            });
            
            return true;
        }

        if (args.length >= 2) {
            if (args[0].equalsIgnoreCase("help")) {
                // Show help
                sendHelp(sender);
                return true;
            }

            // Admin setting another player's message
            if (PermissionUtils.isAdmin(sender)) {
                if (args.length == 2 && args[1].equalsIgnoreCase("reset")) {
                    // Reset another player's message
                    Player target = Bukkit.getPlayer(args[0]);
                    if (target == null) {
                        messageManager.sendMessage(sender, "player-not-found", "%player%", args[0]);
                        return true;
                    }

                    plugin.getDatabaseManager().removeJoinMessage(target.getUniqueId().toString());
                    messageManager.sendMessage(sender, "player-message-cleared", "%player%", target.getName());
                    return true;
                }

                 // Set another player's message
                  Player target = Bukkit.getPlayer(args[0]);
                  if (target == null) {
                      messageManager.sendMessage(sender, "player-not-found", "%player%", args[0]);
                      return true;
                  }

                  StringBuilder message = new StringBuilder();
                  for (int i = 1; i < args.length; i++) {
                      if (i > 1) message.append(" ");
                      message.append(args[i]);
                  }

                  plugin.getDatabaseManager().setJoinMessage(target.getUniqueId().toString(), target.getName(), message.toString());
                  messageManager.sendMessage(sender, "player-message-set", "%player%", target.getName());
                  return true;
            }
        }

        // Set own join message
        if (!(sender instanceof Player)) {
            messageManager.sendMessage(sender, "console-not-allowed");
            return true;
        }

        Player player = (Player) sender;
        StringBuilder message = new StringBuilder();
        for (String arg : args) {
            if (message.length() > 0) message.append(" ");
            message.append(arg);
        }
        
        // Check full message length limit
        int fullModeLengthLimit = plugin.getConfigManager().getFullModeLengthLimit();
        if (MessageLengthUtil.exceedsLengthLimit(message.toString(), fullModeLengthLimit)) {
            messageManager.sendMessage(player, "message-too-long", "%limit%", String.valueOf(fullModeLengthLimit));
            return true;
        }
        
        // Check color permissions and process message
        String processedMessage = PermissionUtils.processMessageColors(player, message.toString());
        if (processedMessage.isEmpty() && !message.toString().isEmpty()) {
            messageManager.sendMessage(player, "no-color-permission");
            return true;
        }

        plugin.getDatabaseManager().setJoinMessage(player.getUniqueId().toString(), player.getName(), processedMessage);
        messageManager.sendMessage(player, "join-message-set");
        return true;
    }

    private void sendHelp(CommandSender sender) {
        // Send help header
        messageManager.sendMessage(sender, "help-header");
        
        // Send current permission level
        String permissionLevel = PermissionUtils.getPermissionLevel(sender);
        messageManager.sendMessage(sender, "help-current-permission", "%level%", permissionLevel);
        
        // Get current mode
        String mode = plugin.getConfigManager().getMessageMode();
        
        // Send mode-specific help
        if ("prefix-suffix".equals(mode)) {
            messageManager.sendMessage(sender, "help-mode-info");
            messageManager.sendMessage(sender, "help-prefix-usage");
            messageManager.sendMessage(sender, "help-suffix-usage");
            messageManager.sendMessage(sender, "help-prefix-suffix-note");
        } else {
            messageManager.sendMessage(sender, "help-message-usage");
            messageManager.sendMessage(sender, "help-view-message");
            messageManager.sendMessage(sender, "help-reset-message");
            messageManager.sendMessage(sender, "help-message-note");
        }
        
        // Send color permission info
        if (PermissionUtils.canUseColors(sender)) {
            messageManager.sendMessage(sender, "help-color-permission");
        } else {
            messageManager.sendMessage(sender, "help-no-color-permission");
        }
        
        // Send admin commands if sender is admin
        if (PermissionUtils.isAdmin(sender)) {
            if ("prefix-suffix".equals(mode)) {
                messageManager.sendMessage(sender, "help-admin-set-message");
                messageManager.sendMessage(sender, "help-admin-reset-message");
            } else {
                messageManager.sendMessage(sender, "help-admin-set-message");
                messageManager.sendMessage(sender, "help-admin-reset-message");
            }
            messageManager.sendMessage(sender, "help-admin-reload");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            if (sender.hasPermission("customjoinmessage.use")) {
                if (plugin.getConfigManager().isPrefixSuffixMode()) {
                    completions.add("prefix");
                    completions.add("suffix");
                } else {
                    completions.add("reset");
                }
                completions.add("help");
            }

            if (PermissionUtils.isAdmin(sender)) {
                // Add online player names
                for (Player player : Bukkit.getOnlinePlayers()) {
                    completions.add(player.getName());
                }
            }
        } else if (args.length == 2) {
            // If the first argument is a player name and sender has admin permission
            if (PermissionUtils.isAdmin(sender) && Bukkit.getPlayer(args[0]) != null) {
                completions.add("reset");
            }
        }

        // Filter completions based on what the user has typed
        List<String> filtered = new ArrayList<>();
        String current = args[args.length - 1].toLowerCase();

        for (String completion : completions) {
            if (completion.toLowerCase().startsWith(current)) {
                filtered.add(completion);
            }
        }

        return filtered;
    }
}