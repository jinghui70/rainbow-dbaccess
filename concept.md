# Rainbow DBAccess 是什么？

Rainbow DBAccess 是一个基于 Spring JDBC 的轻量级数据库访问工具。

## 设计哲学

- **SQL 是第一公民**——SQL 不应该和代码分离，分离的 SQL 不利于阅读、理解和维护
- **约定大于配置**——任意 Java 对象都可以自然地进入 SQL 交互，无需繁琐的映射文件
- **链式 SQL 写法**——让代码灵活、清晰、易维护，比 XML SQL 和字符串拼接都更优雅
- **轻量但强大**——通过 FieldMapper 体系，让 Java 属性更有业务意义，数据库存 CHAR(8) 就是 LocalDate，存 TINYINT 就是 Boolean，存 VARCHAR 就是枚举编码

过于复杂的 ORM 会导致代码繁琐，而直接使用 Spring JDBC 又太底层。Rainbow DBAccess 在两者之间找到平衡：保留对 SQL 的完全控制，消除模板代码，自动处理 Java 类型与数据库类型的映射。

**Dba 是统一入口**——所有数据库操作都从 Dba 开始，包括查询、插入、更新、删除、事务，不需要费别的脑子了。

## 核心特性

### 几乎零配置的对象映射

默认采用驼峰命名规则的对象类名和属性名，与数据库的 snake_case 模式的表名、字段名自动匹配，省去了繁琐的映射代码编写工作。内置对布尔属性、枚举属性、数组属性、LOB 类型以及自定义对象等复杂数据类型的全面支持。

```java
public class UserInfo { // 默认表名: USER_INFO
    @Id
    private String id;
    private String name;
    private Integer age;
    private Double finalScore; // 默认字段名: FINAL_SCORE
}

dba.insert(new UserInfo("1", "Alice", 25, 100.0));
```

### 直观的链式 SQL 编写

编写 SQL 类似直接的 SQL 语法，可读性强，同时解决了 SQL 注入问题。

```java
dba.select().from("T_USER").where("NAME", "Alice").queryForObject(User.class);
```

### 查询结果的多态转换

根据业务需要，查询结果可以直接转换为各类值、对象、Map、列表、树形结构，以及对结果直接分组，简化了数据处理代码。

```java
String name = dba.select("NAME").from("T_USER").where("ID", "1").queryForString();
User user = dba.select().from("T_USER").where("ID", "1").queryForObject(User.class);
Map<String, String> nameMap = dba.select().from("T_USER").queryToMap(rs -> rs.getString("ID"), rs -> rs.getString("NAME"));
Tree<OrgNode> tree = dba.select().from("T_ORG").queryForTree(OrgNode.class);
```

### 内存数据库支持

用数据库的方式在内存中处理复杂的数据结构，跨数据源合并计算轻松搞定。

```java
try (MemoryDba mem = new MemoryDba()) {
    mem.createTable(Field.createKeyString("ID"), Field.createString("NAME"), Field.createMoney("PRICE"));
    mem.mergeOf(purchases).into(Table.DEFAULT_NAME).execute();
    mem.mergeOf(sales).into(Table.DEFAULT_NAME).execute();
    List<ProductProfit> profits = mem.select().from(Table.DEFAULT_NAME).queryForList(ProductProfit.class);
}
```
