package mc506lw.cjm.utils;

import mc506lw.cjm.CustomJoinMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * 权限工具类，用于检查玩家是否有特定权限
 */
public class PermissionUtils {
    
    /**
     * 获取预设权限组的优先级
     * @param permissionName 权限名称
     * @return 优先级，如果未找到则返回0
     */
    public static int getPredefinedPermissionPriority(String permissionName) {
        CustomJoinMessage plugin = CustomJoinMessage.getInstance();
        ConfigurationSection predefinedSection = plugin.getConfigManager().getConfig()
                .getConfigurationSection("predefined-permissions");
        
        if (predefinedSection == null) {
            return 0;
        }
        
        for (String key : predefinedSection.getKeys(false)) {
            String permission = predefinedSection.getString(key + ".permission");
            if (permission != null && permission.equals(permissionName)) {
                return predefinedSection.getInt(key + ".priority", 0);
            }
        }
        
        return 0;
    }
    
    /**
     * 检查玩家是否可以使用颜色
     * @param sender 命令发送者
     * @return 如果可以使用颜色返回true，否则返回false
     */
    public static boolean canUseColors(CommandSender sender) {
        return sender.hasPermission("customjoinmessage.use.color");
    }
    
    /**
     * 检查玩家是否可以设置无颜色的消息
     * @param sender 命令发送者
     * @return 如果可以设置无颜色的消息返回true，否则返回false
     */
    public static boolean canUseNoColors(CommandSender sender) {
        return sender.hasPermission("customjoinmessage.use.nocolor");
    }
    
    /**
     * 检查玩家是否可以使用基本的消息功能
     * @param sender 命令发送者
     * @return 如果可以使用基本功能返回true，否则返回false
     */
    public static boolean canUseBasic(CommandSender sender) {
        return sender.hasPermission("customjoinmessage.use");
    }
    
    /**
     * 检查玩家是否有管理员权限
     * @param sender 命令发送者
     * @return 如果有管理员权限返回true，否则返回false
     */
    public static boolean isAdmin(CommandSender sender) {
        return sender.hasPermission("customjoinmessage.admin");
    }
    
    /**
     * 获取玩家的最高优先级权限组（包括自定义权限组和预设权限组）
     * @param player 玩家
     * @return 权限组名称，如果没有权限组则返回null
     */
    public static String getHighestPriorityPermissionGroup(Player player) {
        CustomJoinMessage plugin = CustomJoinMessage.getInstance();
        ConfigurationSection groupsSection = plugin.getConfigManager().getConfig()
                .getConfigurationSection("permission-groups");
        
        String highestGroup = null;
        int highestPriority = -1;
        
        // 检查所有自定义权限组
        if (groupsSection != null) {
            for (String groupName : groupsSection.getKeys(false)) {
                String permission = "customjoinmessage.use." + groupName;
                
                if (player.hasPermission(permission)) {
                    int priority = groupsSection.getInt(groupName + ".priority", 0);
                    
                    if (priority > highestPriority) {
                        highestPriority = priority;
                        highestGroup = groupName;
                    }
                }
            }
        }
        
        // 检查预设权限组
        ConfigurationSection predefinedSection = plugin.getConfigManager().getConfig()
                .getConfigurationSection("predefined-permissions");
        
        if (predefinedSection != null) {
            for (String key : predefinedSection.getKeys(false)) {
                String permission = predefinedSection.getString(key + ".permission");
                
                if (permission != null && player.hasPermission(permission)) {
                    int priority = predefinedSection.getInt(key + ".priority", 0);
                    
                    if (priority > highestPriority) {
                        highestPriority = priority;
                        highestGroup = key;
                    }
                }
            }
        }
        
        return highestGroup;
    }
    
    /**
     * 获取权限组的加入消息（完整模式）
     * @param groupName 权限组名称
     * @return 加入消息，如果组不存在则返回null
     */
    public static String getGroupJoinMessage(String groupName) {
        CustomJoinMessage plugin = CustomJoinMessage.getInstance();
        return plugin.getConfigManager().getConfig()
                .getString("permission-groups." + groupName + ".join-message");
    }
    
    /**
     * 获取权限组的加入前缀（前后缀模式）
     * @param groupName 权限组名称
     * @return 加入前缀，如果组不存在则返回null
     */
    public static String getGroupJoinPrefix(String groupName) {
        CustomJoinMessage plugin = CustomJoinMessage.getInstance();
        return plugin.getConfigManager().getConfig()
                .getString("permission-groups." + groupName + ".join-prefix");
    }
    
    /**
     * 获取权限组的加入后缀（前后缀模式）
     * @param groupName 权限组名称
     * @return 加入后缀，如果组不存在则返回null
     */
    public static String getGroupJoinSuffix(String groupName) {
        CustomJoinMessage plugin = CustomJoinMessage.getInstance();
        return plugin.getConfigManager().getConfig()
                .getString("permission-groups." + groupName + ".join-suffix");
    }
    
    /**
     * 根据权限处理消息中的颜色代码
     * @param sender 命令发送者
     * @param message 原始消息
     * @return 处理后的消息
     */
    public static String processMessageColors(CommandSender sender, String message) {
        if (canUseColors(sender)) {
            // 如果有颜色权限，保留所有颜色代码
            return message;
        } else if (canUseNoColors(sender)) {
            // 如果只有无颜色权限，移除所有颜色代码
            return removeColorCodes(message);
        } else {
            // 如果没有基本权限，返回空字符串或默认消息
            return "";
        }
    }
    
    /**
     * 移除消息中的所有颜色代码
     * @param message 原始消息
     * @return 移除颜色代码后的消息
     */
    private static String removeColorCodes(String message) {
        if (message == null) return null;
        
        // 移除传统颜色代码 (&a, &b等)
        String result = message.replaceAll("&[0-9a-fA-Fk-rK-R]", "");
        
        // 移除hex颜色代码 (&#RRGGBB)
        result = result.replaceAll("&#[0-9a-fA-F]{6}", "");
        
        // 移除ChatColor颜色代码 (§a, §b等)
        result = result.replaceAll("§[0-9a-fA-Fk-rK-R]", "");
        
        return result;
    }
    
    /**
     * 获取玩家的权限级别描述
     * @param sender 命令发送者
     * @return 权限级别描述
     */
    public static String getPermissionLevel(CommandSender sender) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            String customGroup = getHighestPriorityPermissionGroup(player);
            
            if (customGroup != null) {
                // 返回最高优先级权限组名称
                return customGroup;
            }
        }
        
        if (isAdmin(sender)) {
            return "管理员";
        } else if (canUseColors(sender)) {
            return "颜色用户";
        } else if (canUseNoColors(sender)) {
            return "普通用户";
        } else {
            return "无权限";
        }
    }
}