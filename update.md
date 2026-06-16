# 更新数据

## 三种更新模式

Rainbow DBAccess 提供三种更新模式，从简到繁覆盖全部场景：

### 1. Bean 全量更新——最简单

```java
dba.update(new User("1", "NewName", 30, 200.0));
```

以主键生成 WHERE 条件，更新所有非主键字段。

### 2. UpdateBuilder——部分更新

当只想更新部分字段时：

```java
dba.updateOf(partial).include("name").execute();

dba.updateOf(partial).exclude("score").execute();

dba.updateOf(partial).excludeNull().execute();
```

- `include`：仅更新指定字段
- `exclude`：排除指定字段
- `excludeNull`：null 字段不参与 SET

`include`/`exclude` 互斥，同时只能使用一个，但是可以和`excludeNull()`一起使用。

部分更新用 UpdateBuilder，避免覆盖其他字段的值，特别是在并发场景下，只更新变化的字段更安全。

### 3. UpdateSql——手动指定 SET

当需要字段计算或精确控制时：

```java
dba.update("T_USER")
    .set("NAME", "NewName")
    .set("AGE", 99)
    .set("COUNT=COUNT+1")
    .where("ID", "1")
    .execute();
```

支持条件开关：

```java
dba.update("T_USER")
    .set(name != null, "NAME", name)
    .set(needIncrement, "COUNT=COUNT+1")
    .where("ID", "1")
    .execute();
```

支持 FieldMapper（BLOB/CLOB 等复杂类型）：

```java
dba.update("T_USER")
    .set("TAGS", ObjectFieldMapper.ofList(String.class), tagList)
    .where("ID", "1")
    .execute();
```

条件开关与 FieldMapper 可以组合使用：

```java
dba.update("T_USER")
    .set(tags != null, "TAGS", ObjectFieldMapper.ofList(String.class), tags)
    .where("ID", "1")
    .execute();
```

枚举和 Boolean 直接使用即可，不需要 FieldMapper：

```java
dba.update("T_ENUM")
    .set("STATUS", Status.PENDING)
    .where("ID", "e1")
    .execute();
```

支持 Map 批量设置：

```java
dba.update("T_USER").setMap(Map.of("NAME", "X", "AGE", 20)).where("ID", "1").execute();
```

为什么需要三种模式？更新的场景最复杂：有时候你有一个完整对象想直接更新，有时候只想改几个字段，有时候需要做字段计算。三种模式覆盖了从简单到复杂的全部场景。
