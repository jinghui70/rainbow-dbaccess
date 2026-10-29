package io.github.jinghui70.rainbow.dbaccess;

import io.github.jinghui70.rainbow.dbaccess.cnd.Cnd;
import io.github.jinghui70.rainbow.dbaccess.cnd.Op;
import io.github.jinghui70.rainbow.dbaccess.memory.Field;
import io.github.jinghui70.rainbow.dbaccess.model.ComplexKeyEntity;
import io.github.jinghui70.rainbow.dbaccess.model.Status;
import io.github.jinghui70.rainbow.dbaccess.model.User;
import io.github.jinghui70.rainbow.dbaccess.sql.PageData;
import io.github.jinghui70.rainbow.dbaccess.sql.Sql;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 文档示例回归测试 —— 把 Javadoc 里的示例代码当真实代码跑一遍。
 * <p>
 * 存在的理由：Javadoc 里 {@code {@code ...}} 块中的示例既不被编译器检查，也不被 doclint 检查。
 * 曾经出现过 {@code Sql} 类级文档的 3 个示例全部抛 NPE、以及示例里有多余逗号无法编译的情况。
 * 因此凡是文档里承诺「可以这样用」的链式调用，都在这里跑通一遍，改文档时同步改这里。
 *
 * @author lijinghui
 */
class JavadocExampleTest extends BaseTest {

    /** 文档示例中的列名常量 */
    private static final String NAME = "NAME";
    private static final String AGE = "AGE";
    private static final String ID = "ID";

    /** 示例文档用的测试枚举，对应 Sql 类级文档里的 {@code Gender}。 */
    enum Gender {
        Male, Female
    }

    /** 建一张带 STATUS / GENDER 的用户表，供 Sql 类级文档示例使用。 */
    private void createDocUserTable() {
        dba.createTable("T_USER",
                Field.createKeyString("ID"),
                Field.createString("NAME"),
                Field.createInt("AGE"),
                Field.createDouble("SCORE"),
                Field.createString("STATUS"),
                Field.createString("GENDER"));
    }

    // ------------------------------------------------------------------
    // Sql 类级 Javadoc 示例
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Sql 类级文档：简单查询示例可执行")
    void sqlDocSimpleQuery() {
        createDocUserTable();
        dba.insert(new User("1", "Tom", 20, 90.0));
        dba.insert(new User("2", "Kid", 10, 60.0));

        // 文档原文：dba.select().from("T_USER").where("AGE", Op.GT, 18).queryForList(User.class)
        List<User> users = dba.select().from("T_USER").where("AGE", Op.GT, 18).queryForList(User.class);

        assertEquals(1, users.size());
        assertEquals("Tom", users.get(0).getName());
    }

    @Test
    @DisplayName("Sql 类级文档：Cnd 嵌套复杂查询示例可执行")
    void sqlDocComplexQuery() {
        createDocUserTable();
        insertDocUser("1", "Adult male", 20, Status.ACTIVE, Gender.Male);
        insertDocUser("2", "Young male", 19, Status.ACTIVE, Gender.Male);     // 男性要求 >= 20，排除
        insertDocUser("3", "Adult female", 18, Status.ACTIVE, Gender.Female); // 女性要求 >= 18，命中
        insertDocUser("4", "Inactive", 30, Status.INACTIVE, Gender.Male);     // STATUS 不符，排除

        // 文档原文（注意：这里不能出现多余逗号，否则文档示例无法编译）
        List<User> users = dba.select("NAME", "AGE")
                .from(User.class)
                .where("STATUS", Status.ACTIVE)
                .and(
                        Cnd.or(
                                Cnd.and(Cnd.where("GENDER", Gender.Male), Cnd.where("AGE", Op.GE, 20)),
                                Cnd.and(Cnd.where("GENDER", Gender.Female), Cnd.where("AGE", Op.GE, 18))
                        )
                )
                .orderBy("NAME")
                .queryForList(User.class);

        assertEquals(2, users.size());
        assertEquals("Adult female", users.get(0).getName());
        assertEquals("Adult male", users.get(1).getName());
    }

