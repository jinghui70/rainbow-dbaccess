# 规则与最佳实践

## 强制规则

1. **禁止用 `+` 拼接参数**——绝对不要用字符串拼接构造 SQL 参数值，必须使用 Cnd 或 `?` 占位符，防止 SQL 注入

2. **不要用 `+` 拼接 SQL 字符串**——长 SQL 用 """ 文本块——避免字符串拼接，保持可读性和格式对齐：

```java
  dba.sql("""
      SELECT u.*, d.NAME AS DEPT_NAME
      FROM T_USER u
      LEFT JOIN T_DEPT d ON u.DEPT_ID = d.ID
      WHERE u.STATUS = ? AND u.AGE > ?
      ORDER BY u.NAME
      """)
      .addParam(Status.ACTIVE, 18).queryForList(UserVO.class);
```

3. **禁止写 where("1=1")**——框架可以自动处理带开关的条件，无需关心是否添加 `where`

## 陷阱

1. **Map 插入必须指定表名**——`dba.insert(Map)` 会抛异常，必须用 `dba.insertOf(map).into("TABLE").execute()`

2. **UpdateSql 所有 set 条件为 false 时生成无效 SQL**——如果所有 `set(boolean, ...)` 的条件都为 false，会生成 `UPDATE T SET WHERE ...`，执行时抛异常

3. **空集合作为 IN 值会抛异常**——`Cnd.where("ID", Op.IN, Collections.emptyList())` 在生成 SQL 时会报错

4. **BLOB/CLOB 大量读取性能**——一次性读取大量记录时，如果字段对应的数据结构极其复杂，JSON 反序列化每一行会很慢，建议用 String / JSONObject / JSONArray

5. **忘记调用 `execute()`**——`deleteFrom`、`update(table)`、`updateOf`、`insertOf` 等函数返回的是构建器，必须调用 `execute()` 才会真正执行，忘记调用不会报错但数据不会变更

6. **`append` 拼接时注意前后空格**——`append` 是原样追加，不会自动添加空格。例如 `append("LEFT JOIN ...")` 可能会拼成 `... FROM T_USERLEFT JOIN ...`，要写成 ` append(" LEFT JOIN ...")`，开头带一个空格

7. **不要用数据库关键字做属性名**——例如用 `value` 做属性名，框架自动推导的列名为 `VALUE`，这是 SQL 关键字，执行时会报错。遇到这种情况用 `@Column(name)` 指定一个非关键字的列名

## 最佳实践

1. **尽量写简单标准的 SQL**——保持代码的可移植性，不要写过于复杂的 SQL。复杂查询可以拆成几个简单查询，可以借助 MemoryDba，在 Java 中使用数据库的方式高效处理数据

2. **优先使用 Class 而非字符串指定表名**——`from(User.class)` 比 `from("T_USER")` 更安全，表名由框架通过 `@Table` 注解或类名自动推导，重构时只需修改实体类，不会遗漏。适用于 `from(Class)`、`into(Class)`、`update(Class)`、`deleteFrom(Class)` 等所有支持 Class 参数的方法

3. **优先使用实体类而非 Map**——实体类有类型安全、自动映射、枚举处理等优势，Map 仅在动态场景使用

4. **条件查询用条件开关**——`where(name != null, "NAME", name)` 比 if-else 包裹更清晰。框架已自动处理 WHERE/AND 的位置

5. **不需要 DO/VO/DTO 转换**——查询结果可直接映射为任意类，同一个查询可以映射为不同的 VO

6. **状态字段定义为 Enum**——业务概念清晰，查询条件可直接写 `where("STATUS", Status.ACTIVE)`。建议枚举在数据库中保存 name，查看数据库数据直观；其次继承 `CodeEnum` 使用 code

7. **Boolean 字段用 Boolean 类型**——数据库用 TINYINT/CHAR(1) 存 0/1，自动走 `BoolFieldMapper`，查询可直接写 `where("ACTIVE", true)`

8. **复杂对象存 BLOB/CLOB**——用 `@Column(sqlType = Types.BLOB)` 或 `@Column(sqlType = Types.CLOB)`，自动 JSON 序列化

9. **部分更新用 UpdateBuilder**——`dba.updateOf(bean).excludeNull().execute()` 是最常见的"只更新非空字段"模式

10. **自定义 FieldMapper**——如果不想用默认的 JSON 保存，比如用分号分隔字符串保存列表，可以自己写 FieldMapper

11. **表名和字段名提取为常量**——将表名和字段名统一提取到常量类中，规避代码检查工具的重复字符串报警，同时重构安全、编译期检查、代码更可读：

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
12. **字符串拼接用 StringBuilderX**——当需要拼接带分隔符的字符串时（如动态 SQL 片段、CSV 等），`StringBuilderX` 的 `appendTemp` 机制自动处理分隔符位置，比手动判断"是不是第一个"更简洁。`join` 方法可以方便地将数组/集合用分隔符连接，`limit` 方法可以截断过长字符串
