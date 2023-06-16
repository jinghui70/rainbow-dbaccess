English | [简体中文](./README-CN.md)

# rainbow-dbaccess

This is a project to enhance Spring JdbcTemplate.

## Introduction

The approach of writing Sql in a configuration file, or having to define a JavaBean that corresponds to a data table, is
not comfortable for me. I prefer to write SQL directly in the code, while still being able to simply do object mapping.
Spring JdbcTemplate already provides a good foundation for this, so we just need to take another small step forward...

## Dependency

* JDK 1.8+
* SpringBoot 2.6+ / Spring5.3+
* [Hutool](https://hutool.cn) / 5.8.6+
* [H2 database](http://www.h2database.com) 2.1.214

## Install

Add the following to the dependencies in the project's pom.xml:

```xml

<dependency>
    <groupId>io.github.jinghui70</groupId>
    <artifactId>rainbow-dbaccess</artifactId>
    <version>5.1.14</version>
</dependency>
```

In SpringBoot project, rainbow-dbaccess supports automatic configuration and can be injected directly

```java
@Autowired
private Dba dba;
```

You can also create a ```dba``` object by passing a ```dataSource``` into the constructor.

## Basic Usage

The underlying layer of ```Dba``` is ```JdbcTemplate``` or ```NamedJdbcTemplate```, the usage is similar. except that
the sql statements and parameters are encapsulated into ```Sql``` or ```NamedSql``` objects. The main job of these two
objects is to splice Sql and collect parameters.

They are both derived from ```StringBuilderWrapper```, a base class that allows better splicing of strings.
The ```Sql``` object uses a list to keep the parameters, and the ```NamedSql``` object uses a Map to keep the
parameters.

For add, delete, update, use ```execute()``` to execute, for query, use various queryForXXX to get results.

```java
dba.sql("select * from FOO where ID=1").queryForObject(Foo.class);

        dba.namedSql("update FOO set salary=100").where("ID",1).execute();
```

In development, you can write a sql statement directly with a string. A more convenient and intuitive way to simplify
the splicing process is to use the functions provided by Dba (e.g. select, from, where, and, orderBy).

## Query

### Query conditions

```java
dba.sql("select * from student where gender=?").addParam("男").append(" and age>?").addParam(16);
        dba.sql("select * from student where gender=? and age>?").addParam("男"，16);
        dba.sql("select * from student").where("gender","男").and("age",">",16);
```

The three statements above have the same effect, and the last statement, the ```where``` and ```and``` functions each
enclose a query condition object, ```Cnd```, which supports the following operators.

```
"<=", "<", ">=", ">", "=", "!=", IN, LIKE, NOT_IN, NOT_LIKE
```

### Subquery

Sql objects can be query parameters as well：

```java
Sql sql=Sql.create("select student_id from score").where("score",">",60);
// Calculate the number of students who passed the exam
        dba.select("count(1)").from("student").where("id",sql).queryForInt();
// Find the names of all students who failed the exam
        dba.select("name").from("student").where("id",Cnd.NOT_IN,sql).queryForValueList(String.class);
```

### where 1=1

A lot of code will be written in this way, especially when using the loop to add conditions, someone
invented ```where 1=1``` to simplify Sql splicing

Now there is no need for this at all, you can write ether ```where``` or ```and```, and Dba will do the math.

### Query result

To query only one field, use the following function.

```java
// Result with one record
<T> Optional<T> queryForValue(Class<T> requiredType);
        String queryForString();
        int queryForInt();
        double queryForDouble();

// Sample
        dba.select("date").from("foo").where("id",1).queryForValue(LocalDate.class);
        dba.select("date").from("foo").where("id",1).queryForString();

// Result with multiple records
<T> List<T> queryForValueList(Class<T> requiredType);
// Sample
        dba.select("code").from("foo").queryForValueList(String.class);
```

When querying multiple fields JdbcTemplate uses RowMapper to map objects, Dba provides three Mappers.

* **MapRowMapper** Used to map to Map
* **BeanMapper** Used to map to Object
* **ObjectArrayMapper** Used to map to Object[]

> BeanMapper assumes that the database field name is in horizontal concatenation (kebab-case) by default, and the object property is in CamelCase. But if the property has a ```@Column``` annotation, it uses the field name specified by the annotation.
>
> BeanMapper also supports mapping of array properties, just add @ArrayField annotation to the property, you can map the value[] property to VALUE_1,VALUE_2...
>
> MapRowMapper and BeanMapper provide default behavior, but they can also do special operations when mapping, please read the JavaDoc for details

Searching for only one record

```java
Map<String, Object> map=dba.sql("select * from student").where("id",1).queryForMap();
// Gender code to text
        Function<String, String> genderFunction=(code)->{
        return"1".equals(code)?"male":"female";
        };
// Query result： Gender field content to text
        Map<String, Object> map=dba.sql("select * from student").where("id",1).queryForMap(
        MapRowMapper.create().transform("gender",genderFunction));
// Query for a bean
        Student student=dba.sql("select * from student").where("id",1).queryForObject(Student.class);
```

Query multiple records：

```java
Sql sql=dba.sql("select * from student");
        List<Map<String, Object>>list=sql.queryForList();
        List<Student> students=sql.queryForList(Student.class);
```

More complex result processing：

* queryToMap: Turn the query result into a Map
* queryToGroup：Group the query result
* query: Process each record yourself

Paging queries, achieved by different database dialects ```Dialect``` (database dialects are set at the time of Dba
creation).

```java
// Take the first page, 20 items per page
PageData<Student> data=dba.select("*").from("student").pageQuery(Student.class ,1,20);
// Top Ten
        List<Student> data=dba.select("*").from("student").limit(10).queryForList(Student.class);
// Take the 10th to 20th
        List<Student> data=dba.select("*").from("student").range(10,20).queryForList(Student.class);
```

## Insert and Update

For ordinary insert updates, you can write sql directly.

```java
dba.sql("insert into STUDENT(ID,NAME,AGE).values(?,?.?)").addParam("007","JAMES",40).execute();

        dba.update("STUDENT").set("NAME","BOND").set("AGE",27).where("ID","007").execute();
```

For Bean objects, Dba provides a more convenient method.

```java
// insert a bean，The object table name and the class name are kebab-camelCase relationship by default, you can also add annotation @Table on the class to specify the table name
dba.insert(student);
// insert a map
        Map<String, Object> map=...
        dba.insert("TBL_STUDENT",map);
// insert a list
        List<Student> student=...
        dba.insert(students);
        List<Map<String, Object>>list=...
        dba.insert("TBL_STUDENT",maps);

// Update an object whose primary key property has ``@Id`` annotation
        dba.update(student);
```

> For every ```insert``` function, Dba has a corresponding ```merge``` function，to conditionally insert or update data depending on its presence。This function is not supported by all databases, so be careful when using it.

## Transaction

```java
// simple transaction
dba.transaction(()->{
        dba.insert(...);
        dba.deleteFrom("xxx").where(...).execute();
        dba.sql("update ....").execute();
        })

// or return a value
        Object result=dba.transaction((status)->{
        dba.insert(...);
        dba.deleteFrom("xxx").where(...).execute();
        Object result=...;
        return result;
        })
```

## Memory Table

When developing, we usually use List, Set, Map and other collection class objects to maintain data in memory, but when
we encounter complex business logic, these collection classes sometimes do not meet the needs well. Thankfully, H2
provides in-memory tables that allow us to use Sql to process data in memory.

```java
try(MemoryDba mDba=new MemoryDba()){ // Guaranteed final release of memory tables
        // Create the table first
        mDba.createTable(Table.create("T").add(
        Field.createKeyInt("ID")
        Field.createString("NAME")
        ...
        ));
        // mDba can now be used freely
        }
```

## Support for enum

Object properties, conditional parameters support the direct use of enumerations, the default value stored in the
database is the ordinal value of the enumeration. To save an arbitrary string value, make this enumeration implement
the ``CodeEnum`` interface.

## Support for tree structure data

The tree structure usually have a field pointing to upper level， For example, look at the following table:

|PARENT_NO | MY_NO | NAME  |
|----------|-------|-------|
|root| 01    | Asia  |
|01| 0101  | China |

```java
class Country extends TreeNode<Country> {
    private String myNo;
    private String name;
    ...
}
    List<Country> list = dba.sql("select PARENT_NO AS PID,MY_NO AS ID, MY_NO,NAME from COUNTRY").queryForTree(Country.class)
```

> Note that you need to have ``PID, ID`` in the query fields, the result object can be without these two fields.
>
> Derived from a TreeNode, the resulting object will have the ```children``` property

If you don't want to derive from a TreeNode, you can use ````WrapTreeNode````, written as follows:

```java
class Country {
    private String myNo;
    private String name;
    ...
}
    List<WrapTreeNode<Country>> list = dba.sql("select PARENT_NO AS PID,MY_NO AS ID, MY_NO,NAME from COUNTRY").queryForWrapTree(Country.class)
```