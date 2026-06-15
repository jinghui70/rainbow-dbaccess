package io.github.jinghui70.rainbow.dbaccess;

import io.github.jinghui70.rainbow.dbaccess.model.*;
import org.junit.jupiter.api.Test;

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
