package io.github.jinghui70.rainbow.dbaccess;

import cn.hutool.core.collection.ListUtil;
import io.github.jinghui70.rainbow.dbaccess.memory.Field;
import io.github.jinghui70.rainbow.dbaccess.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.BadSqlGrammarException;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Dba CRUD 操作全面测试 — 覆盖 insert/update/delete/selectByKey/transaction 等。
 */
class DbaCrudTest extends BaseTest {

    // ===== insert 单条 Bean =====

    @Test
    void testInsertSingleBean() {
        createUserTable();
        dba.insert(new User("1", "Alice", 25, 100.0));
        User u = dba.selectByKey(User.class, "1");
        assertNotNull(u);
        assertEquals("Alice", u.getName());
        assertEquals(25, u.getAge());
        assertEquals(100.0, u.getScore());
    }

    // ===== insert Bean 数组 =====

    @Test
    void testInsertBeanArray() {
        createUserTable();
        User[] users = {new User("1", "A", 20, 80.0), new User("2", "B", 30, 90.0)};
        dba.insert(users);
        assertEquals(2, dba.select().from("T_USER").count());
    }

    // ===== insert Bean 集合 =====

    @Test
    void testInsertBeanCollection() {
        createUserTable();
        List<User> users = Arrays.asList(new User("1", "X", 22, 70.0), new User("2", "Y", 28, 75.0));
        dba.insert(users);
        assertEquals(2, dba.select().from("T_USER").count());
    }

    // ===== insert Map（必须指定表名） =====

    @Test
    void testInsertSingleMap() {
        createUserTable();
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("ID", "m1");
        map.put("NAME", "MapUser");
        map.put("AGE", 30);
        map.put("SCORE", 88.5);
        dba.insertOf(map).into("T_USER").execute();
        assertEquals(1, dba.select().from("T_USER").count());
    }

    // ===== insert Map 批量 =====

    @Test
    void testInsertMapBatch() {
        createUserTable();
        Map<String, Object> m1 = new LinkedHashMap<>();
        m1.put("ID", "k1");
        m1.put("NAME", "K1");
        List<Map<String, Object>> maps = ListUtil.of(m1);
        dba.insertOf(maps).into("T_USER").execute();
        assertEquals(1, dba.select().from("T_USER").count());
    }

    // ===== insert merge =====

    @Test
    void testInsertMerge() {
        createUserTable();
        User u = new User("1", "Alice", 25, 100.0);
        dba.insert(u);
        User u2 = new User("1", "AliceNew", 30, 200.0);
        dba.merge(u2);
        User result = dba.selectByKey(User.class, "1");
        assertEquals("AliceNew", result.getName());
        assertEquals(30, result.getAge());
    }

    // ===== insert 空集合不报错 =====

    @Test
    void testInsertEmptyCollection() {
        createUserTable();
        dba.insert(Collections.emptyList());
        assertEquals(0, dba.select().from("T_USER").count());
    }

    // ===== insert 到自定义表名 =====

    @Test
    void testInsertIntoCustomTable() {
        createUserTable();
        dba.insertOf(new User("1", "Z", 40, 60.0)).into("T_USER").execute();
        assertNotNull(dba.selectByKey(User.class, "1"));
    }

    // ===== selectByKey =====

    @Test
    void testSelectByKeyExists() {
        createUserTable();
        dba.insert(new User("u1", "Bob", 22, 99.0));
        assertNotNull(dba.selectByKey(User.class, "u1"));
    }

    @Test
    void testSelectByKeyNotExists() {
        createUserTable();
        assertNull(dba.selectByKey(User.class, "notExist"));
    }

    // ===== update Bean 模式（setBean） =====

    @Test
    void testUpdateBean() {
        createUserTable();
        dba.insert(new User("u1", "Old", 10, 50.0));
        dba.update(new User("u1", "New", 20, 100.0));
        User u = dba.selectByKey(User.class, "u1");
        assertEquals("New", u.getName());
        assertEquals(20, u.getAge());
        assertEquals(100.0, u.getScore());
    }

    // ===== update Bean 带字段过滤 =====

