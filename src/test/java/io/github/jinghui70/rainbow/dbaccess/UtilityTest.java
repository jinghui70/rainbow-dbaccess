package io.github.jinghui70.rainbow.dbaccess;

import io.github.jinghui70.rainbow.dbaccess.memory.DataType;
import io.github.jinghui70.rainbow.dbaccess.memory.Field;
import io.github.jinghui70.rainbow.dbaccess.memory.Table;
import io.github.jinghui70.rainbow.dbaccess.model.*;
import io.github.jinghui70.rainbow.dbaccess.object.BeanMapper;
import io.github.jinghui70.rainbow.dbaccess.object.PropInfoCache;
import io.github.jinghui70.rainbow.dbaccess.sql.OrderBy;
import io.github.jinghui70.rainbow.dbaccess.sql.PageData;
import io.github.jinghui70.rainbow.dbaccess.sql.Range;
import io.github.jinghui70.rainbow.dbaccess.sql.Sql;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 工具方法测试。
 */
class UtilityTest extends BaseTest {

    // ===== tableName =====

    @Test void testTableNameWithAnnotation() {
        assertEquals("T_USER", DbaUtil.tableName(User.class));
    }

    @Test void testTableNameWithoutAnnotation() {
        assertEquals("PRODUCT_ENTITY", DbaUtil.tableName(ProductEntity.class));
    }

    // ===== enumCheck =====

    @Test void testEnumCheckName() {
        assertEquals("ACTIVE", DbaUtil.enumCheck(Status.ACTIVE));
    }

    @Test void testEnumCheckCode() {
        assertEquals("R", DbaUtil.enumCheck(Color.RED));
    }

    @Test void testEnumCheckNull() {
        assertNull(DbaUtil.enumCheck(null));
    }

    @Test void testEnumCheckNonEnum() {
        assertEquals("hello", DbaUtil.enumCheck("hello"));
    }

    // ===== validTableName =====

    @Test void testValidTableNameOk() {
        assertEquals("T_USER", DbaUtil.validTableName("T_USER"));
    }

    @Test void testValidTableNameInvalid() {
        assertThrows(IllegalArgumentException.class, () -> DbaUtil.validTableName("DROP TABLE"));
    }

    // ===== Range =====

    @Test void testRange() {
        Range<Integer> r = Range.of(10, 20);
        assertTrue(r.fullRange());
        assertFalse(r.singleValue());
        assertEquals(10, r.getFrom());
        assertEquals(20, r.getTo());
    }

    @Test void testRangeReverse() {
        Range<Integer> r = Range.of(20, 10);
        assertEquals(10, r.getFrom());
        assertEquals(20, r.getTo());
    }

    @Test void testRangeSingleValue() {
        Range<Integer> r = Range.of(5, 5);
        assertTrue(r.singleValue());
    }

    // ===== OrderBy =====

    @Test void testOrderBy() {
        OrderBy o = new OrderBy("NAME", true);
        assertEquals("NAME", o.getField());
        assertTrue(o.isDesc());
        assertTrue(o.toString().contains("DESC"));
    }

    @Test void testOrderByAsc() {
        OrderBy o = new OrderBy("NAME", false);
        assertFalse(o.isDesc());
        assertFalse(o.toString().contains("DESC"));
    }

    @Test void testOrderByParse() {
        assertEquals("NAME", OrderBy.parse("NAME").getField());
        assertFalse(OrderBy.parse("NAME").isDesc());

        assertEquals("NAME", OrderBy.parse("NAME DESC").getField());
        assertTrue(OrderBy.parse("NAME DESC").isDesc());

        // 大小写不敏感，多余空白容忍
        assertTrue(OrderBy.parse("  NAME   desc  ").isDesc());
        assertEquals("NAME", OrderBy.parse("  NAME   desc  ").getField());

        // ASC 和无法识别的后缀都按升序
        assertFalse(OrderBy.parse("NAME ASC").isDesc());
        assertFalse(OrderBy.parse("NAME 乱写").isDesc());

        // 带别名前缀
        assertEquals("O.CREATE_TIME", OrderBy.parse("O.CREATE_TIME DESC").getField());
    }

