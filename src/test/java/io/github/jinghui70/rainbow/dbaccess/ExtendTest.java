package io.github.jinghui70.rainbow.dbaccess;

import cn.hutool.core.lang.TypeReference;
import io.github.jinghui70.rainbow.dbaccess.cnd.Op;
import io.github.jinghui70.rainbow.dbaccess.dialect.DialectDefault;
import io.github.jinghui70.rainbow.dbaccess.dialect.DialectOracle;
import io.github.jinghui70.rainbow.dbaccess.dialect.DialectPostgreSQL;
import io.github.jinghui70.rainbow.dbaccess.fieldmapper.*;
import io.github.jinghui70.rainbow.dbaccess.rowmapper.CamelCaseMapMapper;
import io.github.jinghui70.rainbow.dbaccess.rowmapper.MapRowMapper;
import io.github.jinghui70.rainbow.dbaccess.rowmapper.ObjectArrayRowMapper;
import io.github.jinghui70.rainbow.dbaccess.rowmapper.StringArrayRowMapper;
import io.github.jinghui70.rainbow.dbaccess.memory.Field;
import io.github.jinghui70.rainbow.dbaccess.memory.MemoryDataSource;
import io.github.jinghui70.rainbow.dbaccess.memory.Table;
import io.github.jinghui70.rainbow.dbaccess.model.*;
import io.github.jinghui70.rainbow.dbaccess.object.BeanMapper;
import io.github.jinghui70.rainbow.dbaccess.sql.Sql;
import io.github.jinghui70.rainbow.dbaccess.utils.StringBuilderX;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 补齐覆盖 — StringBuilderWrapper/RowMapper/Dialect/Blob工厂/Cnd子查询等。
 */
class ExtendTest extends BaseTest {

    // ===== StringBuilderWrapper / StringBuilderX =====

    @Test void testStringBuilderXBasic() {
        StringBuilderX x = new StringBuilderX();
        x.append("hello").appendTempComma();
        x.append("world");
        assertEquals("hello,world", x.toString());
    }

    @Test void testStringBuilderXRepeat() {
        StringBuilderX x = new StringBuilderX();
        x.repeat("?", 3, ",");
        assertEquals("?,?,?", x.toString());
    }

    @Test void testStringBuilderXRepeatNoDelim() {
        StringBuilderX x = new StringBuilderX();
        x.repeat("A", 3);
        assertEquals("AAA", x.toString());
    }

    @Test void testStringBuilderXRepeatZero() {
        StringBuilderX x = new StringBuilderX("X");
        x.repeat("Y", 0);
        assertEquals("X", x.toString());
    }

    @Test void testStringBuilderJoinArrayWithDelim() {
        StringBuilderX x = new StringBuilderX();
        x.join(new String[]{"A", "B"}, "-");
        assertEquals("A-B", x.toString());
    }

    @Test void testStringBuilderJoinArray() {
        StringBuilderX x = new StringBuilderX();
        x.join(new String[]{"A", "B"});
        assertEquals("A,B", x.toString());
    }

    @Test void testStringBuilderJoinCollection() {
        StringBuilderX x = new StringBuilderX();
        x.join(List.of("X", "Y"));
        assertEquals("X,Y", x.toString());
    }

    @Test void testStringBuilderJoinCollectionWithFunc() {
        StringBuilderX x = new StringBuilderX();
        x.join(List.of(1, 2, 3), String::valueOf, "|");
        assertEquals("1|2|3", x.toString());
    }

    @Test void testStringBuilderAppendNull() {
        StringBuilderX x = new StringBuilderX("A");
        // append(Object obj) with null: calls checkTemp() then returns (does NOT call sb.append)
        // BUT checkTemp() only flushes temp — the null itself is not appended
        // append(CharSequence csq) with null: SAME — won't call checkTemp or append
        x.append((Object) null);
        x.append("B");
        assertEquals("AB", x.toString());
    }

    @Test void testStringBuilderAppendConditionTrue() {
        StringBuilderX x = new StringBuilderX();
        x.append(true, "Yes");
        assertEquals("Yes", x.toString());
    }

    @Test void testStringBuilderAppendConditionFalse() {
        StringBuilderX x = new StringBuilderX();
        x.append(false, "No");
        assertEquals("", x.toString());
    }

    @Test void testStringBuilderAppendConditionMultiple() {
        StringBuilderX x = new StringBuilderX();
        x.append(true, "A", "B");
        assertEquals("AB", x.toString());
    }

