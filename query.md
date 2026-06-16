# 查询：链式 SQL 构建

## 设计哲学

Sql 对象的本质是一个 SQL 字符串加对应的参数列表。链式 API 让 SQL 拼接像说话一样自然，而 `appendTemp/checkTemp` 机制解决了"前一条有了才加分隔符"的经典问题——你不需要判断这是第一个条件还是第二个，框架自动处理 WHERE/AND/逗号。这个机制也通过 `StringBuilderX` 类暴露给使用者，在拼接其他字符串时也可以使用。

## 基本查询

```java
dba.select().from("T_USER").where("ID", "1").queryForObject(User.class);

dba.select("NAME", "AGE").from("T_USER").where("AGE", Op.GT, 20).queryForList(User.class);

dba.select().from(User.class).count();
```

## 关联查询

`from` 方法接受原始 SQL 片段，可以直接写 JOIN：

```java
dba.select("P.*", "O.QTY")
    .from("PRODUCT P LEFT JOIN ORDERS O ON P.ID=O.PRODUCT_ID")
    .where("O.QTY", Op.GT, 0)
    .queryForList(ProductVO.class);
```

关联查询的表名和 JOIN 条件写在 `from` 中，WHERE 条件仍然用链式 API 拼接。也可以用 `append` 分步拼接：

```java
dba.select("P.*", "O.QTY")
    .from("PRODUCT P")
    .append(" LEFT JOIN ORDERS O ON P.ID=O.PRODUCT_ID")
    .where("O.QTY", Op.GT, 0)
    .queryForList(ProductVO.class);
```

## 条件拼接

```java
dba.select().from("T_USER")
    .where("AGE", Op.GT, 20)
    .and("AGE", Op.LE, 30)
    .or("NAME", "Alice")
    .queryForList(User.class);
```

`where`/`and`/`or` 方法会自动管理 WHERE 关键字和 AND/OR 连接符的位置。

## 调试与底层访问

```java
String sql = dba.select().from("T_USER").where("ID", "1").getSql();
// 获取最终生成的 SQL 字符串，设置了 limit/range 时会自动通过方言包装分页 SQL

List<Object> params = dba.select().from("T_USER").where("AGE", Op.GT, 20).getParams();
// 获取当前参数列表
```

`Dba` 也暴露了底层组件的 getter：`getJdbcTemplate()`、`getTransactionTemplate()`、`getDialect()`，在需要直接使用 Spring JDBC 原生能力或获取方言信息时可用。

## 条件开关：避免 if 打断链式调用

```java
public List<User> getUser(String name, int minAge) {
    return dba.select().from("T_USER")
        .where(StrUtil.isNotBlank(name), "NAME", name) // name 参数为空则不包括此条件
        .and(minAge > 0, "AGE", Op.GT, minAge) // minAge 大于0 才有此条件
        .queryForList(User.class);
}
```

## 查询结果类型

### 单条查询

| 方法 | 返回类型 | 无结果时 |
|------|---------|---------|
| `queryForObject(Class)` | `T` | `null` |
| `queryForObjectOptional(Class)` | `Optional<T>` | `Optional.empty()` |
| `queryForValue(Class)` | 简单类型值 | `null` |
| `queryForValue(FieldMapper)` | 使用 FieldMapper 转换 | `null` |
| `queryForString()` | `String` | `""` |
| `queryForInt()` | `int` | `0` |
| `queryForDouble()` | `double` | `0.0` |
| `queryForDate()` | `LocalDate` | `null` |
| `queryForMap()` | `Map<String, Object>` | 空 Map |

为什么 `queryForInt()` 返回 0 而不是抛异常？因为业务代码中"查不到就默认0"是最常见的场景，不需要每次都 try-catch。如果需要区分"无结果"和"值为0"，建议使用 `queryForIntOptional()`。

### 列表查询

