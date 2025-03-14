# Dba 对象

Dba 对象，是访问一个数据源的唯一入口，它提供了很多工具方法直接访问数据库，也提供了

## 工具方法

### 基于实体类对象的方法

```java
// 插入单个对象
int <T> insert(T bean)
// 批量插入对象列表
<T> void insert(List<T> beans)
// 批量插入对象列表，指定每批次大小
<T> void insert(List<T> beans, int batchSize)

// 插入或更新单个对象
<T> int merge(T bean)
// 批量插入或更新对象列表
<T> void merge(List<T> beans)
// 批量插入或对象列表，指定每批次大小
<T> void merge(List<T> beans, int batchSize)

// 更新单个对象
<T> int update(T bean)
// 更新单个对象，传入表名和对象数据，表名和对象配置无关
<T> int update(String tableName, T bean)

// 删除单个对象
<T> int delete(T object)
// 根据主键删除
<T> int deleteByKey(Class<T> deleteClass, Object... keys)

// 根据主键查询
<T> T selectByKey(Class<T> selectClass, Object... keys)
```

::: warning 请注意
merge 方法不是所有数据库都支持的。[内存 Dba](/other/memoryDba)是基于 H2 的，可以放心使用。
:::

### 基于 Map 表示数据的方法

在数据类项目中，元数据配置常被用于描述表结构，而不是通过创建 Java 对象与一个数据表对应。此场景下，Map 常被用作表示一条记录，其中 Map 的键（key）代表字段名，值（value）则代表字段值。

```java
// 插入单个对象
int insert(String tableName, Map<String, Object> map)
// 批量插入对象列表
void insert(String tableName, List<Map<String, Object>> data)
// 批量插入对象列表,指定每批次大小
void insert(String tableName, List<Map<String, Object>> data，int batchSize)

// 插入或更新单个对象
int merge(String tableName, Map<String, Object> map)
// 批量插入或更新对象列表
void merge(String tableName, List<Map<String, Object>> data)
// 批量插入或更新对象列表,指定每批次大小
void merge(String tableName, List<Map<String, Object>> data，int batchSize)
```

### 其它工具

```java
// 判断表是否存在
boolean exist(String tableName)
// 执行事务
transaction(Runnable runnable)
```

## 创建 Sql 对象

[Sql 对象](/sql/sql) 是构建并执行 SQL 语句的核心对象。

```java
// 创建一个空的 Sql 对象
Sql sql()
// 创建一个 Sql 对象，并设置 SQL
Sql sql(String sql)

// 创建一个【SELECT * 】开头的查询 Sql
Sql select()
// 创建一个查询 Sql，并设置查询字段
Sql select(String select)

// 创建一个更新 Sql，并设置表名
Sql update(String table)
// 创建一个删除 Sql，并设置表名
Sql deleteFrom(String table)
```

## 创建 [ObjectSql 对象](/sql/objectSql)

```java
// 创建一个查询的 ObjectSql
<T> ObjectSql<T> select(Class<T> selectClass)
// 创建一个更新的 ObjectSql
<T> ObjectSql<T> update(Class<T> updateClass)
// 创建一个插入的 ObjectSql
<T> ObjectSql<T> insertInto(Class<T> insertClass)
// 创建一个选择对象所有字段，并把一些字段替换为常量的 ObjectSql
<T> ObjectSql<T> select(Class<T> selectClass, Map<String, Object> replaceMap)

// 创建一个删除的 ObjectSql
<T> ObjectSql<T> deleteFrom(Class<T> deleteClass)
```
