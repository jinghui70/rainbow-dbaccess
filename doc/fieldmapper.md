# FieldMapper：让 Java 属性更有业务意义

## 设计哲学

传统 ORM 要求数据库列类型和 Java 属性类型直接对应。但现实中，数据库里存的往往不是 Java 想要的类型——比如 CHAR(8) 存日期、TINYINT 存布尔、VARCHAR 存枚举编码。FieldMapper 就是解决这个问题的：**在 ORM 映射时自动转换，让 Java 对象属性更有业务意义，减少大量转换代码。**

FieldMapper 是双向转换的单一职责抽象：`formDB(ResultSet, index)` 从数据库读取，`saveToDB(PreparedStatement, paramIndex, value)` 写入数据库。通过 `FieldValue` 桥接——`PropInfo.getValue()` 返回 `FieldValue`（包装了原始值 + mapper），`DbaUtil.setParameterValue()` 在设置参数时能识别并委托给 mapper，调用方不需要知道映射细节。

## 内置 FieldMapper

| FieldMapper | Java 类型 | 数据库类型 | 转换规则 |
|-------------|----------|-----------|---------|
| `BoolFieldMapper` | `Boolean` | `TINYINT` (1/0) | `true→1, false→0` |
| `BoolYN` | `Boolean` | `CHAR` (Y/N) | `true→"Y", false→"N"` |
| `EnumFieldMapper` | 枚举 | `VARCHAR`/`INT` | 见下方枚举映射 |
| `BlobStringFieldMapper` | `String` | `BLOB` | 大文本存为 BLOB |
| `BlobByteArrayFieldMapper` | `byte[]` | `BLOB` | 字节数组存为 BLOB |
| `BlobObjectFieldMapper` | 任意对象 | `BLOB` | JSON 序列化存为 BLOB |
| `ObjectFieldMapper` | 任意对象 | `CLOB`/`VARCHAR` | JSON 序列化存为 CLOB |

## 约定优先：自动推导

无注解时，FieldMapper 按类型自动推导：
- 枚举字段 → `EnumFieldMapper`
- Boolean 字段 → `BoolFieldMapper`（数据库存 1/0）
- `@Column(sqlType = Types.BLOB)` 时，根据字段类型自动选择：`String` → `BlobStringFieldMapper`，`byte[]` → `BlobByteArrayFieldMapper`，其他 → `BlobObjectFieldMapper`
- `@Column(sqlType = Types.CLOB/VARCHAR)` 时，`String` 不需要特殊处理，其他 → `ObjectFieldMapper`

BLOB/CLOB 字段如果属性类型是 String / JSONObject / JSONArray / Array / Collection / Map / Bean，默认都会配置对应的预置 FieldMapper 进行处理。

## 枚举映射

```java
public enum Status { ACTIVE, INACTIVE, PENDING }
public enum Color implements CodeEnum { RED("R"), GREEN("G"), BLUE("B"); ... }
```

| 策略 | 接口 | 数据库存储 | 适用场景 |
|------|------|-----------|---------|
| name() | 无（默认） | 枚举名称字符串 | 枚举值可读性优先 |
| code() | `CodeEnum` | `code()` 返回值 | 数据库用缩写编码 |

**建议：状态类字段都定义为 Enum**，这样业务概念清晰，查询条件可直接写 `where("STATUS", Status.ACTIVE)`，非常业务化可读。建议枚举在数据库中保存 name，查看数据直观；其次继承 `CodeEnum` 使用 code。

## Boolean 映射

无注解的 Boolean 字段自动走 `BoolFieldMapper`（存 1/0）。建议数据库用 TINYINT或CHAR(1) 存 0/1。如果数据库用 Y/N：

```java
@Column(mapper = BoolYN.class)
private Boolean active;
```

BoolYN 字段查询时：`where(field, new BoolYN().ofValue(true))` 或直接 `where(field, "Y")`，但不如 `where(field, true)` 看着方便，所以建议优先用 `BoolFieldMapper`。