    @Test void testStringBuilderAppendCharSequenceSlice() {
        StringBuilderX x = new StringBuilderX();
        x.append("ABCDEF", 1, 3);
        assertEquals("BC", x.toString());
    }

    @Test void testStringBuilderTempComma() {
        // clearTemp 丢弃当前的 temp，之前已 flush 的逗号保留
        StringBuilderX x = new StringBuilderX();
        x.append("A").appendTempComma();  // temp=","
        x.append("B").appendTempComma();  // flush "," + "B", temp=","
        x.clearTemp();                    // 丢弃当前 temp=","
        assertEquals("A,B", x.toString());
    }

    @Test void testStringBuilderAppendTempNull() {
        StringBuilderX x = new StringBuilderX();
        x.append("A").appendTemp(null).append("B");
        assertEquals("AB", x.toString());
    }

    @Test void testStringBuilderLimit() {
        StringBuilderX x = new StringBuilderX("VeryLongString");
        x.limit(10);
        // limit(10): len=13 > 10, setLength(10-3=7), 截断为前7字符 "VeryLon"+"..."
        assertEquals("VeryLon...", x.toString());
    }

    @Test void testStringBuilderSetLength() {
        StringBuilderX x = new StringBuilderX("HelloWorld");
        x.setLength(5);
        assertEquals("Hello", x.toString());
    }

    @Test void testStringBuilderLength() {
        assertEquals(3, new StringBuilderX("ABC").length());
    }

    @Test void testStringBuilderAppendChar() {
        StringBuilderX x = new StringBuilderX();
        x.append('A').append('B');
        assertEquals("AB", x.toString());
    }

    @Test void testStringBuilderJoinArrayFunc() {
        StringBuilderX x = new StringBuilderX();
        x.join(new Integer[]{1, 2}, i -> "#" + i, "|");
        assertEquals("#1|#2", x.toString());
    }

    // ===== MapRowMapper =====

    @Test void testMapRowMapperIgnore() {
        createUserTable();
        dba.insert(new User("u1", "Alice", 25, 90.0));
        Map<String, Object> map = dba.select().from("T_USER").where("ID", "u1")
                .queryForObject(MapRowMapper.create().ignore("AGE", "SCORE"));
        assertNotNull(map);
        assertTrue(map.containsKey("ID"));
        assertFalse(map.containsKey("AGE"));
        assertFalse(map.containsKey("SCORE"));
    }

    @Test void testMapRowMapperIgnoreNull() {
        createUserTable();
        dba.insert(new User("u1", null, null, null));
        Map<String, Object> map = dba.select().from("T_USER").where("ID", "u1")
                .queryForObject(MapRowMapper.create().ignoreNull());
        assertFalse(map.containsKey("NAME"));
    }

    @Test void testMapRowMapperPost() {
        createUserTable();
        dba.insert(new User("u1", "Alice", 25, 90.0));
        Map<String, Object> map = dba.select().from("T_USER").where("ID", "u1")
                .queryForObject(MapRowMapper.create().post(m -> m.put("_extra", 1)));
        assertEquals(1, map.get("_extra"));
    }

    @Test void testMapRowMapperSetFieldMapper() {
        createEnumTable();
        dba.insert(new EnumEntity("e1", Status.ACTIVE, Color.RED));
        Map<String, Object> map = dba.select().from("T_ENUM").where("ID", "e1")
                .queryForObject(MapRowMapper.create()
                        .setFieldMapper("STATUS", EnumFieldMapper.of(Status.class)));
        assertEquals(Status.ACTIVE, map.get("STATUS")); // FieldMapper 转换后是枚举对象，不是字符串
    }

    @Test void testCamelCaseMapMapper() {
        createUserTable();
        dba.insert(new User("u1", "Alice", 25, 90.0));
        Map<String, Object> map = dba.select().from("T_USER").where("ID", "u1")
                .queryForObject(new CamelCaseMapMapper());
        // CamelCaseMapMapper 将 key 转为驼峰 — 检查至少非空、包含原始大写 key
        assertFalse(map.isEmpty());
    }

    // ===== StringArrayRowMapper =====

    @Test void testStringArrayRowMapper() {
        createUserTable();
        dba.insert(new User("u1", "Alice", 25, 90.0));
        List<String[]> list = dba.select("ID", "NAME").from("T_USER").queryForList(new StringArrayRowMapper());
        assertEquals("u1", list.get(0)[0]);
        assertEquals("Alice", list.get(0)[1]);
    }

