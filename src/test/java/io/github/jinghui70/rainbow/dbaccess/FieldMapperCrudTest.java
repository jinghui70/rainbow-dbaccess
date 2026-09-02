package io.github.jinghui70.rainbow.dbaccess;

import io.github.jinghui70.rainbow.dbaccess.fieldmapper.BoolFieldMapper;
import io.github.jinghui70.rainbow.dbaccess.fieldmapper.FieldValue;
import io.github.jinghui70.rainbow.dbaccess.model.*;
import io.github.jinghui70.rainbow.dbaccess.object.PropInfoCache;
import io.github.jinghui70.rainbow.dbaccess.sql.UpdateSql;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FieldMapper 在 UPDATE/DELETE 操作中的交叉测试。
 */
class FieldMapperCrudTest extends BaseTest {

    // ===== UPDATE 测试 =====

    @Test
    void testUpdateEnum() {
        createEnumTable();
        EnumEntity e = new EnumEntity("e1", Status.PENDING, Color.RED);
        dba.insert(e);

        // 修改枚举字段
        e.setStatus(Status.ACTIVE);
        e.setColor(Color.BLUE);
        dba.update(e);

        EnumEntity updated = dba.selectByKey(EnumEntity.class, "e1");
        assertEquals(Status.ACTIVE, updated.getStatus());
        assertEquals(Color.BLUE, updated.getColor());
    }

    @Test
    void testCodeEnumUnmatchedCodeReadsNull() {
        createEnumTable();
        dba.insert(new EnumEntity("e1", Status.ACTIVE, Color.RED));

        // 写入无对应枚举的 code：CodeEnum 读取静默返回 null（普通枚举才会抛异常）
        dba.sql("UPDATE T_ENUM SET COLOR='X' WHERE ID=?").addParam("e1").execute();

        EnumEntity e = dba.selectByKey(EnumEntity.class, "e1");
        assertEquals(Status.ACTIVE, e.getStatus());
        assertNull(e.getColor());
    }

    @Test
    void testUpdateSqlSetBoolean() {
        createBoolTable();
        dba.insert(new BoolEntity("b1", false, false));

        // set(field, Boolean) 应自动走 BoolFieldMapper，以 1/0 写入 INT 列
        int count = dba.update("T_BOOL")
                .set("ACTIVE", true)
                .set("FLAG", true)
                .where("ID", "b1")
                .execute();
        assertEquals(1, count);

        // 直接以整型验证底层存储的是 1 而非其它形式
        assertEquals(1, dba.select("ACTIVE").from("T_BOOL").where("ID", "b1").queryForInt());
        assertEquals(1, dba.select("FLAG").from("T_BOOL").where("ID", "b1").queryForInt());

        // false 写 0
        dba.update("T_BOOL").set("ACTIVE", false).set("FLAG", false).where("ID", "b1").execute();
        assertEquals(0, dba.select("ACTIVE").from("T_BOOL").where("ID", "b1").queryForInt());
        assertEquals(0, dba.select("FLAG").from("T_BOOL").where("ID", "b1").queryForInt());

        // null Boolean 走 null 参数路径，不报错
        dba.update("T_BOOL").set("ACTIVE", (Object) null).where("ID", "b1").execute();
        assertNull(dba.select("ACTIVE").from("T_BOOL").where("ID", "b1").queryForIntOptional().orElse(null));
    }

    /**
     * 在 SQL 构建层断言：Boolean 值必须包装为 FieldValue(BoolFieldMapper)，
     * 而非裸 Boolean 直接交给 JDBC。H2 对 Boolean 直写 INT 列较宽容，
     * 执行层测试无法暴露此问题，需在参数层断言（严格类型数据库如 Oracle 会直接报错）。
     */
    @Test
    void testUpdateSqlSetBooleanParamWrapped() {
        UpdateSql sql = dba.update("T_BOOL").set("ACTIVE", true).set("FLAG", false);

        List<Object> params = sql.getParams();
        assertEquals(2, params.size());
        for (Object param : params)
            assertInstanceOf(FieldValue.class, param, "Boolean 参数应包装为 FieldValue 走 BoolFieldMapper，而非裸 Boolean");

        FieldValue active = (FieldValue) params.get(0);
        assertEquals(Boolean.TRUE, active.getValue());
        FieldValue flag = (FieldValue) params.get(1);
        assertEquals(Boolean.FALSE, flag.getValue());

        // 非 Boolean 值不应被误包装
        UpdateSql sql2 = dba.update("T_BOOL").set("ACTIVE", 1);
        assertInstanceOf(Integer.class, sql2.getParams().get(0));
    }

    @Test
    void testUpdateSqlSetMapWithBoolean() {
        createBoolTable();
        dba.insert(new BoolEntity("b1", false, false));

        // setMap 委托给 set(field, value)，Boolean 同样应转 1/0
        dba.update("T_BOOL").setMap(Map.of("ACTIVE", true, "FLAG", false)).where("ID", "b1").execute();

        assertEquals(1, dba.select("ACTIVE").from("T_BOOL").where("ID", "b1").queryForInt());
        assertEquals(0, dba.select("FLAG").from("T_BOOL").where("ID", "b1").queryForInt());
    }

