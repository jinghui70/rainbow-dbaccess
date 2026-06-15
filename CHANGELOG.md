## [6.2.0](https://github.com/jinghui70/rainbow-dbaccess/compare/v6.1.4..v6.2.0) (2026-06-15)

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