    @Test void testOrderByParseRoundTrip() {
        // parse 是 toString 的逆运算
        for (String text : List.of("NAME", "NAME DESC", "O.CREATE_TIME", "O.ID DESC")) {
            assertEquals(text, OrderBy.parse(text).toString());
        }
    }

    @Test void testOrderByFactories() {
        // of = 升序，desc = 降序，等价于 new OrderBy(field, false/true)
        assertEquals("NAME", OrderBy.of("NAME").getField());
        assertFalse(OrderBy.of("NAME").isDesc());
        assertEquals("NAME", OrderBy.of("NAME").toString());

        assertEquals("NAME", OrderBy.desc("NAME").getField());
        assertTrue(OrderBy.desc("NAME").isDesc());
        assertEquals("NAME DESC", OrderBy.desc("NAME").toString());
    }

    // ===== PageData =====

    @Test void testPageData() {
        PageData<String> p = new PageData<>(100, List.of("a", "b"));
        assertEquals(100, p.getTotal());
        assertEquals(2, p.getData().size());
    }

    @Test void testPageDataEmpty() {
        PageData<String> p = new PageData<>(0);
        assertEquals(0, p.getTotal());
        assertTrue(p.getData().isEmpty());
    }

    @Test void testPageDataDefault() {
        PageData<String> p = new PageData<>();
        assertEquals(0, p.getTotal());
        assertTrue(p.getData().isEmpty());
    }

    // ===== StrConst =====

    @Test void testStrConst() {
        assertEquals("SELECT ", DbaUtil.SELECT);
        assertEquals(" WHERE ", DbaUtil.WHERE);
        assertEquals(" AND ", DbaUtil.AND);
        assertEquals(" OR ", DbaUtil.OR);
    }

    // ===== keyArray =====

    @Test void testKeyArray() {
        List<io.github.jinghui70.rainbow.dbaccess.object.PropInfo> keys = DbaUtil.keyArray(User.class);
        assertEquals(1, keys.size());
        assertEquals("ID", keys.get(0).getFieldName()); // @Id String id → 列名 "ID"
    }

    /**
     * 列名归一化的两侧必须一致：PropInfoCache 生成 propMap 的 key，
     * BeanMapper 用结果集列名去查这个 map。只改一侧会让整表映射静默返回 null 字段。
     */
    @Test void testColumnNameNormalizationIsUpperCase() {
        PropInfoCache.get(User.class).keySet()
                .forEach(key -> assertEquals(key.toUpperCase(), key, "propMap 的 key 必须是大写列名"));
        // 结果集列名无论什么大小写都要能映射上
        createUserTable();
        dba.insertOf(new User("U1", "Alice", 30, 1.5)).execute();
        assertEquals("Alice", dba.sql("SELECT ID, NAME, AGE, SCORE FROM T_USER")
                .queryForObject(User.class).getName());
        assertEquals("Alice", dba.sql("SELECT id AS ID, name AS name FROM T_USER")
                .queryForObject(User.class).getName());
    }

    @Test void testKeyArrayComplex() {
        List<io.github.jinghui70.rainbow.dbaccess.object.PropInfo> keys = DbaUtil.keyArray(ComplexKeyEntity.class);
        assertEquals(2, keys.size());
    }

    // ===== Sql.whereByKey / whereByObj：按主键拼 WHERE =====

    @Test void testWhereByKeySingle() {
        Sql sql = dba.sql("DELETE FROM T_USER").whereByKey(User.class, "u1");
        assertEquals("DELETE FROM T_USER WHERE ID=?", sql.getSql());
        assertEquals(List.of("u1"), sql.getParams());
    }

