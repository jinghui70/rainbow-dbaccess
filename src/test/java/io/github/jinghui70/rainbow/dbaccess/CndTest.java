package io.github.jinghui70.rainbow.dbaccess;

import cn.hutool.core.collection.CollUtil;
import io.github.jinghui70.rainbow.dbaccess.cnd.Cnd;
import io.github.jinghui70.rainbow.dbaccess.cnd.Op;
import io.github.jinghui70.rainbow.dbaccess.model.User;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Cnd 条件对象全面测试。
 */
class CndTest extends BaseTest {

    private void setupUsers() {
        createUserTable();
        dba.insert(new User[]{
                new User("1", "Alice", 25, 90.0),
                new User("2", "Bob", 30, 80.0),
                new User("3", null, null, null)
        });
    }

    @Test
    void testEqNull() {
        setupUsers();
        User u = dba.select().from("T_USER")
                .where("NAME", null)
                .queryForObject(User.class);
        assertNotNull(u); // NAME IS NULL 应该找到 user 3
        assertNull(u.getName());
    }

    @Test
    void testNeNull() {
        setupUsers();
        List<User> list = dba.select().from("T_USER")
                .where("NAME", Op.NE, null)
                .queryForList(User.class);
        assertEquals(2, list.size());
    }

    @Test
    void testEqIn() {
        setupUsers();
        List<User> list = dba.select().from("T_USER")
                .where("ID", Arrays.asList("1", "2"))
                .queryForList(User.class);
        assertEquals(2, list.size());
    }

    @Test
    void testNeNotIn() {
        setupUsers();
        List<User> list = dba.select().from("T_USER")
                .where("ID", Op.NE, Arrays.asList("0", "1"))
                .queryForList(User.class);
        assertEquals(2, list.size());
    }

    @Test
    void testLikeVariants() {
        setupUsers();
        User u = dba.select().from("T_USER")
                .where("NAME", Op.LIKE, "lice")
                .queryForObject(User.class);
        assertNotNull(u);
        assertEquals("Alice", u.getName());
    }

    @Test
    void testLikeLeft() {
        setupUsers();
        User u = dba.select().from("T_USER")
                .where("NAME", Op.LIKE_LEFT, "Ali").queryForObject(User.class);
        assertNotNull(u);
    }

    @Test
    void testNotLike() {
        setupUsers();
        List<User> list = dba.select().from("T_USER")
                .where("NAME", Op.NOT_LIKE, "Alice")
                .queryForList(User.class);
        // NULL 不匹配 LIKE，也不会被 NOT_LIKE 选中
        assertTrue(CollUtil.isNotEmpty(list));
    }

    @Test
    void testComparison() {
        setupUsers();
        List<User> list = dba.select().from("T_USER")
                .where("AGE", Op.GT, 20)
                .and("AGE", Op.LE, 30)
                .queryForList(User.class);
        assertEquals(2, list.size());
    }

    @Test
    void testIn() {
        setupUsers();
        List<User> list = dba.select().from("T_USER")
                .where("ID", Op.IN, new String[]{"1", "3"}).queryForList(User.class);
        assertEquals(2, list.size());
        // 不写默认 IN
        list = dba.select().from("T_USER")
                .where("ID", new String[]{"1", "3"}).queryForList(User.class);
        assertEquals(2, list.size());
    }

    @Test
    void testNotIn() {
        setupUsers();
        List<User> list = dba.select().from("T_USER")
                .where("ID", Op.NOT_IN, Arrays.asList("1", "2")).queryForList(User.class);
        assertEquals(1, list.size());
    }

    @Test
    void testIsNull() {
        setupUsers();
        User u = dba.select().from("T_USER").where(Cnd.isNull("NAME")).queryForObject(User.class);
        assertNotNull(u);
        assertNull(u.getName());
    }

    @Test
    void testIsNotNull() {
        setupUsers();
        List<User> list = dba.select().from("T_USER").where(Cnd.isNotNull("NAME")).queryForList(User.class);
        assertEquals(2, list.size());
    }

    @Test
    void testInWithNull() {
        setupUsers();
        Sql sql = dba.select().from("T_USER")
                .where("NAME", Op.IN, Arrays.asList("Alice", null));
        assertEquals("SELECT * FROM T_USER WHERE (NAME=? OR NAME IS NULL)", sql.getSql());
        List<User> list = sql.queryForList(User.class);
        // IN 中的 null 额外生成 IS NULL 条件
        assertTrue(CollUtil.isNotEmpty(list));
    }

    @Test
    void testInWithEmpty() {
        setupUsers();
        Object[] array = new Object[0];
        assertThrows(IllegalArgumentException.class,
                () -> dba.select().from("T_USER")
                        .where("NAME", Op.IN, array));
    }

    @Test
    void testCndAnd() {
        setupUsers();
        Cnd compound = Cnd.and(Cnd.where("AGE", Op.GT, 20), Cnd.where("AGE", Op.LT, 32));
        List<User> list = dba.select().from(User.class).where(compound).queryForList(User.class);
        assertEquals(2, list.size());
    }

    @Test
    void testCndOr() {
        setupUsers();
        Cnd compound = Cnd.or(Cnd.where("ID", "1"), Cnd.where("ID", "2"));
        List<User> list = dba.select().from("T_USER").where(compound).queryForList(User.class);
        assertEquals(2, list.size());
    }

    @Test
    void testAndSingle() {
        // 单个条件简化为自身
        Cnd single = Cnd.and(Cnd.where("ID", "1"));
        assertNotNull(single);
        assertEquals("ID", single.getField());
    }

    @Test
    void testAndEmpty() {
        assertNull(Cnd.and());
        assertNull(Cnd.or());
    }

    @Test
    void testIsNullStatic() {
        Cnd c = Cnd.isNull("NAME");
        assertEquals(Op.IS_NULL, c.getOp());
    }

    @Test
    void testWhereConditionStatic() {
        Cnd c = Cnd.where(true, "NAME", "X");
        assertNotNull(c);
        assertNull(Cnd.where(false, "NAME", "X"));
    }

    @Test
    void testRangeCondition() {
        setupUsers();
        Range<Integer> range = Range.of(20, 35);
        List<User> list = dba.select().from("T_USER").where("AGE", range).queryForList(User.class);
        assertEquals(2, list.size()); // 25 and 30 are in (20,35)
    }

    @Test
    void testRangeSingleValue() {
        setupUsers();
        Range<Integer> range = Range.of(25, 25);
        User u = dba.select().from("T_USER").where("AGE", range).queryForObject(User.class);
        assertNotNull(u);
        assertEquals(25, u.getAge());
    }

    @Test
    void testCndToString() {
        Cnd c = Cnd.where("NAME", "X");
        assertTrue(c.toString().contains("NAME"));
    }

    @Test
    void testOpStr() {
        assertEquals("=", Op.EQ.str());
        assertEquals(">", Op.GT.str());
        assertEquals(" LIKE ", Op.LIKE.str());
        assertEquals(" IS NULL", Op.IS_NULL.str());
    }
}
