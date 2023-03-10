[English](./README.md) | 简体中文

# rainbow-dbaccess

这是一个用来增强Spring JdbcTemplate的项目。

## 简介
那种把Sql写在一个配置文件，或者必须定义一个与数据表对应的JavaBean的做法，对我来说都不舒服。我更喜欢在代码中直接写SQL，同时还能简单的做对象映射。Spring JdbcTemplate已经提供了很好的基础，因此，只需要向前再迈一小步...

## 依赖

* JDK 1.8+
* SpringBoot 2.6+ / Spring5.3+
* [Hutool](https://hutool.cn) / 5.8.6+
* [H2 database](http://www.h2database.com) 2.1.214

## 安装
在项目的pom.xml中的依赖项中添加以下内容
```xml
<dependency>
    <groupId>io.github.jinghui70</groupId>
    <artifactId>rainbow-dbaccess</artifactId>
    <version>5.1.11</version>
</dependency>
```
在SpringBoot项目中，rainbow-dbaccess支持自动配置，可以直接注入：
```java
@Autowired
private Dba dba;
```
也可以通过构造函数传入一个```dataSource```来创建```dba```对象。

## 基本使用
```Dba```底层使用的是 ```JdbcTemplate``` 或者 ```NamedJdbcTemplate```，用法与它们类似，只是把sql语句和参数封装成了```Sql```或者```NamedSql```对象。这两个对象的主要工作就是拼接Sql和收集参数。

它们都是从```StringBuilderWrapper```这个可以更好的拼接string的基类派生下来的，```Sql```对象用一个列表保存参数，```NamedSql```对象使用一个Map保存参数。

对于增、删、改，使用execute()执行，对于查，使用各种queryForXXX得到结果。
```java
dba.sql("select * from FOO where ID=1").queryForObject(Foo.class);

dba.namedSql("update FOO set salary=100").where("ID",1).execute();
```
开发中，可以用字符串直接写一个sql语句，更方便直观的方式，是利用Dba提供的许多函数（如：select、from、where、and、orderBy）简化拼接过程。

## 查询

### 查询条件
```java
dba.sql("select * from student where gender=?").addParam("男").append(" and age>?").addParam(16);
dba.sql("select * from student where gender=? and age>?").addParam("男"，16);
dba.sql("select * from student").where("gender", "男").and("age",">", 16);
```
上面三个语句的效果是一样的，最后一个语句，```where```和```and```函数都各自封了一个查询条件对象```Cnd```，它支持以下的操作符：
```
"<=", "<", ">=", ">", "=", "!=", IN, LIKE, NOT_IN, NOT_LIKE
```
### 子查询
Sql对象也可以成为查询参数：
```java
Sql sql = Sql.create("select student_id from score").where("score",">", 60);
// 计算考试及格的学生个数
dba.select("count(1)").from("student").where("id",sql).queryForInt();
// 找到所有不及格学生姓名
dba.select("name").from("student").where("id", Cnd.NOT_IN, sql).queryForValueList(String.class);
```
### where 1=1
很多代码会有这样的写法，特别是当循环加入条件的时候，第一个前面要用where，后续的要用and，于是有人发明了```where 1=1```这种写法。

现在完全没有这个必要了，你可以都写where，也可以都写and，执行的sql不会有问题。

### 查询结果
只查询一个字段，使用以下函数：
```java
// 结果只有一条记录
<T> Optional<T> queryForValue(Class<T> requiredType);
String queryForString();
int queryForInt();
double queryForDouble();

// 例子
dba.select("date").from("foo").where("id",1).queryForValue(LocalDate.class);
dba.select("date").from("foo").where("id",1).queryForString(); 

// 结果有多条记录
<T> List<T> queryForValueList(Class<T> requiredType);
// 例子
dba.select("code").from("foo").queryForValueList(String.class);
```
查询多个字段情况 JdbcTemplate使用RowMapper来映射对象，Dba提供了三个Mapper：
* MapRowMapper 用于映射为Map
* BeanMapper 用于映射为对象
* ObjectArrayMapper 用于映射为数组

> BeanMapper对象映射的默认原则是：数据库字段名是横线连接 (kebab-case)，对象属性是CamalCase。如果属性有```@Column```标记，则按标记指定的字段名映射。
> 
> BeanMapper还支持数组属性的映射，只要在属性上加```@ArrayField```标记，就可以把value[] 属性映射为VALUE_1,VALUE_2...字段
>
> MapRowMapper 和 BeanMapper 提供了默认行为，同时，它们还可以在映射时做特殊的调整，详细内容见JavaDoc

只查询一条记录情况
```java
Map<String, Object> map = dba.sql("select * from student").where("id", 1).queryForMap();
// 性别代码转文字
Function<String, String> genderFunction = (code)-> {
    return "1".equals(code) ? "男" : "女";
};
// 查询结果性别字段转文字
Map<String, Object> map = dba.sql("select * from student").where("id", 1).queryForMap(
        MapRowMapper.create().transform("gender", genderFunction));
// 查询一个对象
Student student = dba.sql("select * from student").where("id",1).queryForObject(Student.class);
```
查询多条记录情况：
```java
Sql sql = dba.sql("select * from student");
List<Map<String, Object>> list = sql.queryForList();
List<Student> students = sql.queryForList(Student.class);
```
更复杂的结果处理：
* queryToMap: 把多条查询结果转为一个Map
* queryToGroup：把查询结果分组
* query: 自己处理每一条记录

分页查询，通过不同的数据库方言```Dialect```实现分页查询（数据库方言是在创建Dba的时候设置的）：
```java
// 取第一页，每页20条
PageData<Student> data = dba.select("*").from("student").pageQuery(Student.class , 1, 20);
// 取前十
List<Student> data = dba.select("*").from("student").limit(10).queryForList(Student.class);
// 取第10条到第20条
List<Student> data = dba.select("*").from("student").range(10,20).queryForList(Student.class);
```

## 插入和更新

普通的插入更新，可以直接写sql实现。
```java
dba.sql("insert into STUDENT(ID,NAME,AGE).values(?,?.?)").addParam("007","JAMES",40).execute();

dba.update("STUDENT").set("NAME","BOND").set("AGE",27).where("ID", "007").execute();
```

对于Bean对象的插入更新，Dba提供了更方便的方法。
```java
// 插入一个对象，对象表名与类名默认是 kebab-camelCase关系,也可以在类上标记@Table指定表名
dba.insert(student);
// 插入一个Map
Map<String, Object> map = ...
dba.insert("TBL_STUDENT", map);
// 插入一个列表
List<Student> student = ...        
dba.insert(students);
List<Map<String, Object>> list = ...
dba.insert("TBL_STUDENT", maps);

// 更新一个对象，对象的主键属性用```@Id```标记
dba.update(student);
```
> 每一个```insert```函数，Dba都另有一个对应的```merge```函数，提供有则更新无则插入功能。这个函数不是所有的数据库都支持，使用的时候需要注意。

## 事务
```java
// 简单包裹一个事务
dba.transaction(()->{
  dba.insert(...);
  dba.deleteFrom("xxx").where(...).execute();
  dba.sql("update ....").execute();
})

// 或者返回一个对象
Object result = dba.transaction((status)->{
    dba.insert(...);
    dba.deleteFrom("xxx").where(...).execute();
    Object result = ...;
    return result;
})
```
## 内存表
开发时一般会用到List、Set、Map等集合类对象在内存中维护数据，但是对于复杂的业务逻辑，这些集合类有时不能很好的满足开发需求。 感谢H2提供了内存表，使得我们可以在内存中使用Sql来处理数据。
```java
try(MemoryDba mDba = new MemoryDba()) { // 要保证最后释放内存表
    // 先创建表
    mDba.createTable(Table.create("T").add(
        Field.createKeyInt("ID")
        Field.createString("NAME")
        ...
    ));
    // mDba 可以随意使用了
}
```

## 对枚举的支持
对象属性、条件参数支持直接使用枚举，默认存到数据库中的是枚举的 ordinal 值。
如果要保存任意设定的字符串值，请让这个枚举实现```CodeEnum```接口。

## 树形结构的支持
树形结构通常会有一个字段指向上级，如下面定义的表结构：

|PARENT_NO | MY_NO | NAME |
|----------|-------|------|
|root| 01    | 亚洲   |
|01| 0101  | 中国   |

定义对象如下：
```java
class Country extends TreeNode<Country> {
    private String myNo;
    private String name;
    ...
}
```
查询方法如下：
```java
List<Country> list = dba.sql("select PARENT_NO AS PID,MY_NO AS ID, MY_NO,NAME from COUNTRY").queryForTree(Country.class)
```
> 注意需要查询字段中要有```PID、ID```两个字段，结果对象可以不必有这两个字段。
> 
> 从 TreeNode 派生，得到的对象会有```children```属性

如果不希望从TreeNode派生，可以用```WrapTreeNode```来包裹对象，写法如下:
```java
class Country {
    private String myNo;
    private String name;
    ...
}
List<WrapTreeNode<Country>> list = dba.sql("select PARENT_NO AS PID,MY_NO AS ID, MY_NO,NAME from COUNTRY").queryForWrapTree(Country.class)
```