    @Test
    void testUpdateBeanInclude() {
        createUserTable();
        dba.insert(new User("u1", "Old", 10, 50.0));
        User partial = new User("u1", "NameOnly", 99, 999.0);
        dba.updateOf(partial).include("name").execute();
        User u = dba.selectByKey(User.class, "u1");
        assertEquals("NameOnly", u.getName());
        assertEquals(10, u.getAge());   // 未更新
        assertEquals(50.0, u.getScore()); // 未更新
    }

    @Test
    void testUpdateBeanExclude() {
        createUserTable();
        dba.insert(new User("u1", "Old", 10, 50.0));
        User partial = new User("u1", "NameOnly", 99, 999.0);
        dba.updateOf(partial).exclude("name").execute();
        User u = dba.selectByKey(User.class, "u1");
        assertEquals("Old", u.getName()); // 被排除
        assertEquals(99, u.getAge());
        assertEquals(999.0, u.getScore());
    }

    @Test
    void testUpdateBeanExcludeNull() {
        createUserTable();
        dba.insert(new User("u1", "Old", 10, 50.0));
        // 只更新 name，age/score 为 null 被排除
        User partial = new User("u1", "NewName", null, null);
        dba.updateOf(partial).excludeNull().execute();
        User u = dba.selectByKey(User.class, "u1");
        assertEquals("NewName", u.getName());
        assertEquals(10, u.getAge());
    }

    // ===== update 到同结构的另一张表 =====

    @Test
    void testUpdateIntoCustomTable() {
        createUserTable();
        // 同结构的副本表
        dba.createTable("T_USER_COPY",
                Field.createKeyString("ID"),
                Field.createString("NAME"),
                Field.createInt("AGE"),
                Field.createDouble("SCORE"));
        // 两张表都插入同主键初始行
        dba.insert(new User("u1", "Old", 10, 50.0));
        dba.insertOf(new User("u1", "Old", 10, 50.0)).into("T_USER_COPY").execute();
        // 用 User bean 更新到副本表，而非 Bean 默认的 T_USER
        int rows = dba.updateOf(new User("u1", "New", 20, 100.0)).into("T_USER_COPY").execute();
        assertEquals(1, rows);
        // 副本表被更新
        User copy = dba.select().from("T_USER_COPY").where("ID", "u1").queryForObject(User.class);
        assertEquals("New", copy.getName());
        assertEquals(20, copy.getAge());
        assertEquals(100.0, copy.getScore());
        // 原表不受影响
        User origin = dba.selectByKey(User.class, "u1");
        assertEquals("Old", origin.getName());
        assertEquals(10, origin.getAge());
    }

    @Test
    void testUpdateSqlMode() {
        createUserTable();
        dba.insert(new User("u1", "Old", 10, 50.0));
        dba.update("T_USER").set("NAME", "SqlMode").set("AGE", 99).where("ID", "u1").execute();
        User u = dba.selectByKey(User.class, "u1");
        assertEquals("SqlMode", u.getName());
        assertEquals(99, u.getAge());
    }

    // ===== update 条件 set =====

    @Test
    void testUpdateConditionalSetTrue() {
        createUserTable();
        dba.insert(new User("u1", "Old", 10, 50.0));
        dba.update("T_USER").set(true, "NAME", "NewName").where("ID", "u1").execute();
        assertEquals("NewName", dba.selectByKey(User.class, "u1").getName());
    }

    @Test
    void testUpdateConditionalSetFalse() {
        createUserTable();
        dba.insert(new User("u1", "Old", 10, 50.0));
        // 全部条件为 false → 没有任何 SET 字段 → 抛异常
        assertThrows(BadSqlGrammarException.class, () ->
                dba.update("T_USER").
                        set(false, "NAME", "ShouldNot")
                        .where("ID", "u1")
                        .execute());
    }

    // ===== delete 对象 =====

    @Test
    void testDeleteObject() {
        createUserTable();
        dba.insert(new User("u1", "X", 1, 1.0));
        assertEquals(1, dba.select().from("T_USER").count());
        dba.delete(new User("u1", null, null, null));
        assertEquals(0, dba.select().from("T_USER").count());
    }

    // ===== deleteByKey =====

    @Test
    void testDeleteByKey() {
        createUserTable();
        dba.insert(new User("u1", "X", 1, 1.0));
        dba.deleteByKey(User.class, "u1");
        assertNull(dba.selectByKey(User.class, "u1"));
    }

