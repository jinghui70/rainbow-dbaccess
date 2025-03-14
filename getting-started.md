# 快速开始

## 依赖

- JDK 1.8+
- SpringBoot 2.7+ / Spring5.3+
- [Hutool](https://hutool.cn) / 5.8.25
- [H2 database](http://www.h2database.com) 2.2.228

## 安装

在项目的 pom.xml 中的依赖项中添加以下内容

```xml

<dependency>
    <groupId>io.github.jinghui70</groupId>
    <artifactId>rainbow-dbaccess</artifactId>
    <version>5.2.12</version>
</dependency>
```

## 使用

Rainbow DBAccess 中，[Dba 对象](/dba) 是访问一个数据库的唯一入口。

### 自动配置 Dba 对象

在 SpringBoot 项目中，[Dba 对象](/dba) 支持自动配置，可以直接注入：

```java
@Autowired
private Dba dba;
```

### 手动创建 Dba 对象

构造函数：

```java
// 传入 java.sql.dataSource 对象
public Dba(DataSource dataSource)

// 传入 java.sql.dataSource 对象和数据库方言示例
public Dba(DataSource dataSource, Dialect dialect)
```

默认情况下，Dba 会根据 dataSource 的信息，自动创建相应的[数据库方言](/other/dialect)对象。如果使用的是 Rainbow-DBAccess 不支持的数据库，可以实现一个新的[数据库方言](/other/dialect)，调用第二个构造函数来创建 [Dba 对象](/dba) 。

### 多个数据源配置

一个 [Dba 对象](/dba)，对应一个 DataSource。有些数据类的项目，会配置很多数据源并同时访问，下面的例子，演示了使用 Hiraki 连接池，动态配置多个 Dba 的场景。

```java
public class DbaMaster() {

    // 项目主数据源对应的 Dba
    @Autowired
    private Dba dba;

    private Map<String, Dba> dbaMap = new HashMap<>();


}

```
