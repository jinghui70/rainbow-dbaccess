# 删除数据

## 统一入口

`delete` 方法支持 bean / bean 数组 / bean 集合，和 `insert` 的设计模式一致：

```java
dba.delete(existingUser);

dba.delete(new User[]{user1, user2});

dba.delete(userList);
```

## 按主键值删除

```java
dba.deleteByKey(User.class, "1");
```

## 条件删除

```java
dba.deleteFrom("T_USER").where("AGE", Op.LT, 18).execute();

dba.deleteFrom(User.class).where("NAME", null).execute();
```

::: danger 不要忘记 execute()
`deleteFrom` 返回的是构建器，必须调用 `execute()` 才会真正执行，忘记调用不会报错但数据不会变更。
:::
