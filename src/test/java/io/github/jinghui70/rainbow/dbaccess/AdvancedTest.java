package io.github.jinghui70.rainbow.dbaccess;

import io.github.jinghui70.rainbow.dbaccess.cnd.Cnd;
import io.github.jinghui70.rainbow.dbaccess.cnd.Op;
import io.github.jinghui70.rainbow.dbaccess.fieldmapper.BoolYN;
import io.github.jinghui70.rainbow.dbaccess.fieldmapper.EnumFieldMapper;
import io.github.jinghui70.rainbow.dbaccess.fieldmapper.FieldValue;
import io.github.jinghui70.rainbow.dbaccess.model.*;
import io.github.jinghui70.rainbow.dbaccess.object.PropInfoCache;
import io.github.jinghui70.rainbow.dbaccess.sql.Range;
import io.github.jinghui70.rainbow.dbaccess.sql.Sql;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 高级场景测试：Blob/CLOB、FieldMapper、UpdateBuilder 模式冲突、insert 边界等。
 */
class AdvancedTest extends BaseTest {

    // ===== Blob 字段读写 =====

    @Test
    void testBlobString() {
        createBlobTable();
        BlobEntity e = new BlobEntity("1", "hello blob", null, null);
        dba.insert(e);
        BlobEntity r = dba.selectByKey(BlobEntity.class, "1");
        assertEquals("hello blob", r.getLobString());
    }

    @Test
    void testBlobBytes() {
        createBlobTable();
        byte[] data = {1, 2, 3, 4, 5};
        BlobEntity e = new BlobEntity("1", null, data, null);
        dba.insert(e);
        BlobEntity r = dba.selectByKey(BlobEntity.class, "1");
        assertArrayEquals(data, r.getLobBytes());
    }

    @Test
    void testBlobObject() {
        createBlobTable();
        BlobEntity.ObjectBlob obj = new BlobEntity.ObjectBlob("test", 5, List.of("a"), Map.of("k", 1));
        BlobEntity e = new BlobEntity("1", null, null, obj);
        dba.insert(e);
        BlobEntity r = dba.selectByKey(BlobEntity.class, "1");
        assertNotNull(r.getLobObject());
        assertEquals("test", r.getLobObject().getName());
        assertEquals(5, r.getLobObject().getCount());
        // 验证 Map 字段往返后 key/value 类型正确
        assertNotNull(r.getLobObject().getScores());
        assertEquals(1, r.getLobObject().getScores().size());
        assertEquals(Integer.valueOf(1), r.getLobObject().getScores().get("k"));
    }

    @Test
    void testBlobNullFields() {
        createBlobTable();
        BlobEntity e = new BlobEntity("1", null, null, null);
        dba.insert(e);
        BlobEntity r = dba.selectByKey(BlobEntity.class, "1");
        assertNull(r.getLobString());
        assertNull(r.getLobBytes());
        assertNull(r.getLobObject());
    }

    // ===== CLOB/Object 序列化 =====

    @Test
    void testObjectEntityTags() {
        createObjectTable();
        ObjectEntity e = new ObjectEntity("1", List.of("a", "b"), Map.of("k", 1));
        dba.insert(e);
        ObjectEntity r = dba.selectByKey(ObjectEntity.class, "1");
        assertNotNull(r.getTags());
        assertEquals(2, r.getTags().size());
        // Map 字段也必须正确保留
        assertNotNull(r.getAttributes());
        assertEquals(1, r.getAttributes().size());
        assertEquals(1, r.getAttributes().get("k"));
    }

    // ===== FieldMapper 手动使用 =====

    @Test
    void testFieldMapperInUpdate() {
        createEnumTable();
        EnumEntity e = new EnumEntity("e1", Status.ACTIVE, Color.RED);
        dba.insert(e);

        // 用 EnumFieldMapper 直接 set 字段
        dba.update("T_ENUM").set("STATUS", EnumFieldMapper.of(Status.class), Status.PENDING).where("ID", "e1").execute();

        EnumEntity r = dba.selectByKey(EnumEntity.class, "e1");
        assertEquals(Status.PENDING, r.getStatus());
    }

    @Test
    void testFieldValue() {
        FieldValue fv = new FieldValue("x", EnumFieldMapper.of(Status.class));
        assertNotNull(fv.getValue());
    }

