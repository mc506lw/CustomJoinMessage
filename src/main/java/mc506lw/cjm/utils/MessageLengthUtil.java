package mc506lw.cjm.utils;

import org.bukkit.ChatColor;

import java.util.regex.Pattern;

/**
 * 工具类，用于处理消息长度计算（不包含颜色代码）
 */
public class MessageLengthUtil {
    
    /**
     * 计算字符串中非颜色代码的字符数
     * @param text 要计算的文本
     * @return 非颜色代码的字符数
     */
    public static int getLengthWithoutColorCodes(String text) {
        if (text == null) {
            return 0;
        }
        
        // 移除所有颜色代码（&a, &b, &c...以及&l, &m, &n, &o, &r, &#RRGGBB, &#RGB）
        String strippedText = text.replaceAll("(?i)&([0-9a-fk-or])|&#([0-9a-f]{6})|&#([0-9a-f]{3})", "");
        
        // 返回移除颜色代码后的字符数
        return strippedText.length();
    }
    
    /**
     * 检查消息是否超过长度限制
     * @param text 要检查的文本
     * @param maxLength 最大长度限制
     * @return 如果超过限制返回true，否则返回false
     */
    public static boolean exceedsLengthLimit(String text, int maxLength) {
        if (maxLength < 0) {
            // 如果限制为-1，表示无限制
            return false;
        }
        
        return getLengthWithoutColorCodes(text) > maxLength;
    }
    
    /**
     * 检查字符串是否包含颜色代码
     * @param text 要检查的文本
     * @return 如果包含颜色代码返回true，否则返回false
     */
    public static boolean containsColorCodes(String text) {
        if (text == null) {
            return false;
        }
        
        // 检查是否包含标准颜色代码 (&a, &b, &c...以及&l, &m, &n, &o, &r)
        if (text.matches("(?i).*&([0-9a-fk-or]).*")) {
            return true;
        }
        
        // 检查是否包含十六进制颜色代码 (&#RRGGBB, &#RGB)
        if (text.matches("(?i).*&#[0-9a-f]{3}.*") || text.matches("(?i).*&#[0-9a-f]{6}.*")) {
            return true;
        }
        
        // 检查是否包含ChatColor颜色代码 (§a, §b, §c...以及§l, §m, §n, §o, §r)
        if (text.matches("(?i).*§([0-9a-fk-or]).*")) {
            return true;
        }
        
        return false;
    }
    
    /**
     * 截断消息到指定长度（不包含颜色代码）
     * @param text 要截断的文本
     * @param maxLength 最大长度
     * @return 截断后的文本
     */
    public static String truncateToLength(String text, int maxLength) {
        if (text == null || maxLength < 0) {
            return text;
        }
        
        // 如果消息未超过长度限制，直接返回
        if (!exceedsLengthLimit(text, maxLength)) {
            return text;
        }
        
        // 构建结果字符串
        StringBuilder result = new StringBuilder();
        int nonColorCharCount = 0;
        boolean inColorCode = false;
        boolean inHexColorCode = false;
        int hexColorCodeLength = 0;
        
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            
            // 检查颜色代码
            if (c == '&' && i + 1 < text.length()) {
                char nextChar = text.charAt(i + 1);
                if (nextChar == '#') {
                    // 十六进制颜色代码开始
                    inHexColorCode = true;
                    hexColorCodeLength = 0;
                    result.append(c);
                    continue;
                } else if (Pattern.matches("[0-9a-fk-orA-FK-OR]", String.valueOf(nextChar))) {
                    // 标准颜色代码
                    inColorCode = true;
                    result.append(c);
                    continue;
                }
            }
            
            if (inColorCode) {
                result.append(c);
                inColorCode = false;
                continue;
            }
            
            if (inHexColorCode) {
                result.append(c);
                hexColorCodeLength++;
                
                // 十六进制颜色代码格式为 &#RRGGBB 或 &#RGB
                if ((hexColorCodeLength == 3 && i + 1 < text.length() && text.charAt(i + 1) != ' ') || 
                    hexColorCodeLength == 6) {
                    inHexColorCode = false;
                }
                continue;
            }
            
            // 非颜色代码字符
            if (nonColorCharCount < maxLength) {
                result.append(c);
                nonColorCharCount++;
            } else {
                break;
            }
        }
        
        return result.toString();
    }
}