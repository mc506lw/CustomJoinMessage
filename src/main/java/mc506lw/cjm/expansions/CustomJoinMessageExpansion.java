package mc506lw.cjm.expansions;

import mc506lw.cjm.CustomJoinMessage;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * PlaceholderAPI expansion for CustomJoinMessage
 * CustomJoinMessage的PlaceholderAPI扩展
 */
public class CustomJoinMessageExpansion extends PlaceholderExpansion {
    
    private final CustomJoinMessage plugin;
    private final Map<String, String> cache = new HashMap<>();
    private final long CACHE_EXPIRY = 60000; // 60秒缓存
    private final Map<String, Long> cacheTimestamps = new HashMap<>();
    
    public CustomJoinMessageExpansion(CustomJoinMessage plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public @NotNull String getIdentifier() {
        return "cjm";
    }
    
    @Override
    public @NotNull String getAuthor() {
        return String.join(", ", plugin.getDescription().getAuthors());
    }
    
    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }
    
    @Override
    public boolean persist() {
        return true;
    }
    
    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) {
            return "";
        }
        
        String playerName = player.getName();
        String cacheKey = playerName + "_" + params;
        
        // 检查缓存是否过期
        Long timestamp = cacheTimestamps.get(cacheKey);
        if (timestamp != null && System.currentTimeMillis() - timestamp < CACHE_EXPIRY) {
            return cache.getOrDefault(cacheKey, "");
        }
        
        // 使用CompletableFuture处理异步操作
        try {
            String result = null;
            switch (params.toLowerCase()) {
                case "join_message":
                    // 使用同步方法获取结果，避免阻塞主线程
                    result = getJoinMessageSync(playerName);
                    break;
                case "join_prefix":
                    result = getJoinPrefixSync(playerName);
                    break;
                case "join_suffix":
                    result = getJoinSuffixSync(playerName);
                    break;
                default:
                    return null;
            }
            
            // 更新缓存
            cache.put(cacheKey, result != null ? result : "");
            cacheTimestamps.put(cacheKey, System.currentTimeMillis());
            
            return result;
        } catch (Exception e) {
            plugin.getLogger().warning("Error retrieving placeholder data: " + e.getMessage());
            return "";
        }
    }
    
    /**
     * 同步获取加入消息
     * @param playerName 玩家名
     * @return 加入消息
     */
    private String getJoinMessageSync(String playerName) {
        try {
            CompletableFuture<String> future = plugin.getDatabaseManager().getJoinMessage(playerName);
            return future.get(); // 等待异步操作完成
        } catch (Exception e) {
            plugin.getLogger().warning("Error getting join message: " + e.getMessage());
            return "";
        }
    }
    
    /**
     * 同步获取加入前缀
     * @param playerName 玩家名
     * @return 加入前缀
     */
    private String getJoinPrefixSync(String playerName) {
        try {
            CompletableFuture<String> future = plugin.getDatabaseManager().getJoinPrefix(playerName);
            return future.get(); // 等待异步操作完成
        } catch (Exception e) {
            plugin.getLogger().warning("Error getting join prefix: " + e.getMessage());
            return "";
        }
    }
    
    /**
     * 同步获取加入后缀
     * @param playerName 玩家名
     * @return 加入后缀
     */
    private String getJoinSuffixSync(String playerName) {
        try {
            CompletableFuture<String> future = plugin.getDatabaseManager().getJoinSuffix(playerName);
            return future.get(); // 等待异步操作完成
        } catch (Exception e) {
            plugin.getLogger().warning("Error getting join suffix: " + e.getMessage());
            return "";
        }
    }
}