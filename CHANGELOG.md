
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