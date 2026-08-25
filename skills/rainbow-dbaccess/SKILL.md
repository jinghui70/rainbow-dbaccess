---
name: rainbow-dbaccess
description: >
  Use for any database access code -- CRUD, queries, pagination, transactions,
  entity mapping, SQL. All database operations go through Dba.
---

# Rainbow DBAccess 使用指南

> 本 skill 由 [rainbow-dbaccess](https://github.com/jinghui70/rainbow-dbaccess) 库项目维护，随库演进；本文件是消费项目副本，升级 dba 依赖版本时从库项目同步。文档已对照源码逐类核对，行为语义由库项目测试套件实测覆盖；用法问题以本文为终点，不必查源码。

## 使用流程

写或改任何数据库代码前，先过一遍下方强制规则；写完对照"常见陷阱"表。

## 强制规则（最高优先级）

1. **所有数据库操作必须通过 `Dba` 进行** 见到非 `Dba` 的数据库写法，主动给出 `Dba` 等价实现。
2. **禁止用 `+` 拼接 SQL 参数。** 必须使用 `?` 占位符，防止 SQL 注入。
3. **禁止写 `where("1=1")`。** 用条件开关 `where(condition, field, value)` 代替。
4. **平铺 `AND` 条件走链式。** 多个平铺 `AND` 在 `Dba` 查询链上 `.where().and().and()` 顺次连接；`Cnd.and(...)` 复合条件仅用于含 `OR` 的嵌套逻辑（详见 [Cnd 条件](#cnd-条件)）。

## 核心理念

- **SQL 是第一公民**--SQL 不与代码分离，链式写法优于 XML 和字符串拼接
- **约定大于配置**--属性名自动转下划线列名，无需映射文件
- **Dba 是统一入口**--所有操作从 `Dba` 开始

## 快速开始

### 注入

```java
@Autowired
private Dba dba;
```

数据库方言自动识别：H2/MySQL -> Default（LIMIT）、PostgreSQL/Kingbase -> PostgreSQL（OFFSET）、Oracle -> ROWNUM。分页 SQL 自动按方言包装，无需关心。

## 实体映射

```java
public class UserInfo {       // 默认表名: USER_INFO
    @Id
    private String id;
    private String userName;  // 默认列名: USER_NAME
    private Integer age;
}
```

| 注解                      | 作用                                                    |
| ------------------------- | ------------------------------------------------------- |
| `@Table(name)`            | 自定义表名（默认类名转 UPPER_SNAKE_CASE）               |
| `@Id`                     | 主键，支持复合主键（多个字段标 `@Id`）                  |
| `@Id(autoIncrement=true)` | 自增主键，插入时自动跳过                                |
| `@Column(name)`           | 自定义列名（默认属性名转 lower_snake_case）             |
| `@Column(mapper)`         | 自定义 FieldMapper                                      |
| `@Column(sqlType)`        | 指定 SQL 类型（BLOB/CLOB 自动选映射器）                 |
| `@GeneratedValue`         | 插入或更新时自动生成字段值（可配置 timing，回填到对象） |
| `@Transient`              | 排除字段                                                |

**列名匹配规则**：结果集列名去空格转小写后与下划线列名比对，**大小写不敏感**（`AS Name` 别名也能映射到 `name` 属性）；但风格敏感（列 `userId` 映射不到属性 `userName` 对应的 `user_name`，别名需转下划线风格）。

### 枚举映射

- **普通枚举**：存取 `name()`，读到不认识的值抛异常
- **CodeEnum 枚举**：实现 `code()` 接口后存 `code()`；**读到不认识的 code 静默返回 null**（不抛异常）

```java
public enum Status implements CodeEnum {
    ACTIVE("01"), INACTIVE("02");
    private final String code;
    Status(String code) { this.code = code; }
    public String code() { return code; }
}
// where("STATUS", Status.ACTIVE) 自动传 "01"；queryForObject 映射 "01" -> Status.ACTIVE
```

## 查询

```java
// 单条
dba.select().from("T_USER").where("ID", "1").queryForObject(User.class);

// 列表
dba.select().from(User.class).where("AGE", Op.GT, 20).queryForList(User.class);

// 条件开关（避免 if-else 打断链式调用）
dba.select().from("T_USER")
    .where(StrUtil.isNotBlank(name), "NAME", name)
    .and(minAge > 0, "AGE", Op.GT, minAge)
    .queryForList(User.class);

// 分页
PageData<User> page = dba.select().from("T_USER")
    .orderBy("ID").queryPage(User.class, pageNo, pageSize);

// 计数 / 存在判断
int total = dba.select().from(User.class).count();
boolean exists = dba.select().from("T_USER").where("NAME", "Alice").exist();

// JOIN（from 接受原始片段）
dba.select("P.*", "O.QTY")
    .from("PRODUCT P LEFT JOIN ORDERS O ON P.ID=O.PRODUCT_ID")
    .where("O.QTY", Op.GT, 0).queryForList(ProductVO.class);

// 限制行数
dba.select().from("T_LOG").orderBy("ID").limit(100).queryForList(Log.class);
```

**count/exist/queryPage 自动优化**：SQL 不含 DISTINCT/GROUP BY/UNION 时，`count()` 去掉 SELECT 列和 ORDER BY 生成 `SELECT COUNT(*) FROM ...`，`exist()` 走 `SELECT 1 ... LIMIT 1`；含上述关键字时自动包子查询计数。`disableCountOptimization()` 可强制关闭。`queryPage` 在 count=0 或页码超范围时直接返回空数据（带 total），不再查列表。

**queryForMap()**：单行 Map，无数据返回**空 Map**（非 null）。

### 结果转 Map / 分组

```java
// key 从 ResultSet 提取（写列名）；value 映射为实体
Map<String, User> byId = dba.select().from(User.class)
    .queryToMap(rs -> rs.getString("ID"), User.class);

// value 是单列
Map<String, String> idName = dba.select("ID", "NAME").from("T_USER")
    .queryToMap(rs -> rs.getString("ID"), rs -> rs.getString("NAME"));

// 保序：Supplier 重载传 LinkedHashMap（仅 Class/RowMapper 版有此重载）
Map<String, User> ordered = dba.select().from(User.class).orderBy("ID")
    .queryToMap(rs -> rs.getString("ID"), User.class, LinkedHashMap::new);

// 一对多分组
Map<Long, List<Order>> byUser = dba.select().from("T_ORDER")
    .queryToGroup(rs -> rs.getLong("USER_ID"), Order.class);
```

- keyFunc/valueFunc 参数是 **`ResultSet`**（lambda 内可抛 SQLException），取值写**列名/列序号**，不是属性名
- **重复 key 后行覆盖前行**（Map.put 语义，静默）
- 默认 `HashMap` 无序；`(keyFunc, Class/RowMapper, Supplier<Map>)` 重载可传 `LinkedHashMap::new` 保序。**没有** (keyFunc, valueFunc, Supplier) 这个组合
- value 的四种给法：`Class` / `RowMapper` / `ResultSetFunction` / 不传（value 为列名 Map）

## Cnd 条件

Sql对象的 `where` `and` `or` 函数会自动产生 Cnd 条件对象。

**EQ 智能推导**（默认操作符为 EQ；NE 同理，null -> IS NOT NULL、集合 -> NOT IN）：

| 传入值                 | 生成 SQL                                |
| ---------------------- | --------------------------------------- |
| `"Alice"`              | `NAME=?`                                |
| `null`                 | `NAME IS NULL`                          |
| `List.of("1","2")`     | `ID IN (?,?)`                           |
| `Range.of(20, 30)`     | `AGE BETWEEN ? AND ?`                   |
| `Range.of(30, null)`   | `AGE>=?`                                |
| `Range.of(null, 30)`   | `AGE<=?`                                |
| `Range.of(20, 20)`     | `AGE=?`（from==to 塌缩为等值）          |
| `Map`（含 from/to 键） | 按 Range 处理（前端 JSON 传区间的入口） |

**IN 列表智能处理**（EQ/NE/IN/NOT_IN 传集合或数组时）：

- 元素含 null：null 被过滤，IN 自动补 `OR field IS NULL`；全为 null 生成 `IS NULL`；NOT_IN 不补
- 单元素：塌缩为 `field=?`（NOT_IN 为 `!=`）
- 元素超 1000：**自动拆分多组**，IN 用 OR、NOT_IN 用 AND 连接（规避 Oracle ORA-01795）
- 空集合：报错（见陷阱表）

**操作符：** `EQ NE GT GE LT LE LIKE LIKE_LEFT LIKE_RIGHT NOT_LIKE NOT_LIKE_LEFT NOT_LIKE_RIGHT IN NOT_IN IS_NULL IS_NOT_NULL`

**LIKE 语义**（值 "abc" 为例）：

| 操作符       | 生成参数 | 含义     |
| ------------ | -------- | -------- |
| `LIKE`       | `%abc%`  | 两侧模糊 |
| `LIKE_LEFT`  | `abc%`   | 前缀匹配 |
| `LIKE_RIGHT` | `%abc`   | 后缀匹配 |

值自带 `%` 开头/结尾时原样使用，不再包。

**复合条件：**

仅在复杂逻辑情况下，才使用复合条件：

```java
Cnd.or(
    Cnd.and(Cnd.where("A", v1), Cnd.where("B", v2)),
    Cnd.and(Cnd.where("C", v3), Cnd.where("D", v4))
)
// ((A=? AND B=?) OR (C=? AND D=?))
```

**子查询**（不限 IN，比较运算符都可用；value 传 `Sql` 即可）：

```java
Sql sub = dba.select("ID").from("T_USER").where("NAME", "Alice");
dba.select().from("T_ORDER").where("USER_ID", Op.IN, sub).queryForList(Order.class);
dba.select().from("T_ORDER").where("QTY", Op.GT, dba.select("MAX(QTY)").from("T_ORDER"));
// QTY>(SELECT MAX(QTY) FROM T_ORDER)
```

## 插入

```java
dba.insert(user);                         // 单条
dba.insert(userList);                     // 批量（一次 addBatch）
dba.insertOf(beans).into("OTHER").batchSize(500).execute(); // 分批提交
dba.insertOf(map).into("T_USER").execute(); // Map 插入（必须指定表名）
dba.merge(user);                           // Insert or Update（MERGE INTO，仅部分库支持，如 H2）
```

- `into(String)` / `into(Class)`；表名只接受 `table`/`schema.table` 合法标识符（**不能写 JOIN**）
- 空集合插入直接返回，不发 SQL；失败抛 DataAccessException（不返回行数）
- **Map 批量插入列以第一条 Map 的 keySet 为准**，后续行缺的 key 静默插 null--各行 key 必须一致
- `batchSize`：0（默认）= 最后一次性 executeBatch；>0 = 每 N 条 executeBatch 一次

### @GeneratedValue -- 插入或更新时自动生成字段值

标注在实体字段上，根据 `timing` 配置在插入和/或更新时自动生成并**回填到对象**（仅 Bean 操作生效，Map 不处理）。批量操作逐行生成。

```java
public class User {
    @Id @GeneratedValue(param = "USR_")                   // 雪花 id，String 带前缀；long/Long 则为数字 id
    private String id;

    @GeneratedValue(strategy = "now")                     // 默认 timing=INSERT，仅插入时生成
    private LocalDateTime createTime;

    @GeneratedValue(strategy = "now", timing = GenerationTiming.INSERT_UPDATE)
    private LocalDateTime updateTime;                     // 插入和更新时都生成
}
dba.insert(user);   // id, createTime, updateTime 都被生成并回填
dba.update(user);   // 仅 updateTime 重新生成（强制覆盖，即使手动设置也会被替换）
```

**timing 参数**（`GenerationTiming` 枚举）：

- `INSERT`（默认）：仅插入时生成（字段为 null 才生成，已有值不覆盖）。适用于主键 id、创建时间 createTime、创建人 createBy。
- `INSERT_UPDATE`：插入时生成（字段为 null 才生成），**更新时强制重新生成并覆盖对象当前值**。适用于更新时间 updateTime、更新人 updateBy、版本号 version。

内置策略：

- `default`（默认）：雪花 id。String 字段 = `param` 前缀 + 36 进制大写；long/Long = 数字 id。workerId/datacenterId 取 `-D` 参数 -> 环境变量 `SNOWFLAKE_WORKER_ID`/`SNOWFLAKE_DATACENTER_ID` -> 0
- `now`：当前时间。按字段类型返回 LocalDateTime/Timestamp/Date；**String 字段按 `param` 指定的日期格式格式化**（默认 `yyyy-MM-dd HH:mm:ss`）

自定义策略：实现 `ValueGenerator`（`getName()` + `generate(GenerateContext)`，`GenerateContext` 含 `dba`/`data`/`field`/`param`，dba 可用于查库生成如序列号）。Spring 下在类上标 `@Component` 自动注册；否则 `ValueGeneratorRegistry.register(gen)`。

## 更新

```java
// 全量更新（按主键）
dba.update(user);

// 部分更新
dba.updateOf(bean).include("name").execute();
dba.updateOf(bean).excludeNull().execute();
dba.updateOf(bean).into("T_USER_2024").execute();  // 更新同结构的另一张表

// 手动指定 SET
dba.update("T_USER")
    .set("NAME", "NewName")
    .set("COUNT=COUNT+1")
    .set(tags != null, "TAGS", ObjectFieldMapper.ofList(String.class), tags)
    .where("ID", "1").execute();

// 枚举/Boolean 直接用，不需要手动转换
dba.update("T_USER").set("STATUS", Status.ACTIVE).where("ID", "1").execute();
dba.update("T_USER").set("ACTIVE", true).where("ID", "1").execute();
```

**注意参数风格差异**：`updateOf(bean).include("userName")` 传 **Java 属性名**（驼峰）；`update("T_USER").set("USER_NAME", ...)` 传**数据库列名**（下划线）。两者别混用。

## 删除

```java
dba.delete(user);
dba.deleteByKey(User.class, "1");
dba.deleteFrom("T_USER").where("AGE", Op.LT, 18).execute();
```

- `delete(data)` 支持 bean / 数组 / 集合；批量按主键**逐条执行**，返回受影响行数合计
- 复合主键：`selectByKey`/`deleteByKey` 的 keys 按 `@Id` 字段**声明顺序**传入

## 事务

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

## 原始 SQL

```java
dba.sql("SELECT * FROM T_USER WHERE ID=?").addParam("1").queryForObject(User.class);
dba.sql("INSERT INTO T_USER(ID,NAME) VALUES(?,?)").addParam("1", "Alice").execute();
dba.sql("INSERT INTO T_USER(ID,NAME,AGE) VALUES(?,?,?)")
    .batchUpdate(largeList, 500);
```

## FieldMapper

解决 Java 类型与数据库列类型不匹配的问题，在 ORM 映射时自动双向转换。

**内置 FieldMapper：**

| FieldMapper                | Java 类型 | 数据库           | 说明                                                |
| -------------------------- | --------- | ---------------- | --------------------------------------------------- |
| `BoolFieldMapper`（默认）  | `Boolean` | `TINYINT` 1/0    | 无注解自动推导                                      |
| `BoolYN`                   | `Boolean` | `CHAR` Y/N       | `@Column(mapper=BoolYN.class)`                      |
| `EnumFieldMapper`          | 枚举      | `VARCHAR`        | 无注解自动推导；CodeEnum 存 code                    |
| `ObjectFieldMapper`        | 任意对象  | `CLOB`/`VARCHAR` | JSON 序列化                                         |
| `BlobObjectFieldMapper`    | 任意对象  | `BLOB`           | JSON 序列化 + 可压缩（GZIP）                        |
| `BlobStringFieldMapper`    | `String`  | `BLOB`           | UTF-8 字节，`@Column(sqlType=BLOB)` String 字段自动 |
| `BlobByteArrayFieldMapper` | `byte[]`  | `BLOB`           | 原始字节，byte[] 字段自动                           |

**BLOB/CLOB 示例：**

```java
@Column(sqlType = Types.CLOB)
private List<String> tags;

@Column(sqlType = Types.BLOB, compress = true)
private Map<String, Object> attributes;
```

**手动使用（非实体映射场景）：**

```java
dba.update("T_USER")
    .set("TAGS", ObjectFieldMapper.ofList(String.class), tagList)
    .where("ID", "1").execute();

List<String> tags = dba.select("TAGS").from("T_USER")
    .where("ID", "1").queryForValue(ObjectFieldMapper.ofList(String.class));
```

**工厂方法：** `ObjectFieldMapper.ofList/ofArray/ofMap(Class)` `BlobObjectFieldMapper.ofList/ofMap(Class)`；复杂泛型用 `ofMap(Type)` / `ofType(Type)`（传 `new TypeReference<...>(){}` 之类的 Type）。

**自定义 FieldMapper：**

```java
public class SemicolonListMapper extends FieldMapper<List<String>> {
    @Override
    public List<String> formDB(ResultSet rs, int index) throws SQLException {
        String str = rs.getString(index);
        return str == null ? null : Arrays.asList(str.split(";"));
    }
    @Override
    public void saveToDB(PreparedStatement ps, int paramIndex, Object value) throws SQLException {
        ps.setString(paramIndex, String.join(";", (List<String>) value));
    }
}
```

**MapRowMapper -- Map 结果的定制**（`queryForList()`/`queryForMap()` 默认用它）：

```java
MapRowMapper mapper = MapRowMapper.create()
    .ignore("PASSWORD")      // 忽略指定列（大小写不敏感）
    .ignoreNull()            // null 值列不进 Map
    .setFieldMapper("TAGS", ObjectFieldMapper.ofList(String.class)) // 指定列映射器（也可按列序号）
    .post(m -> m.remove("TMP")); // 每行 Map 的后处理
List<Map<String, Object>> rows = dba.select().from("T_USER").queryForList(mapper);

// CamelCaseMapMapper：列名转驼峰 key + LinkedHashMap 保序（USER_NAME -> userName）
List<Map<String, Object>> rows2 = dba.select().from("T_USER").queryForList(new CamelCaseMapMapper());
```

## 树形数据

**`queryForTree` 的三个硬性要求：**

1. 实体实现 `ITreeNode<T>`（getChildren/setChildren/addChild；或直接继承 `TreeNode<T>`，children 已标 `@Transient`）
2. 结果集**列名必须是 ID、PID**（不是这两个列名的用 `AS ID` / `AS PID` 别名）
3. PID 在结果集中找不到父节点的记录作为根节点

```java
public class OrgNode extends TreeNode<OrgNode> {
    @Id private String id;
    private String pid;
    private String name;
    // getter/setter
}

Tree<OrgNode> tree = dba.select("ORG_ID AS ID", "PARENT_ID AS PID", "NAME")
    .from("T_ORG").queryForTree(OrgNode.class);
tree.getRoots();     // 根节点列表
tree.getNode("2");   // 按 ID 取任意节点（getNodeMap() 拿全量映射）
```

**TreeUtils -- 树工具：**

```java
TreeUtils.traverse(roots, node -> ...);                      // 前序遍历
TreeUtils.traverse(roots, node -> ..., false);               // 后序遍历
TreeUtils.traverse(roots, (node, parent, level) -> ...);     // 带父节点和层级（根 level=1）
TreeUtils.transform(roots, vo -> new VoNode(vo));            // 树节点类型转换
TreeUtils.filter(roots, n -> "启用".equals(n.getStatus()), true); // 过滤（会修改原树 children，慎用）
TreeUtils.printTree(roots, OrgNode::getName);                // 树形打印
```

## MemoryDba：内存数据库

用数据库方式处理内存数据，适合跨数据源合并计算或简化单元测试。底层是 H2 内存库（`jdbc:h2:mem:`），需 H2 依赖。

```java
try (MemoryDba mem = new MemoryDba()) {
    mem.createTable(
        Field.createKeyString("ID"),
        Field.createString("NAME"),
        Field.createNumeric("PROFIT", 2));

    mem.mergeOf(purchases).into(Table.DEFAULT_NAME).execute();
    mem.mergeOf(sales).into(Table.DEFAULT_NAME).execute();
    mem.update(Table.DEFAULT_NAME).set("PROFIT=(SELL-COST)*QTY").execute();

    List<ProductProfit> result = mem.select().from(Table.DEFAULT_NAME)
        .queryForList(ProductProfit.class);
}
```

- `createTable` 会先 `DROP TABLE IF EXISTS`，可重复调用
- 不指定表名时用 `Table.DEFAULT_NAME`（"X"）；也可 `createTable(String tableName, Field...)`
- 建表 DDL 为 H2 语法，仅适合在 MemoryDba 内使用

**Field DSL（`Field.` 静态工厂）：** 单参 `(name)`：`createKeyString`/`createKeyInt`/`createKeyDate`/`createString`/`createInt`/`createDouble`/`createMoney`/`createDate`/`createTimestamp`；带长度：`createKeyString(name, length)`/`createString(name, length)`；带精度：`createNumeric(name, scale)`；BLOB/CLOB：`create(name).setType(DataType.BLOB/CLOB)`；更多属性链式调 `setAutoIncrement/setMandatory/setDefaultValue`。

## 快速 CRUD

`CrudService<T>` + `QueryDTO` + `CrudController<T>` 三件套，继承即用，查询条件由前端拼 JSON 直接传，后端零 SQL。

**CrudService -- 泛型增删改查：**

```java
@Service
public class UserService extends CrudService<User> {
    public UserService(Dba dba) { super(dba, User.class); }
    @Override protected void beforeInsert(User u) { u.setId(...); } // 可选钩子，另有 beforeUpdate
}
```

方法：`insert / insert(Collection, batchSize) / update / deltaUpdate / getByKey / getByObject / getAll / queryPage / queryList / delete / deleteByKey`。

- `getByObject(T)`：按主键回查完整记录（拿自增主键、数据库默认值）
- `deltaUpdate(UpdateDTO<T>)`：只更新 `changedProps` 指定的字段（空列表直接返回 0）
- `deleteByKey` 返回 void（注意与 `dba.deleteByKey` 返回 int 不同）
- 复合主键：`getByKey`/`deleteByKey` 的 keys 按 `@Id` 声明顺序传
- 查询方法都有 `voClass` 重载，把结果映射成任意 VO

**QueryDTO -- 前端直接拼查询条件：**

```json
{
  "fields": "ID,NAME,AGE",
  "cnds": [
    { "field": "NAME", "op": "LIKE", "value": "李" },
    { "field": "AGE", "op": "GE", "value": 18 },
    {
      "field": "CREATE_TIME",
      "op": "EQ",
      "value": { "from": "2024-01-01", "to": null }
    }
  ],
  "orderBys": [{ "field": "AGE", "desc": true }],
  "pageNo": 1,
  "pageSize": 20
}
```

- `cnds` 每项是 `Cnd` 三要素（`field` / `op` / `value`），`op` 缺省为 `EQ`；复合条件用 `children` + `field`=`AND`/`OR`；`value` 传 `{"from":..,"to":..}` 即区间（Range 智能推导）
- 安全：字段名经 `validateFieldName` 校验（必须合法 SQL 标识符，含嵌套 children），条件值走 `?` 占位--前端注入不进来
- 表名由后端 `setEntity(clazz)` 推导，前端无须指定；排序前端没传时用主键默认排序

**后端定制（前端以为查对象，后端可 JOIN / 复杂查询）：**

```java
// 写法一：派生 Service 用链式定制
dto.setEntity("T_ORDER O JOIN T_USER U ON O.USER_ID=U.ID")
   .setFields("O.*, U.NAME AS USER_NAME")
   .defaultOrderBy("O.CREATE_TIME")
   .addCnd(Cnd.where("O.DELETED", false)); // 强制条件，前端绕不过
return dto.queryPage(dba, OrderVO.class);

// 写法二：继承 QueryDTO 加属性，复写 getSql()（继承后别用链式--会丢子类类型）
public class OrderQueryDTO extends QueryDTO {
    private String keyword; // + getter/setter
    @Override public Sql getSql(Dba dba) {
        Sql sql = super.getSql(dba);
        sql.and(StrUtil.isNotBlank(keyword),
            Cnd.or(
                Cnd.where("NAME", Op.LIKE, keyword),
                Cnd.where("PHONE", Op.LIKE, keyword)
            )
        );
        return sql;
    }
}
```

**CrudController -- 继承后加 `@RestController` + `@RequestMapping` 即暴露 REST 端点：**

```java
@RestController
@RequestMapping("/api/user")
public class UserController extends CrudController<User> {
    public UserController(Dba dba) { super(dba, User.class); }
}
```

端点（全 POST）：`/insert /update /delta-update /get-by-key /query-page /query-list /delete /batch-delete`。`/insert`、`/update` 返回数据库最新记录（内部 getByObject 回查）；`/delete`、`/batch-delete` 只读主键字段。也可当开发参考，端点逻辑很薄，照着写自定义 Controller 同样几行。

## 常见陷阱

| 陷阱                                | 说明                                                                                            |
| ----------------------------------- | ----------------------------------------------------------------------------------------------- |
| **Map 插入必须指定表名**            | `dba.insert(Map)` 会抛异常                                                                      |
| **忘记调用 `execute()`**            | `deleteFrom`、`update(table)` 等返回构建器，不调用不执行                                        |
| **空集合作为 IN 值**                | `Op.IN, Collections.emptyList()` 会报错，使用前判断                                             |
| **`append` 开头忘加空格**           | `append("LEFT JOIN")` 会拼成 `...T_USERLEFT JOIN...`                                            |
| **UpdateSql 所有 set 条件为 false** | 生成 `UPDATE T SET WHERE ...` 报错                                                              |
| **数据库关键字做属性名**            | 如 `value` -> 列名 `VALUE` 是关键字，用 `@Column(name)` 指定别名                                |
| **include/exclude 是属性名**        | `updateOf(bean).include(...)` 传 Java 属性名（驼峰）；`update(table).set(...)` 传列名，两者别混 |
| **Map 批量插入 key 不齐**           | 列以第一条 Map 的 keySet 为准，后续行缺 key 静默插 null                                         |
| **queryForTree 三要素**             | 实体实现 ITreeNode + 列名 AS ID/AS PID + PID 无父为根，缺一不可                                 |
| **CodeEnum 读到陌生 code**          | 静默返回 null 字段不报错；普通枚举读陌生值才抛异常                                              |
| **queryToMap 重复 key**             | 后行覆盖前行，静默；一对多改用 queryToGroup                                                     |
| **into/update 表名不能带 JOIN**     | 表名只接受 table/schema.table 合法标识符；JOIN 只能写在 `from(...)` 里                          |

## 最佳实践

- **长 SQL 用 `"""` 文本块**--避免字符串拼接，保持可读性和格式对齐：
  ```java
  dba.sql("""
      SELECT u.*, d.NAME AS DEPT_NAME
      FROM T_USER u
      LEFT JOIN T_DEPT d ON u.DEPT_ID = d.ID
      WHERE u.STATUS = ? AND u.AGE > ?
      ORDER BY u.NAME
      """).addParam(Status.ACTIVE, 18).queryForList(UserVO.class);
  ```
- **优先用 `from(Class)` 而非字符串表名**--重构时自动同步
- **状态字段定义为 Enum**--查询直接写 `where("STATUS", Status.ACTIVE)`；要存数字/自定义码就实现 `CodeEnum`
- **Boolean 字段用 `Boolean` 类型**--自动走 `BoolFieldMapper`，无需手动转换
- **部分更新首选 `updateOf(bean).excludeNull()`**
- **查完按 id 转 Map 直接用 `queryToMap`**--不要 queryForList 后再 stream `Collectors.toMap`；要有序传 `LinkedHashMap::new`，一对多用 `queryToGroup`
- **表名和字段名提取为常量**--规避重复字符串，重构安全
- **不需要 DO/VO/DTO 转换**--查询结果可直接映射为任意类
- **复杂跨数据源计算考虑 `MemoryDba`**--比 Java 数据结构处理更直观
- **BLOB/CLOB 大量读取注意性能**--复杂结构建议用 `String`/`JSONObject`，避免逐行反序列化过慢

## API 能力清单

> 常用类的能力点速查，方便确认"有没有这个方法"以及返回类型。

### Dba（统一入口）

| 方法                                                              | 返回                     | 说明                                                       |
| ----------------------------------------------------------------- | ------------------------ | ---------------------------------------------------------- |
| `select()` / `select(fields...)`                                  | `Sql`                    | 开始 SELECT 查询                                           |
| `selectByKey(Class, keys...)`                                     | `T`                      | 按主键查单条，未找到返回 `null`；复合主键按 @Id 声明顺序传 |
| `sql(String)`                                                     | `Sql`                    | 原始 SQL                                                   |
| `insert(data)`                                                    | `void`                   | 插入，data 可为 bean / 数组 / 集合                         |
| `insertOf(data)`                                                  | `InsertBuilder`          | 插入构建器（`into(String/Class)`/`batchSize`/`execute`）   |
| `merge(data)` / `mergeOf(data)`                                   | `void` / `InsertBuilder` | Insert or Update（MERGE INTO，部分库不支持）               |
| `update(bean)`                                                    | `int`                    | 按主键全量更新                                             |
| `updateOf(bean)`                                                  | `UpdateBuilder`          | 部分更新（`include`/`exclude`/`excludeNull`/`execute`）    |
| `update(table)` / `update(Class)`                                 | `UpdateSql`              | 手写 SET（`set`/`setMap`/`where`/`execute`）               |
| `delete(data)`                                                    | `int`                    | 按主键删除，支持 bean / 数组 / 集合                        |
| `deleteByKey(Class, keys...)`                                     | `int`                    | 按主键值删除                                               |
| `deleteFrom(table)` / `deleteFrom(Class)`                         | `Sql`                    | 条件删除（接 `where`/`execute`）                           |
| `transaction(Runnable)` / `transaction(TransactionCallback<T>)`   | `void` / `T`             | 事务                                                       |
| `exist(tableName)`                                                | `boolean`                | 表是否存在                                                 |
| `dropTable(tableName)`                                            | `void`                   | 删表（DROP TABLE IF EXISTS）                               |
| `getJdbcTemplate()` / `getTransactionTemplate()` / `getDialect()` | -                        | 取底层组件                                                 |

### Sql -- 构建（返回 `Sql`，可链式）

| 方法                                                                      | 说明                                                                                                                                        |
| ------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------- |
| `from(table)` / `from(Class)`                                             | FROM，接受原始片段（可写 JOIN）                                                                                                             |
| `where(...)` / `and(...)` / `or(...)`                                     | 条件，重载：`(str)`、`(field,value)`、`(field,Op,value)`、`(Cnd)`、`(List<Cnd>)`，及条件开关 `(boolean, ...)` 和 `(boolean, Supplier<Cnd>)` |
| `append(Sql)` / `append(Cnd)`                                             | 追加（原样拼接，注意空格）                                                                                                                  |
| `addParam(...)` / `addParams(List)` / `setParam(...)` / `setParams(List)` | 参数管理                                                                                                                                    |
| `orderBy(fields...)` / `orderBy(List<OrderBy>)` / `groupBy(fields...)`    | 排序 / 分组；字段串里可带 ` DESC`，或用 `DbaUtil.desc(field)`                                                                               |
| `limit(n)` / `range(from, to)`                                            | 限制行数 / 范围（from 为 null 等价 limit）                                                                                                  |
| `disableCountOptimization()`                                              | 关闭分页 count 优化                                                                                                                         |
| `getSql()` / `getParams()`                                                | 取最终 SQL（含分页包装）/ 参数列表                                                                                                          |

### Sql -- 执行与查询（终结方法）

| 方法                                                           | 返回                    | 说明                                                                                                                                                                                                                                                                |
| -------------------------------------------------------------- | ----------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `queryForObject(Class/RowMapper)`                              | `T` / `null`            | 单条；`...Optional` 版返回 `Optional<T>`；Class 传简单类型即单列映射                                                                                                                                                                                                |
| `queryForValue(Class/FieldMapper)`                             | `T` / `null`            | 单列值；`...Optional` 版返回 `Optional`                                                                                                                                                                                                                             |
| `queryForString/Int/Double/Date()`                             | 对应类型                | 无结果返回 `""`/`0`/`0.0`/`null` 而非抛异常；各有 `...Optional` 版                                                                                                                                                                                                  |
| `queryForMap()`                                                | `Map<String,Object>`    | 单行 Map，无数据返回空 Map                                                                                                                                                                                                                                          |
| `queryForList(Class/RowMapper/FieldMapper/无参/MapRowMapper)`  | `List<T>` / `List<Map>` | 列表                                                                                                                                                                                                                                                                |
| `queryToMap(keyFunc, value)`                                   | `Map<K,V>`              | 结果转 Map。keyFunc 是 `ResultSetFunction`（`rs -> rs.getString("ID")`）；value 可传 `Class` / `RowMapper` / `ResultSetFunction` / 不传（Map 行）；`(keyFunc, Class/RowMapper, Supplier<Map>)` 重载指定 Map 实现（如 `LinkedHashMap::new` 保序）。重复 key 后行覆盖 |
| `queryToGroup(keyFunc, value)`                                 | `Map<K,List<V>>`        | 结果分组，value 同上四种给法                                                                                                                                                                                                                                        |
| `query(RowCallbackHandler)` / `query(ResultSetExtractor<T>)`   | `void` / `T`            | 直接操作 `ResultSet`                                                                                                                                                                                                                                                |
| `count()`                                                      | `int`                   | 总记录数（自动优化，见上文）                                                                                                                                                                                                                                        |
| `exist()`                                                      | `boolean`               | 是否存在记录（SELECT 1 + LIMIT 1）                                                                                                                                                                                                                                  |
| `queryPage(Class/RowMapper/无参, pageNo, pageSize)`            | `PageData<T>`           | 分页；count=0 或页码超范围返回空数据带 total                                                                                                                                                                                                                        |
| `queryForTree(Class/RowMapper)`                                | `Tree<T>`               | 树形；实体实现 ITreeNode，列必须为 ID、PID（详见[树形数据](#树形数据)）                                                                                                                                                                                             |
| `execute()`                                                    | `int`                   | 执行 DML                                                                                                                                                                                                                                                            |
| `batchUpdate(List<Object[]>)` / `batchUpdate(list, batchSize)` | `int[]` / `int[][]`     | 批量执行                                                                                                                                                                                                                                                            |

### UpdateBuilder（`updateOf(bean)` 返回，按主键部分更新）

| 方法                                         | 说明                                                                     |
| -------------------------------------------- | ------------------------------------------------------------------------ |
| `include(fields...)` / `include(Collection)` | 仅更新指定字段，**传 Java 属性名**                                       |
| `exclude(fields...)` / `exclude(Collection)` | 排除指定字段，**传 Java 属性名**                                         |
| `excludeNull()`                              | null 字段不参与 SET，可与 include/exclude 组合                           |
| `into(tableName)`                            | 指定更新的表名（更新同结构的另一张表，如分表）；不调用用 Bean 对应的表名 |
| `execute()`                                  | 执行，返回受影响行数                                                     |

`include` / `exclude` 互斥，只能二选一。

### Cnd（静态工厂，返回 `Cnd`）

| 方法                                                                        | 说明                                                                                  |
| --------------------------------------------------------------------------- | ------------------------------------------------------------------------------------- |
| `Cnd.where(field, value)`                                                   | 等值（EQ 智能推导；value 传 `Op.IS_NULL`/`Op.IS_NOT_NULL` 等价 `isNull`/`isNotNull`） |
| `Cnd.where(field, Op, value)`                                               | 指定操作符；value 传 `Sql` 即子查询（不限 IN）                                        |
| `Cnd.where(boolean, field, value)` / `Cnd.where(boolean, field, Op, value)` | 条件开关，false 返回 `null`                                                           |
| `Cnd.and(Cnd...)` / `Cnd.and(List)`                                         | AND 组合（0 个返回 `null`，1 个返回自身）                                             |
| `Cnd.or(Cnd...)` / `Cnd.or(List)`                                           | OR 组合                                                                               |
| `Cnd.isNull(field)` / `Cnd.isNotNull(field)`                                | IS NULL / IS NOT NULL                                                                 |
