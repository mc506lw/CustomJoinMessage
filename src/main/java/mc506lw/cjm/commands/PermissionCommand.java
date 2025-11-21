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

    public PermissionCommand(CustomJoinMessage plugin) {
        this.plugin = plugin;
        this.messageManager = plugin.getMessageManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!PermissionUtils.isAdmin(sender)) {
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

            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                messageManager.sendMessage(sender, "player-not-found", "%player%", args[1]);
                return true;
            }

            String permissionLevel = PermissionUtils.getPermissionLevel(target);
            boolean canUseColors = PermissionUtils.canUseColors(target);
            boolean canUseNoColors = PermissionUtils.canUseNoColors(target);
            boolean canUseBasic = PermissionUtils.canUseBasic(target);
            boolean isAdmin = PermissionUtils.isAdmin(target);

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