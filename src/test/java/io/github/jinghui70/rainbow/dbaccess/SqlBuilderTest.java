package io.github.jinghui70.rainbow.dbaccess;

import io.github.jinghui70.rainbow.dbaccess.cnd.Cnd;
import io.github.jinghui70.rainbow.dbaccess.cnd.Op;
import io.github.jinghui70.rainbow.dbaccess.model.User;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Sql WHERE/AND/OR/ORDER BY/GROUP BY 构建测试。
 */
class SqlBuilderTest extends BaseTest {

    private void setupUsers() {
        createUserTable();
        dba.insert(new User[]{
                new User("1", "Alice", 25, 90.0),
                new User("2", "Bob", 30, 80.0)
        });
    }

    @Test void testWhereEq() {
        setupUsers();
        User u = dba.select().from("T_USER").where("ID", "1").queryForObject(User.class);
        assertNotNull(u);
    }

    @Test void testWhereOp() {
        setupUsers();
        List<User> list = dba.select().from("T_USER")
                .where("AGE", Op.GT, 20).and("AGE", Op.LT, 40).queryForList(User.class);
        assertEquals(2, list.size());
    }

    @Test void testWhereCnd() {
        setupUsers();
        List<User> list = dba.select().from("T_USER")
                .where("NAME", "Alice").queryForList(User.class);
        assertEquals(1, list.size());
    }

    @Test void testWhereCondition() {
        setupUsers();
        User u = dba.select().from("T_USER").where(true, "ID", "1").queryForObject(User.class);
        assertNotNull(u);
        List<User> all = dba.select().from("T_USER").where(false, "ID", "X").queryForList(User.class);
        assertEquals(2, all.size()); // where 条件跳过后返回全部
    }

    @Test void testWhereCndList() {
        setupUsers();
        // where(List<Cnd>) 使用 Cnd.and 包装以生成 AND 连接
        Cnd compound = Cnd.and(Cnd.where("NAME", "Alice"), Cnd.where("AGE", Op.GT, 20));
        List<User> list = dba.select().from("T_USER").where(compound).queryForList(User.class);
        assertEquals(1, list.size());
    }

    @Test void testAndOr() {
        setupUsers();
        User u = dba.select().from("T_USER")
                .where("ID", "1").and("NAME", "Alice").queryForObject(User.class);
        assertNotNull(u);
        List<User> list = dba.select().from("T_USER")
                .where("ID", "1").or("ID", "2").queryForList(User.class);
        assertEquals(2, list.size());
    }

    @Test void testAndOrCondition() {
        setupUsers();
        User u = dba.select().from("T_USER").where("ID", "1").and(true, "NAME", "Alice").queryForObject(User.class);
        assertNotNull(u);
        User none = dba.select().from("T_USER").where("ID", "1").and(false, "NAME", "XXX").queryForObject(User.class);
        assertNotNull(none); // false 跳过，仍然返回 ID=1
    }

    @Test void testAndOrSupplier() {
        setupUsers();
        User u = dba.select().from("T_USER").where("ID", "1").and(true, () -> Cnd.where("NAME", "Alice")).queryForObject(User.class);
        assertNotNull(u);
        User none = dba.select().from("T_USER").where("ID", "1").and(false, () -> Cnd.where("NAME", "XXX")).queryForObject(User.class);
        assertNotNull(none); // false 跳过
    }

    @Test void testWhereNullCnd() {
        setupUsers();
        List<User> list = dba.select().from("T_USER").where((Cnd) null).queryForList(User.class);
        assertEquals(2, list.size());
    }

    @Test void testOrNullCnd() {
        setupUsers();
        List<User> list = dba.select().from("T_USER").or((Cnd) null).queryForList(User.class);
        assertEquals(2, list.size());
    }

    @Test void testOrderBy() {
        setupUsers();
        List<User> list = dba.select().from("T_USER").orderBy("NAME").queryForList(User.class);
        assertEquals("Alice", list.get(0).getName());
    }

    @Test void testOrderByList() {
        setupUsers();
        List<OrderBy> orders = List.of(new OrderBy("AGE", true));
        List<User> list = dba.select().from("T_USER").orderBy(orders).queryForList(User.class);
        assertEquals(2, list.size());
    }

    @Test void testGroupBy() {
        setupUsers();
        List<User> list = dba.select("NAME").from("T_USER").groupBy("NAME").queryForList(User.class);
        assertEquals(2, list.size());
    }

    @Test void testFromString() {
        setupUsers();
        assertEquals(2, dba.select().from("T_USER").count());
    }

    @Test void testFromClass() {
        setupUsers();
        assertEquals(2, dba.select().from(User.class).count());
    }

    @Test void testSelectStatic() {
        setupUsers();
        Sql s1 = Sql.select();
        assertTrue(s1.toString().contains("SELECT *"));
        Sql s2 = Sql.select("ID", "NAME");
        assertTrue(s2.toString().contains("ID"));
    }

    @Test void testAppendSql() {
        Sql s1 = new Sql("SELECT * FROM T_USER").addParam(1);
        Sql s2 = new Sql(" WHERE ID=?").addParam(2);
        s1.append(s2);
        assertEquals(2, s1.getParams().size());
    }

    @Test void testParams() {
        setupUsers();
        Sql s = dba.sql("SELECT * FROM T_USER WHERE ID=?");
        s.addParam("1");
        s.addParam("2"); // extra
        s.setParam("1");
        assertEquals(1, s.getParams().size());
    }

    @Test void testBatchUpdate() {
        createUserTable();
        dba.sql("INSERT INTO T_USER(ID,NAME,AGE,SCORE) VALUES(?,?,?,?)")
                .batchUpdate(List.of(
                        new Object[]{"a", "A", 20, 80.0},
                        new Object[]{"b", "B", 30, 70.0}
                ));
        assertEquals(2, dba.select().from("T_USER").count());
    }

    @Test void testBatchUpdateWithSize() {
        createUserTable();
        List<Object[]> batch = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            batch.add(new Object[]{"b" + i, "N", i, (double) i});
        }
        dba.sql("INSERT INTO T_USER(ID,NAME,AGE,SCORE) VALUES(?,?,?,?)")
                .batchUpdate(batch, 2);
        assertEquals(5, dba.select().from("T_USER").count());
    }
}
