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
        assertEquals("id", keys.get(0).getFieldName()); // @Id String id → 列名 "ID"
    }

    @Test void testKeyArrayComplex() {
        List<io.github.jinghui70.rainbow.dbaccess.object.PropInfo> keys = DbaUtil.keyArray(ComplexKeyEntity.class);
        assertEquals(2, keys.size());
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
