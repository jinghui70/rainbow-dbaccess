# 快速 CRUD：几行代码搞定一张表

一张表的增删改查，在传统 Spring Boot 项目里往往要写 Controller、Service、Mapper、XML 一整套样板。rainbow-dbaccess 提供了 `CrudService<T>` + `QueryDTO` + `CrudController<T>` 三件套：**继承即用，查询条件由前端用 JSON 直接传，后端一行 SQL 都不用写。**

核心思路是把"查询"这件事拆成两半——后端只负责定义实体和暴露端点，前端按需拼装 SELECT 字段、WHERE 条件、排序和分页，序列化成 JSON 发过来，`QueryDTO` 反序列化后直接生成 `Sql` 执行。

## CrudService：泛型增删改查服务

`CrudService<T>` 封装了对单个实体类的全部增删改查。直接 new 出来即可，也可以继承后覆写钩子方法：

```java
// 直接使用
CrudService<User> userService = new CrudService<>(dba, User.class);

// 或继承，覆写钩子做校验/填充
@Service
public class UserService extends CrudService<User> {
    public UserService(Dba dba) {
        super(dba, User.class);
    }

    @Override
    protected void beforeInsert(User user) {
        user.setId(IdUtil.fastSimpleUUID());
        user.setCreateTime(LocalDateTime.now());
    }
}
```

主要方法：

| 方法 | 说明 |
|------|------|
| `insert(T)` / `insert(Collection)` / `insert(Collection, batchSize)` | 单条 / 批量插入 |
| `update(T)` | 按主键全量更新 |
| `deltaUpdate(UpdateDTO<T>)` | 增量更新，只更新指定字段 |
| `getByKey(Object...)` | 按主键值查单条 |
| `getByObject(T)` | 按对象的主键值回查完整记录 |
| `getAll()` / `getAll(voClass)` | 查全部，按主键排序 |
| `queryPage(QueryDTO)` / `queryPage(QueryDTO, voClass)` | 分页查询 |
| `queryList(QueryDTO)` / `queryList(QueryDTO, voClass)` | 列表查询 |
| `delete(Object)` | 删除，支持单条 / 数组 / 集合 |

### 插入前后钩子

`beforeInsert`、`beforeUpdate` 是空实现，子类按需覆写，用来做主键生成、默认值填充、时间戳、校验等。`insert`、`update` 会在真正落库前调用它们。

### 插入后回查完整记录

`getByObject(T)` 用对象上的主键值回查整条记录——典型场景是插入后拿到数据库生成的字段（自增主键、默认值、触发器写入的时间戳等）：

```java
service.insert(user);          // 自增主键会被回填到 user
User full = service.getByObject(user);  // 据主键回查，拿到完整记录
```

### 增量更新

`deltaUpdate` 配合 `UpdateDTO<T>` 使用：`record` 携带主键和变更后的值，`changedProps` 指定哪些**属性**参与更新，底层走 `updateOf(record).include(changedProps)`。变更列表为空时直接返回 0，不执行 SQL：

```java
UpdateDTO<User> dto = new UpdateDTO<>(user, "name", "age");
service.deltaUpdate(dto); // 只 UPDATE name、age 两个字段
```

这正是前端表单"只提交改动字段"的后端落点，避免全量更新覆盖掉其他字段的并发修改。

## QueryDTO：把查询条件交给前端

`QueryDTO` 把一次查询的全部要素打包成一个可 JSON 序列化的对象：

| 字段 | 类型 | 说明 |
|------|------|------|
| `fields` | `String` | SELECT 字段，逗号分隔；为空查全部列 |
| `cnds` | `List<Cnd>` | WHERE 条件列表，多个之间 AND 连接 |
| `orderBys` | `List<OrderBy>` | 排序，每项含 `field` 和 `desc` |
| `pageNo` / `pageSize` | `int` | 分页参数 |