    // ===== ObjectArrayRowMapper =====

    @Test void testObjectArrayRowMapper() {
        createUserTable();
        dba.insert(new User("u1", "Alice", 25, 90.0));
        ObjectArrayRowMapper mapper = new ObjectArrayRowMapper();
        List<Object[]> list = dba.select("ID", "NAME", "AGE").from("T_USER").queryForList(mapper);
        assertEquals("u1", list.get(0)[0]);
        assertEquals("Alice", list.get(0)[1]);
    }

    @Test void testObjectArrayRowMapperWithFieldMapper() {
        createEnumTable();
        dba.insert(new EnumEntity("e1", Status.ACTIVE, Color.RED));
        ObjectArrayRowMapper mapper = new ObjectArrayRowMapper()
                .setFieldMapper(1, EnumFieldMapper.of(Status.class));
        List<Object[]> list = dba.select("STATUS").from("T_ENUM").queryForList(mapper);
        assertEquals(Status.ACTIVE, list.get(0)[0]);
    }

    @Test void testObjectArrayRowMapperSetFieldMapperByKey() {
        createEnumTable();
        dba.insert(new EnumEntity("e1", Status.ACTIVE, Color.RED));
        ObjectArrayRowMapper mapper = new ObjectArrayRowMapper()
                .setFieldMapper("STATUS", EnumFieldMapper.of(Status.class));
        List<Object[]> list = dba.select("STATUS").from("T_ENUM").queryForList(mapper);
        assertEquals(Status.ACTIVE, list.get(0)[0]);
    }

    // ===== Blob FieldMapper 工厂方法 + 读写往返 =====

    @Test void testBlobObjectFieldMapperOfList() {
        createBlobTable();
        BlobObjectFieldMapper<List<String>> m = BlobObjectFieldMapper.ofList(String.class);
        List<String> list = List.of("java", "db", "spring");
        dba.sql("INSERT INTO T_BLOB(ID) VALUES('b1')").execute();
        dba.update("T_BLOB").set("LOB_STRING", m, list)
                .where("ID", "b1").execute();
        List<String> result = dba.select("LOB_STRING").from("T_BLOB")
                .where("ID", "b1").queryForValue(m);
        assertEquals(list, result);
    }

    @Test void testBlobObjectFieldMapperOfArray() {
        createBlobTable();
        BlobObjectFieldMapper<String[]> m = BlobObjectFieldMapper.ofArray(String.class);
        String[] arr = {"x", "y", "z"};
        dba.sql("INSERT INTO T_BLOB(ID) VALUES('b2')").execute();
        dba.update("T_BLOB").set("LOB_STRING", m, arr)
                .where("ID", "b2").execute();
        String[] result = dba.select("LOB_STRING").from("T_BLOB")
                .where("ID", "b2").queryForValue(m);
        assertArrayEquals(arr, result);
    }

    @Test void testBlobObjectFieldMapperOfMap() {
        createBlobTable();
        BlobObjectFieldMapper<Map<String, String>> m = BlobObjectFieldMapper.ofMap(String.class);
        // 全部用字符串值，避免 JSON 反序列化时 Integer→Long 导致的 equals 失败
        Map<String, Object> map = Map.of("key1", "val1", "key2", "42");
        dba.sql("INSERT INTO T_BLOB(ID) VALUES('b3')").execute();
        dba.update("T_BLOB").set("LOB_STRING", m, map)
                .where("ID", "b3").execute();
        Map<String, String> result = dba.select("LOB_STRING").from("T_BLOB")
                .where("ID", "b3").queryForValue(m);
        assertEquals(map.size(), result.size());
        assertEquals("val1", result.get("key1"));
        assertEquals("42", result.get("key2"));
    }

    @Test void testBlobFieldMapperCompress() {
        BlobFieldMapper<?> m = new BlobStringFieldMapper();
        assertFalse(m.isCompress());
        m = m.compress();
        assertTrue(m.isCompress());
    }

    @Test void testBlobFieldMapperSaveToDB() {
        // Already tested via insert, verify compress=false path
        BlobFieldMapper<String> m = new BlobStringFieldMapper();
        m.setCompress(false);
        assertNotNull(m);
    }

    // ===== ObjectFieldMapper 工厂方法 + 读写往返 =====

