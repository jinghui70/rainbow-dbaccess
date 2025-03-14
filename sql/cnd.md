# 查询条件

一个 SQL 查询条件，由三部分组成：字段名、操作符和值。Sql 对像添加一个查询条件，会自动用参数化的方式拼接到 sql 字符串后面，并把条件值添加到 [Sql 对象](/sql/sql) 的参数列表中。

```java
// 查询大于 18 岁姓李的用户
Sql sql = dba.select().from("user").where("name", Op.LIKE_LEFT, "李").and("age", Op.GT, 18);

// 最终的 sql 字符串： SELECT * FROM user WHERE name like ? AND age > ?
// 参数列表：["李%", 18]
```

## 查询操作符

查询操作符是一个枚举，内容如下：

```java
public enum Op {
    EQ("="), // 如果值为空，会自动转为 IS NULL
    NE("!="),  // 如果值为空，会自动转为 IS NOT NULL
    GT(">"),
    GE(">="),
    LT("<"),
    LE("<="),
    LIKE(" like "), // 模糊匹配，如果参数左右都没有 % 符号，会自动把参数两边加上 % 符号
    LIKE_LEFT(" like "), // 左匹配，会自动把参数右边加上 % 符号
    LIKE_RIGHT(" like "), // 右匹配，会自动把参数左边加上 % 符号
    NOT_LIKE(" not like "),
    NOT_LIKE_LEFT(" not like "),
    NOT_LIKE_RIGHT(" not like "),
    IN(" in "),
    NOT_IN(" not in ");

    private final String op;

    Op(String op) {
        this.op = op;
    }
}
```

当省略操作符时，会根据条件值做不同的处理。

| 值类型                     | 处理方式                                         |
| -------------------------- | ------------------------------------------------ |
| null                       | 值为空，转为 IS NULL                             |
| 数组                       | 转为 Op.IN                                       |
| Collection                 | 转为 Op.IN                                       |
| [Range 对象](/other/range) | 见 [例子](/other/range#range-对象的作为查询参数) |
| 其它                       | 转为 Op.EQ                                       |

## 条件对象 Cnd

条件对象 Cnd 是封装了一个查询条件的三要素（字段名、操作符和值）的对象，构造函数如下：

```java
public class Cnd {
    public Cnd(String field, Op op, Object value) {
        ...
    }
}
```

一般我们不会直接使用 Cnd，而是通过 Sql 对象的 where、and 和 or 方法来添加查询条件。

## 条件方法

where 和 and 方法在使用上没有太大区别，无论调用了多少个 where 或 and，Sql 对象最终只会拼接一个 where 到生成的 SQL 语句中。

where、and 和 or 方法调用参数类似，有以下几种形式（以 where 举例）：

```java
public Sql where(String field, Op op, Object value) // 添加一个条件
public Sql where(String field, Object value) // 省略操作符
public Sql where(String str) // 无需参数的情况下，直接拼接字符串，如 where("status=1")
public Sql where(Cnd cnd) // 添加一个条件对象
public Sql where(Cnds cnds) // 添加一个条件组对象

// 条件判断版本，如果 condition 为 true 时才添加条件
public Sql where(boolean condition, String field, Op op, Object value)
public Sql where(boolean condition, String field, Object value)
public Sql where(boolean condition, String str)
public Sql where(boolean condition, Cnd cnd)
// 使用 Supplier 延迟创建条件组，避免不必要的计算
public Sql where(boolean condition, Supplier<Cnds> supplier)
```

### 复杂条件

在构建复杂的 SQL 查询条件时，尤其是涉及 OR 条件时，确保逻辑的正确性至关重要。为此，我们引入了 Cnds 对象，以简化条件管理，处理优先级问题。

Cnds 对象内部维护着一组查询条件，它的 and、or 方法，和 Sql 对象的 and、or 方法一致。拼接 SQL 条件时，可以简单的把 Cnds 对象理解为一个括号，实际上它会根据上下文自动处理，以确保逻辑运算的优先级正确无误。

```java
// 查询年龄大于18岁的男生，或者年龄大于 16 岁的女生
public List<Student> query() {
    return dba.select().from("student")
        .where(Cnds.of("age", Op.GT, 18).and("gender", Gender.MALE))
        .or(Cnds.of("age", Op.GT, 16).and("gender", Gender.FEMALE))
        .queryForList(Student.class);
}

// 按条件判断是否要增加对应的条件组,使用 Supplier 延迟创建条件组，避免不必要的计算。
public List<Student> query(boolean includeFemale) {
    return dba.select().from("student")
        .where(Cnds.of("age", Op.GT, 18).and("gender", Gender.MALE))
        .or(includeFemale, ()->Cnds.of("age", Op.GT, 16).and("gender", Gender.FEMALE))
        .queryForList(Student.class);
}
```
