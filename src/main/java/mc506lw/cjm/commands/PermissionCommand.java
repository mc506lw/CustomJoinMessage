package mc506lw.cjm.commands;

import mc506lw.cjm.CustomJoinMessage;
import mc506lw.cjm.utils.MessageManager;
import mc506lw.cjm.utils.PermissionUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PermissionCommand implements CommandExecutor, TabCompleter {
    private final CustomJoinMessage plugin;
    private final MessageManager messageManager;
    private final PermissionUtils permissionUtils;

    public PermissionCommand(CustomJoinMessage plugin) {
        this.plugin = plugin;
        this.messageManager = plugin.getMessageManager();
        this.permissionUtils = plugin.getPermissionUtils();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!permissionUtils.isAdmin(sender)) {
            messageManager.sendMessage(sender, "no-permission");
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelp(sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("check")) {
            if (args.length < 2) {
                messageManager.sendMessage(sender, "permission-check-usage");
                return true;
            }

            String playerName = args[1];
            Player target = Bukkit.getPlayer(playerName);
            
            if (target == null) {
                // Player is offline, check offline permissions asynchronously
                checkOfflinePlayerPermissions(sender, playerName);
                return true;
            }

            // Player is online, check permissions normally
            String permissionLevel = permissionUtils.getPermissionLevel(target);
            boolean canUseColors = permissionUtils.canUseColors(target);
            boolean canUseNoColors = permissionUtils.canUseNoColors(target);
            boolean canUseBasic = permissionUtils.canUseBasic(target);
            boolean isAdmin = permissionUtils.isAdmin(target);

            messageManager.sendMessage(sender, "permission-check-result", "%player%", target.getName());
            messageManager.sendMessage(sender, "permission-level-info", "%level%", permissionLevel);
            messageManager.sendMessage(sender, "permission-color-info", "%status%", canUseColors ? "§a是" : "§c否");
            messageManager.sendMessage(sender, "permission-nocolor-info", "%status%", canUseNoColors ? "§a是" : "§c否");
            messageManager.sendMessage(sender, "permission-basic-info", "%status%", canUseBasic ? "§a是" : "§c否");
            messageManager.sendMessage(sender, "permission-admin-info", "%status%", isAdmin ? "§a是" : "§c否");
            return true;
        }

        messageManager.sendMessage(sender, "unknown-command");
        return true;
    }

    /**
     * 检查离线玩家的权限
     * @param sender 命令发送者
     * @param playerName 玩家名称
     */
    private void checkOfflinePlayerPermissions(CommandSender sender, String playerName) {
        // 通过玩家名获取UUID
        plugin.getDatabaseManager().getPlayerUuid(playerName).thenAccept(uuid -> {
            if (uuid == null) {
                messageManager.sendMessage(sender, "player-not-found", "%player%", playerName);
                return;
            }
            
            // 检查玩家是否存在于数据库中
            plugin.getDatabaseManager().playerExists(uuid).thenAccept(exists -> {
                if (!exists) {
                    messageManager.sendMessage(sender, "player-not-found", "%player%", playerName);
                    return;
                }
                
                // 获取离线玩家的权限信息
                String permissionLevel = "离线玩家";
                boolean canUseColors = false;
                boolean canUseNoColors = false;
                boolean canUseBasic = false;
                boolean isAdmin = false;
                
                // 发送权限信息
                messageManager.sendMessage(sender, "permission-check-result", "%player%", playerName);
                messageManager.sendMessage(sender, "permission-level-info", "%level%", permissionLevel);
                messageManager.sendMessage(sender, "permission-color-info", "%status%", "§7无法检查");
                messageManager.sendMessage(sender, "permission-nocolor-info", "%status%", "§7无法检查");
                messageManager.sendMessage(sender, "permission-basic-info", "%status%", "§7无法检查");
                messageManager.sendMessage(sender, "permission-admin-info", "%status%", "§7无法检查");
            });
        });
    }

    private void sendHelp(CommandSender sender) {
        // Send help header
        messageManager.sendMessage(sender, "permission-help-header");
        
        // Send help commands
        messageManager.sendMessage(sender, "permission-help-check");
        messageManager.sendMessage(sender, "permission-help-help");
        
        // Send permission level descriptions
        messageManager.sendMessage(sender, "permission-levels-header");
        messageManager.sendMessage(sender, "permission-level-admin");
        messageManager.sendMessage(sender, "permission-level-color");
        messageManager.sendMessage(sender, "permission-level-basic");
        messageManager.sendMessage(sender, "permission-level-none");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("check");
            completions.add("help");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("check")) {
            // Add online player names
            for (Player player : Bukkit.getOnlinePlayers()) {
                completions.add(player.getName());
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