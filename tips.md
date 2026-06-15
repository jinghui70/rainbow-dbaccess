# 规则与最佳实践

## 强制规则

1. **禁止用 `+` 拼接参数**——绝对不要用字符串拼接构造 SQL 参数值，必须使用 Cnd 或 `?` 占位符，防止 SQL 注入

2. **不要用 `+` 拼接 SQL 字符串**——多行 SQL 用 `"""` 文本块，动态拼接用 `append` 函数，保持代码可读性

## 陷阱

1. **Map 插入必须指定表名**——`dba.insert(Map)` 会抛异常，必须用 `dba.insertOf(map).into("TABLE").execute()`

2. **UpdateSql 所有 set 条件为 false 时生成无效 SQL**——如果所有 `set(boolean, ...)` 的条件都为 false，会生成 `UPDATE T SET WHERE ...`，执行时抛异常

3. **空集合作为 IN 值会抛异常**——`Cnd.where("ID", Op.IN, Collections.emptyList())` 在生成 SQL 时会报错

4. **count 优化在 DISTINCT/GROUP BY/UNION 时自动禁用**——如果需要强制禁用，调用 `disableCountOptimization()`

5. **BLOB/CLOB 大量读取性能**——一次性读取大量记录时，如果字段对应的数据结构极其复杂，JSON 反序列化每一行会很慢，建议用 String / JSONObject / JSONArray

6. **忘记调用 `execute()`**——`deleteFrom`、`update(table)`、`updateOf`、`insertOf` 等函数返回的是构建器，必须调用 `execute()` 才会真正执行，忘记调用不会报错但数据不会变更

## 最佳实践

1. **尽量写简单标准的 SQL**——保持代码的可移植性，不要写过于复杂的 SQL。复杂查询可以拆成几个简单查询，可以借助 MemoryDba，在 Java 中使用数据库的方式高效处理数据

2. **状态字段定义为 Enum**——业务概念清晰，查询条件可直接写 `where("STATUS", Status.ACTIVE)`。建议枚举在数据库中保存 name，查看数据库数据直观；其次继承 `CodeEnum` 使用 code

3. **Boolean 字段用 Boolean 类型**——数据库用 TINYINT/CHAR(1) 存 0/1，自动走 `BoolFieldMapper`，查询可直接写 `where("ACTIVE", true)`

4. **复杂对象存 BLOB/CLOB**——用 `@Column(sqlType = Types.BLOB)` 或 `@Column(sqlType = Types.CLOB)`，自动 JSON 序列化

5. **优先使用实体类而非 Map**——实体类有类型安全、自动映射、枚举处理等优势，Map 仅在动态场景使用

6. **部分更新用 UpdateBuilder**——`dba.updateOf(bean).excludeNull().execute()` 是最常见的"只更新非空字段"模式

7. **条件查询用条件开关**——`where(name != null, "NAME", name)` 比 if-else 包裹更清晰。框架已自动处理 WHERE/AND 的位置，**不要写 `where("1=1")` 这种丑陋的 hack**

8. **不需要 DO/VO/DTO 转换**——查询结果可直接映射为任意类，同一个查询可以映射为不同的 VO

9. **自定义 FieldMapper**——如果不想用默认的 JSON 保存，比如用分号分隔字符串保存列表，可以自己写 FieldMapper

10. **表名和字段名提取为常量**——将表名和字段名统一提取到常量类中，规避代码检查工具的重复字符串报警，同时重构安全、编译期检查、代码更可读：

```java
public class StrConst {
    public static final String USER_INFO = "USER_INFO";
    public static final String ID = "ID";
    public static final String NAME = "NAME";
    public static final String AGE = "AGE";
}

import static StrConst.*;

dba.select().from(USER_INFO)
    .where(NAME, "Alice")
    .and(AGE, Op.GT, 20)
    .queryForList(UserInfo.class);
```
