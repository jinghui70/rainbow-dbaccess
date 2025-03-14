# Update

在构建 SQL 更新语句时，set 函数用于灵活地设置字段值。以下是 set 函数的四种形式及其详细解释：

## set 函数形式

1. 单个字段赋值

这种形式用于将指定的字段 fieldName 设置为给定的值 value。

```java
dba.update("tableName").set("fieldName", value)
```

使用 [FieldMapper]() 转换的复杂对象，需要增加 FieldMapper 参数。下面的例子展示了如何把一个复杂的 java 对象转为 json 并压缩保存到数据表的一个 BLOB 字段中：

```java
FieldMapper<Resume> resumeMapper = BlobObjectFieldMapper.of(Resume.class);
dba.update("tableName").set("resume", resumeMapper, resume);
```

2. 原生 SET 语句拼接

对于简单设置常量值，或自增、自减表达式，可以直接拼接原生 SQL 语句。对于带参数的表达式，如 salary=salary\*?，需结合 addParam 方法传递参数：

```java
dba.update("tableName").set("times=1")
dba.update("tableName").set("count=count+1")
dba.update("tableName").set("salary=salary*?").addParam(rate)
```

3. 条件性单个字段赋值

当条件 condition 为真时，将字段 fieldName 设置为值 value。

```java
dba.update("tableName").set(condition, "fieldName", value)
```

4. 条件性原生 SET 语句拼接

根据条件决定是否拼接指定的原生 SQL 更新语句。

```java
dba.update("tableName").set(condition, "field=value")
```

## 示例

```java
dba.update("user")
  .set("name", "Jack")
  .set("age", 18)
  .set("times=1")
  .set("count=count+1")
  .set("salary=salary\*?").addParam(rate)
  // 当 email 不为空时拼接为 email=?
  .set(StrUtil.isNotEmpty(email), "email", email)
  // 当 isFirst 为 true 时拼接为 times=1
  .set(isFirst, "times=1")
  .where("id", 1)
  .excute();
```

:::info 注意
构建好了更新语句的 Sql 对象，一定要通过 execute 方法执行更新操作。
:::