    @Test
    @DisplayName("Sql 类级文档：分页查询示例可执行")
    void sqlDocQueryPage() {
        createDocUserTable();
        for (int i = 1; i <= 25; i++) {
            dba.insert(new User(String.valueOf(i), "U" + i, 20 + i, 1.0 * i));
        }

        // 文档原文：dba.select().from(User.class).queryPage(User.class, 1, 10)
        PageData<User> page = dba.select().from(User.class).queryPage(User.class, 1, 10);

        assertNotNull(page);
        assertEquals(25, page.getTotal());
        assertEquals(10, page.getData().size());
    }

    @Test
    @DisplayName("静态 Sql.select() 未关联 Dba，执行查询抛 NPE —— 文档已声明此约定")
    void staticSqlSelectCannotExecute() {
        // 这是 Sql.select(String...) 的 Javadoc 明确承诺的行为：
        // 静态工厂只拼 SQL 文本，要执行必须走 dba.select() 或 setDba()。
        // 一旦哪天静态工厂能执行了，这个测试会失败，提醒同步修改文档。
        Sql sql = Sql.select().from("T_USER").where("AGE", Op.GT, 18);
        assertThrows(NullPointerException.class, () -> sql.queryForList(User.class));

        // 但拼 SQL 文本是它的正当用途，不该抛异常
        assertTrue(sql.getSql().toUpperCase().contains("SELECT"));

        // setDba 之后即可执行
        createDocUserTable();
        dba.insert(new User("1", "Tom", 20, 90.0));
        assertEquals(1, Sql.select().from("T_USER").where("AGE", Op.GT, 18)
                .setDba(dba).queryForList(User.class).size());
    }

    // ------------------------------------------------------------------
    // Dba 类级 Javadoc 示例
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Dba 类级文档：查询 / 插入 / 更新 / 删除 / 事务示例全链路可执行")
    void dbaClassDocExamples() {
        createUserTable();
        dba.insert(new User("1", "Tom", 20, 90.0));

        // 查询
        User user = dba.selectByKey(User.class, "1");
        assertEquals("Tom", user.getName());
        List<User> users = dba.select().from("T_USER").where("AGE", Op.GT, 18).queryForList(User.class);
        assertEquals(1, users.size());

        // 插入
        dba.insert(new User("2", "Jerry", 22, 80.0));
        Map<String, Object> mapData = new HashMap<>();
        mapData.put("ID", "3");
        mapData.put("NAME", "MapUser");
        mapData.put("AGE", 33);
        mapData.put("SCORE", 70.0);
        dba.insertOf(mapData).into("T_USER").execute();
        assertEquals(3, dba.select().from("T_USER").count());

        // 更新
        user.setName("NewTom");
        dba.update(user);
        assertEquals("NewTom", dba.selectByKey(User.class, "1").getName());
        dba.update("T_USER").set("NAME", "NewName").where("ID", "1").execute();
        assertEquals("NewName", dba.selectByKey(User.class, "1").getName());

        // 删除
        dba.deleteByKey(User.class, "3");
        assertNull(dba.selectByKey(User.class, "3"));
        dba.deleteFrom("T_USER").where("NAME", "NewName").execute();
        assertNull(dba.selectByKey(User.class, "1"));

        // 事务
        dba.transaction(() -> {
            dba.insert(new User("10", "T1", 10, 1.0));
            dba.insert(new User("11", "T2", 11, 2.0));
        });
        assertNotNull(dba.selectByKey(User.class, "10"));
        assertNotNull(dba.selectByKey(User.class, "11"));
    }

    // ------------------------------------------------------------------
    // Dba 方法级 Javadoc 示例
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Dba 方法级文档：sql / select / selectByKey 示例可执行")
    void dbaQueryMethodDocExamples() {
        createUserTable();
        dba.insert(new User("1", "Tom", 20, 90.0));

        // sql(String) 的示例
        assertEquals(1, dba.sql("SELECT * FROM T_USER WHERE ID=?")
                .addParam("1").queryForList(User.class).size());

        // select() 的示例
        assertEquals("Tom", dba.select().from("T_USER").where("ID", "1")
                .queryForObject(User.class).getName());

        // select(String...) 的示例
        assertEquals(1, dba.select("NAME", "AGE").from("T_USER").queryForList(User.class).size());

        // selectByKey 的示例
        assertEquals("Tom", dba.selectByKey(User.class, "1").getName());
    }

