## 仓库性质

这是 [Rainbow DBAccess](https://github.com/jinghui70/rainbow-dbaccess)（基于 Spring JDBC 的轻量级数据库访问工具）的**文档站点**，使用 VitePress 构建。本仓库**不包含被文档化的 Java 库源码**——所有 Markdown 中的 Java 示例都引用外部库的 API，无法在本地运行验证。

## 常用命令

```bash
pnpm install          # 安装依赖（包管理器固定为 pnpm 10）
pnpm docs:dev         # 本地预览（默认 http://localhost:5173）
pnpm docs:build       # 构建到 .vitepress/dist
pnpm docs:preview     # 预览已构建产物
```

## 架构要点

- **内容布局**：Markdown 直接放在仓库根目录（`concept.md`、`query.md`、`cnd.md` 等），不分子目录。`index.md` 是 VitePress 主页（hero/features 布局）。
- **导航与侧栏**：在 `.vitepress/config.mjs` 中集中维护。**新增一篇文档时必须同步更新 `sidebar`**，否则不会出现在站点导航里。
- **部署**：`.github/workflows/deploy.yml` 监听 `doc` 分支的 push，用 pnpm 10 + Node 22 构建并发布到 GitHub Pages。`base` 路径是 `/rainbow-dbaccess/`——任何站内绝对链接需要考虑这个前缀。
- **主分支约定**：`main` 是 PR 目标分支，但 **`doc` 分支才会触发部署**。修改文档后通常推到 `doc` 才能上线。

## 写作约定（参考现有文档的语气与结构）

- 文档面向 Java 开发者，**SQL 与链式 API 是叙事主线**；优先用最小可运行片段说明用法，避免堆 API 列表（`api.md` 已是速查表，不要在指南页重复它）。
- 文档站使用简体中文；技术名词（`Cnd`、`FieldMapper`、`MemoryDba` 等）保留原形，不翻译。
- 代码块标注语言（` ```java`、` ```sql`），章节标题层级与 `outline.level: [2, 3]` 对齐——只用 `##` 和 `###` 作为大纲项。

## 源码位置

~/work/rainbow-dbaccess