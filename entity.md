# 实体映射：约定大于配置

## 核心注解

| 注解 | 作用 | 默认行为 |
|------|------|----------|
| `@Table(name)` | 指定表名 | 类名转 UPPER_SNAKE_CASE（如 `UserInfo` → `USER_INFO`） |
| `@Id` | 标记主键字段 | 支持多字段复合主键 |
| `@Id(autoIncrement=true)` | 自增主键 | 插入时自动跳过该字段 |
| `@Column(name)` | 自定义列名 | 属性名转 lower_snake_case（如 `userName` → `user_name`） |
| `@Column(sqlType)` | 指定 SQL 类型 | BLOB/CLOB 类型自动选择映射器 |
| `@Column(mapper)` | 自定义 FieldMapper | 无注解时按类型自动推导 |
| `@GeneratedValue` | 插入或更新时自动生成字段值（可配置 timing） | 雪花 id，详见[自动生成字段值](/generated-value) |
| `@Transient` | 排除字段 | 不参与任何数据库交互 |

## 最简实体

```java
public class UserInfo { // 默认表名: USER_INFO
    @Id
    private String id;
    private String name;
    private Integer age;
    private Double finalScore; // 默认字段名: FINAL_SCORE
}
```

不需要 XML 映射文件，不需要额外注解，属性名自动转下划线列名，直接就能用。

## 复合主键

```java
public class ComplexKeyEntity {
    @Id
    private String keyA;
    @Id
    private String keyB;
    private String data;
}
```

使用时按声明顺序传入主键值：`dba.selectByKey(ComplexKeyEntity.class, "A", "B")`

## 自增主键

```java
public class AutoEntity {
    @Id(autoIncrement = true)
    private Integer id;
    private String name;
}
```

插入时自动跳过 id 字段，由数据库生成。

## 插入时自动生成字段值

主键 id、创建时间、更新时间这类值由程序生成而非业务录入。在字段上标注 `@GeneratedValue`，插入或更新时自动生成并**回填到对象本身**，不必在每个 Service 里手写赋值：

```java
public class User {
    @Id
    @GeneratedValue(param = "USR_")                       // 雪花 id 主键，String 带前缀
    private String id;

    @GeneratedValue(strategy = "now")                     // 插入时记录创建时间
    private LocalDateTime createTime;

    @GeneratedValue(strategy = "now", timing = GenerationTiming.INSERT_UPDATE)
    private LocalDateTime updateTime;                     // 插入和更新时都自动刷新
}

User user = new User();
dba.insert(user);
String id = user.getId();  // 已回填，如 "USR_2J9K7X..."

user.setName("Updated");
dba.update(user);          // updateTime 自动刷新为当前时间
```

内置 `default`（雪花 id）和 `now`（当前时间）两种策略，也可实现 `ValueGenerator` 自定义。`timing` 参数控制生成时机：默认 `INSERT` 仅插入时生成，`INSERT_UPDATE` 在更新时也强制重新生成（适用于 updateTime、版本号等）。完整说明见[自动生成字段值](/generated-value)。

## 查询结果可映射为任意对象

不需要定义专门的 DO/VO/DTO 再转来转去。同一个查询，可以映射为不同的类：

```java
List<User> users = dba.select().from("T_USER").queryForList(User.class);
List<UserSimpleVO> simpleList = dba.select("ID", "NAME").from("T_USER").queryForList(UserSimpleVO.class);
Map<String, Object> map = dba.select().from("T_USER").where("ID", "1").queryForMap();
```