    @Test
    void testUpdateBuilderBoolean() {
        createBoolTable();
        dba.insert(new BoolEntity("b1", false, true));

        // UpdateBuilder 部分更新 Boolean 字段 — PropInfoCache 已为 Boolean 属性注册 BoolFieldMapper
        BoolEntity e = new BoolEntity("b1", true, false);
        dba.updateOf(e).include("active", "flag").execute();

        assertEquals(1, dba.select("ACTIVE").from("T_BOOL").where("ID", "b1").queryForInt());
        assertEquals(0, dba.select("FLAG").from("T_BOOL").where("ID", "b1").queryForInt());

        // 全量更新路径
        dba.updateOf(new BoolEntity("b1", false, true)).execute();
        assertEquals(0, dba.select("ACTIVE").from("T_BOOL").where("ID", "b1").queryForInt());
        assertEquals(1, dba.select("FLAG").from("T_BOOL").where("ID", "b1").queryForInt());
    }

    /**
     * UpdateBuilder 不走 UpdateSql.set(field, value)，而是经 PropInfo.getValue() 取值。
     * 断言 Boolean 属性在属性缓存中已注册 BoolFieldMapper，取值时会包装为 FieldValue —— 这是 UpdateBuilder 无此问题的根基。
     */
    @Test
    void testUpdateBuilderBooleanPropMapper() {
        assertSame(BoolFieldMapper.INSTANCE, PropInfoCache.get(BoolEntity.class).get("active").getMapper());
        assertSame(BoolFieldMapper.INSTANCE, PropInfoCache.get(BoolEntity.class).get("flag").getMapper());
    }

    /**
     * 真实报错场景的复现：布尔列以 VARCHAR(1) 存储。
     * 裸 Boolean 参数直写 VARCHAR 列会触发数据库转换错误（H2 亦然），
     * set(field, Boolean) 必须经 BoolFieldMapper 转为 1/0。
     */
    @Test
    void testUpdateSqlSetBooleanVarchar() {
        createBoolVarcharTable();
        dba.insert(new BoolVarcharEntity("b1", false, true));

        dba.update("T_BOOL_VARCHAR")
                .set("ACTIVE", true)
                .set("FLAG", false)
                .where("ID", "b1")
                .execute();

        assertEquals("1", dba.select("ACTIVE").from("T_BOOL_VARCHAR").where("ID", "b1").queryForString());
        assertEquals("0", dba.select("FLAG").from("T_BOOL_VARCHAR").where("ID", "b1").queryForString());

        // 经映射器读回
        BoolVarcharEntity e = dba.selectByKey(BoolVarcharEntity.class, "b1");
        assertTrue(e.getActive());
        assertFalse(e.getFlag());
    }

    @Test
    void testUpdateBuilderBooleanVarchar() {
        createBoolVarcharTable();
        dba.insert(new BoolVarcharEntity("b1", false, true));

        // UpdateBuilder 部分更新
        dba.updateOf(new BoolVarcharEntity("b1", true, false)).include("active", "flag").execute();
        assertEquals("1", dba.select("ACTIVE").from("T_BOOL_VARCHAR").where("ID", "b1").queryForString());
        assertEquals("0", dba.select("FLAG").from("T_BOOL_VARCHAR").where("ID", "b1").queryForString());

        // UpdateBuilder 全量更新
        dba.updateOf(new BoolVarcharEntity("b1", false, true)).execute();
        assertEquals("0", dba.select("ACTIVE").from("T_BOOL_VARCHAR").where("ID", "b1").queryForString());
        assertEquals("1", dba.select("FLAG").from("T_BOOL_VARCHAR").where("ID", "b1").queryForString());
    }