    // ===== delete List =====

    @Test
    void testDeleteList() {
        createUserTable();
        List<User> list = List.of(
                new User("1", "A", 1, 1.0),
                new User("2", "B", 2, 2.0)
        );
        dba.insert(list);
        assertEquals(2, dba.select().from("T_USER").count());
        int result = dba.delete(list);
        assertEquals(2, result);
        assertEquals(0, dba.select().from("T_USER").count());
    }

    @Test
    void testDeleteEmptyList() {
        assertEquals(0, dba.delete(Collections.emptyList()));
    }

    // ===== deleteFrom + where =====

    @Test
    void testDeleteFromWhere() {
        createUserTable();
        dba.insert(new User("1", "A", 1, 1.0));
        dba.deleteFrom("T_USER").where("ID", "1").execute();
        assertEquals(0, dba.select().from("T_USER").count());
    }

    @Test
    void testDeleteFromClass() {
        createUserTable();
        dba.insert(new User("1", "A", 1, 1.0));
        dba.deleteFrom(User.class).where("ID", "1").execute();
        assertEquals(0, dba.select().from("T_USER").count());
    }

    // ===== autoIncrement =====

    @Test
    void testAutoIncrement() {
        createAutoTable();
        dba.insert(new AutoEntity("Auto1", 90.0));
        dba.insert(new AutoEntity("Auto2", 80.0));
        List<AutoEntity> list = dba.select().from("T_AUTO").queryForList(AutoEntity.class);
        assertEquals(2, list.size());
        assertNotNull(list.get(0).getId());
        assertNotNull(list.get(1).getId());
    }

    // ===== 复合主键 =====

    @Test
    void testComplexKeyCRUD() {
        createComplexKeyTable();
        // 手动插入绕过列名 "VALUE" 与 PropInfo 的映射问题
        dba.sql("INSERT INTO T_COMPLEX_KEY(KEY_A, KEY_B, \"VALUE\") VALUES(?,?,?)")
                .addParam("A").addParam("B").addParam("val1").execute();
        ComplexKeyEntity result = dba.selectByKey(ComplexKeyEntity.class, "A", "B");
        assertNotNull(result);
        assertEquals("val1", result.getValue());
        dba.deleteByKey(ComplexKeyEntity.class, "A", "B");
        assertNull(dba.selectByKey(ComplexKeyEntity.class, "A", "B"));
    }

    // ===== transaction =====

    @Test
    void testTransactionRunnable() {
        createUserTable();
        dba.transaction(() -> {
            dba.insert(new User("t1", "Tx1", 1, 1.0));
            dba.insert(new User("t2", "Tx2", 2, 2.0));
        });
        assertEquals(2, dba.select().from("T_USER").count());
    }

    @Test
    void testTransactionCallback() {
        createUserTable();
        dba.insert(new User("t1", "Tx1", 1, 1.0));
        int count = dba.transaction(status -> dba.select().from("T_USER").count());
        assertEquals(1, count);
    }

    // ===== exist 表 =====

    @Test
    void testExistTableTrue() {
        createUserTable();
        assertTrue(dba.exist("T_USER"));
    }

    @Test
    void testExistTableFalse() {
        assertFalse(dba.exist("NO_SUCH_TABLE"));
    }

    // ===== dropTable =====

    @Test
    void testDropTable() {
        createUserTable();
        assertTrue(dba.exist("T_USER"));
        dba.dropTable("T_USER");
        assertFalse(dba.exist("T_USER"));
    }

    // ===== 枚举字段映射 =====

    @Test
    void testEnumMapping() {
        createEnumTable();
        EnumEntity e = new EnumEntity("e1", Status.ACTIVE, Color.RED);
        dba.insert(e);
        EnumEntity result = dba.selectByKey(EnumEntity.class, "e1");
        assertEquals(Status.ACTIVE, result.getStatus());
        assertEquals(Color.RED, result.getColor());
    }

    // ===== Boolean 映射 =====

    @Test
    void testBoolMapping() {
        createBoolTable();
        BoolEntity b = new BoolEntity("b1", true, false);
        dba.insert(b);
        BoolEntity result = dba.selectByKey(BoolEntity.class, "b1");
        assertTrue(result.getActive());
        assertFalse(result.getFlag());
    }
}
