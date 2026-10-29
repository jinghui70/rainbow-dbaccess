## [6.5.0](https://github.com/jinghui70/rainbow-dbaccess/compare/v6.4.2..v6.5.0) (2026-10-29)

### Breaking Changes

- **`CrudService.getAll()` / `getAll(Class)` 改为 `String...` 可变参数**：新签名为 `getAll(String... orderBys)`、`getAll(Class<VT>, String... orderBys)`。源码兼容（`getAll()`、`getAll(UserVO.class)` 照常编译），但二进制描述符变了，用旧 jar 的调用方需重新编译
- **`UpdateBuilder` 对无 `@Id` 的实体抛 `IllegalArgumentException`**：此前会静默拼出**不带 WHERE** 的 `UPDATE`，把整张表改掉。SQL 模式的 `update(table).set(...).where(...)` 不受影响

### Bug Fixes

- **`CrudService.getAll()` 对无 `@Id` 实体抛 NPE**：取不到主键时把 `null` 交给 `orderBy` 导致空指针，现在这种情况不生成 `ORDER BY`
- **`Dba.dropTable` 在 Oracle 上语法报错**：改为按方言生成语句。`Dialect.dropTableSql` 默认仍是 `DROP TABLE IF EXISTS`（其它库行为不变）；`DialectOracle` 覆写为 PL/SQL 匿名块 `EXECUTE IMMEDIATE 'DROP TABLE ... PURGE'`，忽略表不存在（`SQLCODE = -942`），其它错误照常抛

### Features

- **`Sql.whereByKey(Class, keys...)` / `Sql.whereByObj(obj)`**：按 `@Id` 拼 WHERE 并返回 `this`，可继续链式调用。前者校验键值个数，后者从实体对象取主键值
- **`CrudService.getByKey(Class<VT> voClass, Object... keys)`**：按主键查单条并直接映射为 VO
- **`CrudService.getAll(String... orderBys)` / `getAll(Class<VT>, String... orderBys)`**：支持 `"FIELD ASC"`、`"FIELD DESC"` 格式与多字段。传了就完全按它排，不传才按主键；不追加主键兜底（全量查询没有翻页边界，与 `QueryDTO` 的分页排序不同）
- **`OrderBy.of(field)` / `OrderBy.desc(field)`**：升序、降序构造 `OrderBy` 的静态工厂

### Other

- **主键拼 WHERE 去重**：`Dba.selectByKey`、`Dba.deleteByKey`、`CrudService.getByObject`、`DeleteBuilder.delete`（单条）、`UpdateBuilder` 五处重复循环收敛到上面两个 `Sql` 方法
- **`DbaUtil.defaultOrderBy` 并入 `CrudService.orderOf`**：该方法只有 `getAll` 一处调用，不再由通用工具类承担
- **去掉 javadoc 插件的 `failOnError=false`**：该配置让 doclint 错误不阻断构建，实测会照常发布带坏链接的 javadoc jar；现在 doclint 错误即构建失败

## [6.4.2](https://github.com/jinghui70/rainbow-dbaccess/compare/v6.4.1..v6.4.2) (2026-10-26)

### Bug Fixes

- **`Cnd` 的 `NE` 补齐 `Range` 处理**：此前只有 `EQ` 识别 `Range`/`Map` 值，`NE` 会把区间对象当普通值绑定，拼出 `AGE != ?` 后在驱动层报错。现在与 `EQ` 共用同一套推导规则：`from == to` 取反为 `!=`、仅下限为 `<`、仅上限为 `>`、双边界为 `not between`
- **`Range`/`Map` 值只允许 `EQ`/`NE`**：`GT`、`LIKE` 等其他操作符传入区间值会直接抛 `IllegalArgumentException`，避免静默拼出错误 SQL

## [6.4.1](https://github.com/jinghui70/rainbow-dbaccess/compare/v6.4.0..v6.4.1) (2026-09-29)

### Features

