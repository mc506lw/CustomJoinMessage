# CustomJoinMessage

[![Build Status](https://github.com/mc506lw/CustomJoinMessage/workflows/Build%20and%20Release/badge.svg)](https://github.com/mc506lw/CustomJoinMessage/actions)
[![Pull Request Check](https://github.com/mc506lw/CustomJoinMessage/workflows/Pull%20Request%20Check/badge.svg)](https://github.com/mc506lw/CustomJoinMessage/actions)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java Version](https://img.shields.io/badge/java-17+-orange.svg)](https://openjdk.java.net/)
[![Spigot Version](https://img.shields.io/badge/spigot-1.16.5+-green.svg)](https://www.spigotmc.org/)
[![Folia Version](https://img.shields.io/badge/folia-1.20.1+-green.svg)](https://papermc.io/software/folia/)

一个强大的Minecraft Bukkit/Spigot/Folia插件，允许玩家自定义加入和退出消息，支持多种消息模式、分离的权限系统和颜色代码。

## 功能特性

- **自定义加入和退出消息**：玩家可以设置自己的加入和退出消息
- **多种消息模式**：支持完整消息模式和前缀-后缀模式
- **分离的权限系统**：加入和退出消息拥有独立的权限节点
- **自定义权限组**：支持通过配置文件创建自定义权限组，如VIP、Premium等
- **动态权限注册**：自定义权限组会自动注册对应的权限节点
- **优先级系统**：支持自定义权限组和预设权限组的优先级配置
- **颜色支持**：支持标准颜色代码（&a、&b等）和十六进制颜色代码（&#RRGGBB）
- **数据库支持**：使用SQLite或MySQL数据库存储玩家消息
- **PlaceholderAPI集成**：支持使用PlaceholderAPI的占位符
- **多语言支持**：消息完全可配置，支持自定义语言
- **命令系统**：完整的命令系统，包括权限检查和帮助信息
- **异步操作**：所有数据库操作都是异步的，不会阻塞主线程
- **Tab补全**：支持命令的Tab补全功能

## 安装方法

1. 下载最新版本的CustomJoinMessage.jar文件
2. 将文件放入服务器的`plugins`目录
3. 重启服务器或使用`/reload`命令加载插件
4. 插件将自动生成配置文件和数据库

## 权限系统

插件使用分离的权限系统，加入和退出消息拥有独立的权限节点：

### 加入消息权限

- `customjoinmessage.join.use`：基本加入消息权限
- `customjoinmessage.join.use.color`：加入消息颜色权限
- `customjoinmessage.join.use.nocolor`：加入消息基本权限（无颜色）
- `customjoinmessage.join.vip`：加入消息VIP权限（示例）
- `customjoinmessage.join.<组名>`：加入消息自定义权限组

### 退出消息权限

- `customjoinmessage.quit.use`：基本退出消息权限
- `customjoinmessage.quit.use.color`：退出消息颜色权限
- `customjoinmessage.quit.use.nocolor`：退出消息基本权限（无颜色）
- `customjoinmessage.quit.vip`：退出消息VIP权限（示例）
- `customjoinmessage.quit.<组名>`：退出消息自定义权限组

### 通用权限

- `customjoinmessage.admin`：管理员权限，可以管理其他玩家的消息和重载配置
- `customjoinmessage.vip`：通用VIP权限，同时拥有加入和退出消息的VIP权限
- `customjoinmessage.<组名>`：通用权限组，同时拥有加入和退出消息的对应权限组

### 向后兼容权限

为了保持向后兼容性，以下旧权限仍然支持：

- `customjoinmessage.use`：等同于同时拥有`customjoinmessage.join.use`和`customjoinmessage.quit.use`
- `customjoinmessage.use.color`：等同于同时拥有`customjoinmessage.join.use.color`和`customjoinmessage.quit.use.color`
- `customjoinmessage.use.nocolor`：等同于同时拥有`customjoinmessage.join.use.nocolor`和`customjoinmessage.quit.use.nocolor`
- `customjoinmessage.use.<组名>`：等同于同时拥有`customjoinmessage.join.<组名>`和`customjoinmessage.quit.<组名>`

### 权限级别

1. **管理员**（customjoinmessage.admin）：拥有所有权限，包括管理其他玩家的消息
2. **颜色用户**（customjoinmessage.join/quit.use.color）：可以在消息中使用颜色代码
3. **普通用户**（customjoinmessage.join/quit.use.nocolor）：只能使用纯文本消息
4. **无权限**：无法自定义消息

### 自定义权限组

插件支持通过配置文件创建自定义权限组，例如VIP、Premium等。每个自定义权限组可以：

- 设置优先级（数值越高优先级越高）
- 定义完整的加入和退出消息（完整模式）
- 定义加入和退出前缀和后缀（前后缀模式）
- 自动注册对应的权限节点（如`customjoinmessage.join.vip`和`customjoinmessage.quit.vip`）

### 优先级系统

系统按照以下优先级处理消息：

1. **最高优先级**：玩家个人自定义消息（数据库中存储的消息）
2. **次高优先级**：玩家最高优先级权限组的消息配置
3. **默认优先级**：插件默认配置的消息

预设权限组的优先级也可以在配置文件中自由调整。

## 命令说明

### 加入消息命令

- `/setjoin`：查看当前消息模式和你的加入消息
- `/setjoin <消息>`：设置你的加入消息（完整模式）
- `/setjoin reset`：清除你的加入消息
- `/setjoin prefix <前缀>`：设置加入消息前缀（前缀-后缀模式）
- `/setjoin suffix <后缀>`：设置加入消息后缀（前缀-后缀模式）
- `/setjoin help`：显示帮助信息

### 退出消息命令

- `/setquit`：查看当前退出消息
- `/setquit set <消息>`：设置你的退出消息
- `/setquit remove`：移除你的退出消息
- `/setquit prefix <前缀>`：设置退出消息前缀（前缀-后缀模式）
- `/setquit suffix <后缀>`：设置退出消息后缀（前缀-后缀模式）
- `/setquit help`：显示帮助信息

### 管理员命令

- `/cjm reload`：重载配置文件
- `/cjm permission check <玩家>`：检查玩家的权限级别
- `/cjm permission help`：显示权限命令帮助
- `/cjm help`：显示管理命令帮助
- `/setjoin <玩家> <消息>`：为其他玩家设置加入消息
- `/setjoin <玩家> reset`：清除其他玩家的加入消息
- `/setquit <玩家> set <消息>`：为其他玩家设置退出消息
- `/setquit <玩家> remove`：为其他玩家移除退出消息
- `/cjm group list`：列出所有自定义权限组和预设权限组
- `/cjm group info <组名>`：查看指定权限组的详细信息
- `/cjm group help`：显示权限组管理命令的帮助信息

## 配置文件

### config.yml

```yaml
# Message mode: "full" or "prefix_suffix"
message-mode: "full"

# Default join message when player hasn't set a custom one
default-join-message: "&e%player_name% 加入了服务器"

# Default join prefix and suffix for prefix-suffix mode
default-join-prefix: "&e欢迎 "
default-join-suffix: " 加入服务器！"

# Default quit message when player hasn't set a custom one
default-quit-message: "&e%player_name% 离开了服务器"

# Default quit prefix and suffix for prefix-suffix mode
default-quit-prefix: "&e再见 "
default-quit-suffix: " 离开了服务器！"

# Message length limits (excluding color codes)
length-limits:
  full-mode: 50
  prefix: 20
  suffix: 20

# Whether to hide the default join/quit messages
hide-default-join-message: true
hide-default-quit-message: true

# PlaceholderAPI support
placeholders:
  enabled: true

# Database settings
database:
  type: sqlite
  sqlite:
    file: joinmessages.db
  mysql:
    host: localhost
    port: 3306
    database: minecraft
    username: root
    password: password
    table: joinmessages

# Custom permission groups
permission-groups:
  vip:
    priority: 10
    join-message: "&6[&eVIP&6] &e%player_name% 加入了服务器"
    join-prefix: "&6[&eVIP&6] &e欢迎 "
    join-suffix: " 加入服务器！"
    quit-message: "&6[&eVIP&6] &e%player_name% 离开了服务器"
    quit-prefix: "&6[&eVIP&6] &e再见 "
    quit-suffix: " 离开了服务器！"
  
  premium:
    priority: 20
    join-message: "&b[&3PREMIUM&b] &3%player_name% 加入了服务器"
    join-prefix: "&b[&3PREMIUM&b] &3欢迎 "
    join-suffix: " 加入服务器！"
    quit-message: "&b[&3PREMIUM&b] &3%player_name% 离开了服务器"
    quit-prefix: "&b[&3PREMIUM&b] &3再见 "
    quit-suffix: " 离开了服务器！"

# Predefined permission groups with their priorities
predefined-permissions:
  admin:
    priority: 100
    permission: "customjoinmessage.admin"
  color:
    priority: 50
    permission: "customjoinmessage.use.color"
  nocolor:
    priority: 10
    permission: "customjoinmessage.use.nocolor"
```

### messages.yml

所有消息都可以在此文件中自定义，包括：

- 玩家消息（成功、错误、权限等）
- 错误消息（命令使用错误、玩家未找到等）
- 帮助消息（命令帮助、权限说明等）
- 其他消息（当前模式、重载成功等）

## 消息模式

### 完整模式（Full）

玩家设置一个完整的消息，例如：
```
/setjoin &a欢迎 %player_name% 来到服务器！
/setquit set &c再见 %player_name%，期待下次见面！
```

### 前缀-后缀模式（Prefix-Suffix）

玩家分别设置前缀和后缀，系统会自动组合：
```
/setjoin prefix &a[VIP]
/setjoin suffix &b刚刚加入了游戏！
/setquit prefix &c[VIP]
/setquit suffix &e刚刚离开了游戏！
```
最终显示为：`[VIP] 玩家名 刚刚加入了游戏！`和`[VIP] 玩家名 刚刚离开了游戏！`

## 颜色代码支持

插件支持以下颜色代码格式：

- 标准颜色代码：`&a`、`&b`、`&c`等
- 十六进制颜色代码：`&#RRGGBB`，例如`&#FF0000`表示红色
- 格式代码：`&l`（粗体）、`&n`（下划线）、`&o`（斜体）等

## 占位符支持

如果启用了PlaceholderAPI支持，可以在消息中使用任何PlaceholderAPI提供的占位符，例如：
- `%player_name%`：玩家名称
- `%player_displayname%`：玩家显示名称
- `%player_world%`：玩家所在世界
- `%player_health%`：玩家生命值
- `%player_level%`：玩家经验等级

## 数据库支持

插件支持两种数据库类型：

### SQLite
- 默认选项，无需额外配置
- 数据存储在插件目录的`joinmessages.db`文件中
- 适合小型服务器

### MySQL
- 适合大型服务器或多服务器网络
- 需要在config.yml中配置连接参数
- 支持远程数据库连接

## 开发信息

- **版本**：1.2
- **Minecraft版本**：
  - Folia：1.20.1+
  - Spigot：1.16+
- **依赖**：PlaceholderAPI（可选）
- **数据库**：SQLite或MySQL

## 架构更新

### 工具类实例化重构

在v1.2版本中，我们对插件的核心架构进行了重要重构，将所有工具类从静态方法转换为实例方法。这一改进带来了以下好处：

1. **更好的依赖管理**：通过依赖注入明确各组件之间的关系
2. **提高可测试性**：实例方法更容易进行单元测试
3. **增强封装性**：每个工具类可以维护自己的状态
4. **符合面向对象原则**：更清晰的职责分离

#### 重构的工具类

以下工具类已从静态方法转换为实例方法：

1. **MessageManager** - 负责消息处理和格式化
2. **PermissionUtils** - 负责权限检查和管理
3. **SchedulerUtils** - 负责异步任务调度
4. **PlaceholderUtil** - 负责占位符处理
5. **MessageLengthUtil** - 负责消息长度计算

#### 依赖注入实现

所有工具类现在通过CustomJoinMessage主类初始化，并通过依赖注入传递给需要它们的类：

```java
// 在CustomJoinMessage主类中初始化
messageManager = new MessageManager(this);
permissionUtils = new PermissionUtils(this);
schedulerUtils = new SchedulerUtils(this);
placeholderUtil = new PlaceholderUtil(this);
messageLengthUtil = new MessageLengthUtil(this);

// 在其他类中通过构造函数注入
public SetJoinCommand(CustomJoinMessage plugin) {
    this.plugin = plugin;
    this.messageManager = plugin.getMessageManager();
    this.permissionUtils = plugin.getPermissionUtils();
    // ...
}
```

这种架构设计使插件更加模块化，便于维护和扩展。

## 故障排除

### 常见问题

1. **命令不工作**
   - 检查是否有正确的权限
   - 确认插件已正确加载
   - 查看控制台是否有错误信息

2. **颜色代码不显示**
   - 确认你有`customjoinmessage.join/quit.use.color`权限
   - 检查颜色代码格式是否正确

3. **消息没有保存**
   - 检查数据库文件是否可写
   - 查看控制台是否有数据库错误

4. **权限组不生效**
   - 确认权限组配置正确
   - 检查玩家是否有对应的权限
   - 使用`/cjm permission check <玩家>`命令检查权限级别

### 获取帮助

如果遇到问题，请：

1. 查看控制台日志
2. 检查配置文件是否正确
3. 确认所有依赖都已安装
4. 尝试重载配置（`/cjm reload`）
5. 检查玩家权限（`/cjm permission check <玩家>`）

## 更新日志

### v1.2
- 重构权限系统，将加入和退出消息权限分离
- 新增`customjoinmessage.join/quit.use.color`等分离权限节点
- 添加`customjoinmessage.vip`等通用权限，同时拥有加入和退出权限
- 保持向后兼容性，旧权限节点仍然有效
- 新增退出消息命令`/setquit`
- 完善退出消息功能，与加入消息功能保持一致
- 优化权限检查逻辑，提高系统性能
- 更新配置文件，添加退出消息相关配置项
- **架构重构**：将所有工具类从静态方法转换为实例方法，实现依赖注入
- 改进离线玩家权限检查逻辑，避免假设离线玩家权限状态
- 修复管理员权限显示异常问题，确保权限检查的一致性
- 添加MySQL数据库支持
- 优化异步操作，所有数据库操作都是异步的
- 增强Tab补全功能

### v1.1
- 添加自定义权限组功能，支持通过配置文件创建自定义权限组
- 实现动态权限注册，自定义权限组会自动注册对应的权限节点
- 增强优先级系统，支持自定义权限组和预设权限组的优先级配置
- 添加权限组管理命令（/cjm group list/info/help）
- 预设权限组优先级可在配置文件中自由调整
- 修复配置文件和消息文件的自动更新机制
- 改进文件加载错误处理，提高系统稳定性
- 优化消息文件读取，支持UTF-8编码
- 增强配置版本检查，确保平滑升级
- 添加更详细的日志记录，便于问题排查

### v1.0
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