| 方法 | 返回类型 | 说明 |
|------|---------|------|
| `queryForList(Class)` | `List<T>` | 自动判断简单类型 vs Bean |
| `queryForList(RowMapper)` | `List<T>` | 自定义 RowMapper |
| `queryForList(FieldMapper)` | `List<T>` | 单列值列表 |
| `queryForList()` | `List<Map<String, Object>>` | Map 列表 |

### Map 映射

将查询结果映射为一个 Map，key 由 `keyFunc` 从 ResultSet 中提取，value 可以是单列值、Bean 对象或整行 Map：

```java
Map<String, String> nameMap = dba.select().from("T_USER")
    .queryToMap(rs -> rs.getString("ID"), rs -> rs.getString("NAME"));
// 结果: {"1" -> "Alice", "2" -> "Bob"}

Map<String, User> userMap = dba.select().from("T_USER")
    .queryToMap(rs -> rs.getString("ID"), User.class);
// 结果: {"1" -> User对象, "2" -> User对象}

Map<String, Map<String, Object>> allMap = dba.select().from("T_USER")
    .queryToMap(rs -> rs.getString("ID"));
// 结果: {"1" -> {ID:"1", NAME:"Alice", ...}, "2" -> {ID:"2", NAME:"Bob", ...}}
```

`queryToMap` 还支持传入 `Supplier<Map>` 来指定 Map 实现类，例如保持插入顺序：

```java
Map<String, User> orderedMap = dba.select().from("T_USER")
    .queryToMap(rs -> rs.getString("ID"), User.class, LinkedHashMap::new);
```

### 分组

将查询结果按 key 分组，value 为该 key 对应的记录列表：

```java
Map<Integer, List<User>> ageGroups = dba.select().from("T_USER")
    .queryToGroup(rs -> rs.getInt("AGE"), User.class);
// 结果: {20 -> [User1, User3], 30 -> [User2]}

Map<String, List<String>> nameGroups = dba.select().from("T_USER")
    .queryToGroup(rs -> rs.getString("ID"), rs -> rs.getString("NAME"));

Map<String, List<Map<String, Object>>> groupMap = dba.select().from("T_USER")
    .queryToGroup(rs -> rs.getString("ID"));
```

### 逐行处理与整体处理

Sql 提供了两个 `query` 方法，可以直接操作 `ResultSet`，适合内置方法无法满足的场景：

```java
dba.select().from("T_USER").query(rs -> {
    System.out.println(rs.getString("NAME"));
});

List<User> users = dba.select().from("T_USER").query(rs -> {
    List<User> list = new ArrayList<>();
    while (rs.next()) {
        User user = new User();
        user.setId(rs.getString("ID"));
        user.setName(rs.getString("FIRST_NAME") + " " + rs.getString("LAST_NAME"));
        list.add(user);
    }
    return list;
});
```

- `query(RowCallbackHandler)`——逐行回调，无返回值，适合导出、统计等只处理不收集的场景
- `query(ResultSetExtractor)`——整体处理，自己控制 `rs.next()`，返回自定义结果，适合需要手动遍历 ResultSet 的场景

### 特殊查询

| 方法 | 返回类型 | 说明 |
|------|---------|------|
| `count()` | `int` | 总记录数 |
| `exist()` | `boolean` | 是否存在记录 |
| `pageQuery(Class, pageNo, pageSize)` | `PageData<T>` | 分页查询 |
| `queryForTree(Class)` | `Tree<T>` | 树形结构 |

## 分页查询

```java
PageData<User> page = dba.select().from("T_USER")
    .orderBy("ID")
    .pageQuery(User.class, pageNo, pageSize);
int total = page.getTotal();
List<User> data = page.getData();
```

分页查询和 `count()` 都会尽量使用 count 优化——将 `SELECT ... FROM ... ORDER BY ...` 优化为 `SELECT COUNT(*) FROM ...`，去除不必要的列和排序。包含 DISTINCT/GROUP BY/UNION 时自动禁用优化。如果不需要优化，可以调用 `disableCountOptimization()` 手动关闭。