    @Test void testWhereByKeyComplexKeepsDeclareOrder() {
        Sql sql = dba.select().from("T_COMPLEX_KEY").whereByKey(ComplexKeyEntity.class, "A", "B");
        assertEquals("SELECT * FROM T_COMPLEX_KEY WHERE KEY_A=? AND KEY_B=?", sql.getSql());
        // 参数按 @Id 声明顺序绑定，顺序错了会查到别人的行
        assertEquals(List.of("A", "B"), sql.getParams());
    }

    @Test void testWhereByKeyArgMismatch() {
        assertThrows(IllegalArgumentException.class,
                () -> dba.select().from("T_COMPLEX_KEY").whereByKey(ComplexKeyEntity.class, "A"));
        assertThrows(IllegalArgumentException.class,
                () -> dba.select().from("T_COMPLEX_KEY").whereByKey(ComplexKeyEntity.class, "A", "B", "C"));
    }

    @Test void testWhereByKeyNoId() {
        assertThrows(IllegalArgumentException.class,
                () -> dba.select().from("T_NO_KEY").whereByKey(NoKeyEntity.class, "1"));
    }

    @Test void testWhereByObj() {
        Sql sql = dba.select().from("T_USER").whereByObj(new User("u1", "Alice", 30, 1.5));
        // 只取主键字段，非主键属性不进入 WHERE
        assertEquals("SELECT * FROM T_USER WHERE ID=?", sql.getSql());
        assertEquals(List.of("u1"), sql.getParams());
    }

    @Test void testWhereByObjNullKey() {
        // 主键值为 null 会拼出 WHERE ID = null 静默匹配不到行，必须直接报错
        assertThrows(IllegalArgumentException.class,
                () -> dba.select().from("T_USER").whereByObj(new User(null, "Alice", 30, 1.5)));
    }

    @Test void testWhereByObjComplex() {
        Sql sql = dba.select().from("T_COMPLEX_KEY")
                .whereByObj(new ComplexKeyEntity("A", "B", "remark"));
        assertEquals("SELECT * FROM T_COMPLEX_KEY WHERE KEY_A=? AND KEY_B=?", sql.getSql());
        assertEquals(List.of("A", "B"), sql.getParams());
    }

    @Test void testWhereByObjNoId() {
        assertThrows(IllegalArgumentException.class,
                () -> dba.select().from("T_NO_KEY").whereByObj(new NoKeyEntity("x")));
    }

    // ===== BeanMapper =====

    @Test void testBeanMapper() {
        BeanMapper<User> mapper = BeanMapper.of(User.class);
        assertNotNull(mapper);
    }

    // ===== PropInfoCache =====

    @Test void testPropInfoCacheCached() {
        LinkedHashMap<String, io.github.jinghui70.rainbow.dbaccess.object.PropInfo> m1 = PropInfoCache.get(User.class);
        LinkedHashMap<String, io.github.jinghui70.rainbow.dbaccess.object.PropInfo> m2 = PropInfoCache.get(User.class);
        assertSame(m1, m2);
    }

    // ===== Table DDL =====

    @Test void testTableDdlComplexKey() {
        Table t = new Table("T",
                Field.createKeyString("KEY_A"),
                Field.createKeyString("KEY_B"),
                Field.createString("VALUE"));
        String ddl = t.ddl();
        System.out.println("DDL: " + ddl);
        // 复合主键 DDL 不应有多余逗号
        assertTrue(ddl.contains("PRIMARY KEY"), "应包含 PRIMARY KEY");
    }

    @Test void testTableHasKey() {
        Table t = new Table("T", Field.createKeyString("ID"));
        assertTrue(t.hasKey());
        Table t2 = new Table("T", Field.createString("X"));
        assertFalse(t2.hasKey());
    }

    // ===== Field =====

    @Test void testFieldDefaults() {
        Field f = Field.createInt("CNT");
        assertFalse(f.isKey());
        assertFalse(f.isAutoIncrement());
        assertEquals(DataType.INT, f.getType());
        assertEquals(0, f.getDefaultValue());
    }
}
