# AI 辅助开发

本仓库提供一份面向 AI 编程助手的**技能文件**（[skills/rainbow-dbaccess/SKILL.md](https://github.com/jinghui70/rainbow-dbaccess/blob/doc/skills/rainbow-dbaccess/SKILL.md)），内容与官方文档同步核对，覆盖强制规则、API 能力清单和常见陷阱。

把它安装到使用 Rainbow DBAccess 的项目后，AI 在写数据库代码时会遵循 Dba 的写法约定——统一走 `Dba`、`?` 占位符防注入、条件开关代替 `where("1=1")`——而不是凭训练记忆生成 JPA / MyBatis 风格的代码。

## 安装到消费项目

以 Claude Code 为例，把技能文件复制到项目的 `.claude/skills/` 目录：

```bash
mkdir -p .claude/skills/rainbow-dbaccess
curl -o .claude/skills/rainbow-dbaccess/SKILL.md \
  https://raw.githubusercontent.com/jinghui70/rainbow-dbaccess/doc/skills/rainbow-dbaccess/SKILL.md
```

安装后无需任何配置，AI 在涉及数据库访问的任务中会自动参考该文件。

该文件是纯 Markdown，也可以作为其他 AI 工具的规则或上下文文件引入（如 Cursor 的 Rules、GitHub Copilot 的 instructions）。

## 文件内容说明

技能文件面向 AI 的阅读习惯组织，包含：

- **强制规则**：所有数据库操作必须通过 `Dba`、禁止拼接 SQL 参数等最高优先级约定
- **用法速览**：实体映射、查询、Cnd 条件、插入、更新、删除、事务等核心用法
- **API 能力清单**：常用类的方法与返回类型速查，方便确认"有没有这个方法"
- **常见陷阱**：AI 容易写错的场景对照表（如 `include` 传属性名而 `set` 传列名）

文件随本文档一起维护，如发现内容与实际行为不符，欢迎提 issue。
