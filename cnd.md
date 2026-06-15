# Cnd 条件系统

## 设计哲学

Cnd 的设计原则就是**简单**。多数情况下操作符都是 EQ，所以不写默认就是 EQ，而且对 EQ 做了智能推导——传 null 自动变 IS NULL，传集合自动变 IN，传 Range 自动变 BETWEEN。**让简单的事情更简单，减少使用者的心智负担。**

## Cnd 三要素

每个 Cnd 条件由三要素组成：**字段名、操作符、条件值**。

- **字段名**：数据库列名
- **操作符**：`Op` 枚举，不指定时默认为 `EQ`
- **条件值**：普通值、集合、Range、子查询 Sql 对象均可

## 操作符一览

| 操作符 | SQL | 说明 |
|--------|-----|------|
| `EQ` | `=` | 等于（默认，支持智能推导） |
| `NE` | `!=` | 不等于 |
| `GT` | `>` | 大于 |
| `GE` | `>=` | 大于等于 |
| `LT` | `<` | 小于 |
| `LE` | `<=` | 小于等于 |
| `LIKE` | `LIKE` | 模糊匹配（两端加 %） |
| `LIKE_LEFT` | `LIKE` | 左模糊（右侧加 %） |
| `LIKE_RIGHT` | `LIKE` | 右模糊（左侧加 %） |
| `NOT_LIKE` | `NOT LIKE` | 不模糊匹配 |
| `NOT_LIKE_LEFT` | `NOT LIKE` | 左不模糊匹配 |
| `NOT_LIKE_RIGHT` | `NOT LIKE` | 右不模糊匹配 |
| `IN` | `IN` | 在集合中 |
| `NOT_IN` | `NOT IN` | 不在集合中 |
| `IS_NULL` | `IS NULL` | 为空 |
| `IS_NOT_NULL` | `IS NOT NULL` | 不为空 |

## Cnd 的产生方式

Sql 的 `where`、`and`、`or` 函数都会自动产生 Cnd 对象，这是最常见的使用方式：

```java
dba.select().from("T_USER")
    .where("AGE", Op.GT, 20)
    .and("NAME", "Alice")
    .or("STATUS", Status.ACTIVE)
    .queryForList(User.class);
```

也可以通过 `Cnd.where()` 函数手动创建 Cnd，常用于创建**复合条件**分支：

```java
Cnd.or(
    Cnd.and(Cnd.where("A", v1), Cnd.where("B", v2)),
    Cnd.and(Cnd.where("C", v3), Cnd.where("D", v4))
)
// 生成: ((A=? AND B=?) OR (C=? AND D=?))
```

## 复合条件：嵌套组合

`Cnd.and()` / `Cnd.or()` 可以嵌套组合，生成任意复杂的条件表达式。复合条件会自动简化：0 个条件返回 null，1 个条件返回自身。

## 条件开关

```java
Cnd.where(name != null, "NAME", name)
Cnd.where(minAge != null, "AGE", Op.GT, minAge)
```

条件为 false 时返回 null，传入 `where(Cnd)` 时 null 被自动跳过。

## EQ 智能推导

当操作符为 EQ（默认）时，根据条件值类型自动推导：

| 传入值 | 推导结果 | 生成的 SQL |
|--------|---------|-----------|
| `Cnd.where("NAME", "Alice")` | EQ | `NAME=?` |
| `Cnd.where("NAME", null)` | IS NULL | `NAME IS NULL` |
| `Cnd.where("ID", Arrays.asList("1","2"))` | IN | `ID IN (?,?)` |
| `Cnd.where("AGE", Range.of(20, 30))` | BETWEEN | `AGE between ? and ?` |
| `Cnd.where("AGE", Range.of(30, null))` | >= | `AGE>=?` |
| `Cnd.where("AGE", Range.of(null, 50))` | <= | `AGE<=?` |
| `Cnd.where("AGE", Range.of(25, 25))` | = | `AGE=?` |
| `Cnd.where("NAME", Op.NE, null)` | IS NOT NULL | `NAME IS NOT NULL` |
| `Cnd.where("ID", Op.NE, list)` | NOT IN | `ID NOT IN (?,?)` |

## LIKE 变体

```java
Cnd.where("NAME", Op.LIKE, "lice")         // %lice%
Cnd.where("NAME", Op.LIKE_LEFT, "Ali")     // Ali%
Cnd.where("NAME", Op.LIKE_RIGHT, "ice")    // %ice
Cnd.where("NAME", Op.NOT_LIKE, "Alice")    // %Alice%
```

如果值本身已包含 `%`，则直接使用，不再额外添加。

## IN 条件中的 null 处理

```java
Cnd.where("NAME", Op.IN, Arrays.asList("Alice", null))
// 生成: (NAME=? OR NAME IS NULL)
```

IN 中的 null 会额外生成 `OR IS NULL` 条件。NOT IN 中的 null 被忽略（因为 `NOT IN NULL` 没有实际意义）。

## 子查询作为条件值

条件值也可以是子查询 Sql 对象：

```java
Sql sub = dba.select("ID").from("T_USER").where("NAME", "Alice");
dba.select().from("T_ORDER")
    .where(Cnd.where("USER_ID", Op.IN, sub))
    .queryForList(Order.class);
```