- **`Sql.queryForSet`**：查询结果去重为 `Set<T>`，与 `queryForList` 对齐的三个重载（`Class` / `RowMapper` / `FieldMapper`），返回 `LinkedHashSet` 保持结果顺序。选型：单列值去重优先 `selectDistinct`（数据库侧去重、传输量小），查 Bean 按 `equals` 去重用 `queryForSet`
- **`Dba.selectDistinct(fields...)` / `Sql.selectDistinct(fields...)`**：生成 `SELECT DISTINCT ...`，不传字段为 `DISTINCT *`；静态工厂不关联 Dba，供子查询拼接。与 `count()` 的交互：SQL 含 DISTINCT 时自动跳过 count 优化、包子查询计数，去重行数正确
- **`DbaUtil.DISTINCT`**：新增 SQL 关键字常量（拼接用，带尾空格；`getCountOptimizeInfo` 的检测仍用无空格字面量，避免漏检 `DISTINCT(...)` 写法）

### Docs

- doc/query.md 新增"集合查询"与"去重查询"小节；SKILL.md 同步两个新 API 的示例与能力清单

## [6.4.0](https://github.com/jinghui70/rainbow-dbaccess/compare/v6.3.5..v6.4.0) (2026-09-26)

### Breaking Changes

- **`TreeUtils` 遍历顺序参数改为枚举**：新增 `TraverseOrder`（`PRE_ORDER` / `POST_ORDER`），`traverse`、`traverseNode` 的 `boolean isPreOrder` 参数全部替换为 `TraverseOrder`。原 `true` 对应 `PRE_ORDER`，`false` 对应 `POST_ORDER`
- **移除 `TreeUtils.traverse` 的两个便捷重载**：`traverse(List, Consumer)` 和 `traverse(List, TreeNodeConsumer)` 不再提供，调用方需显式传入遍历顺序。原行为等价于传 `PRE_ORDER`
- **`TreeUtils` 过滤参数改为枚举**：新增 `FilterMode`（`FILTER_SUBTREE` / `KEEP_SUBTREE`），`filter`、`filterNode` 的 `boolean recurseOnMatch` 参数替换为 `FilterMode`。原 `true` 对应 `FILTER_SUBTREE`（匹配节点继续过滤子树），`false` 对应 `KEEP_SUBTREE`（匹配节点整棵子树原样保留）
- **生成的 SQL 列名改为大写**：`PropInfo.getFieldName()` 及 `PropInfoCache` 属性缓存的 key 从小写改为大写，`SELECT * FROM T_USER WHERE id=?` 变为 `... WHERE ID=?`，与一贯大写的表名一致，也与文档描述一致。`@Column(name)` 指定的列名同样会转大写。未加引号的标识符在各数据库都不区分大小写（PostgreSQL 折叠为小写、Oracle 折叠为大写），**SQL 语义不变**；但如果你的测试断言了生成的 SQL 文本，或有工具按小写列名解析日志，需要相应调整
- **`QueryDTO` 排序总会带上主键**：原来只在前端没传 `orderBys` 时才按主键排序，现在无论前端传没传，都把实体主键列追加到 ORDER BY 末尾（按列名去重，大小写不敏感）。按非唯一列排序时数据库对并列行的返回顺序没有保证，OFFSET 分页会出现某行重复出现或整页丢失，追加主键让排序成为全序。前端已经排过主键时不重复追加
- **`QueryDTO.setEntity(String)` 会清掉主键排序兜底**：裸表名无从得知主键，且原实体的主键列在 JOIN 中可能有歧义。`setEntity(clazz)` 之后再改成 JOIN 片段的场景（派生 Service 定制查询）要用 `defaultOrderBy("O.CREATE_TIME", "O.ID")` 自己带上有前缀的唯一列
- **`QueryDTO.setEntity(Class)` 不再填充 `defaultOrderBys`**：主键改存到新的 `keyOrderBys` 字段，`defaultOrderBys` 只表示"前端没传时用什么排序"这一件事，不会再被 `setEntity` 覆盖
- **`QueryDTO.defaultOrderBys` 字段类型变更**：`protected String` 改为 `protected List<OrderBy>`，直接读写该字段的子类需要调整

### Features

- **`QueryDTO.setOrderBys()` 校验字段名**：与 `setCnds` 一致，每个 `OrderBy.field` 都经 `DbaUtil.validateFieldName` 校验。此前排序字段名从 `@RequestBody` 原样拼进 SQL，存在注入风险
- **新增 `OrderBy.parse(String)`**：`toString()` 的逆运算，把 `"NAME"` / `"NAME DESC"` 解析回 `OrderBy`，方向后缀大小写不敏感，不写或无法识别都按升序
- **新增 `QueryDTO.defaultOrderBy(OrderBy...)`** 重载，可直接传对象，不必走字符串解析

