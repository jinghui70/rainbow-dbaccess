# Rainbow DBAccess 是什么？

Rainbow DBAccess 是基于 Spring JDBC 的一个数据库访问组件。

已经有 Hibernate 、JPA、MyBatis、MybatisPlus，这样的组件了，为什么需要 Rainbow DBAccess 呢？

## 数据库访问组件的痛点

数据库访问的核心在于构建 SQL 语句并执行查询或更新操作。直接用字符串拼接 SQL 语句，会引发 SQL 注入问题，同时也很容易出现拼写错误，很难维护。

Spring JDBC 提供了 JdbcTemplate，可以方便的构建 SQL 语句，通过分离 Sql 与参数，避免 SQL 注入问题，但是它仍然要编写大量的 SQL 语句，也不支持复杂的查询结果转换。

ORM 框架的问世，实现了数据库表与 Java 对象的直接映射，有效解决了增删改 SQL 语句的编写难题。然而，尽管它简化了数据库操作，但往往需要开发者投入大量时间编写实体类、映射文件或注解，以及 DAO 类。这些操作在大型或复杂项目中显得尤为繁琐，维护成本也随之攀升。此外，由于 ORM 框架的抽象层次较高，有时可能会导致生成的 SQL 语句不够优化，影响数据库性能。

对于复杂的查询场景，MyBatis 等 ORM 框架虽然提供了灵活的查询能力，但需要开发者编写 XML 映射文件来定义查询逻辑。这种方式的动态查询条件写法与标准 SQL 写法存在差异，无疑增加了学习和调试的成本。更为关键的是，查询的 SQL 语句与业务代码分离，这种物理上的分隔降低了代码的可读性和可维护性。一旦需要修改查询逻辑，开发者可能需要在多个文件之间来回切换。

对于需要根据元数据配置访问数据库的数据类应用（如报表系统、数据挖掘系统等），由于需要频繁查询和修改数据库表结构，使用 ORM 框架可能并不合适。

## Rainbow DBAccess 的优势

### 轻量且易于集成

Rainbow DBAccess 只有一个 jar 包，采用 SpringBoot 的自动配置方式，只需要在代码中注入 [Dba 对象](/dba)，即可立即开始工作。

```java
@Autowired
private Dba dba;
```

Rainbow DBAccess 仅依赖三个库：[Spring JDBC](https://docs.spring.io/spring-framework/reference/data-access/jdbc.html)、[H2 Database](https://www.h2database.com/html/main.html) 和 [Hutool](https://hutool.cn/)。其中前两个库通常是 SpringBoot 项目已包含的依赖，无需额外配置。此外，Rainbow DBAccess 也可以与其它数据库访问组件（例如 MyBatis、Hibernate 等）一起使用，无需修改原有代码。

### 几乎零配置的对象映射

Rainbow DBAccess 默认采用驼峰命名规则的对象类名和属性名，与数据库的 snake_case 模式的表名、字段名自动匹配，从而省去了繁琐的映射代码编写工作。

此外，还内置了对布尔属性、枚举属性、数组属性、LOB 类型以及自定义对象等复杂数据类型的全面支持，确保在数据保存与读取过程中，无需编写额外的处理代码。

下面是一个用户表结构的示例：

```sql
CREATE TABLE user (
  id varchar(36) primary key,
  user_name varchar(50),
  password varchar(50)
);
```

定义的 User 实体类与表结构自动匹配，无需编写额外的映射代码，可以直接插入到数据库中。查询数据时，根据查询字段匹配，可以直接转为任意对象。例子中查询结果直接转换为 `UserVO` 对象了。

```java
class User {
  String id;
  String userName;
  String password;
  UserStatus status;
}
class UserVO {
  String id;
  String name; // 故意用了不同的字段名，演示转换功能
  UserStatus status;
}
public UserVO saveUser(User user) {
  user.setPassword(passwordEncoder.encode(user.getPassword()));
  user.setStatus(UserStatus.NORMAL);
  dba.insert(user)
  return dba.select("id,user_name as name,status").from("user").where("id", user.getId()).queryForObject(UserVO.class);
}
```

### 直观的 SQL 编写

编写 SQL 类似直接的 SQL 语法，可读性强，同时解决了 SQL 注入问题。

```java
dba.select().from("user").where("name", "张三").queryForObject(User.class);
```

### 查询结果的多态转换

根据业务需要，查询结果可以直接转换为各类值、对象、Map、列表、树形结构，以及对结果直接分组，简化了数据处理代码。

```java
dba.select("id").from("user").where("name", "张三").queryForString();
dba.select().from("user").where("name", "张三").queryForObject(User.class);
dba.select("subject,score").from("score").where("student", "zhangsan").queryToMap();
dba.select().from("org").where("status", normalStatusSet).queryForTree(Org.class);
```

### 内存数据库支持

支持内存数据库，以数据库的方式在内存中处理复杂的数据结构。

```java
// 创建内存数据库，合并三个表的数据
try (MemoryDba memoryDba = new MemoryDba()){
  memoryDba.createTable(
    Field.createKeyString("ID"),
    Field.createString("NAME"),
    Field.createMoney("PRICE"),
    Field.createInt("STOCK"),
    Field.createMoney("SALES")
  );
  List<Map<String, Object> list = dba.select("ID,NAME,PRICE").from("product").queryForList();
  memoryDba.insert(list);
  list = dba.select("ID,STOCK").from("stock").queryForList();
  memoryDba.merge(list);
  list = dba.select("ID,SALES").from("sale_report").queryForList();
  memoryDba.merge(list);
}
```

### 简化单元测试

自动接入 PDManer 设计文件，使用内存数据库模拟，简化单元测试。
