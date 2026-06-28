# 自动生成字段值：@GeneratedValue

很多字段的值由程序生成而非业务录入——主键 id、创建时间、更新时间、流水号等。`@GeneratedValue` 把这类逻辑声明在实体字段上，插入或更新时自动生成并回填，不必在每个 Service 里手写赋值。

```java
@Table
public class User {
    @Id
    @GeneratedValue(param = "USR_")                       // 雪花 id 主键，生成 USR_ 开头的字符串
    private String id;

    @GeneratedValue(strategy = "now")                     // 插入时记录创建时间
    private LocalDateTime createTime;

    @GeneratedValue(strategy = "now", timing = GenerationTiming.INSERT_UPDATE)
    private LocalDateTime updateTime;                     // 插入和更新时都自动刷新

    private String name;
    // getter / setter ...
}

User user = new User();
user.setName("Alice");
dba.insert(user);
// 三个字段在插入时自动生成，并回填到 user 本身
String id = user.getId();              // 如 "USR_2J9K7X..."
LocalDateTime created = user.getCreateTime();   // 插入时的当前时间
LocalDateTime updated = user.getUpdateTime();   // 同 createTime

// 更新时，updateTime 会自动刷新为当前时间
user.setName("Alice Updated");
dba.update(user);
LocalDateTime newUpdateTime = user.getUpdateTime();  // 更新后的最新时间
```

## 注解与触发规则

`@GeneratedValue` 标注在实体字段上：

```java
import io.github.jinghui70.rainbow.dbaccess.annotation.GeneratedValue;
import io.github.jinghui70.rainbow.dbaccess.annotation.GenerationTiming;

@GeneratedValue(strategy = "default", param = "", timing = GenerationTiming.INSERT)
```

| 属性 | 说明 | 默认值 |
|------|------|--------|
| `strategy` | 生成策略名，对应已注册的 `ValueGenerator` | `default` |
| `param` | 传给生成器的参数，含义由策略决定 | 空字符串 |
| `timing` | 生成时机，控制在插入和/或更新时是否生成 | `GenerationTiming.INSERT` |

### 生成时机（timing）

`timing` 参数决定字段值在何时自动生成：

| timing 值 | 插入时行为 | 更新时行为 | 典型场景 |
|-----------|-----------|-----------|----------|
| `INSERT`（默认） | 字段为 `null` 时生成，已有值不覆盖 | 不触发生成，使用对象当前值 | 主键 id、创建时间 createTime |
| `INSERT_UPDATE` | 字段为 `null` 时生成，已有值不覆盖 | **强制生成新值，覆盖对象当前值** | 更新时间 updateTime、版本号、更新人 |

**INSERT_UPDATE 的强制覆盖行为**：当 `timing = INSERT_UPDATE` 时，更新操作会**无条件重新生成字段值并回填到对象**，即使你在代码中手动设置了该字段的值也会被覆盖。这是设计行为，确保更新时间等字段始终反映最新操作时刻。

行为约定：

- **插入时：仅当字段值为 `null` 时才生成**（对 `INSERT` 和 `INSERT_UPDATE` 都成立）——已有非 null 值不会被覆盖，业务可显式指定值绕过生成。
- **更新时：`INSERT` 不生成，`INSERT_UPDATE` 强制生成**——后者会覆盖对象中的现有值。
- **生成的值回填到入参对象本身**——`insert` / `update` 之后即可直接从对象拿到。
- **批量操作时逐行生成、逐行回填**。
- **仅对 Bean 操作生效**——`Map` 插入/更新不处理 `@GeneratedValue`。

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

## 典型场景：创建时间与更新时间

最常见的使用场景是为实体自动维护创建时间和更新时间：

```java
@Table(name = "T_ORDER")
public class Order {
    @Id
    @GeneratedValue
    private String id;

    @GeneratedValue(strategy = "now")  // 默认 timing = INSERT，仅插入时生成
    private LocalDateTime createTime;

    @GeneratedValue(strategy = "now", timing = GenerationTiming.INSERT_UPDATE)
    private LocalDateTime updateTime;  // 插入和更新时都生成

    private String orderNo;
    private BigDecimal amount;
    // getter / setter ...
}
```

**使用时的行为**：

```java
// 插入
Order order = new Order();
order.setOrderNo("ORD001");
order.setAmount(new BigDecimal("100.00"));
dba.insert(order);

// createTime 和 updateTime 都被自动生成
System.out.println(order.getCreateTime());  // 如 2024-03-15 10:30:00
System.out.println(order.getUpdateTime());  // 如 2024-03-15 10:30:00

// 更新
Thread.sleep(1000);
order.setAmount(new BigDecimal("200.00"));
dba.update(order);

// createTime 保持不变，updateTime 自动刷新
System.out.println(order.getCreateTime());  // 仍为 2024-03-15 10:30:00
System.out.println(order.getUpdateTime());  // 如 2024-03-15 10:30:01
```

**注意**：如果你在更新前手动设置了 `updateTime`，该值会被生成器覆盖——这是 `INSERT_UPDATE` 的设计行为，确保更新时间始终反映真实操作时刻，避免业务代码误传旧值或错误时间。

## 最佳实践

### 字段生成策略选择

| 字段类型 | 推荐 timing | 原因 |
|---------|------------|------|
| 主键 id | `INSERT`（默认） | 主键仅在创建时生成一次，更新时保持不变 |
| 创建时间 createTime | `INSERT`（默认） | 记录首次创建时刻，永不改变 |
| 更新时间 updateTime | `INSERT_UPDATE` | 每次更新都应刷新为当前时间 |
| 创建人 createBy | `INSERT`（默认） | 记录首次创建者，不应改变 |
| 更新人 updateBy | `INSERT_UPDATE` | 配合自定义生成器从上下文获取当前用户 |
| 版本号 version | `INSERT_UPDATE` | 配合自定义生成器实现乐观锁，每次更新递增 |

### 更新人字段的自定义生成器

配合 `INSERT_UPDATE` 可以实现自动维护更新人：

```java
@Component
public class CurrentUserGenerator implements ValueGenerator {
    @Override
    public String getName() {
        return "current-user";
    }

    @Override
    public Object generate(GenerateContext context) {
        // 从 Spring Security 或其他上下文获取当前用户
        return SecurityContextHolder.getContext()
            .getAuthentication()
            .getName();
    }
}

// 实体字段
@GeneratedValue(strategy = "current-user", timing = GenerationTiming.INSERT_UPDATE)
private String updateBy;
```

### 版本号乐观锁

```java
@Component
public class VersionGenerator implements ValueGenerator {
    @Override
    public String getName() {
        return "version";
    }

    @Override
    public Object generate(GenerateContext context) {
        Object data = context.data();
        if (data == null) return 1;  // 插入时初始版本
        
        // 更新时递增
        try {
            java.lang.reflect.Field field = context.field();
            field.setAccessible(true);
            Integer current = (Integer) field.get(data);
            return current == null ? 1 : current + 1;
        } catch (Exception e) {
            return 1;
        }
    }
}

// 实体字段
@GeneratedValue(strategy = "version", timing = GenerationTiming.INSERT_UPDATE)
private Integer version;
```
