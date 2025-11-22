package mc506lw.cjm.commands;

import mc506lw.cjm.CustomJoinMessage;
import mc506lw.cjm.utils.MessageManager;
import mc506lw.cjm.utils.PermissionUtils;
import mc506lw.cjm.utils.SchedulerUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CjmCommand implements CommandExecutor, TabCompleter {
    private final CustomJoinMessage plugin;
    private final MessageManager messageManager;
    private final PermissionCommand permissionCommand;
    private final GroupCommand groupCommand;

    public CjmCommand(CustomJoinMessage plugin) {
        this.plugin = plugin;
        this.messageManager = plugin.getMessageManager();
        this.permissionCommand = new PermissionCommand(plugin);
        this.groupCommand = new GroupCommand(plugin);
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

        if (args[0].equalsIgnoreCase("reload")) {
            // Reload configuration and check if it was updated
            boolean configUpdated = plugin.getConfigManager().reloadConfig();
            
            // Check if config was updated
            boolean configFileUpdated = plugin.getConfigManager().getLastBackupFile() != null;
            boolean messagesFileUpdated = plugin.getConfigManager().getLastMessagesBackupFile() != null;
            
            // Send appropriate messages based on what was updated
            if (configFileUpdated) {
                messageManager.sendMessage(sender, "config-updated");
                String backupFile = plugin.getConfigManager().getLastBackupFile();
                if (backupFile != null) {
                    messageManager.sendMessage(sender, "config-backup-created", "%backup_file%", backupFile);
                }
            }
            
            if (messagesFileUpdated) {
                messageManager.sendMessage(sender, "messages-updated");
                String backupFile = plugin.getConfigManager().getLastMessagesBackupFile();
                if (backupFile != null) {
                    messageManager.sendMessage(sender, "messages-backup-created", "%backup_file%", backupFile);
                }
            }
            
            if (!configFileUpdated && !messagesFileUpdated) {
                messageManager.sendMessage(sender, "config-reloaded");
            }
            
            return true;
        }

        if (args[0].equalsIgnoreCase("permission")) {
            // Forward permission subcommand to PermissionCommand
            String[] permissionArgs = new String[args.length - 1];
            System.arraycopy(args, 1, permissionArgs, 0, args.length - 1);
            return permissionCommand.onCommand(sender, command, "permission", permissionArgs);
        }

        if (args[0].equalsIgnoreCase("group")) {
            // Forward group subcommand to GroupCommand
            String[] groupArgs = new String[args.length - 1];
            System.arraycopy(args, 1, groupArgs, 0, args.length - 1);
            return groupCommand.onCommand(sender, command, "group", groupArgs);
        }

        // Unknown command
        messageManager.sendMessage(sender, "unknown-command");
        return true;
    }

    private void sendHelp(CommandSender sender) {
        // Send help header
        messageManager.sendMessage(sender, "admin-help-header");
        
        // Send help commands
        messageManager.sendMessage(sender, "admin-help-reload");
        messageManager.sendMessage(sender, "admin-help-permission");
        messageManager.sendMessage(sender, "admin-help-group");
        messageManager.sendMessage(sender, "admin-help-help");
        
        // Send config note
        messageManager.sendMessage(sender, "admin-help-config-note");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("reload");
            completions.add("permission");
            completions.add("group");
            completions.add("help");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("permission")) {
            // Forward tab completion to PermissionCommand
            String[] permissionArgs = new String[args.length - 1];
            System.arraycopy(args, 1, permissionArgs, 0, args.length - 1);
            return permissionCommand.onTabComplete(sender, command, "permission", permissionArgs);
        } else if (args.length == 2 && args[0].equalsIgnoreCase("group")) {
            // Forward tab completion to GroupCommand
            String[] groupArgs = new String[args.length - 1];
            System.arraycopy(args, 1, groupArgs, 0, args.length - 1);
            return groupCommand.onTabComplete(sender, command, "group", groupArgs);
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