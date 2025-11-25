package mc506lw.cjm.commands;

import mc506lw.cjm.CustomJoinMessage;
import mc506lw.cjm.database.DatabaseVersionManager;
import mc506lw.cjm.utils.MessageManager;
import mc506lw.cjm.utils.PermissionUtils;
import mc506lw.cjm.utils.SchedulerUtils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
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
    private final PermissionUtils permissionUtils;

    public CjmCommand(CustomJoinMessage plugin) {
        this.plugin = plugin;
        this.messageManager = plugin.getMessageManager();
        this.permissionCommand = new PermissionCommand(plugin);
        this.groupCommand = new GroupCommand(plugin);
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

        if (args[0].equalsIgnoreCase("resetplayer")) {
            // Reset player data command
            if (args.length < 2) {
                messageManager.sendMessage(sender, "resetplayer-usage");
                return true;
            }

            String playerName = args[1];
            final boolean force = args.length >= 3 && args[2].equalsIgnoreCase("--force");
            
            // 异步执行数据库操作
            plugin.getSchedulerUtils().runTaskAsynchronously(() -> {
                try {
                    DatabaseVersionManager dbVersionManager = new DatabaseVersionManager(plugin, plugin.getDatabaseManager());
                    
                    // 获取玩家UUID
                    String playerUuid = null;
                    try {
                        playerUuid = plugin.getDatabaseManager().getPlayerUuid(playerName).get();
                    } catch (Exception e) {
                        // 获取UUID失败，如果强制模式则继续尝试
                        if (!force) {
                            String errorMsg = e.getMessage() != null ? e.getMessage() : "获取玩家UUID失败";
                            String message = messageManager.getMessage("player-data-reset-failed", "%player%", playerName)
                                    .replace("%error%", errorMsg);
                            sender.sendMessage(message);
                            return;
                        }
                    }
                    
                    // 如果获取UUID失败且是强制模式，尝试直接通过用户名删除
                    if (playerUuid != null) {
                        // 使用CompletableFuture处理异步结果
                        dbVersionManager.resetPlayerData(playerName).thenAccept(success -> {
                            plugin.getSchedulerUtils().runTask(() -> {
                                if (success) {
                                    messageManager.sendMessage(sender, "player-data-reset", "%player%", playerName);
                                } else {
                                    String message = messageManager.getMessage("player-data-reset-failed", "%player%", playerName)
                                            .replace("%error%", "数据库操作失败");
                                    sender.sendMessage(message);
                                }
                            });
                        }).exceptionally(e -> {
                            plugin.getSchedulerUtils().runTask(() -> {
                                String message = messageManager.getMessage("player-data-reset-failed", "%player%", playerName)
                                        .replace("%error%", e.getMessage());
                                sender.sendMessage(message);
                            });
                            return null;
                        });
                    } else if (force) {
                        // 强制模式：尝试直接删除用户名记录
                        boolean success = dbVersionManager.forceResetPlayerDataByName(playerName);
                        plugin.getSchedulerUtils().runTask(() -> {
                            if (success) {
                                messageManager.sendMessage(sender, "player-data-reset", "%player%", playerName);
                            } else {
                                String message = messageManager.getMessage("player-data-reset-failed", "%player%", playerName)
                                        .replace("%error%", "强制重置失败");
                                sender.sendMessage(message);
                            }
                        });
                    } else {
                        // 非强制模式且UUID获取失败
                        String message = messageManager.getMessage("player-data-reset-failed", "%player%", playerName)
                                .replace("%error%", "找不到该玩家的UUID");
                        sender.sendMessage(message);
                    }
                } catch (Exception e) {
                    String message = messageManager.getMessage("player-data-reset-failed", "%player%", playerName)
                            .replace("%error%", e.getMessage());
                    sender.sendMessage(message);
                }
            });
            
            return true;
        }

        if (args[0].equalsIgnoreCase("listplayers")) {
            // List all players command
            plugin.getDatabaseManager().getAllPlayers().thenAccept(players -> {
                plugin.getSchedulerUtils().runTask(() -> {
                    if (players.isEmpty()) {
                        messageManager.sendMessage(sender, "listplayers-empty");
                        return;
                    }
                    
                    // Send header
                    messageManager.sendMessage(sender, "listplayers-header");
                    messageManager.sendMessage(sender, "listplayers-info");
                    sender.sendMessage(" ");
                    
                    for (mc506lw.cjm.database.DatabaseManager.PlayerInfo player : players) {
                        String uuid = player.getUuid();
                        String username = player.getUsername();
                        
                        // 显示玩家信息，包括可点击的删除按钮
                        String playerInfo = messageManager.getMessage("listplayers-format", "%username%", username)
                            .replace("%uuid%", uuid);
                        String deleteButton = messageManager.getMessage("listplayers-delete-button");
                        
                        // 创建可点击的删除命令
                        String deleteCommand = "/cjm deleteplayer " + uuid;
                        String hoverText = messageManager.getMessage("listplayers-delete-hover", "%username%", username);
                        
                        // 发送带有可点击删除按钮的消息
                        sender.spigot().sendMessage(
                            new ComponentBuilder(playerInfo)
                                .append(deleteButton)
                                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, deleteCommand))
                                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hoverText).create()))
                                .create()
                        );
                    }
                    
                    sender.sendMessage(" ");
                    messageManager.sendMessage(sender, "listplayers-footer", "%count%", String.valueOf(players.size()));
                });
            }).exceptionally(e -> {
                plugin.getSchedulerUtils().runTask(() -> {
                    messageManager.sendMessage(sender, "listplayers-failed", "%error%", e.getMessage());
                });
                return null;
            });
            
            return true;
        }

        if (args[0].equalsIgnoreCase("deleteplayer")) {
            // Delete player by UUID command
            if (args.length < 2) {
                messageManager.sendMessage(sender, "deleteplayer-usage");
                return true;
            }
            
            String uuid = args[1];
            
            plugin.getDatabaseManager().deletePlayerByUuid(uuid).thenAccept(success -> {
                plugin.getSchedulerUtils().runTask(() -> {
                    if (success) {
                        messageManager.sendMessage(sender, "deleteplayer-success", "%uuid%", uuid);
                    } else {
                        messageManager.sendMessage(sender, "deleteplayer-failed");
                    }
                });
            }).exceptionally(e -> {
                plugin.getSchedulerUtils().runTask(() -> {
                    messageManager.sendMessage(sender, "deleteplayer-error", "%error%", e.getMessage());
                });
                return null;
            });
            
            return true;
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
        messageManager.sendMessage(sender, "admin-help-resetplayer");
        messageManager.sendMessage(sender, "admin-help-listplayers");
        messageManager.sendMessage(sender, "admin-help-deleteplayer");
        messageManager.sendMessage(sender, "admin-help-help");
        
        // Send config note
        messageManager.sendMessage(sender, "admin-help-config-note");
        
        // Send join message note
        messageManager.sendMessage(sender, "admin-help-join-note");
        
        // Send quit message note
        messageManager.sendMessage(sender, "admin-help-quit-note");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("reload");
            completions.add("permission");
            completions.add("group");
            completions.add("resetplayer");
            completions.add("listplayers");
            completions.add("deleteplayer");
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