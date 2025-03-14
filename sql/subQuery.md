# 子查询

可以把一个 Sql 对象当做另一个查询的查询条件参数，实现子查询的功能。

```java
Sql subSql = dba.select("id").from("score").where("score", Op.GT, 60);

dba.select("id,name").from("user").where("id", Op.IN, subSql);
```

上面的代码，生成的 SQL 为

```sql
SELECT id,name FROM user WHERE id IN (SELECT id FROM score WHERE score > ?)
参数：[60]
```