    /**
     * set(field, null) 各形态：null 不是 Boolean 实例，走 null 参数路径，不应报错且置列为 NULL。
     */
    @Test
    void testUpdateSqlSetNull() {
        createBoolVarcharTable();
        dba.insert(new BoolVarcharEntity("b1", true, true));

        // 裸 null
        dba.update("T_BOOL_VARCHAR").set("ACTIVE", null).where("ID", "b1").execute();
        assertFalse(dba.select("ACTIVE").from("T_BOOL_VARCHAR").where("ID", "b1").queryForStringOptional().isPresent());

        // 类型化的 null Boolean — 同样走 null 路径而非 BoolFieldMapper
        Boolean typedNull = null;
        dba.update("T_BOOL_VARCHAR").set("FLAG", typedNull).where("ID", "b1").execute();
        assertFalse(dba.select("FLAG").from("T_BOOL_VARCHAR").where("ID", "b1").queryForStringOptional().isPresent());

        // setMap 含 null 值 — Map.of 不允许 null，用 HashMap
        Map<String, Object> map = new HashMap<>();
        map.put("ACTIVE", null);
        map.put("FLAG", true);
        dba.update("T_BOOL_VARCHAR").setMap(map).where("ID", "b1").execute();
        assertFalse(dba.select("ACTIVE").from("T_BOOL_VARCHAR").where("ID", "b1").queryForStringOptional().isPresent());
        assertEquals("1", dba.select("FLAG").from("T_BOOL_VARCHAR").where("ID", "b1").queryForString());

        // INT 列场景同样置 NULL
        createBoolTable();
        dba.insert(new BoolEntity("b2", true, true));
        dba.update("T_BOOL").set("ACTIVE", null).where("ID", "b2").execute();
        assertFalse(dba.select("ACTIVE").from("T_BOOL").where("ID", "b2").queryForIntOptional().isPresent());

        // UpdateBuilder 全量更新含 null Boolean 字段（未开 excludeNull，应写入 NULL）
        dba.updateOf(new BoolVarcharEntity("b1", null, false)).include("active", "flag").execute();
        assertFalse(dba.select("ACTIVE").from("T_BOOL_VARCHAR").where("ID", "b1").queryForStringOptional().isPresent());
        assertEquals("0", dba.select("FLAG").from("T_BOOL_VARCHAR").where("ID", "b1").queryForString());
    }

    @Test
    void testUpdateBool() {
        createBoolTable();
        BoolEntity e = new BoolEntity("b1", true, false);
        dba.insert(e);

        // 修改布尔字段
        e.setActive(false);
        e.setFlag(true);
        dba.update(e);

        BoolEntity updated = dba.selectByKey(BoolEntity.class, "b1");
        assertFalse(updated.getActive());
        assertTrue(updated.getFlag());
    }

    @Test
    void testUpdateBlobString() {
        createBlobTable();
        BlobEntity e = new BlobEntity("1", "original", null, null);
        dba.insert(e);

        // 修改 BLOB 字符串
        e.setLobString("updated string");
        dba.update(e);

        BlobEntity updated = dba.selectByKey(BlobEntity.class, "1");
        assertEquals("updated string", updated.getLobString());
    }

    @Test
    void testUpdateBlobBytes() {
        createBlobTable();
        byte[] original = {1, 2, 3};
        BlobEntity e = new BlobEntity("2", null, original, null);
        dba.insert(e);

        // 修改 BLOB 字节
        byte[] updated = {4, 5, 6, 7};
        e.setLobBytes(updated);
        dba.update(e);

        BlobEntity result = dba.selectByKey(BlobEntity.class, "2");
        assertArrayEquals(updated, result.getLobBytes());
    }

    @Test
    void testUpdateBlobObject() {
        createBlobTable();
        BlobEntity.ObjectBlob original = new BlobEntity.ObjectBlob("old", 1, List.of("a"), Map.of("k", 1));
        BlobEntity e = new BlobEntity("3", null, null, original);
        dba.insert(e);

        // 修改 BLOB 对象
        BlobEntity.ObjectBlob updated = new BlobEntity.ObjectBlob("new", 99, List.of("x", "y"), Map.of("k2", 2));
        e.setLobObject(updated);
        dba.update(e);

        BlobEntity result = dba.selectByKey(BlobEntity.class, "3");
        assertNotNull(result.getLobObject());
        assertEquals("new", result.getLobObject().getName());
        assertEquals(99, result.getLobObject().getCount());
    }

    @Test
    void testUpdateObjectCLOB() {
        createObjectTable();
        ObjectEntity e = new ObjectEntity("o1", List.of("tag1"), Map.of("k", 1));
        dba.insert(e);

        // 修改 CLOB/Object 字段
        e.setTags(List.of("tag2", "tag3"));
        e.setAttributes(Map.of("k2", 2, "k3", 3));
        dba.update(e);

        ObjectEntity updated = dba.selectByKey(ObjectEntity.class, "o1");
        assertEquals(2, updated.getTags().size());
        assertTrue(updated.getTags().contains("tag2"));
        assertEquals(2, updated.getAttributes().size());
    }

    // ===== DELETE 测试（枚举做主键）=====

    @Test
    void testDeleteByEnumKey() {
        // 创建一个用枚举做主键的表
        dba.sql("CREATE TABLE T_ENUM_KEY(STATUS VARCHAR(32) PRIMARY KEY, NAME VARCHAR(64))").execute();

        // 插入数据 — 手动插入，因为没有对应的实体类
        dba.sql("INSERT INTO T_ENUM_KEY VALUES(?, ?)").addParam("ACTIVE", "Test").execute();

        // 用枚举值删除（需要手动构造 Sql，或者用 Map）
        int count = dba.sql("DELETE FROM T_ENUM_KEY WHERE STATUS=?")
                .addParam(Status.ACTIVE.name()).execute();

        assertEquals(1, count);
        assertFalse(dba.select().from("T_ENUM_KEY").exist());
    }
}
