# RowMapper

RowMapper 是 Spring JDBC 中一个非常重要的接口，它用于将数据库查询结果集 `ResultSet` 中的每一行数据映射到一个特定的 Java 对象中。Rainbows DBAccess 提供了诸多查询结果的转换函数，都是基于 RowMapper 实现的。

## 内置的 RowMapper

### BeanMapper

将 `ResultSet` 转换为一个 Java Bean 对象。当使用 `queryForObject(User.class)` 之类的方法时，底层就是使用了 BeanMapper。

```java
User user = dba.select().from("user").where("id", 1).queryForObject(User.class);
// 等同于
BeanMapper<User> userMapper = BeanMapper.of(User.class);
User user = dba.select().from("user").where("id", 1).queryForObject(userMapper);
```

### MapRowMapper

将 `ResultSet` 转换为一个 Map 对象。字段名是 Key，字段值是 Value。

```java
Map<String, Object> map = dba.select().from("user").where("id", 1).queryForMap();
```

如果对转换的结果 Map 有更多的要求，MapRowMapper 还提供了一下方法满足需求：

- `ignore(String... ignoreKeys)`：忽略指定的字段
- `ignoreNull()`：忽略值为 null 的字段
- `setFieldMapper(String key, FieldMapper fieldMapper)`：为指定字段设置转换器
- `setFieldMapper(int columnIndex, FieldMapper fieldMapper)`：根据结果字段的顺序设置转换器
- `post(Consumer<Map<String, Object>> consumer)`：在数据转换后执行指定的处理逻辑

```java
MapRowMapper mapper = MapRowMapper.create()
  .ignore("password", "salt")
  .ignoreNull()
  // avatar 字段是 blob 字段，转换为 byte[]
  .setFieldMapper("avatar", new BlobByteArrayFieldMapper())
  // 第4个字段是枚举类型，转换为枚举对象
  .setFieldMapper(4, new EnumMapper(UserStatus.class))
  // 转换后的 map 增加一个字段
  .post(map->map.put("fullname", map.get("first name") + map.get("last name")));

Map<String, Object> map = dba.select().from("user").queryForList(mapper);
```