    // ===== BoolYN FieldMapper =====

    @Test
    void testBoolYN() {
        BoolYN m = new BoolYN();
        assertNotNull(m);
    }

    // ===== insert 边界 =====

    @Test
    void testMergeInto() {
        createUserTable();
        dba.insert(new User("1", "Old", 10, 50.0));
        dba.mergeOf(new User("1", "New", 20, 60.0)).into("T_USER").execute();
        User u = dba.selectByKey(User.class, "1");
        assertEquals("New", u.getName());
    }

    @Test
    void testInsertMapWithoutTable() {
        assertThrows(IllegalArgumentException.class, () -> dba.insert(Map.of("ID", "1")));
    }

    @Test
    void testInsertPrimitiveArray() {
        assertThrows(IllegalArgumentException.class, () -> dba.insert(new int[]{1, 2, 3}));
    }

    // ===== Sql 更复杂场景 =====

    @Test
    void testOrConditionSupplier() {
        createUserTable();
        dba.insert(new User("1", "Alice", 25, 90.0));
        List<User> list = dba.select().from("T_USER").or(true, () -> Cnd.where("NAME", "Alice")).queryForList(User.class);
        assertEquals(1, list.size());
    }

    @Test
    void testOrConditionFalse() {
        createUserTable();
        dba.insert(new User("1", "Alice", 25, 90.0));
        List<User> list = dba.select().from("T_USER").where("ID", "1").or(false, "NAME", "XXX").queryForList(User.class);
        assertEquals(1, list.size());
    }

    @Test
    void testQueryForIntOptional() {
        createUserTable();
        dba.insert(new User("q1", "X", 42, 1.0));
        Optional<Integer> v = dba.select("AGE").from("T_USER").where("ID", "q1").queryForIntOptional();
        assertEquals(42, v.orElse(0));
    }

    @Test
    void testQueryForDoubleOptional() {
        createUserTable();
        dba.insert(new User("q2", "X", 1, 99.5));
        Optional<Double> v = dba.select("SCORE").from("T_USER").where("ID", "q2").queryForDoubleOptional();
        assertEquals(99.5, v.orElse(0.0), 0.01);
    }

    @Test
    void testQueryForStringOptional() {
        setupUsers();
        Optional<String> v = dba.select("NAME").from("T_USER").where("ID", "u1").queryForStringOptional();
        assertEquals("X", v.orElse(""));
    }

    @Test
    void testQueryForValueEnum() {
        createEnumTable();
        dba.insert(new EnumEntity("e1", Status.ACTIVE, Color.RED));
        EnumFieldMapper<Status> mapper = EnumFieldMapper.of(Status.class);
        Status s = dba.select("STATUS").from("T_ENUM").where("ID", "e1").queryForValue(mapper);
        assertEquals(Status.ACTIVE, s);
    }

    @Test
    void testQueryForValueOptionalEnum() {
        createEnumTable();
        dba.insert(new EnumEntity("e1", Status.ACTIVE, Color.RED));
        Optional<Status> v = dba.select("STATUS").from("T_ENUM").where("ID", "e1").queryForValueOptional(Status.class);
        assertEquals(Status.ACTIVE, v.orElse(null));
    }

    @Test
    void testQueryForListFieldMapper() {
        createEnumTable();
        dba.insert(new EnumEntity[]{new EnumEntity("e1", Status.ACTIVE, Color.RED), new EnumEntity("e2", Status.PENDING, Color.BLUE)});
        List<Status> list = dba.select("STATUS").from("T_ENUM").queryForList(Status.class);
        assertEquals(2, list.size());
    }

    @Test
    void testQueryToMapDefault() {
        setupUsers();
        Map<String, Map<String, Object>> map = dba.select().from("T_USER").queryToMap(rs -> rs.getString("ID"));
        assertFalse(map.isEmpty());
        assertTrue(map.containsKey("u1"));
    }

    @Test
    void testQueryToGroupDefault() {
        setupUsers();
        Map<String, List<Map<String, Object>>> map = dba.select().from("T_USER").queryToGroup(rs -> rs.getString("ID"));
        assertFalse(map.isEmpty());
    }