- **`TreeUtils` 空值处理统一**：`traverse`、`traverseNode`、`filter`、`filterNode` 入参为 `null` 时不再抛 `NullPointerException`，与 `transform`、`printTree` 行为一致
- **`TreeUtils.filter()`、`transform()`** 入参为空时返回可修改的空列表，调用方可直接往返回值里添加节点

### Bug Fixes

- **修正 `Sql` 类文档里无法运行的示例**：类级 Javadoc 的 3 个示例都写成 `Sql.select()...queryForList(...)`，而静态工厂 `Sql.select()` 不关联 `Dba`（`dba.select()` 实际是 `Sql.select().setDba(this)`），照抄必抛 `NullPointerException`。已改为 `dba.select()`，并在 `Sql.select(String...)` 的文档上注明它只用于拼 SQL 文本（如构造子查询）
- **修正 `Sql` 类文档示例的语法错误**：`Cnd.or(...)` 的参数列表末尾有多余逗号，示例代码无法编译
- **修正 `Dba.update(String)` 文档中指向具体重载的误导链接**，以及 `Dba.selectByKey` 示例里不存在的类名

### Docs

- **补齐 Javadoc**：`InsertBuilder`（构造器、`into`、`batchSize`、`merge`）、`PageData`、`Range`、`DataType`、`TreeObject`、`CodeEnum`、`StringBuilderWrapper`、`SnowflakeGenerator`、`@Table` 等处缺失的说明与 `@param`/`@return` 全部补全，doclint 非构造器告警从 68 条降为 0
- **开启 Javadoc 结构性检查**：`maven-javadoc-plugin` 的 `doclint` 从 `none` 改为 `reference,syntax,html`，坏链接和坏 HTML 以后会在构建中暴露（暂不开 `missing`）
- **新增 `JavadocExampleTest`**：把 Javadoc 里的示例代码当真实代码跑一遍。doclint 不检查 `{@code}` 块，上面两个示例 bug 正是这样漏掉的；改文档示例时需同步改该测试

## [6.3.5](https://github.com/jinghui70/rainbow-dbaccess/compare/v6.3.3..v6.3.5) (2026-09-11)
 
- 恢复 TreeObject，与 queryForWrapTree
- crudService insert 返回对象

## [6.3.3](https://github.com/jinghui70/rainbow-dbaccess/compare/v6.3.1..v6.3.3) (2026-09-01)

- ** `@Table` 增加value
- ValueGenerator 自动注册
- update sql fix set boolean

### Breaking Changes

- 去掉冗余的OrderBy.Desc
- PageData 去掉 isEmpty

## [6.3.1](https://github.com/jinghui70/rainbow-dbaccess/compare/v6.3.0..v6.3.1) (2026-07-07)

### Features

- **`UpdateBuilder.into(String)`**：指定更新的表名，用于更新与 Bean 结构相同的另一张表（如分表场景）；不调用时仍用 Bean 类对应的表名
- **`Cnd`**：IN/NOT IN 条件值超过 1000 时自动拆分为多组连接（IN 用 OR、NOT IN 用 AND），规避 Oracle IN 列表 1000 上限（ORA-01795）；参数不超过 1000 时行为不变

## [6.3.0](https://github.com/jinghui70/rainbow-dbaccess/compare/v6.2.2..v6.3.0) (2026-06-26)

### Breaking Changes

- **包结构调整**，以下类从顶层包移入子包，需更新 import：`Sql`、`UpdateSql`、`InsertBuilder`、`UpdateBuilder`、`DeleteBuilder`、`OrderBy`、`PageData`、`Range`、`ResultSetFunction` 移至 `io.github.jinghui70.rainbow.dbaccess.sql`；`CodeEnum` 移至 `io.github.jinghui70.rainbow.dbaccess.object`
- **`Sql.pageQuery()`** 重命名为 `queryPage()`，含 `(RowMapper, pageNo, pageSize)`、`(pageNo, pageSize)`、`(Class, pageNo, pageSize)` 三个重载；`QueryDTO` 同名方法一并改为 `queryPage`

