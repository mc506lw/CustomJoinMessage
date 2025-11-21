package mc506lw.cjm.utils;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * 权限工具类，用于检查玩家是否有特定权限
 */
public class PermissionUtils {
    
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