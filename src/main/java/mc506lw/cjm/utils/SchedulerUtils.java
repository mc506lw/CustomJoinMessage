package mc506lw.cjm.utils;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * 工具类，用于适配Spigot和Folia的调度系统
 */
public class SchedulerUtils {
    private final Plugin plugin;
    private static boolean isFolia = false;
    private static boolean checked = false;

    public SchedulerUtils(Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * 检查服务器是否为Folia
     */
    private static boolean isFolia() {
        if (!checked) {
            try {
                Class.forName("io.papermc.paper.threadedregions.RegionizedServerInitEvent");
                isFolia = true;
            } catch (ClassNotFoundException e) {
                isFolia = false;
            }
            checked = true;
        }
        return isFolia;
    }

    /**
     * 运行任务（全局）
     */
    public void runTask(Runnable task) {
        if (isFolia()) {
            try {
                // 使用反射调用Folia的GlobalRegionScheduler
                Method getGlobalRegionScheduler = Bukkit.class.getMethod("getGlobalRegionScheduler");
                Object globalRegionScheduler = getGlobalRegionScheduler.invoke(null);
                
                Method run = globalRegionScheduler.getClass().getMethod("run", Plugin.class, Consumer.class);
                run.invoke(globalRegionScheduler, plugin, (Consumer<Object>) scheduledTask -> task.run());
            } catch (Exception e) {
                // 如果反射调用失败，回退到同步执行
                Bukkit.getScheduler().runTask(plugin, task);
            }
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }

    /**
     * 异步运行任务
     */
    public void runTaskAsynchronously(Runnable task) {
        if (isFolia()) {
            try {
                // 使用反射调用Folia的AsyncScheduler
                Method getAsyncScheduler = Bukkit.class.getMethod("getAsyncScheduler");
                Object asyncScheduler = getAsyncScheduler.invoke(null);
                
                Method runNow = asyncScheduler.getClass().getMethod("runNow", Plugin.class, Consumer.class);
                runNow.invoke(asyncScheduler, plugin, (Consumer<Object>) scheduledTask -> task.run());
            } catch (Exception e) {
                // 如果反射调用失败，回退到Spigot的异步调度
                Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
            }
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
        }
    }
}