### Features

- 新增 **`@GeneratedValue`** 注解：插入时为值为 `null` 的字段按策略自动生成并回填到入参对象，已有值不覆盖，批量插入逐行回填
- 新增 **`GenerationTiming.INSERT_UPDATE`** 策略：支持在更新时也自动生成值。插入时同 `INSERT` 行为（仅当字段为 `null` 时生成），更新时强制重新生成并覆盖原有值，适用于更新时间、版本号等场景
- 内置两种生成策略：`default`（雪花 id，`long`/`Long` 返回数字，`String` 返回 36 进制大写且 `param` 作前缀）、`now`（当前时间，支持 `LocalDateTime`/`Timestamp`/`Date`，`String` 按 `param` 格式化）
- 支持自定义 `ValueGenerator`：注册为 Spring Bean 即由 `DbaAutoConfiguration` 自动注册，非 Spring 环境可用 `ValueGeneratorRegistry.register()`
- **`QueryDTO.setFields()`** 改为可变参数，`getFields()` 未设置时默认返回 `*`

## [6.2.2](https://github.com/jinghui70/rainbow-dbaccess/compare/v6.1.4..v6.2.2) (2026-06-15)

### Breaking Changes

- **Dba.insert()** 重构：`insert(T bean)` 返回值从 `int` 改为 `void`；移除 `insert(List)`、`insert(List, batchSize)`、`insert(bean, tableName)`、`insert(List, tableName)`、`insert(List, tableName, batchSize)` 等重载；Map 插入的重载全部移除。改用 `insertOf(data)` 返回 `InsertBuilder` 构建器
- **Dba.merge()** 重构：`merge(T bean)` 返回值从 `int` 改为 `void`；移除 `merge(List)`、`merge(bean, tableName)`、`merge(List, tableName)` 等重载；Map 合并的重载全部移除。改用 `mergeOf(data)` 返回 `InsertBuilder` 构建器
- **Dba.update()** 返回类型变更：`update(String/Class)` 返回类型从 `Sql` 改为 `UpdateSql`；移除 `update(String tableName, T bean)`，改用 `updateOf(bean)` 返回 `UpdateBuilder` 构建器
- **Sql.set()** 系列方法全部移除，迁移到 `UpdateSql`，包括 `set(field, value)`、`set(field, fieldMapper, value)`、`set(setStr)` 及其条件式变体
- **Sql.queryForWrapTree()** 移除
- **删除 ObjectDao 类**，功能拆分到 `InsertBuilder`、`UpdateBuilder`、`DeleteBuilder`
- **删除 MapHandler 类**，逻辑合并到 `InsertBuilder`
- **删除 QueryParam 类**
- **删除 StrConst 类**
- **删除 OrdinalEnum 接口**，`EnumFieldMapper` 不再支持 ordinal 模式，枚举统一按 name 存储
- **删除 TreeObject、WrapTree 类**，树结构直接使用 `Tree<T>` 和 `ITreeNode<T>`
- **FieldMapper.toValue()** 重命名为 `ofValue()`
- **Cnd.field** 从 `protected` 改为 `private`
- **Cnd.op** 默认值从 `null` 改为 `Op.EQ`

### Features

- 新增 crud 一个对象相关代码，可用于SpringBoot项目
- 新增 `InsertBuilder` 构建器，统一处理 Bean/Map、单条/批量、默认表名/指定表名、INSERT/MERGE 等场景
- 新增 `UpdateBuilder` 构建器，支持 include/exclude/excludeNull 字段过滤
- 新增 `UpdateSql` 构建器，替代 Sql 的 set 方法，支持条件式 set
- 新增 `DeleteBuilder` 构建器，支持按 Bean/Bean 数组/Bean 集合删除
- 新增 `Dba.selectByKey()` 方法，根据主键值查询单条记录，支持复合主键
- 新增 `Dba.updateOf()` 方法，创建 Bean 模式更新构建器
- 新增 `Dba.delete()` 方法，按主键删除记录
- 新增 `Sql.exist()` 方法，使用 LIMIT 1 优化存在性检查
- 新增 `ObjectCodec` 类，提取 ObjectFieldMapper/BlobObjectFieldMapper 的共享 JSON 编解码逻辑
- 新增 `InsertBuilder.into(Class)` 方法，通过实体类指定表名
- 全面补充 Javadoc 文档

