# 执行 SQL

## 可执行 SQL

对于 insert、update、delete 以及数据库 DDL 语句，可以通过 execute() 函数来执行。

```java
// 新增用户张三
dba.sql("insert into user(id, name) values (?, ?)")
  .addParam(1001, "张三").execute();

// 张三丢了工作
dba.update("user").set("job", null).where("name", "张三").execute();

// 李四涨了工资
dba.update("user").set("job", "工程师").set("salary=salary*2")
  .where("name", "李四").execute();

// 删除张三
dba.deleteFrom("user").where("id", 1001).execute();
```

## 查询 SQL

对于 select 语句，select 部分可能是一个字段，或者多个字段，查询结果可能是一条记录或多条记录。

|              | 查询一个字段                                                                                                            | 查询多个字段          |
| ------------ | ----------------------------------------------------------------------------------------------------------------------- | --------------------- |
| 查询一条记录 | queryForString()<br>queryForInt()<br>queryForDouble()<br>queryForDate()<br> queryForValue(Class&lt;T&gt; requiredClass) | executeQueryList()    |
|              |                                                                                                                         | executeQueryMap       |
| 查询多条记录 | executeQueryList()                                                                                                      | executeQueryMapList() |

### 查询一个字段，可以通过一下函数来获取查询结果

```java
// 查询张三的工资
int salary = dba.sql("select salary from user where name=?").addParam("张三").executeQuery().get(0, "salary");

```

## count

对当前的 Sql 语句执行 count 操作，返回结果为总记录数。

```java
int count = dba.select().from("user").count();
```

当 SQL 内容 不包含 DISTINCT， GROUP BY 或者 UNION 时，默认会优化 SQL 语句内容，抛弃 ORDER BY 部分。
可以调用 `disableCountOptimization()` 关闭优化。

```java
// 默认优化
int count = dba.select().from("user").orderBy("ID").count();
// 执行的 SQL：SELECT COUNT(1) FROM USER

// 关闭优化
dba.select("*").from("user").orderBy("ID").disableCountOptimization().count();
// 执行的 SQL： SELECT COUNT(*) FROM (SELECT * FROM USER ORDER BY ID) C
```
