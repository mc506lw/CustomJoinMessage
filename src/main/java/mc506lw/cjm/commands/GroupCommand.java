package mc506lw.cjm.commands;

import mc506lw.cjm.CustomJoinMessage;
import mc506lw.cjm.utils.MessageManager;
import mc506lw.cjm.utils.PermissionUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

public class GroupCommand implements CommandExecutor, TabCompleter {
    private final CustomJoinMessage plugin;
    private final MessageManager messageManager;
    private final PermissionUtils permissionUtils;

    public GroupCommand(CustomJoinMessage plugin) {
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

        if (args[0].equalsIgnoreCase("list")) {
            listGroups(sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("info")) {
            if (args.length < 2) {
                messageManager.sendMessage(sender, "group-info-usage");
                return true;
            }

            String groupName = args[1];
            showGroupInfo(sender, groupName);
            return true;
        }

        messageManager.sendMessage(sender, "unknown-command");
        return true;
    }

    private void sendHelp(CommandSender sender) {
        // Send help header
        messageManager.sendMessage(sender, "group-help-header");
        
        // Send help commands
        messageManager.sendMessage(sender, "group-help-list");
        messageManager.sendMessage(sender, "group-help-info");
        messageManager.sendMessage(sender, "group-help-help");
    }

    private void listGroups(CommandSender sender) {
        ConfigurationSection groupsSection = plugin.getConfigManager().getConfig()
                .getConfigurationSection("permission-groups");
        
        ConfigurationSection predefinedSection = plugin.getConfigManager().getConfig()
                .getConfigurationSection("predefined-permissions");
        
        boolean hasCustomGroups = groupsSection != null && !groupsSection.getKeys(false).isEmpty();
        boolean hasPredefinedGroups = predefinedSection != null && !predefinedSection.getKeys(false).isEmpty();
        
        if (!hasCustomGroups && !hasPredefinedGroups) {
            messageManager.sendMessage(sender, "no-groups-defined");
            return;
        }

        // 列出自定义权限组
        if (hasCustomGroups) {
            messageManager.sendMessage(sender, "groups-list-header");
            
            for (String groupName : groupsSection.getKeys(false)) {
                int priority = groupsSection.getInt(groupName + ".priority", 0);
                String permission = "customjoinmessage.use." + groupName;
                
                // 创建消息字符串并替换占位符
                String message = messageManager.getMessage("groups-list-item")
                        .replace("%group%", groupName)
                        .replace("%priority%", String.valueOf(priority))
                        .replace("%permission%", permission);
                
                messageManager.sendMessage(sender, message);
            }
        }
        
        // 列出预设权限组
        if (hasPredefinedGroups) {
            messageManager.sendMessage(sender, "predefined-groups-list-header");
            
            for (String key : predefinedSection.getKeys(false)) {
                int priority = predefinedSection.getInt(key + ".priority", 0);
                String permission = predefinedSection.getString(key + ".permission", "未知");
                
                // 创建消息字符串并替换占位符
                String message = messageManager.getMessage("predefined-groups-list-item")
                        .replace("%group%", key)
                        .replace("%priority%", String.valueOf(priority))
                        .replace("%permission%", permission);
                
                messageManager.sendMessage(sender, message);
            }
        }
    }

    private void showGroupInfo(CommandSender sender, String groupName) {
        ConfigurationSection groupsSection = plugin.getConfigManager().getConfig()
                .getConfigurationSection("permission-groups");
        
        ConfigurationSection predefinedSection = plugin.getConfigManager().getConfig()
                .getConfigurationSection("predefined-permissions");
        
        boolean isCustomGroup = groupsSection != null && groupsSection.contains(groupName);
        boolean isPredefinedGroup = predefinedSection != null && predefinedSection.contains(groupName);
        
        if (!isCustomGroup && !isPredefinedGroup) {
            messageManager.sendMessage(sender, "group-not-found", "%group%", groupName);
            return;
        }

        if (isCustomGroup) {
            // 显示自定义权限组信息
            String permission = "customjoinmessage.use." + groupName;
            int priority = groupsSection.getInt(groupName + ".priority", 0);
            String joinMessage = groupsSection.getString(groupName + ".join-message", "未定义");
            String joinPrefix = groupsSection.getString(groupName + ".join-prefix", "未定义");
            String joinSuffix = groupsSection.getString(groupName + ".join-suffix", "未定义");

            messageManager.sendMessage(sender, "group-info-header", "%group%", groupName);
            messageManager.sendMessage(sender, "group-info-permission", "%permission%", permission);
            messageManager.sendMessage(sender, "group-info-priority", "%priority%", String.valueOf(priority));
            
            if (plugin.getConfigManager().isFullMode()) {
                messageManager.sendMessage(sender, "group-info-join-message", "%message%", joinMessage);
            } else {
                messageManager.sendMessage(sender, "group-info-join-prefix", "%prefix%", joinPrefix);
                messageManager.sendMessage(sender, "group-info-join-suffix", "%suffix%", joinSuffix);
            }
        } else {
            // 显示预设权限组信息
            String permission = predefinedSection.getString(groupName + ".permission", "未知");
            int priority = predefinedSection.getInt(groupName + ".priority", 0);
            
            messageManager.sendMessage(sender, "predefined-group-info-header", "%group%", groupName);
            messageManager.sendMessage(sender, "group-info-permission", "%permission%", permission);
            messageManager.sendMessage(sender, "group-info-priority", "%priority%", String.valueOf(priority));
            messageManager.sendMessage(sender, "predefined-group-note", "%group%", groupName);
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("list");
            completions.add("info");
            completions.add("help");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("info")) {
            // Add custom group names
            ConfigurationSection groupsSection = plugin.getConfigManager().getConfig()
                    .getConfigurationSection("permission-groups");
            
            if (groupsSection != null) {
                completions.addAll(groupsSection.getKeys(false));
            }
            
            // Add predefined group names
            ConfigurationSection predefinedSection = plugin.getConfigManager().getConfig()
                    .getConfigurationSection("predefined-permissions");
            
            if (predefinedSection != null) {
                completions.addAll(predefinedSection.getKeys(false));
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