## BLOB/CLOB 对象序列化

将复杂对象 JSON 序列化后存入数据库：

```java
@Column(sqlType = Types.BLOB)
private String lobString;

@Column(sqlType = Types.BLOB)
private byte[] lobBytes;

@Column(sqlType = Types.BLOB)
private ObjectBlob lobObject;

@Column(sqlType = Types.CLOB)
private List<String> tags;

@Column(sqlType = Types.CLOB)
private Map<String, Integer> attributes;
```

BLOB 支持压缩：`@Column(sqlType = Types.BLOB, compress = true)`

::: warning 性能注意
如果一次性读取记录特别多，且字段对应的数据结构极其复杂，建议使用 String / JSONObject / JSONArray 类型，因为底层使用 Hutool 的 JSON 处理机制反序列化每一行对象会很慢。
:::

## 自定义 FieldMapper

如果不想用默认的 JSON 保存方式，比如一个字符串列表想用分号分隔的字符串保存，可以自己写一个 FieldMapper：

```java
public class SemicolonListMapper extends FieldMapper<List<String>> {
    @Override
    public List<String> formDB(ResultSet rs, int index) throws SQLException {
        String str = rs.getString(index);
        if (str == null) return null;
        return Arrays.asList(str.split(";"));
    }

    @Override
    public void saveToDB(PreparedStatement ps, int paramIndex, Object value) throws SQLException {
        ps.setString(paramIndex, String.join(";", (List<String>) value));
    }
}
```

## 枚举和 Boolean 在条件/更新中直接使用

`DbaUtil.enumCheck()` 是统一的枚举转换入口，所有通过 `addParam` 传入的参数都会自动处理。所以在 `where`、`set` 等操作中，枚举和 Boolean 值可以直接使用，不需要手动转换：

```java
dba.select().from("T_USER").where("STATUS", Status.ACTIVE).queryForList(User.class);

dba.update("T_USER").set("STATUS", Status.PENDING).where("ID", "1").execute();

dba.update("T_USER").set("ACTIVE", true).where("ID", "1").execute();
```

`EnumFieldMapper` 只在**读取查询结果**时才需要——当不使用实体映射，而是用 `queryForValue` 或 `MapRowMapper` 读取单列枚举值时：

```java
Status s = dba.select("STATUS").from("T_USER")
    .where("ID", "1").queryForValue(EnumFieldMapper.of(Status.class));

Map<String, Object> map = dba.select().from("T_USER").where("ID", "1")
    .queryForObject(MapRowMapper.create()
        .setFieldMapper("STATUS", EnumFieldMapper.of(Status.class)));
```

在实体映射中不需要手动指定 `EnumFieldMapper`，`PropInfoCache` 会根据字段类型自动配置。

## FieldMapper 手动使用场景

FieldMapper 在 SQL 构建中手动使用的典型场景是 BLOB/CLOB 类型的写入和读取：

```java
dba.update("T_USER")
    .set("TAGS", ObjectFieldMapper.ofList(String.class), tagList)
    .where("ID", "1").execute();

List<String> tags = dba.select("TAGS").from("T_USER")
    .where("ID", "1").queryForValue(ObjectFieldMapper.ofList(String.class));
```

## FieldMapper 工厂方法

`ObjectFieldMapper` 和 `BlobObjectFieldMapper` 提供了便捷的工厂方法：

```java
ObjectFieldMapper.ofList(String.class)
ObjectFieldMapper.ofArray(String.class)
ObjectFieldMapper.ofMap(String.class)
ObjectFieldMapper.of(MyClass.class)
BlobObjectMapper.ofList(String.class)
BlobObjectFieldMapper.ofMap(Integer.class)
```

对于复杂泛型（如 `Map<String, List<Integer>>`），使用 `TypeReference`：

```java
TypeReference<Map<String, List<Integer>>> typeRef = new TypeReference<>() {};
ObjectFieldMapper.ofMap(typeRef.getType())
```