## 限制行数

```java
dba.select().from("T_USER").limit(10).queryForList(User.class);
dba.select().from("T_USER").range(1, 10).queryForList(User.class);
```

## 树查询

查询结果必须包含 ID 和 PID 字段，如果实际列名不同，可以用别名 `AS`：

```java
dba.select("ORG_ID AS ID", "PARENT_ID AS PID", "NAME").from("T_ORG")
    .queryForTree(OrgNode.class);
```

实体实现 `ITreeNode` 接口即可：

```java
public class OrgNode implements ITreeNode<OrgNode> {
    private String id;
    private String name;
    @Transient
    private List<OrgNode> children = new ArrayList<>();
    // 实现 getChildren(), addChild(), setChildren() 或者直接继承 TreeNode 类
}

Tree<OrgNode> tree = dba.select().from("T_ORG").queryForTree(OrgNode.class);
List<OrgNode> roots = tree.getRoots();
OrgNode node = tree.getNode("2"); // 通过 ID 快速查找任意节点
```

**TreeUtils 工具方法：**

```java
TreeUtils.traverse(roots, node -> { ... });
TreeUtils.traverse(roots, (node, parent, level) -> { ... }, true);
List<T> filtered = TreeUtils.filter(roots, node -> node.isActive(), true);
List<T> transformed = TreeUtils.transform(roots, node -> newDto(node));
String print = TreeUtils.printTree(roots, OrgNode::getName);
```

## RowMapper：结果映射的扩展点

`RowMapper` 是 Spring JDBC 提供的接口，负责将 `ResultSet` 的每一行映射为 Java 对象。rainbow-dbaccess 内置了多种实现，覆盖常见场景；当内置实现不满足需求时，可以自己实现 `RowMapper` 接口。

**内置 RowMapper：**

| RowMapper | 映射结果 | 说明 |
|-----------|---------|------|
| `BeanMapper<T>` | Java Bean | 最常用，通过 `PropInfoCache` 自动映射列名到属性 |
| `MapRowMapper` | `Map<String, Object>` | 行转 Map，支持 ignore/ignoreNull/setFieldMapper/post |
| `CamelCaseMapMapper` | `Map<String, Object>` | 继承 MapRowMapper，列名自动转驼峰 |
| `StringArrayRowMapper` | `String[]` | 行转字符串数组 |
| `ObjectArrayRowMapper` | `Object[]` | 行转对象数组，支持 setFieldMapper |
| `SingleColumnFieldRowMapper` | 单值 | 单列映射，通过 FieldMapper 转换 |

**MapRowMapper 增强：**

```java
MapRowMapper.create()
    .ignore("AGE", "SCORE")
    .ignoreNull()
    .post(m -> m.put("_extra", 1))
    .setFieldMapper("STATUS", EnumFieldMapper.of(Status.class))
    .setFieldMapper(3, EnumFieldMapper.of(Color.class));
```

`setFieldMapper` 支持按列名和按列索引两种方式设置 FieldMapper。

**自定义 RowMapper：**

当内置实现无法满足需求时，实现 `RowMapper` 接口即可。比如需要从 ResultSet 中做复杂计算、多列合并、或特殊的类型转换：

```java
public class UserSummaryMapper implements RowMapper<UserSummary> {
    @Override
    public UserSummary mapRow(ResultSet rs, int rowNum) throws SQLException {
        UserSummary summary = new UserSummary();
        summary.setId(rs.getString("ID"));
        summary.setDisplayName(rs.getString("FIRST_NAME") + " " + rs.getString("LAST_NAME"));
        summary.setTotalScore(rs.getInt("MATH_SCORE") + rs.getInt("ENGLISH_SCORE"));
        return summary;
    }
}

List<UserSummary> list = dba.select().from("T_USER").queryForList(new UserSummaryMapper());
```