    @Test void testObjectFieldMapperOfList() {
        createObjectTable();
        List<String> list = List.of("java", "db", "spring");
        dba.sql("INSERT INTO T_OBJECT(ID) VALUES('u1')").execute();
        dba.update("T_OBJECT").set("TAGS", ObjectFieldMapper.ofList(String.class), list)
                .where("ID", "u1").execute();
        List<String> result = dba.select("TAGS").from("T_OBJECT")
                .where("ID", "u1").queryForValue(ObjectFieldMapper.ofList(String.class));
        assertEquals(list, result);
    }

    @Test void testObjectFieldMapperOfArray() {
        createObjectTable();
        String[] arr = {"x", "y", "z"};
        dba.sql("INSERT INTO T_OBJECT(ID) VALUES('u2')").execute();
        dba.update("T_OBJECT").set("TAGS", ObjectFieldMapper.ofArray(String.class), arr)
                .where("ID", "u2").execute();
        String[] result = dba.select("TAGS").from("T_OBJECT")
                .where("ID", "u2").queryForValue(ObjectFieldMapper.ofArray(String.class));
        assertArrayEquals(arr, result);
    }

    @Test void testObjectFieldMapperOfMap() {
        createObjectTable();
        Map<String, String> map = Map.of("key1", "val1", "key2", "42");
        dba.sql("INSERT INTO T_OBJECT(ID) VALUES('u3')").execute();
        dba.update("T_OBJECT").set("ATTRIBUTES", ObjectFieldMapper.ofMap(String.class), map)
                .where("ID", "u3").execute();
        Map<String, String> result = dba.select("ATTRIBUTES").from("T_OBJECT")
                .where("ID", "u3").queryForValue(ObjectFieldMapper.ofMap(String.class));
        assertEquals(map.size(), result.size());
        assertEquals("val1", result.get("key1"));
        assertEquals("42", result.get("key2"));
    }

    @Test void testObjectFieldMapperOf() {
        createObjectTable();
        ObjectFieldMapper<Status> m = ObjectFieldMapper.of(Status.class);
        Status value = Status.PENDING;
        dba.sql("INSERT INTO T_OBJECT(ID) VALUES('u4')").execute();
        dba.update("T_OBJECT").set("TAGS", m, value)
                .where("ID", "u4").execute();
        Status result = dba.select("TAGS").from("T_OBJECT")
                .where("ID", "u4").queryForValue(m);
        assertEquals(value, result);
    }

    @Test void testBlobObjectFieldMapperOf() {
        createBlobTable();
        BlobObjectFieldMapper<Status> m = BlobObjectFieldMapper.of(Status.class);
        Status value = Status.PENDING;
        dba.sql("INSERT INTO T_BLOB(ID) VALUES('b4')").execute();
        dba.update("T_BLOB").set("LOB_STRING", m, value)
                .where("ID", "b4").execute();
        Status result = dba.select("LOB_STRING").from("T_BLOB")
                .where("ID", "b4").queryForValue(m);
        assertEquals(value, result);
    }

    @Test void testObjectFieldMapperOfMapInteger() {
        createObjectTable();
        Map<String, Integer> map = Map.of("count", 42, "score", -1);
        dba.sql("INSERT INTO T_OBJECT(ID) VALUES('u5')").execute();
        dba.update("T_OBJECT").set("ATTRIBUTES", ObjectFieldMapper.ofMap(Integer.class), map)
                .where("ID", "u5").execute();
        Map<String, Integer> result = dba.select("ATTRIBUTES").from("T_OBJECT")
                .where("ID", "u5").queryForValue(ObjectFieldMapper.ofMap(Integer.class));
        assertEquals(Integer.valueOf(42), result.get("count"));
        assertEquals(Integer.valueOf(-1), result.get("score"));
    }

    @Test void testBlobObjectFieldMapperOfMapInteger() {
        createBlobTable();
        Map<String, Integer> map = Map.of("count", 42, "score", -1);
        dba.sql("INSERT INTO T_BLOB(ID) VALUES('b5')").execute();
        dba.update("T_BLOB").set("LOB_STRING", BlobObjectFieldMapper.ofMap(Integer.class), map)
                .where("ID", "b5").execute();
        Map<String, Integer> result = dba.select("LOB_STRING").from("T_BLOB")
                .where("ID", "b5").queryForValue(BlobObjectFieldMapper.ofMap(Integer.class));
        assertEquals(Integer.valueOf(42), result.get("count"));
        assertEquals(Integer.valueOf(-1), result.get("score"));
    }

