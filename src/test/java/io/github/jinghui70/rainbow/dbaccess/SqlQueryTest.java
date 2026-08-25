package io.github.jinghui70.rainbow.dbaccess;

import io.github.jinghui70.rainbow.dbaccess.model.*;
import io.github.jinghui70.rainbow.dbaccess.sql.PageData;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Sql 查询方法全面测试。
 */
class SqlQueryTest extends BaseTest {

    private void setupUsers() {
        createUserTable();
        dba.insert(new User[]{
                new User("1", "Alice", 25, 90.0),
                new User("2", "Bob", 30, 80.0),
                new User("3", "Charlie", 35, 70.0)
        });
    }

    @Test void testQueryForObject() {
        setupUsers();
        User u = dba.select().from("T_USER").where("ID", "1").queryForObject(User.class);
        assertNotNull(u);
        assertEquals("Alice", u.getName());
    }

    @Test void testQueryForObjectOptional() {
        setupUsers();
        Optional<User> u = dba.select().from("T_USER").where("ID", "1").queryForObjectOptional(User.class);
        assertTrue(u.isPresent());
        Optional<User> none = dba.select().from("T_USER").where("ID", "999").queryForObjectOptional(User.class);
        assertFalse(none.isPresent());
    }

    @Test void testQueryForValue() {
        setupUsers();
        Integer age = dba.select("AGE").from("T_USER").where("ID", "1").queryForValue(Integer.class);
        assertEquals(25, age);
    }

    @Test void testQueryForString() {
        setupUsers();
        String name = dba.select("NAME").from("T_USER").where("ID", "1").queryForString();
        assertEquals("Alice", name);
    }

    @Test void testQueryForStringEmpty() {
        createUserTable();
        assertEquals("", dba.select("NAME").from("T_USER").where("ID", "X").queryForString());
    }

    @Test void testQueryForInt() {
        setupUsers();
        assertEquals(25, dba.select("AGE").from("T_USER").where("ID", "1").queryForInt());
    }

    @Test void testQueryForIntZero() {
        createUserTable();
        assertEquals(0, dba.select("AGE").from("T_USER").where("ID", "X").queryForInt());
    }

    @Test void testQueryForDouble() {
        setupUsers();
        double s = dba.select("SCORE").from("T_USER").where("ID", "1").queryForDouble();
        assertEquals(90.0, s, 0.01);
    }

    @Test void testQueryForList() {
        setupUsers();
        List<User> list = dba.select().from("T_USER").queryForList(User.class);
        assertEquals(3, list.size());
    }

    @Test void testQueryForListSimpleType() {
        setupUsers();
        List<String> names = dba.select("NAME").from("T_USER").queryForList(String.class);
        assertTrue(names.contains("Alice"));
    }

    @Test void testQueryForMap() {
        setupUsers();
        Map<String, Object> map = dba.select().from("T_USER").where("ID", "1").queryForMap();
        assertEquals("Alice", map.get("NAME"));
    }

    @Test void testQueryForMapEmpty() {
        createUserTable();
        Map<String, Object> map = dba.select().from("T_USER").where("ID", "X").queryForMap();
        assertTrue(map.isEmpty());
    }

    @Test void testCount() {
        setupUsers();
        int c = dba.select().from("T_USER").count();
        assertEquals(3, c);
    }

    @Test void testCountZero() {
        createUserTable();
        assertEquals(0, dba.select().from("T_USER").count());
    }

    @Test void testCountWithGroupBy() {
        setupUsers();
        // GROUP BY 时禁用优化，走子查询路径
        int c = dba.select("NAME, COUNT(*) CNT").from("T_USER").groupBy("NAME").count();
        assertEquals(3, c);
    }

    @Test void testDisableCountOptimization() {
        setupUsers();
        int c = dba.select().from("T_USER").disableCountOptimization().count();
        assertEquals(3, c);
    }

    @Test void testExist() {
        setupUsers();
        assertTrue(dba.select().from("T_USER").where("ID", "1").exist());
        assertFalse(dba.select().from("T_USER").where("ID", "X").exist());
    }

    @Test void testLimit() {
        setupUsers();
        List<User> list = dba.select().from("T_USER").limit(2).queryForList(User.class);
        assertEquals(2, list.size());
    }

    @Test void testRange() {
        setupUsers();
        List<User> list = dba.select().from("T_USER").range(1, 2).queryForList(User.class);
        assertEquals(2, list.size());
    }

    @Test void testQueryPage() {
        setupUsers();
        PageData<User> page = dba.select().from("T_USER").orderBy("ID").queryPage(User.class, 1, 2);
        assertEquals(3, page.getTotal());
        assertEquals(2, page.getData().size());
    }

    @Test void testQueryPageEmpty() {
        createUserTable();
        PageData<User> page = dba.select().from("T_USER").queryPage(User.class, 1, 10);
        assertEquals(0, page.getTotal());
        assertTrue(page.getData().isEmpty());
    }

    @Test void testQueryToMap() {
        setupUsers();
        Map<String, String> map = dba.select().from("T_USER")
                .queryToMap(rs -> rs.getString("ID"), rs -> rs.getString("NAME"));
        assertEquals(3, map.size());
        assertEquals("Alice", map.get("1"));
    }

    @Test void testQueryToMapWithClass() {
        setupUsers();
        Map<String, User> map = dba.select().from("T_USER").queryToMap(rs -> rs.getString("ID"), User.class);
        assertEquals(3, map.size());
        assertNotNull(map.get("1"));
    }

    @Test void testQueryToMapDuplicateKeyOverwrittenByLaterRow() {
        createUserTable();
        dba.insert(new User[]{
                new User("1", "A", 25, 90.0),
                new User("2", "B", 25, 80.0) // 年龄与 user1 相同
        });
        Map<Integer, User> map = dba.select().from("T_USER").orderBy("ID")
                .queryToMap(rs -> rs.getInt("AGE"), User.class);
        assertEquals(1, map.size());
        assertEquals("B", map.get(25).getName()); // 重复 key：后行覆盖前行（Map.put 语义）
    }

    @Test void testColumnAliasCaseInsensitive() {
        setupUsers();
        // 列名匹配按去空格转小写比对，别名大小写不同仍能映射
        User u = dba.select("NAME AS Name").from("T_USER").where("ID", "1").queryForObject(User.class);
        assertNotNull(u);
        assertEquals("Alice", u.getName());
    }

    @Test void testQueryToGroup() {
        setupUsers();
        Map<Integer, List<User>> groups = dba.select().from("T_USER")
                .queryToGroup(rs -> rs.getInt("AGE"), User.class);
        assertEquals(3, groups.size()); // 每人年龄不同
    }

    @Test void testExecuteWithoutParams() {
        createUserTable();
        int updated = dba.sql("UPDATE T_USER SET NAME='X'").execute();
        assertEquals(0, updated);
    }
}
