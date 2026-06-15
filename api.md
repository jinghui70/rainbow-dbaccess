# API 速查

## Dba 方法

| 方法 | 说明 |
|------|------|
| `select()` / `select(fields)` | 创建 SELECT 查询 |
| `selectByKey(Class, keys)` | 按主键查单条 |
| `insert(data)` | 插入数据（bean/数组/集合） |
| `insertOf(data)` | 创建插入构建器 |
| `merge(data)` | MERGE（Insert Or Update） |
| `mergeOf(data)` | 创建 MERGE 构建器 |
| `update(bean)` | 按主键全量更新 |
| `updateOf(bean)` | 创建更新构建器 |
| `update(table)` / `update(Class)` | 创建 UpdateSql |
| `delete(data)` | 按主键删除（bean/数组/集合） |
| `deleteByKey(Class, keys)` | 按主键值删除 |
| `deleteFrom(table)` / `deleteFrom(Class)` | 创建 DELETE 语句 |
| `sql(sql)` | 创建原始 SQL 构建器 |
| `transaction(Runnable)` | 事务执行（无返回值） |
| `transaction(TransactionCallback)` | 事务执行（有返回值） |
| `exist(tableName)` | 检查表是否存在 |
| `dropTable(tableName)` | 删除表 |

## Sql 链式方法

| 方法 | 说明 |
|------|------|
| `from(table)` / `from(Class)` | FROM 子句 |
| `where(str)` | WHERE 原始 SQL 片段 |
| `where(field, value)` | WHERE 等值条件（智能推导） |
| `where(field, Op, value)` | WHERE 指定操作符 |
| `where(Cnd)` | WHERE 条件对象 |
| `where(condition, ...)` | 条件开关 |
| `where(condition, Supplier<Cnd>)` | 条件开关 + 延迟创建 |
| `where(List<Cnd>)` | WHERE 条件列表（AND 连接） |
| `and(...)` / `or(...)` | 追加条件（与 where 同样的重载） |
| `append(Sql)` | 追加另一个 Sql |
| `append(Cnd)` | 追加 Cnd 条件 |
| `addParam(Object...)` / `addParams(List)` | 添加参数 |
| `setParam(Object...)` / `setParams(List)` | 设置参数（覆盖） |
| `orderBy(fields)` | ORDER BY |
| `orderBy(List<OrderBy>)` | ORDER BY（排序对象） |
| `groupBy(fields)` | GROUP BY |
| `limit(n)` | 限制行数 |
| `range(from, to)` | 范围查询 |
| `disableCountOptimization()` | 禁用 count 优化 |

## Sql 查询结果方法

| 方法 | 返回类型 |
|------|---------|
| `queryForObject(Class)` | `T` |
| `queryForObject(RowMapper)` | `T` |
| `queryForObjectOptional(Class)` | `Optional<T>` |
| `queryForObjectOptional(RowMapper)` | `Optional<T>` |
| `queryForValue(Class)` | 简单类型值 |
| `queryForValueOptional(Class)` | `Optional<T>` |
| `queryForValue(FieldMapper)` | 使用 FieldMapper 转换 |
| `queryForValueOptional(FieldMapper)` | `Optional<T>` |
| `queryForString()` | `String` |
| `queryForStringOptional()` | `Optional<String>` |
| `queryForInt()` | `int` |
| `queryForIntOptional()` | `Optional<Integer>` |
| `queryForDouble()` | `double` |
| `queryForDoubleOptional()` | `Optional<Double>` |
| `queryForDate()` | `LocalDate` |
| `queryForDateOptional()` | `Optional<LocalDate>` |
| `queryForMap()` | `Map<String, Object>` |
| `queryForList(Class)` | `List<T>` |
| `queryForList(RowMapper)` | `List<T>` |
| `queryForList(FieldMapper)` | `List<T>` |
| `queryForList()` | `List<Map<String, Object>>` |
| `queryToMap(keyFunc, valueFunc)` | `Map<K, V>` |
| `queryToMap(keyFunc, Class)` | `Map<K, T>` |
| `queryToMap(keyFunc, RowMapper)` | `Map<K, V>` |
| `queryToMap(keyFunc)` | `Map<K, Map<String, Object>>` |
| `queryToGroup(keyFunc, Class)` | `Map<K, List<T>>` |
| `queryToGroup(keyFunc, RowMapper)` | `Map<K, List<T>>` |
| `queryToGroup(keyFunc, valueFunc)` | `Map<K, List<V>>` |
| `queryToGroup(keyFunc)` | `Map<K, List<Map<String, Object>>>` |
| `query(RowCallbackHandler)` | 逐行回调 |
| `query(ResultSetExtractor)` | 自定义结果提取 |
| `count()` | `int` |
| `exist()` | `boolean` |
| `pageQuery(Class, pageNo, pageSize)` | `PageData<T>` |
| `pageQuery(RowMapper, pageNo, pageSize)` | `PageData<T>` |
| `pageQuery(pageNo, pageSize)` | `PageData<Map<String, Object>>` |
| `queryForTree(Class)` | `Tree<T>` |
| `queryForTree(RowMapper)` | `Tree<T>` |

## Cnd 静态方法

| 方法 | 说明 |
|------|------|
| `Cnd.where(field, value)` | 等值条件（智能推导） |
| `Cnd.where(field, Op, value)` | 指定操作符条件 |
| `Cnd.where(condition, field, value)` | 条件开关 |
| `Cnd.where(condition, field, Op, value)` | 条件开关 + 指定操作符 |
| `Cnd.and(Cnd...)` / `Cnd.and(List)` | AND 组合 |
| `Cnd.or(Cnd...)` / `Cnd.or(List)` | OR 组合 |
| `Cnd.isNull(field)` | IS NULL |
| `Cnd.isNotNull(field)` | IS NOT NULL |
