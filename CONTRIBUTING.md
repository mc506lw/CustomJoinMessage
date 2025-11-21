# 贡献指南

感谢您对 CustomJoinMessage 项目的关注！我们欢迎任何形式的贡献，包括但不限于：

- 报告错误
- 提出新功能建议
- 提交代码改进
- 改进文档

## 开发环境设置

### 前置要求

- Java 17 或更高版本
- Gradle 8.0 或更高版本
- IDE（推荐 IntelliJ IDEA 或 Eclipse）

### 克隆仓库

```bash
git clone https://github.com/mc506lw/CustomJoinMessage.git
cd CustomJoinMessage
```

### 构建项目

```bash
./gradlew build
```

### 导入到IDE

#### IntelliJ IDEA

1. 打开 IntelliJ IDEA
2. 选择 "Open" 并选择项目根目录
3. 等待 Gradle 同步完成

#### Eclipse

1. 运行 `./gradlew eclipse` 生成 Eclipse 项目文件
2. 打开 Eclipse 并选择 "Import" -> "Existing Projects into Workspace"
3. 选择项目根目录并导入

## 代码风格

请遵循以下代码风格指南：

- 使用 4 个空格进行缩进，不使用制表符
- 类名使用 PascalCase（大驼峰命名法）
- 方法和变量名使用 camelCase（小驼峰命名法）
- 常量使用全大写字母，单词间用下划线分隔
- 每个类和方法都应有适当的 Javadoc 注释

## 提交 Pull Request

### 分支命名

- 功能分支：`feature/功能名称`
- 修复分支：`fix/问题描述`
- 文档分支：`docs/文档内容`

### 提交信息格式

```
类型(范围): 简短描述

详细描述（可选）

相关问题: #问题编号
```

类型包括：
- `feat`: 新功能
- `fix`: 修复错误
- `docs`: 文档更新
- `style`: 代码格式调整（不影响功能）
- `refactor`: 代码重构
- `test`: 添加或修改测试
- `chore`: 构建过程或辅助工具的变动

### Pull Request 流程

1. Fork 本仓库到您的 GitHub 账户
2. 创建新分支并进行开发
3. 确保所有测试通过
4. 提交 Pull Request 到本仓库的 `main` 分支
5. 等待代码审查和合并

## 测试

在提交 Pull Request 前，请确保：

- 代码能够成功编译
- 所有现有测试通过
- 新功能有相应的测试用例
- 代码符合项目的代码风格

## 报告问题

如果您发现了错误或有功能建议，请：

1. 检查是否已有相关的 issue
2. 如果没有，创建新的 issue
3. 提供详细的问题描述和重现步骤
4. 如果是错误，请提供相关的错误日志和服务器环境信息

## 许可证

通过贡献代码，您同意您的贡献将在与项目相同的 [MIT 许可证](LICENSE) 下授权。

## 联系方式

如有任何问题，您可以通过以下方式联系我们：

- GitHub Issues
- SpigotMC 论坛

感谢您的贡献！