    @Test
    @DisplayName("Dba 方法级文档：复合主键 selectByKey 示例可执行")
    void dbaComplexKeyDocExample() {
        createComplexKeyTable();
        dba.insert(new ComplexKeyEntity("A", "B", "V"));

        // 文档原文：dba.selectByKey(ComplexKeyEntity.class, "A", "B")
        ComplexKeyEntity entity = dba.selectByKey(ComplexKeyEntity.class, "A", "B");

        assertNotNull(entity);
        assertEquals("V", entity.getRemark());
    }

    @Test
    @DisplayName("Dba 方法级文档：update / deleteFrom / delete 示例可执行")
    void dbaWriteMethodDocExamples() {
        createUserTable();
        dba.insert(new User("1", "Tom", 20, 90.0));
        dba.insert(new User("2", "Kid", 10, 60.0));

        // update(String) / update(Class) 的示例
        dba.update("T_USER").set("NAME", "NewName").where("ID", "1").execute();
        assertEquals("NewName", dba.selectByKey(User.class, "1").getName());
        dba.update(User.class).set("NAME", "NewName2").where("ID", "1").execute();
        assertEquals("NewName2", dba.selectByKey(User.class, "1").getName());

        // deleteFrom(String) 的示例：where("AGE", Op.LT, 18)
        dba.deleteFrom("T_USER").where("AGE", Op.LT, 18).execute();
        assertNull(dba.selectByKey(User.class, "2"));

        // delete(bean) 的示例
        User existingUser = dba.selectByKey(User.class, "1");
        dba.delete(existingUser);
        assertNull(dba.selectByKey(User.class, "1"));
    }

    @Test
    @DisplayName("Dba 方法级文档：deleteFrom(Class).where(field, null) 示例可执行")
    void dbaDeleteNullDocExample() {
        createUserTable();
        dba.insert(new User("1", null, 20, 90.0));
        dba.insert(new User("2", "Kid", 10, 60.0));

        // 文档原文：dba.deleteFrom(User.class).where("NAME", null).execute()
        dba.deleteFrom(User.class).where("NAME", null).execute();

        assertNull(dba.selectByKey(User.class, "1"));
        assertNotNull(dba.selectByKey(User.class, "2"));
    }

    @Test
    @DisplayName("doc/crud.md：getAll 排序示例可执行")
    void crudGetAllOrderByDocExamples() {
        createUserTable();
        dba.insert(new User("u2", "Bob", 20, 1.0));
        dba.insert(new User("u1", "Alice", 40, 3.0));
        dba.insert(new User("u3", "Carl", 30, 2.0));

        io.github.jinghui70.rainbow.dbaccess.crud.CrudService<User> service =
                new io.github.jinghui70.rainbow.dbaccess.crud.CrudService<>(dba, User.class);

        // 文档原文用列名常量 + DbaUtil.desc 拼降序
        assertEquals("Carl", service.getAll(io.github.jinghui70.rainbow.dbaccess.DbaUtil.desc(NAME))
                .get(0).getName());
        assertEquals("Alice", service.getAll(io.github.jinghui70.rainbow.dbaccess.DbaUtil.desc(AGE), NAME)
                .get(0).getName());
        // getAll() 与 getAll(ID) 等价
        assertEquals(service.getAll().stream().map(User::getId).toList(),
                service.getAll(ID).stream().map(User::getId).toList());
    }

