## [6.1.1](https://github.com/jinghui70/rainbow-dbaccess/compare/v6.0.1...v6.1.1) (2025-12-12)
### Features
- 优化 groupBy 多个参数
- update，deleteFrom支持class参数
- StringBuilderWrapper 的 join 参数修改顺序

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