# 快速开始

## 依赖引入

项目基于 Java 17，通过 Maven 引入：

```xml
<dependency>
    <groupId>io.github.jinghui70</groupId>
    <artifactId>rainbow-dbaccess</artifactId>
    <version>6.2.0</version>
</dependency>
```

## Spring Boot 自动装配

当 classpath 中存在 `JdbcTemplate` 和 `TransactionTemplate` 时，`DbaAutoConfiguration` 会自动创建 `Dba` Bean，无需手动配置：

```java
@Autowired
private Dba dba;
```

自动装配条件：
- 容器中存在 `JdbcTemplate`（单例）
- 容器中存在 `TransactionTemplate`
- 容器中不存在自定义 `Dba` Bean

方言自动识别：H2/MySQL → `DialectDefault`，PostgreSQL/Kingbase → `DialectPostgreSQL`，Oracle → `DialectOracle`。不支持的数据库可以实现 `Dialect` 接口自行扩展。

## 手动创建

```java
DataSource ds = ...;
Dba dba = new Dba(ds);

Dba dba = new Dba(jdbcTemplate, transactionTemplate);

Dba dba = new Dba(ds, new DialectCustom());
```

内置支持的方言会自动识别，无需手动指定。只有使用不支持的数据库时才需要传入自定义方言。

## 第一个查询

```java
public class UserInfo { // 默认表名: USER_INFO
    @Id
    private String id;
    private String name;
    private Integer age;
}

List<UserInfo> users = dba.select().from("USER_INFO").queryForList(UserInfo.class);

UserInfo user = dba.select().from("USER_INFO").where("ID", "1").queryForObject(UserInfo.class);

dba.insert(new UserInfo("1", "Alice", 25));

dba.update("USER_INFO").set("NAME", "Bob").where("ID", "1").execute();

dba.deleteFrom("USER_INFO").where("ID", "1").execute();
```