    @Test
    @DisplayName("doc/crud.md：queryPage 带排序示例可执行")
    void crudQueryPageOrderByDocExample() {
        createUserTable();
        dba.insert(new User("u2", "Bob", 20, 1.0));
        dba.insert(new User("u1", "Alice", 40, 3.0));

        io.github.jinghui70.rainbow.dbaccess.crud.CrudService<User> service =
                new io.github.jinghui70.rainbow.dbaccess.crud.CrudService<>(dba, User.class);
        io.github.jinghui70.rainbow.dbaccess.crud.QueryDTO dto =
                new io.github.jinghui70.rainbow.dbaccess.crud.QueryDTO();
        dto.setPageNo(1);
        dto.setPageSize(10);

        // 文档原文：service.queryPage(dto, DbaUtil.desc(NAME))
        assertEquals("Bob", service.queryPage(dto, io.github.jinghui70.rainbow.dbaccess.DbaUtil.desc(NAME))
                .getData().get(0).getName());
        // 文档原文：service.queryPage(dto, UserVO.class, DbaUtil.desc(NAME))
        assertEquals("Bob", service.queryPage(dto,
                        io.github.jinghui70.rainbow.dbaccess.model.UserVO.class,
                        io.github.jinghui70.rainbow.dbaccess.DbaUtil.desc(NAME))
                .getData().get(0).getName());
        // 文档原文：service.queryList(dto, DbaUtil.desc(NAME))
        assertEquals("Bob", service.queryList(dto,
                        io.github.jinghui70.rainbow.dbaccess.DbaUtil.desc(NAME))
                .get(0).getName());
    }

    @Test
    @DisplayName("doc/crud.md：getByKeyAs 示例可执行")
    void crudGetByKeyAsDocExample() {
        createUserTable();
        dba.insert(new User("1", "Tom", 20, 90.0));

        io.github.jinghui70.rainbow.dbaccess.crud.CrudService<User> service =
                new io.github.jinghui70.rainbow.dbaccess.crud.CrudService<>(dba, User.class);

        // 文档原文：UserVO one = service.getByKeyAs(UserVO.class, "1")
        io.github.jinghui70.rainbow.dbaccess.model.UserVO one =
                service.getByKeyAs(io.github.jinghui70.rainbow.dbaccess.model.UserVO.class, "1");

        assertNotNull(one);
        assertEquals("Tom", one.getName());
    }

    @Test
    @DisplayName("doc/query.md：Sql.whereByKey / whereByObj 示例可执行")
    void sqlWhereByKeyDocExample() {
        createUserTable();
        dba.insert(new User("1", "Tom", 20, 90.0));

        // 文档原文：dba.select().from(User.class).whereByKey(User.class, "1").queryForObject(User.class)
        assertEquals("Tom", dba.select().from(User.class)
                .whereByKey(User.class, "1").queryForObject(User.class).getName());

        // 文档原文：dba.select().from("T_USER").whereByObj(user).queryForList(User.class)
        List<User> users = dba.select().from("T_USER")
                .whereByObj(new User("1", "ignored", null, null)).queryForList(User.class);
        assertEquals(1, users.size());
        assertEquals("Tom", users.get(0).getName());

        // 文档原文：dba.deleteFrom("T_USER").whereByObj(user).execute()
        assertEquals(1, dba.deleteFrom("T_USER")
                .whereByObj(new User("1", null, null, null)).execute());
        assertNull(dba.selectByKey(User.class, "1"));
    }

    @Test
    @DisplayName("Dba 方法级文档：带返回值的事务示例可执行")
    void dbaTransactionWithResultDocExample() {
        createUserTable();
        User user = new User("1", "Tom", 20, 90.0);

        // 文档原文：
        // int count = dba.transaction(status -> { dba.insert(user); return dba.select().from("T_USER").count(); })
        int count = dba.transaction(status -> {
            dba.insert(user);
            return dba.select().from("T_USER").count();
        });

        assertEquals(1, count);
    }

    /** 插入一条带 STATUS / GENDER 的记录，供文档示例断言使用。 */
    private void insertDocUser(String id, String name, int age, Status status, Gender gender) {
        dba.sql("INSERT INTO T_USER (ID, NAME, AGE, SCORE, STATUS, GENDER) VALUES (?,?,?,?,?,?)")
                .addParam(id).addParam(name).addParam(age).addParam(1.0)
                .addParam(status.name()).addParam(gender.name())
                .execute();
    }
}
