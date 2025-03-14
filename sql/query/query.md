# Sql 查询

## 基本查询

对于 Sql 查询语句，select 部分可能是一个字段，或者多个字段，查询结果可能是一条记录或多条记录。Sql 对象提供了多种方法来获取查询结果。

### 【一个字段，一条记录】的查询方法

```java
public <T> T queryForValue(Class<T> requiredType)
public <T> T queryForValue(FieldMapper<T> mapper)
public String queryForString()
public int queryForInt()
public double queryForDouble()
public LocalDate queryForDate()
public int count()

// optional 版本，如果查询结果为空，则返回 Optional.empty()
public <T> Optional<T> queryForValueOptional(Class<T> requiredType);
public <T> Optional<T> queryForValueOptional(FieldMapper<T> mapper)
public Optional<String> queryForStringOptional()
public Optional<Integer> queryForIntOptional()
public Optional<Double> queryForDoubleOptional()
public Optional<LocalDate> queryForDateOptional()
```

:::info count

:::

### 【多个字段，一条记录】的查询方法

```java
public <T> T queryForObject(Class<T> objectType)
public <T> T queryForObject(RowMapper<T> mapper)
public Map<String, Object> queryForMap()

// optional 版本，如果查询结果为空，则返回 Optional.empty()
public <T> Optional<T> queryForObjectOptional(Class<T> objectType)
public <T> Optional<T> queryForObjectOptional(RowMapper<T> mapper)
```

### 【一个字段，多条记录】的查询方法

```java
public <T> List<T> queryForList(Class<T> objectType)
public <T> List<T> queryForList(FieldMapper<T> fieldMapper)
```

### 【多个字段，多条记录】的查询方法

```java
public <T> List<T> queryForList(Class<T> objectType)
public <T> List<T> queryForList(RowMapper<T> rowMapper)
```