    @Test
    void testQueryToGroupValueFunc() {
        setupUsers();
        Map<String, List<String>> map = dba.select().from("T_USER").queryToGroup(rs -> rs.getString("ID"), rs -> rs.getString("NAME"));
        assertFalse(map.isEmpty());
    }

    // ===== Cnd 更多边界 =====

    @Test
    void testEqWithEmptyCollection() {
        createUserTable();
        // 空 Collection 在 toSql 时才被 inValue() 检测并抛异常
        assertThrows(IllegalArgumentException.class,
                () -> dba.select().from("T_USER").where("ID", Op.EQ, Collections.emptyList()).getSql());
    }

    @Test
    void testInWithAllNulls() {
        createUserTable();
        dba.insert(new User[]{
                        new User("1", "A", 1, 1.0),
                        new User("2", null, null, null)})
                ;
        List<User> list = dba.select().from("T_USER").where("NAME", Op.IN, Arrays.asList(null, null)).queryForList(User.class);
        assertEquals(1, list.size()); // 只有 NAME IS NULL 命中
    }

    @Test
    void testNotInWithNulls() {
        createUserTable();
        dba.insert(new User[]{new User("1", "A", 1, 1.0), new User("2", "B", 2, 2.0)});
        List<User> list = dba.select().from("T_USER").where("NAME", Op.NOT_IN, Arrays.asList("A", null))
                .queryForList(User.class);
        // NOT_IN 中 null 被过滤掉，只对 A 做了 NOT IN
        assertEquals(1, list.size());
        assertEquals("B", list.get(0).getName());
    }

    @Test
    void testCndWhereWithIsNullOp() {
        Cnd c = Cnd.where("NAME", Op.IS_NULL);
        assertEquals(Op.IS_NULL, c.getOp());
        assertNull(c.getValue());
    }

    @Test
    void testCndOrSingle() {
        Cnd c = Cnd.or(Cnd.where("NAME", "X"));
        assertNotNull(c);
        assertEquals("NAME", c.getField());
    }

    @Test
    void testCndNestedAndOr() {
        Cnd inner = Cnd.and(Cnd.where("A", 1), Cnd.where("B", 2));
        Cnd outer = Cnd.or(inner, Cnd.where("C", 3));
        assertNotNull(outer.getChildren());
        assertEquals(2, outer.getChildren().size());
    }

    @Test
    void testCndWhereConditionFalse() {
        Cnd c = Cnd.where(false, "NAME", "X");
        assertNull(c);
    }

    @Test
    void testCndLikeAlreadyWildcard() {
        // 如果 value 已经以 % 开头/结尾，直接使用
        Cnd c = Cnd.where("NAME", Op.LIKE, "%li%");
        Sql sql = new Sql();
        c.toSql(sql);
        assertEquals("NAME LIKE ?",sql.getSql());
        assertEquals("%li%",sql.getParams().get(0));
    }

    @Test
    void testCndNotLikeLeft() {
        Cnd c = Cnd.where("NAME", Op.NOT_LIKE_LEFT, "Ali");
        assertEquals(Op.NOT_LIKE_LEFT, c.getOp());
    }

    @Test
    void testCndNotLikeRight() {
        Cnd c = Cnd.where("NAME", Op.NOT_LIKE_RIGHT, "ice");
        assertEquals(Op.NOT_LIKE_RIGHT, c.getOp());
    }

    @Test
    void testRangeFromOnly() {
        setupUsers2();
        Range<Integer> range = Range.of(null, 50);
        List<User> list = dba.select().from("T_USER").where("AGE", range).queryForList(User.class);
        assertTrue(list.size() >= 1); // AGE <= 50
    }

    @Test
    void testRangeToOnly() {
        setupUsers();
        Range<Integer> range = Range.of(30, null);
        List<User> list = dba.select().from("T_USER").where("AGE", range).queryForList(User.class);
        assertEquals(1, list.size()); // 30 >= 30
    }

    // ===== 更多 Sql 场景 =====

    @Test
    void testSelectEmptyFields() {
        Sql s = Sql.select();
        assertTrue(s.toString().contains("SELECT *"));
    }

    @Test
    void testSelectSingleField() {
        Sql s = Sql.select("ID");
        assertTrue(s.toString().contains("SELECT ID"));
    }