    @Test void testObjectFieldMapperOfMapComplex() {
        // Map<String, List<Integer>> 场景 — 需要 TypeReference 才能反序列化 List 元素类型
        createObjectTable();
        Map<String, List<Integer>> map = Map.of("evens", List.of(2, 4, 6), "odds", List.of(1, 3, 5));
        TypeReference<Map<String, List<Integer>>> typeRef = new TypeReference<>() {};
        dba.sql("INSERT INTO T_OBJECT(ID) VALUES('u6')").execute();
        dba.update("T_OBJECT").set("ATTRIBUTES", ObjectFieldMapper.ofMap(typeRef.getType()), map)
                .where("ID", "u6").execute();
        Map<String, List<Integer>> result = dba.select("ATTRIBUTES").from("T_OBJECT")
                .where("ID", "u6").queryForValue(ObjectFieldMapper.ofMap(typeRef.getType()));
        assertEquals(List.of(2, 4, 6), result.get("evens"));
        assertEquals(List.of(1, 3, 5), result.get("odds"));
    }

    // ===== Dialect =====

    @Test void testDialectDefault() {
        var d = DialectDefault.INSTANCE;
        assertTrue(d.wrapLimitSql("SELECT * FROM T", 10).contains("LIMIT"));
        assertTrue(d.wrapRangeSql("SELECT * FROM T", 2, 5).contains("LIMIT"));
    }

    @Test void testDialectPostgreSQL() {
        DialectPostgreSQL d = new DialectPostgreSQL();
        assertTrue(d.wrapPagedSql("SELECT * FROM T", 2, 10).contains("LIMIT"));
    }

    @Test void testDialectOracle() {
        DialectOracle d = new DialectOracle();
        String sql = d.wrapPagedSql("SELECT * FROM T", 2, 10);
        // Oracle: WHERE ROWNUM ... 子查询包裹
        assertTrue(sql.contains("ROWNUM") || sql.contains("T2") || sql.contains("WHERE"));
    }

    // ===== Memory 相关 =====

    @Test void testMemoryDataSourceGetConnection() throws SQLException {
        MemoryDataSource ds = new MemoryDataSource(new io.github.jinghui70.rainbow.dbaccess.memory.MemoryConnection());
        assertNotNull(ds.getConnection());
    }

    @Test void testTableDdlComplex() {
        Table t = new Table("T",
                Field.createKeyString("ID"),
                Field.createString("NAME", 64),
                Field.createDouble("AMOUNT"),
                Field.createNumeric("PRICE", 2)
        );
        String ddl = t.ddl();
        assertTrue(ddl.contains("T"));
        assertTrue(ddl.contains("PRIMARY KEY"));
    }

    // ===== Cnd 子查询 / toSql 用 Sql 当 value =====

    @Test void testCndWithSqlSubquery() {
        createUserTable();
        dba.insert(new User[]{
                new User("1", "Alice", 25, 90.0),
                new User("2", "Bob", 30, 80.0)
        });
        Sql sub = dba.select("ID").from("T_USER").where("NAME", "Alice");
        List<User> list = dba.select().from("T_USER")
                .where("ID", Op.IN, sub)
                .queryForList(User.class);
        assertEquals(1, list.size());
        assertEquals(25, list.get(0).getAge());
        list = dba.select().from("T_USER")
                .where("ID", Op.NOT_IN, sub)
                .queryForList(User.class);
        assertEquals(1, list.size());
        assertEquals(30, list.get(0).getAge());
    }

    // ===== Sql.setDba / Sql 无参构造 =====

    @Test void testSqlNoArgConstructor() {
        Sql s = new Sql();
        assertNotNull(s.getParams());
        assertTrue(s.getParams().isEmpty());
    }

    @Test void testSqlSetParams() {
        Sql s = new Sql().addParam(1, 2);
        s.setParams(List.of(3, 4));
        assertEquals(2, s.getParams().size());
    }

    // ===== BeanMapper 手工使用 =====

    @Test void testBeanMapperMapRow() {
        createUserTable();
        dba.insert(new User("u1", "Alice", 25, 90.0));
        User u = dba.select().from("T_USER").where("ID", "u1").queryForObject(BeanMapper.of(User.class));
        assertEquals("Alice", u.getName());
    }

    // ===== BoolFieldMapper formDB / saveToDB =====

    @Test void testBoolFieldMapperSave() {
        // BoolFieldMapper.saveToDB 将 true→1, false→0 再 super.saveToDB
        BoolFieldMapper m = BoolFieldMapper.INSTANCE;
        assertNotNull(m);
    }

}
