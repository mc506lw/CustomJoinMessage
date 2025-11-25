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
    private final CustomJoinMessage plugin;
    
    /**
     * 构造函数
     * @param plugin 插件实例
     */
    public PermissionUtils(CustomJoinMessage plugin) {
        this.plugin = plugin;
    }
    
    /**
     * 获取预设权限组的优先级
     * @param permissionName 权限名称
     * @return 优先级，如果未找到则返回0
     */
    public int getPredefinedPermissionPriority(String permissionName) {
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
     * 检查玩家是否可以使用加入消息颜色
     * @param sender 命令发送者
     * @return 如果可以使用颜色返回true，否则返回false
     */
    public boolean canUseJoinColors(CommandSender sender) {
        return sender.hasPermission("customjoinmessage.join.use.color");
    }
    
    /**
     * 检查玩家是否可以使用退出消息颜色
     * @param sender 命令发送者
     * @return 如果可以使用颜色返回true，否则返回false
     */
    public boolean canUseQuitColors(CommandSender sender) {
        return sender.hasPermission("customjoinmessage.quit.use.color");
    }
    
    /**
     * 检查玩家是否可以设置无颜色的加入消息
     * @param sender 命令发送者
     * @return 如果可以设置无颜色的消息返回true，否则返回false
     */
    public boolean canUseJoinNoColors(CommandSender sender) {
        return sender.hasPermission("customjoinmessage.join.use.nocolor");
    }
    
    /**
     * 检查玩家是否可以设置无颜色的退出消息
     * @param sender 命令发送者
     * @return 如果可以设置无颜色的消息返回true，否则返回false
     */
    public boolean canUseQuitNoColors(CommandSender sender) {
        return sender.hasPermission("customjoinmessage.quit.use.nocolor");
    }
    
    /**
     * 检查玩家是否可以使用基本的加入消息功能
     * @param sender 命令发送者
     * @return 如果可以使用基本功能返回true，否则返回false
     */
    public boolean canUseJoinBasic(CommandSender sender) {
        return sender.hasPermission("customjoinmessage.join.use");
    }
    
    /**
     * 检查玩家是否可以使用基本的退出消息功能
     * @param sender 命令发送者
     * @return 如果可以使用基本功能返回true，否则返回false
     */
    public boolean canUseQuitBasic(CommandSender sender) {
        return sender.hasPermission("customjoinmessage.quit.use");
    }
    
    /**
     * 检查玩家是否可以使用颜色（兼容旧方法）
     * @param sender 命令发送者
     * @return 如果可以使用颜色返回true，否则返回false
     */
    public boolean canUseColors(CommandSender sender) {
        return canUseJoinColors(sender) || canUseQuitColors(sender);
    }
    
    /**
     * 检查玩家是否可以设置无颜色的消息（兼容旧方法）
     * @param sender 命令发送者
     * @return 如果可以设置无颜色的消息返回true，否则返回false
     */
    public boolean canUseNoColors(CommandSender sender) {
        return canUseJoinNoColors(sender) || canUseQuitNoColors(sender);
    }
    
    /**
     * 检查玩家是否可以使用基本的消息功能（兼容旧方法）
     * @param sender 命令发送者
     * @return 如果可以使用基本功能返回true，否则返回false
     */
    public boolean canUseBasic(CommandSender sender) {
        return canUseJoinBasic(sender) || canUseQuitBasic(sender);
    }
    
    /**
     * 检查玩家是否有管理员权限
     * @param sender 命令发送者
     * @return 如果有管理员权限返回true，否则返回false
     */
    public boolean isAdmin(CommandSender sender) {
        return sender.hasPermission("customjoinmessage.admin");
    }
    
    /**
     * 获取玩家的最高优先级权限组（包括自定义权限组和预设权限组）
     * @param player 玩家
     * @param messageType 消息类型，"join"或"quit"
     * @return 权限组名称，如果没有权限组则返回null
     */
    public String getHighestPriorityPermissionGroup(Player player, String messageType) {
        ConfigurationSection groupsSection = plugin.getConfigManager().getConfig()
                .getConfigurationSection("permission-groups");
        
        String highestGroup = null;
        int highestPriority = -1;
        
        // 检查所有自定义权限组
        if (groupsSection != null) {
            for (String groupName : groupsSection.getKeys(false)) {
                // 检查新格式权限：customjoinmessage.join.vip 或 customjoinmessage.quit.vip
                String permission = "customjoinmessage." + messageType + "." + groupName;
                
                // 检查旧格式权限：customjoinmessage.use.vip
                String oldPermission = "customjoinmessage.use." + groupName;
                
                // 检查通用权限：customjoinmessage.vip
                String generalPermission = "customjoinmessage." + groupName;
                
                if (player.hasPermission(permission) || player.hasPermission(oldPermission) || player.hasPermission(generalPermission)) {
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
     * 获取玩家的最高优先级权限组（包括自定义权限组和预设权限组）
     * @param player 玩家
     * @return 权限组名称，如果没有权限组则返回null
     */
    public String getHighestPriorityPermissionGroup(Player player) {
        // 为了向后兼容，默认返回加入消息的权限组
        return getHighestPriorityPermissionGroup(player, "join");
    }
    
    /**
     * 获取权限组的加入消息（完整模式）
     * @param groupName 权限组名称
     * @return 加入消息，如果组不存在则返回null
     */
    public String getGroupJoinMessage(String groupName) {
        return plugin.getConfigManager().getConfig()
                .getString("permission-groups." + groupName + ".join-message");
    }
    
    /**
     * 获取权限组的加入前缀（前后缀模式）
     * @param groupName 权限组名称
     * @return 加入前缀，如果组不存在则返回null
     */
    public String getGroupJoinPrefix(String groupName) {
        return plugin.getConfigManager().getConfig()
                .getString("permission-groups." + groupName + ".join-prefix");
    }
    
    /**
     * 获取权限组的加入后缀（前后缀模式）
     * @param groupName 权限组名称
     * @return 加入后缀，如果组不存在则返回null
     */
    public String getGroupJoinSuffix(String groupName) {
        return plugin.getConfigManager().getConfig()
                .getString("permission-groups." + groupName + ".join-suffix");
    }
    
    /**
     * 获取权限组的退出消息（完整模式）
     * @param groupName 权限组名称
     * @return 退出消息，如果组不存在则返回null
     */
    public String getGroupQuitMessage(String groupName) {
        return plugin.getConfigManager().getConfig()
                .getString("permission-groups." + groupName + ".quit-message");
    }
    
    /**
     * 获取权限组的退出前缀（前后缀模式）
     * @param groupName 权限组名称
     * @return 退出前缀，如果组不存在则返回null
     */
    public String getGroupQuitPrefix(String groupName) {
        return plugin.getConfigManager().getConfig()
                .getString("permission-groups." + groupName + ".quit-prefix");
    }
    
    /**
     * 获取权限组的退出后缀（前后缀模式）
     * @param groupName 权限组名称
     * @return 退出后缀，如果组不存在则返回null
     */
    public String getGroupQuitSuffix(String groupName) {
        return plugin.getConfigManager().getConfig()
                .getString("permission-groups." + groupName + ".quit-suffix");
    }
    
    /**
     * 根据权限处理加入消息中的颜色代码
     * @param sender 命令发送者
     * @param message 原始消息
     * @return 处理后的消息
     */
    public String processJoinMessageColors(CommandSender sender, String message) {
        if (canUseJoinColors(sender)) {
            // 如果有加入消息颜色权限，处理所有颜色代码
            return processColorCodes(message);
        } else if (canUseJoinNoColors(sender)) {
            // 如果只有加入消息无颜色权限，移除所有颜色代码
            return removeColorCodes(message);
        } else {
            // 如果没有基本权限，返回空字符串或默认消息
            return "";
        }
    }
    
    /**
     * 根据权限处理退出消息中的颜色代码
     * @param sender 命令发送者
     * @param message 原始消息
     * @return 处理后的消息
     */
    public String processQuitMessageColors(CommandSender sender, String message) {
        if (canUseQuitColors(sender)) {
            // 如果有退出消息颜色权限，处理所有颜色代码
            return processColorCodes(message);
        } else if (canUseQuitNoColors(sender)) {
            // 如果只有退出消息无颜色权限，移除所有颜色代码
            return removeColorCodes(message);
        } else {
            // 如果没有基本权限，返回空字符串或默认消息
            return "";
        }
    }
    
    /**
     * 根据权限处理消息中的颜色代码（兼容旧方法）
     * @param sender 命令发送者
     * @param message 原始消息
     * @return 处理后的消息
     */
    public String processMessageColors(CommandSender sender, String message) {
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
     * 处理消息中的颜色代码，支持标准和RGB格式
     * @param message 要处理的消息
     * @return 处理后的消息
     */
    private String processColorCodes(String message) {
        if (message == null) return "";
        
        // Support for RGB colors (1.16+) - &#RRGGBB format
        message = message.replaceAll("&#([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])", "§x§$1§$2§$3§$4§$5§$6");
        
        // Translate standard color codes (&a, &b, etc.) and &x&r&r&g&g&b&b RGB format
        message = org.bukkit.ChatColor.translateAlternateColorCodes('&', message);
        
        return message;
    }
    
    /**
     * 移除消息中的所有颜色代码
     * @param message 原始消息
     * @return 移除颜色代码后的消息
     */
    private String removeColorCodes(String message) {
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
    public String getPermissionLevel(CommandSender sender) {
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
    
    /**
     * 获取离线玩家的权限级别描述
     * @param playerName 玩家名称
     * @return 权限级别描述
     */
    public String getOfflinePermissionLevel(String playerName) {
        // 检查是否有管理员权限
        if (hasOfflinePermission(playerName, "customjoinmessage.admin")) {
            return "管理员";
        }
        
        // 检查是否有颜色权限
        if (hasOfflinePermission(playerName, "customjoinmessage.join.use.color") || 
            hasOfflinePermission(playerName, "customjoinmessage.quit.use.color")) {
            return "颜色用户";
        }
        
        // 检查是否有基本权限
        if (hasOfflinePermission(playerName, "customjoinmessage.join.use") || 
            hasOfflinePermission(playerName, "customjoinmessage.quit.use")) {
            return "普通用户";
        }
        
        // 检查自定义权限组
        ConfigurationSection groupsSection = plugin.getConfigManager().getConfig()
                .getConfigurationSection("permission-groups");
        
        if (groupsSection != null) {
            String highestGroup = null;
            int highestPriority = -1;
            
            for (String groupName : groupsSection.getKeys(false)) {
                // 检查新格式权限：customjoinmessage.join.vip 或 customjoinmessage.quit.vip
                String permission = "customjoinmessage.join." + groupName;
                String quitPermission = "customjoinmessage.quit." + groupName;
                
                // 检查旧格式权限：customjoinmessage.use.vip
                String oldPermission = "customjoinmessage.use." + groupName;
                
                // 检查通用权限：customjoinmessage.vip
                String generalPermission = "customjoinmessage." + groupName;
                
                if (hasOfflinePermission(playerName, permission) || 
                    hasOfflinePermission(playerName, quitPermission) || 
                    hasOfflinePermission(playerName, oldPermission) || 
                    hasOfflinePermission(playerName, generalPermission)) {
                    int priority = groupsSection.getInt(groupName + ".priority", 0);
                    
                    if (priority > highestPriority) {
                        highestPriority = priority;
                        highestGroup = groupName;
                    }
                }
            }
            
            if (highestGroup != null) {
                return highestGroup;
            }
        }
        
        return "无权限";
    }
    
    /**
     * 检查离线玩家是否有特定权限
     * @param playerName 玩家名称
     * @param permission 权限节点
     * @return 如果有权限返回true，否则返回false
     */
    public boolean hasOfflinePermission(String playerName, String permission) {
        try {
            // 使用Bukkit的权限系统检查离线玩家权限
            org.bukkit.OfflinePlayer offlinePlayer = org.bukkit.Bukkit.getOfflinePlayer(playerName);
            if (offlinePlayer != null) {
                // 如果玩家是OP且有管理员权限，返回true
                if (offlinePlayer.isOp() && permission.equals("customjoinmessage.admin")) {
                    return true;
                }
                
                // 检查权限是否在插件管理器中注册
                if (org.bukkit.Bukkit.getPluginManager().getPermission(permission) == null) {
                    return false;
                }
                
                // 对于离线玩家，我们无法直接检查权限，但可以通过数据库或其他方式
                // 这里使用一个简化的方法：如果玩家是OP，则假设拥有所有权限
                if (offlinePlayer.isOp()) {
                    return true;
                }
                
                // 如果权限是基本权限，假设所有玩家都有
                if (permission.equals("customjoinmessage.join.use") || 
                    permission.equals("customjoinmessage.quit.use")) {
                    return true;
                }
                
                // 对于其他权限，默认返回false
                return false;
            }
        } catch (Exception e) {
            // 如果检查失败，返回false
        }
        
        // 如果无法检查权限，返回false
        return false;
    }
}