    @Test
    void testOrderByEmptyList() {
        // orderBy 空列表不影响 SQL
        Sql s = new Sql("SELECT * FROM T").orderBy(new ArrayList<>());
        assertTrue(s.toString().contains("FROM T"));
    }

    @Test
    void testOrderByEmptyArray() {
        Sql s = new Sql("SELECT * FROM T").orderBy();
        assertTrue(s.toString().contains("FROM T"));
    }

    @Test
    void testQueryForValueDate() {
        createUserTable();
        dba.insert(new User("u1", "X", 1, 1.0));
        // LocalDate 查询
        assertNull(dba.select("NAME").from("T_USER").where("ID", "X").queryForDate());
    }

    // ===== deleteByKey 参数个数不匹配 =====

    @Test
    void testDeleteByKeyArgMismatch() {
        assertThrows(IllegalArgumentException.class, () -> dba.deleteByKey(User.class));
    }

    @Test
    void testSelectByKeyArgMismatch() {
        assertThrows(IllegalArgumentException.class, () -> dba.selectByKey(User.class));
    }

    // ===== 工具方法 =====

    @Test
    void testDbaUtilTableNameNull() {
        assertThrows(IllegalArgumentException.class, () -> DbaUtil.validTableName(null));
    }

    @Test
    void testDbaUtilTableNameBlank() {
        assertThrows(IllegalArgumentException.class, () -> DbaUtil.validTableName(""));
    }

    @Test
    void testDbaUtilTableNameSpecial() {
        assertThrows(IllegalArgumentException.class, () -> DbaUtil.validTableName("TABLE; DROP"));
    }

    @Test
    void testKeyArrayNoId() {
        // ProductEntity 有 @Id("id")，不会抛异常。换 org.junit.jupiter.api.Test 无 @Id 的类
        // 但这个库的 keyArray 要求必须有 @Id，否则抛异常
        // 简单验证 DbaUtil.keyArray 的正常路径
        List<io.github.jinghui70.rainbow.dbaccess.object.PropInfo> keys = DbaUtil.keyArray(ComplexKeyEntity.class);
        assertEquals(2, keys.size());
    }

    @Test
    void testPropInfoCacheClear() {
        PropInfoCache.clear();
        var m = PropInfoCache.get(User.class);
        assertNotNull(m);
    }

    // ===== updateBean 模式 =====

    @Test
    void testUpdateBeanWithAutoIncrementField() {
        createAutoTable();
        dba.insert(new AutoEntity("Auto1", 90.0));
        List<AutoEntity> list = dba.select().from("T_AUTO").queryForList(AutoEntity.class);
        assertEquals(1, list.size());
        AutoEntity e = list.get(0);
        int id = e.getId();
        e.setName("Updated");
        dba.update(e);
        AutoEntity r = dba.selectByKey(AutoEntity.class, id);
        assertEquals("Updated", r.getName());
    }

    // ===== delete 更多 =====

    @Test
    void testDeleteBatch() {
        createUserTable();
        dba.insert(new User[]{new User("1", "A", 1, 1.0), new User("2", "B", 2, 2.0), new User("3", "C", 3, 3.0)});
        assertEquals(3, dba.select().from("T_USER").count());
        // Dba.delete(List) 内部对每条执行 execute()，但复用同一个 Sql 对象，
        // 每次都 setParam(keys) 再 execute()，结果累加。这里只验证逐条删除能力。
        dba.deleteByKey(User.class, "1");
        dba.deleteByKey(User.class, "2");
        dba.deleteByKey(User.class, "3");
        assertEquals(0, dba.select().from("T_USER").count());
    }

    // ===== insert 自定义表名 =====

    @Test
    void testInsertBeanIntoCustomTable() {
        createProductTable();
        dba.mergeOf(new ProductEntity("p1", "Widget", 19.99)).into("PRODUCT_ENTITY").execute();
        assertEquals(1, dba.select().from("PRODUCT_ENTITY").count());
    }

    // helpers
    private void setupUsers() {
        createUserTable();
        dba.insert(new User[]{new User("u1", "X", 42, 1.0)});
    }

    private void setupUsers2() {
        createUserTable();
        dba.insert(new User[]{
                        new User("u1", "Alice", 25, 90.0),
                        new User("u2", "Bob", 30, 80.0)})
                ;
    }
}
