## 目录性质

本目录是 [Rainbow DBAccess](https://github.com/jinghui70/rainbow-dbaccess) 的**文档站点**，使用 VitePress 构建。

与库源码同仓库（`../src`），所以 Markdown 里的 Java 示例**可以对照真实 API 核验**，不要凭记忆写。改动涉及 API 用法时，先确认库里确有该方法与签名。

技能文件不在本目录，在仓库第一级的 `../skills/rainbow-dbaccess/SKILL.md`（它是面向 AI 的交付物，不是站点内容，因此不参与 VitePress 构建）。

## 常用命令

```bash
pnpm install          # 安装依赖（包管理器固定为 pnpm 10），在本目录执行
pnpm docs:dev         # 本地预览（默认 http://localhost:5173）
pnpm docs:build       # 构建到 .vitepress/dist
pnpm docs:preview     # 预览已构建产物
```

JavaDoc（`public/apidocs`）**不入库**，由 CI 在部署时 `mvn javadoc:javadoc` 现场生成。本地要预览 JavaDoc 链接，先在仓库根执行：

```bash
mvn javadoc:javadoc && rm -rf doc/public/apidocs && cp -r target/reports/apidocs doc/public/apidocs
```

## 架构要点

- **内容布局**：Markdown 直接放在本目录（`concept.md`、`query.md`、`cnd.md` 等），不分子目录。`index.md` 是 VitePress 主页（hero/features 布局）。
- **导航与侧栏**：在 `.vitepress/config.mjs` 中集中维护。**新增一篇文档时必须同步更新 `sidebar`**，否则不会出现在站点导航里。
- **部署**：`../.github/workflows/deploy.yml` 监听 `6.0` 分支 push，且路径命中 `doc/**`、`skills/**`、`src/main/**`、`pom.xml` 才触发；构建后发布到 GitHub Pages。`base` 路径是 `/rainbow-dbaccess/`——任何站内绝对链接需要考虑这个前缀。
- **历史**：文档过去住在独立的 `doc` 分支，现已并入代码分支，使文档与源码能在同一个提交里保持同步。`doc` 分支仅作归档，不要再往那边提交。

## 写作约定（参考现有文档的语气与结构）

- 文档面向 Java 开发者，**SQL 与链式 API 是叙事主线**；优先用最小可运行片段说明用法，避免堆 API 列表。
- 文档站使用简体中文；技术名词（`Cnd`、`FieldMapper`、`MemoryDba` 等）保留原形，不翻译。
- 代码块标注语言（` ```java`、` ```sql`），章节标题层级与 `outline.level: [2, 3]` 对齐——只用 `##` 和 `###` 作为大纲项。
