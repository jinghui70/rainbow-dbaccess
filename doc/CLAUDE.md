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

## 版本升级检查清单

改版本号时**这 5 处必须一起改**，漏任何一处都会出现版本不一致：

| # | 文件 | 位置 | 说明 |
| --- | --- | --- | --- |
| 1 | `../pom.xml` | `<version>` | 项目版本 |
| 2 | `../CHANGELOG.md` | 首部新段落 | 标题与 compare 链接各含一个版本号（如 `v6.4.2..v6.5.0`），并写日期 |
| 3 | `getting-started.md` | `## 依赖引入` 的 XML 片段 | 依赖坐标示例的 `<version>` |
| 4 | `ai.md` | `## 安装到消费项目` 的 `curl` 命令 | URL 里的 tag，**带 `v` 前缀** |
| 5 | `ai.md` | 紧随 `curl` 的正文 | “把 `v6.4.1` 换成…” 这句里也有一处，别只改 URL |

**版本号怎么定**：有 Breaking Change 升 minor（`6.4.x` → `6.5.0`），仅修 bug 升 patch。

**不用改的地方**（避免无谓搜索）：`README.md`、`README-CN.md`、`../skills/`、`.vitepress/config.mjs`、`index.md` 均无版本号；`deploy.yml` 与 `ai.md` 开头的 `6.0` 是**开发分支名**而非版本号，不要动。

**防漏做法**：不要只搜“上一个版本号”——历史文档可能停在更早的版本（例如升到 6.5.0 时，`getting-started.md` 和 `ai.md` 还停在 6.4.1）。正确姿势是搜**全部旧版本前缀**，排除历史段落与构建产物：

```bash
# 仓库根执行，改前查残留、改后用同一命令复查
grep -rn "6\.4\.\|6\.3\." --include=*.md --include=*.xml --include=*.mjs --include=*.yml . \
  | grep -v "doc/.vitepress/dist|doc/public|/target/|doc/node_modules|CHANGELOG.md"
```

最后用 `grep -rn "<新版本号>"` 确认恰好命中上述 5 处。
