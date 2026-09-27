# 原始 SQL

当需要完全控制 SQL 时：

```java
dba.sql("SELECT * FROM T_USER WHERE ID=?").addParam("1").queryForObject(User.class);

dba.sql("INSERT INTO T_USER(ID,NAME) VALUES(?,?)").addParam("1", "Alice").execute();

dba.sql("INSERT INTO T_USER(ID,NAME,AGE) VALUES(?,?,?)")
    .batchUpdate(List.of(
        new Object[]{"a", "A", 20},
        new Object[]{"b", "B", 30}
    ));

dba.sql("INSERT INTO T_USER(ID,NAME,AGE) VALUES(?,?,?)")
    .batchUpdate(largeList, 500);
```
