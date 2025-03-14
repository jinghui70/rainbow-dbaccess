# Range 对象

Range 对象表示一个值的范围。定义如下：

```java
public class Range<T extends Comparable<T>> {

    private T from;

    private T to;

    public T getFrom() {
        return from;
    }

    public void setFrom(T from) {
        this.from = from;
    }

    public T getTo() {
        return to;
    }

    public void setTo(T to) {
        this.to = to;
    }
}
```

## Range 对象的作为查询参数

::: info
作为查询参数时，如果一个 Map 有 from 和 to 两个属性，那么这个 Map 也被当做 Range 对象处理。
:::

```json
// Range 对象作为查询参数
Range range = new Range();

// Map 对象作为查询参数
Map<String, Integer> range = new HashMap<>();
range.put("from", 18);
range.put("to", 60);

dba.select().from("USER").where("age", range);
```

| 取值场景           | 转换后的 sql                               | 参数    |
| ------------------ | ------------------------------------------ | ------- |
| from 为空，to 为空 | SELECT from USER where age IS NULL         |         |
| from 为空, to = 60 | SELECT from USER where age < ?             | [60]    |
| from = 18, to 为空 | SELECT from USER where age > ?             | [18]    |
| from = 18, to = 60 | SELECT from USER where age between ？and ? | [18,60] |
| from = 18, to = 18 | SELECT from USER where age = ?             | [18]    |