表名不由前端传——后端在 `queryPage`/`queryList` 里通过 `setEntity(clazz)` 用实体类的 `@Table` 注解推导，前端无从指定。

### 前端直接拼查询条件

前端按下面的结构拼 JSON，POST 给查询端点即可。每个条件就是 `Cnd` 的三要素——字段名 `field`、操作符 `op`、条件值 `value`：

```json
{
  "fields": "ID,NAME,AGE,STATUS",
  "cnds": [
    { "field": "NAME", "op": "LIKE", "value": "李" },
    { "field": "AGE", "op": "GE", "value": 18 },
    { "field": "STATUS", "value": "ACTIVE" }
  ],
  "orderBys": [
    { "field": "AGE", "desc": true }
  ],
  "pageNo": 1,
  "pageSize": 20
}
```

生成的 SQL 等价于：

```sql
SELECT ID,NAME,AGE,STATUS FROM T_USER
WHERE NAME LIKE ? AND AGE>=? AND STATUS=?
ORDER BY AGE DESC
```

`op` 取 `Op` 枚举名（`EQ`、`NE`、`GT`、`GE`、`LT`、`LE`、`LIKE`、`IN`、`IS_NULL` 等），不传时默认 `EQ`。`EQ` 仍然享受[智能推导](/cnd#eq-智能推导)：`value` 传数组变 IN，传 `null` 变 IS NULL，传 `{from, to}` 变 BETWEEN。

```json
{ "field": "ID", "op": "IN", "value": ["1", "2", "3"] }
{ "field": "AGE", "value": { "from": 20, "to": 40 } }
{ "field": "DELETED", "op": "IS_NULL" }
```

### 复合条件

`Cnd` 用 `children` 表达嵌套的 AND/OR——此时 `field` 存逻辑运算符（`AND` / `OR`），`children` 是子条件列表。下面这段表示 `STATUS='ACTIVE' AND (AGE<18 OR AGE>=60)`：

```json
{
  "cnds": [
    { "field": "STATUS", "value": "ACTIVE" },
    {
      "field": "OR",
      "children": [
        { "field": "AGE", "op": "LT", "value": 18 },
        { "field": "AGE", "op": "GE", "value": 60 }
      ]
    }
  ]
}
```

### 安全：字段名校验 + 参数占位

前端能传字段名，天然让人担心 SQL 注入。框架在两个层面挡住它：

- **条件值始终走 `?` 参数占位**，永远不会拼进 SQL 字符串。
- **字段名经过 `validateFieldName` 校验**——`setCnds` 时递归检查每个 `Cnd`（含嵌套 `children`）的 `field`，必须是合法 SQL 标识符（允许 `alias.field` 形式），否则抛异常。

所以前端可以自由组合条件，但传不进任何能构成注入的字段名或值。

### 后端定制：超越简单单表查询

前面都是"前端查什么、后端就查什么"。真实项目里后端常常要在前端的查询之上再加工——**前端以为查的是一个对象，后端可能是一个 JOIN 或更复杂的查询**；或者要附加前端不可见的权限、软删除等强制条件。有两种写法。

**写法一：派生 Service 里用链式定制**

`QueryDTO` 的链式方法可以设定查询表、查询字段、默认排序、追加条件。在派生 Service 的查询方法里，先对前端传来的 dto 做加工再执行：

```java
@Service
public class OrderService extends CrudService<Order> {
    public OrderService(Dba dba) {
        super(dba, Order.class);
    }

    public PageData<OrderVO> search(QueryDTO dto) {
        dto.setEntity("T_ORDER O JOIN T_USER U ON O.USER_ID = U.ID") // 实为 JOIN
           .setFields("O.*, U.NAME AS USER_NAME")
           .defaultOrderBy("O.CREATE_TIME")
           .addCnd(Cnd.where("O.DELETED", false)); // 强制条件，前端绕不过
        return dto.pageQuery(dba, OrderVO.class);
    }
}
```

前端依旧按"查一个对象"拼条件，感知不到背后是 JOIN。`addCnd` 追加的条件与前端条件 AND 连接，适合做软删除过滤、租户隔离、数据权限范围这类强制约束。

**写法二：继承 QueryDTO 扩展属性**

当前端要传一些无法用标准 `Cnd` 表达的参数（如一个关键字同时匹配多列），可以继承 `QueryDTO` 增加属性，并复写 `getSql()` 把它翻译成条件：

```java
public class OrderQueryDTO extends QueryDTO {
    private String keyword;
    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }

    @Override
    public Sql getSql(Dba dba) {
        Sql sql = super.getSql(dba); // 复用父类拼好的字段、条件、排序
        if (StrUtil.isNotBlank(keyword))
            sql.and(Cnd.or(
                Cnd.where("NAME", Op.LIKE, keyword),
                Cnd.where("PHONE", Op.LIKE, keyword)));
        return sql;
    }
}
```

把 `OrderQueryDTO` 作为端点入参，前端 JSON 里多带一个 `keyword` 字段即可。

::: warning 继承后不要用链式写法
链式方法（`setEntity`、`setFields`、`addCnd`、`defaultOrderBy`）返回的是父类型 `QueryDTO`，会丢掉子类。继承场景下直接用属性和复写方法。复写 `getSql()`（或 protected 的 `processCnd`/`processOrderBy`）给了最大的灵活性，几乎可以构造任意查询。
:::

### 结果映射为 VO

列表和分页查询返回的，往往不是数据库表对应的实体，而是裁剪过或多表拼装出来的 VO。所以 `CrudService` 的每个查询方法都有一个 `voClass` 重载，把结果直接映射成任意类，不必先查出实体再转换：

```java
PageData<UserVO> page = service.queryPage(dto, UserVO.class);
List<UserVO> list = service.queryList(dto, UserVO.class);
List<UserVO> all  = service.getAll(UserVO.class);
```

配合上面写法一的 JOIN，VO 正好用来承载多表查询出来的字段（如 `USER_NAME`）。

## CrudController：一键暴露 REST 端点

`CrudController<T>` 是抽象基类，把上面这些能力映射成 REST 端点。子类加上 `@RestController` 和 `@RequestMapping` 指定路径前缀即可：

```java
@RestController
@RequestMapping("/api/user")
public class UserController extends CrudController<User> {
    public UserController(Dba dba) {
        super(dba, User.class); // 自动创建 CrudService
    }
}
```

也可以把已有的 `CrudService` 传进去：`super(userService)`。

内置端点（全部 POST）：

| 端点 | 入参 | 说明 |
|------|------|------|
| `/insert` | 实体 | 插入并返回回查后的完整实体 |
| `/update` | 实体 | 全量更新并返回完整实体 |
| `/delta-update` | `UpdateDTO` | 增量更新，返回受影响行数 |
| `/get-by-key` | 主键值数组 | 按主键查单条 |
| `/query-page` | `QueryDTO` | 分页查询 |
| `/query-list` | `QueryDTO` | 列表查询 |
| `/delete` | 实体 | 按主键删除单条 |
| `/batch-delete` | 实体列表 | 批量删除 |

`/delete` 和 `/batch-delete` 只读取请求体里的主键字段，非主键字段会被忽略。

至此，定义一个实体类、写一个十来行的 Controller，一张表的增删改查接口就全部就绪了，前端按需拼条件查询，后端不写一行 SQL。

`CrudController` 既可以直接继承使用，也可以当成一份**开发参考**——它每个端点的逻辑都很薄，当内置端点不完全契合需求时，照着它的写法自己写一个 Controller 同样只要几行。

> 包里还有一个 `CommonObject`，提供 `id` 和 `name` 两个公共字段，可作字典表、配置项等简单实体的现成模型。
