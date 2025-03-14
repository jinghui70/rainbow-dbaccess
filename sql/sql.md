# Sql 对象

为了避免直接拼接 SQL 语句带来的注入风险，数据库访问应该用参数化的方式，即使用参数占位符 `?` 来代替具体的值。

通过 dba 创建的 Sql 对象，本质上是一个[字符串拼接器](/other/stringBuilderX)，用来拼接 SQL 语句，同时内部维护了查询参数数组。

## 基础用法

Sql 对象使用链式写法，提供了 append 方法拼接 SQL 语句，addParam 方法来添加参数。

```java
Sql sql = dba.sql("select * from user where id = ? and name = ?")
              .addParam(1, "Jack"); // 一次添加多个参数

Sql sql = dba.sql("select * from user")
              .append(" where id = ?").addParam(1) // 按条件来拼接
              .append(" and name = ?").addParam("Jack"); // 字符串拼接要注意空格
```

::: danger 注意
一定要使用参数化的方式来拼接 SQL 语句，避免 SQL 注入的风险
:::

```java
public List<User> findByName(String name) {
    // 错误的写法，这样拼接的 SQL 语句，容易被注入
    return dba.sql("select * from user where name like" + name).queryForList(User.class);
    // 正确的写法
    return dba.sql("select * from user where name like ?").addParam(name).queryForList(User.class);
}
```

## 推荐用法

基础用法表达了 Sql 对象的核心概念，就是一个参数化的数据库访问请求，包含了 SQL 语句和参数。但是直接使用字符串拼接 Sql，要自己写 `?` 占位符，手动添加参数，同时还要注意不要漏写空格，显得比较繁琐。

Sql 对象提供了很多 SQL 关键字相应的方法，如：

- `set` 方法用来拼接更新字段
- `where`, `and`, `or` 方法用来[拼接条件](/sql/cnd)
- `orderBy`, `groupBy` 方法用来排序和分组

通过链式调用，可以更优雅的编写 SQL 语句。
