# COUNT

## 概述

COUNT 是 SQL 中的聚合函数，用于统计满足条件的行数。例如：

```java
int count = dba.select("COUNT(*)").from("user").where("status", 1).queryForInt();
```

## count 方法

Sql 对象提供了 count 方法来简化获取总行数的写法。

当 `select` 部分不包含 `DISTINCT`，`GROUP BY` 或者 `UNION`时，count 方法会默认会优化 SQL 语句内容。

```java
// 默认优化
Sql sql = dba.select().from("sale_record").where("area", "北京").orderBy("id");
int count = sql.count();
// 执行：SELECT COUNT(*) FROM sale_record WHERE area='北京'

// 包含 DISTINCT，不优化
dba.select("DISTINCT id,money").from("sale_record").where("area", "北京").orderBy("id").count();
// 执行：SELECT COUNT(1）FROM (SELECT DISTINCT ID,money FROM sale_record WHERE area='北京' order by id) C

// 手动关闭优化
dba.select("id,money").from("sale_record").where("area", "北京").orderBy("id").disableCountOptimization().count();
// 执行： SELECT COUNT(1) FROM (SELECT ID,money FROM sale_record WHERE area='北京' order by id) C
```
