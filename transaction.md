# 事务管理

Dba 封装了事务操作，所有数据库操作都从 Dba 开始，不需要再去想"事务要用 TransactionTemplate"：

```java
dba.transaction(() -> {
    dba.insert(user1);
    dba.insert(user2);
});

int result = dba.transaction(status -> {
    dba.insert(user);
    return dba.select().from("T_USER").count();
});
```
