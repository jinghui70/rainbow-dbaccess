# 插入数据

## 最简方式

```java
dba.insert(new User("1", "Alice", 25, 100.0));
```

## 批量插入

```java
User[] users = {new User("1", "A", 20, 80.0), new User("2", "B", 30, 90.0)};
dba.insert(users);

List<User> userList = Arrays.asList(new User("1", "X", 22, 70.0), new User("2", "Y", 28, 75.0));
dba.insert(userList);
```

## 构建器模式

需要更多控制时使用构建器：

```java
dba.insertOf(bean).into("OTHER_TABLE").execute();

dba.insertOf(beans).batchSize(500).execute();

dba.insertOf(map).into("T_USER").execute();
```

::: warning Map 插入必须指定表名
因为 Map 没有类信息来推导表名，`dba.insert(Map)` 会抛异常，必须用 `dba.insertOf(map).into("TABLE").execute()`。
:::

## MERGE（Insert Or Update）

```java
dba.merge(new User("1", "NewName", 30, 200.0));

dba.mergeOf(data).into("T_USER").execute();
```

::: warning 注意
不是所有数据库都支持 MERGE 语法。
:::

## 手动拼写 SQL

如果不想定义 Bean，也不愿意用 Map 插入，可以直接拼写 SQL：

```java
dba.sql("INSERT INTO T_USER(ID,NAME,AGE) VALUES(?,?,?)")
    .addParam("1", "Alice", 25).execute();
```
