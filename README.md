# CustomJoinMessage

[![Build Status](https://github.com/mc506lw/CustomJoinMessage/workflows/Build%20and%20Release/badge.svg)](https://github.com/mc506lw/CustomJoinMessage/actions)
[![Pull Request Check](https://github.com/mc506lw/CustomJoinMessage/workflows/Pull%20Request%20Check/badge.svg)](https://github.com/mc506lw/CustomJoinMessage/actions)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java Version](https://img.shields.io/badge/java-17+-orange.svg)](https://openjdk.java.net/)
[![Spigot Version](https://img.shields.io/badge/spigot-1.16.5+-green.svg)](https://www.spigotmc.org/)
[![Folia Version](https://img.shields.io/badge/folia-1.20.1+-green.svg)](https://papermc.io/software/folia/)

一个强大的Minecraft Bukkit/Spigot/Folia插件，允许玩家自定义加入消息，支持多种消息模式、权限系统和颜色代码。

## 功能特性

- **自定义加入消息**：玩家可以设置自己的加入消息
- **多种消息模式**：支持简单消息模式和前缀-后缀模式
- **权限系统**：四个权限级别（管理员、颜色用户、普通用户、无权限）
- **颜色支持**：支持标准颜色代码（&a、&b等）和十六进制颜色代码（&#RRGGBB）
- **数据库支持**：使用SQLite数据库存储玩家消息
- **PlaceholderAPI集成**：支持使用PlaceholderAPI的占位符
- **多语言支持**：消息完全可配置，支持自定义语言
- **命令系统**：完整的命令系统，包括权限检查和帮助信息

## 安装方法

1. 下载最新版本的CustomJoinMessage.jar文件
2. 将文件放入服务器的`plugins`目录
3. 重启服务器或使用`/reload`命令加载插件
4. 插件将自动生成配置文件和数据库

## 权限系统

插件使用以下权限节点：

- `cjm.admin`：管理员权限，可以管理其他玩家的消息和重载配置
- `cjm.color`：颜色权限，可以在消息中使用颜色代码
- `cjm.nocolor`：基本权限，可以设置纯文本消息
- `cjm.basic`：基础权限，可以查看当前消息模式

### 权限级别

1. **管理员**（cjm.admin）：拥有所有权限，包括管理其他玩家的消息
2. **颜色用户**（cjm.color）：可以使用颜色代码自定义加入消息
3. **普通用户**（cjm.nocolor）：只能使用纯文本自定义加入消息
4. **无权限**：无法自定义加入消息

## 命令说明

### 玩家命令

- `/setjoin`：查看当前消息模式和你的加入消息
- `/setjoin <消息>`：设置你的加入消息（简单模式）
- `/setjoin reset`：清除你的加入消息
- `/setjoin prefix <前缀>`：设置加入消息前缀（前缀-后缀模式）
- `/setjoin suffix <后缀>`：设置加入消息后缀（前缀-后缀模式）
- `/setjoin help`：显示帮助信息

### 管理员命令

- `/cjm reload`：重载配置文件
- `/cjm permission check <玩家>`：检查玩家的权限级别
- `/cjm permission help`：显示权限命令帮助
- `/cjm help`：显示管理命令帮助
- `/setjoin <玩家> <消息>`：为其他玩家设置加入消息
- `/setjoin <玩家> reset`：清除其他玩家的加入消息

## 配置文件

### config.yml

```yaml
# Message mode: "simple" or "prefix-suffix"
message-mode: "simple"

# Default join message when player hasn't set a custom one
default-join-message: "&e%player% 加入了游戏"

# Maximum message length (excluding color codes)
message-length-limit: 50

# Maximum prefix length (excluding color codes)
prefix-length-limit: 20

# Maximum suffix length (excluding color codes)
suffix-length-limit: 30

# Whether to hide the default join message
hide-default-join-message: false

# PlaceholderAPI support
placeholders:
  enabled: true

# Database settings
database:
  file: "database.db"
```

### messages.yml

所有消息都可以在此文件中自定义，包括：

- 玩家消息（成功、错误、权限等）
- 错误消息（命令使用错误、玩家未找到等）
- 帮助消息（命令帮助、权限说明等）
- 其他消息（当前模式、重载成功等）

## 消息模式

### 简单模式（Simple）

玩家设置一个完整的加入消息，例如：
```
/setjoin &a欢迎 %player% 来到服务器！
```

### 前缀-后缀模式（Prefix-Suffix）

玩家分别设置前缀和后缀，系统会自动组合：
```
/setjoin prefix &a[VIP]
/setjoin suffix &b刚刚加入了游戏！
```
最终显示为：`[VIP] 玩家名 刚刚加入了游戏！`

## 颜色代码支持

插件支持以下颜色代码格式：

- 标准颜色代码：`&a`、`&b`、`&c`等
- 十六进制颜色代码：`&#RRGGBB`，例如`&#FF0000`表示红色
- 格式代码：`&l`（粗体）、`&n`（下划线）、`&o`（斜体）等

## 占位符支持

如果启用了PlaceholderAPI支持，可以在消息中使用任何PlaceholderAPI提供的占位符

## 开发信息

- **版本**：1.0.0
- **Minecraft版本**：
  - Folia：1.20.1+
  - Spigot：1.16+
- **依赖**：PlaceholderAPI（可选）
- **数据库**：SQLite

## 故障排除

### 常见问题

1. **命令不工作**
   - 检查是否有正确的权限
   - 确认插件已正确加载
   - 查看控制台是否有错误信息

2. **颜色代码不显示**
   - 确认你有`cjm.color`权限
   - 检查颜色代码格式是否正确

3. **消息没有保存**
   - 检查数据库文件是否可写
   - 查看控制台是否有数据库错误

### 获取帮助

如果遇到问题，请：

1. 查看控制台日志
2. 检查配置文件是否正确
3. 确认所有依赖都已安装
4. 尝试重载配置（`/cjm reload`）

## 更新日志

### v1.0.0
- 初始版本发布
- 支持自定义加入消息
- 实现权限系统
- 添加颜色代码支持
- 集成PlaceholderAPI
- 添加多语言支持

## 许可证

本项目采用MIT许可证。详情请参阅LICENSE文件。

## 贡献

欢迎提交问题报告和功能请求！如果您想贡献代码，请：

1. Fork本项目
2. 创建功能分支
3. 提交更改
4. 发起Pull Request

## 作者

MC506LW