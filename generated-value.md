# 自动生成字段值：@GeneratedValue

很多字段在插入时才需要值，且值由程序生成而非业务录入——主键 id、创建时间、流水号等。`@GeneratedValue` 把这类逻辑声明在实体字段上，插入时自动生成并回填，不必在每个 Service 里手写赋值。

```java
@Table
public class GenEntity {
    @Id
    @GeneratedValue(param = "PRE_")                       // 雪花 id 主键，生成 PRE_ 开头的字符串
    private String id;

    @GeneratedValue(strategy = "now")                     // 当前 LocalDateTime
    private LocalDateTime createTime;

    @GeneratedValue(strategy = "now", param = "yyyyMMdd")  // 格式化后的当前日期字符串
    private String createDate;

    // getter / setter ...
}

GenEntity entity = new GenEntity();
dba.insert(entity);
// 三个字段在插入时自动生成，并回填到 entity 本身
String id = entity.getId();  // 如 "PRE_2J9K7X..."
```

## 注解与触发规则

`@GeneratedValue` 标注在实体字段上：

```java
import io.github.jinghui70.rainbow.dbaccess.annotation.GeneratedValue;

@GeneratedValue(strategy = "default", param = "")
```

| 属性 | 说明 | 默认值 |
|------|------|--------|
| `strategy` | 生成策略名，对应已注册的 `ValueGenerator` | `default` |
| `param` | 传给生成器的参数，含义由策略决定 | 空字符串 |

行为约定：

- **仅当字段值为 `null` 时才生成**——已有非 null 值不会被覆盖，业务可显式指定值绕过生成。
- **生成的值回填到入参对象本身**——`insert` 之后即可直接从对象拿到（如主键）。
- **批量插入时逐行生成、逐行回填**。
- **仅对 Bean 插入生效**——`Map` 插入不处理 `@GeneratedValue`。

## 内置策略

### default：雪花算法 id

省略 `strategy` 即用此策略。按字段类型返回不同形式：

| 字段类型 | 返回值 |
|----------|--------|
| `long` / `Long` | 数字雪花 id |
| `String` | 雪花 id 的 36 进制大写形式，`param` 作为前缀 |

```java
@Id
@GeneratedValue                       // strategy 默认 "default"
private Long id;                      // → 数字 id

@Id
@GeneratedValue(param = "ORD_")       // → "ORD_2J9K7X..."
private String orderNo;
```

雪花算法的 workerId / datacenterId 依次取：JVM `-D` 参数 → 系统环境变量 `SNOWFLAKE_WORKER_ID` / `SNOWFLAKE_DATACENTER_ID` → 都没有则为 `0`。多实例部署时务必为每个节点配置不同的 workerId，避免 id 冲突：

```bash
java -DSNOWFLAKE_WORKER_ID=1 -DSNOWFLAKE_DATACENTER_ID=0 -jar app.jar
```

### now：当前时间

按字段类型返回当前时间：

| 字段类型 | 返回值 |
|----------|--------|
| `LocalDateTime` | `LocalDateTime.now()` |
| `Timestamp` | 当前 `Timestamp` |
| `Date`（含子类） | 当前 `Date` |
| `String` | 按 `param` 指定的格式格式化，`param` 为空时用 `yyyy-MM-dd HH:mm:ss` |

```java
@GeneratedValue(strategy = "now")
private LocalDateTime createTime;

@GeneratedValue(strategy = "now", param = "yyyy-MM-dd")
private String createDate;
```

## 自定义策略

实现 `ValueGenerator` 接口即可扩展自己的生成逻辑——例如取数据库序列、按当前最大值递增、生成业务流水号。Spring 环境下直接把它标为组件，`DbaAutoConfiguration` 会自动收集并注册：

```java
import io.github.jinghui70.rainbow.dbaccess.valuegen.ValueGenerator;
import io.github.jinghui70.rainbow.dbaccess.valuegen.GenerateContext;
import org.springframework.stereotype.Component;

@Component
public class SeqGenerator implements ValueGenerator {

    @Override
    public String getName() {
        return "seq";  // 对应 @GeneratedValue(strategy = "seq")
    }

    @Override
    public Object generate(GenerateContext context) {
        // param 作为序列名，用 dba 查库取下一个值
        return context.dba()
                .sql("SELECT " + context.param() + ".NEXTVAL FROM DUAL")
                .queryForValue(Long.class);
    }
}
```

`GenerateContext` 提供生成所需的全部信息：

| 字段 | 说明 |
|------|------|
| `dba()` | 数据库访问对象，供需要查库的策略使用（取序列、查最大值递增等） |
| `data()` | 当前正在插入的行对象，可据其它字段的值来生成 |
| `field()` | 目标字段，可据其类型决定返回值类型 |
| `param()` | 注解里 `param` 的值，含义由策略约定 |

### 注册生成器

- **Spring 环境**：如上在生成器类上标 `@Component`（或声明为任意 Bean）即可，`DbaAutoConfiguration` 通过 `ObjectProvider<ValueGenerator>` 自动收集注册。
- **非 Spring 环境**：手动注册到全局注册表：

  ```java
  import io.github.jinghui70.rainbow.dbaccess.valuegen.ValueGeneratorRegistry;

  ValueGeneratorRegistry.register(new SeqGenerator());
  ```

注册以 `getName()` 为键，同名覆盖。注册后即可在实体上引用：

```java
@GeneratedValue(strategy = "seq", param = "ORDER_SEQ")
private Long id;
```

生成器实现应是无状态、线程安全的。