### Other

- `Dba` 构造函数 Javadoc 优化
- `MapRowMapper.INSTANCE` 改为 `final`
- `MapRowMapper.mapRow()` 返回值加 `@NonNull`，列名处理逻辑调整
- `TreeUtils.filter()` 移除 `TreeObject` 特殊处理逻辑

## [6.1.4](https://github.com/jinghui70/rainbow-dbaccess/compare/v6.1.2..v6.1.4) (2025-12-12)
### Features
- 删除 ArrayField
- 重构 ObjectFieldMapper
- dba insert 支持插入对象到其它表

## [6.1.2](https://github.com/jinghui70/rainbow-dbaccess/compare/v6.0.1...v6.1.2) (2025-12-12)
### Features
- 优化 groupBy 多个参数
- update，deleteFrom支持class参数
- 重构QueryParam

## [6.1.0](https://github.com/jinghui70/rainbow-dbaccess/compare/v6.0.1...v6.1.0) (2025-12-12)

### Features
- FieldMapper添加 toValue 函数
- 优化一些mapper位置与命名
- StringBuilderWrapper 的repeat 默认改为不提供链接符
- PropInfo 提供 getName

### Bug Fixes
- count 函数在可以优化时，sql大写了
- 6.0.3 有严重问题，不可使用

## [6.0.1](https://github.com/jinghui70/rainbow-dbaccess/compare/v6.0.0...v6.0.1) (2025-07-26)

### Features
- Sql 支持 select() 函数
- 统一了 Cnd 的写法，支持Cnd组，去掉了Cnds
- 
### Bug Fixes
- 去掉 notEmpty 的条件设置函数
- Op.IS_NULL 条件无需参数
- count时把sql大写了

## [6.0.0]

### Features

- 支持 jdk 17+，不支持 1.8了
- 废除 ObjectSql 
- 集中API 到 Dba，Sql两个文件
- 增加 CamelCaseMapMapper
- 优化 QueryParam

### Bug Fixes

- **Cnd**: Op 参数为空，SQL错误


## [5.2.11](https://github.com/jinghui70/rainbow-dbaccess/compare/v5.2.11...v5.2.12) (2024-12-26)

### Features

- 废除 selectAll()， 用 select()代替
- queryForTree 返回 Tree 对象
- bool 类型字段，默认保存 1,0
- 废除 selectById()

## [5.2.11](https://github.com/jinghui70/rainbow-dbaccess/compare/v5.2.9...v5.2.11) (2024-11-16)

### Features

- 增加 where(boolean condition, () -> Cnds) 这类函数
- 增加 ClobObjectFieldMapper
- 增加 Bool 到 YN 或 TF 的映射
- ObjectSql Update 的 set 函数，用 FieldMapper 转换值
- TreeUtil 的遍历函数，增加是否先序遍历的参数

## [5.2.9](https://github.com/jinghui70/rainbow-dbaccess/compare/v5.2.8...v5.2.9) (2024-10-21)

### Bug Fixes

- **Op.LIKE**: 使用 LIKE 相关操作符条件，输出错误的 SQL
- 转换 Op.IN 的参数（数组或 Collection）时，空元素和枚举元素转换有错

### Features

- 增加 Cnds 对象，用来组合一组条件
- LIKE 操作符判断首尾都没有 `%` 时，才在首尾自动加上

## [5.2.8](https://github.com/jinghui70/rainbow-dbaccess/compare/v5.2.7...v5.2.8) (2024-10-21)

### Other

- 调整常量字符串的位置到 DbaUtil 中
- 子查询条件不默认 Op 为 IN

## [5.2.7](https://github.com/jinghui70/rainbow-dbaccess/compare/v5.2.6...v5.2.7) (2024-10-15)

### Bug Fixes

- **FieldMapper**: Blob 字段对应对象如果是 List 或者数组有问题

### Features

- **BlobObjectFieldMapper**: 提供了of、ofList、ofArray 创建函数