package io.github.jinghui70.rainbow.dbaccess.fieldmapper.lob;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.map.MapUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import io.github.jinghui70.rainbow.dbaccess.Sql;
import io.github.jinghui70.rainbow.dbaccess.fieldmapper.BlobObjectFieldMapper;
import io.github.jinghui70.rainbow.dbaccess.fieldmapper.FieldMapper;
import io.github.jinghui70.rainbow.dbaccess.memory.DataType;
import io.github.jinghui70.rainbow.dbaccess.memory.Field;
import io.github.jinghui70.rainbow.dbaccess.object.SimpleObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static io.github.jinghui70.rainbow.dbaccess.StrConst.ID;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class BlobTest extends LobTest{

    @BeforeAll
    static void init() {
        dba.createTable("LOB_OBJECT",
                Field.createKeyInt(ID),
                Field.create("LOB_STRING").setType(DataType.BLOB),
                Field.create("LOB_BYTE_ARRAY").setType(DataType.BLOB),
                Field.create("LOB_OBJECT").setType(DataType.BLOB),
                Field.create("LOB_ARRAY").setType(DataType.BLOB),
                Field.create("LOB_LIST").setType(DataType.BLOB),
                Field.create("LOB_SET").setType(DataType.BLOB),
                Field.create("LOB_MAP").setType(DataType.BLOB)
        );
        BlobObject obj = new BlobObject();
        obj.setId(1);
        obj.setLobString(contentStr);
        obj.setLobByteArray(contentByte);
        obj.setLobObject(objectList.get(0));
        obj.setLobList(objectList);
        obj.setLobArray(objectList.toArray(new SimpleObject[0]));
        obj.setLobSet(new HashSet<>(objectList));
        obj.setLobMap(Map.of("A", objectList));
        dba.insert(obj);
    }

    private BlobObject obj;

    @BeforeEach
    void start() {
        obj = dba.selectByKey(BlobObject.class, 1);
    }

    @Test
    public void test() {
        assertEquals(contentStr, obj.getLobString());
        assertArrayEquals(contentByte, obj.getLobByteArray());
    }

    @Test
    public void testObject() {
        assertTom(obj.getLobObject());

        Sql sql = dba.select("LOB_OBJECT").from("LOB_OBJECT").where(ID, 1);
        JSONObject jsonObject = sql.queryForValue(BlobObjectFieldMapper.of(JSONObject.class));
        assertEquals("Tom", jsonObject.getStr("name"));

        SimpleObject tom = sql.queryForValue(BlobObjectFieldMapper.of(SimpleObject.class));
        assertTom(tom);
    }

    @Test
    public void testArray() {
        // Array
        SimpleObject[] array = obj.getLobArray();
        assertEquals(2, array.length);
        assertTom(array[0]);
        assertJerry(array[1]);

        Sql sql = dba.select("LOB_ARRAY").from("LOB_OBJECT").where("id", 1);
        array = sql.queryForValue(BlobObjectFieldMapper.ofArray(SimpleObject.class));
        assertTom(array[0]);
        assertJerry(array[1]);

        JSONArray jsonArray = sql.queryForValue(BlobObjectFieldMapper.of(JSONArray.class));
        assertEquals("Tom", jsonArray.getJSONObject(0).getStr("name"));
        assertEquals("Jerry", jsonArray.getJSONObject(1).getStr("name"));
    }

    @Test
    public void testList() {
        // 列表测试
        List<SimpleObject> list = obj.getLobList();
        assertEquals(2, list.size());
        assertTom(list.get(0));
        assertJerry(list.get(1));

        Sql sql = dba.select("LOB_ARRAY").from("LOB_OBJECT").where("id", 1);
        list = sql.queryForValue(BlobObjectFieldMapper.ofList(SimpleObject.class));
        assertTom(list.get(0));
        assertJerry(list.get(1));
    }

    @Test
    public void testSet() {
        // Set测试
        Set<SimpleObject> set = obj.getLobSet();
        assertEquals(2, set.size());
        List<SimpleObject> list = set.stream().sorted(Comparator.comparing(SimpleObject::getId)).toList();
        assertTom(list.get(0));
        assertJerry(list.get(1));
    }

    @Test
    public void testMap() {
        // Map 测试
        Map<String, List<SimpleObject>> map = obj.getLobMap();
        assertEquals(1, map.size());
        List<SimpleObject> list = map.get("A");
        assertTom(list.get(0));
        assertJerry(list.get(1));

        Sql sql = dba.select("LOB_MAP").from("LOB_OBJECT").where(ID, 1);
        FieldMapper<Map<String, List<SimpleObject>>> mapMapper = new BlobObjectFieldMapper<>(
                new TypeReference<>() {
                });
        map = sql.queryForValue(mapMapper);
        assertEquals(1, map.size());
        list = map.get("A");
        assertTom(list.get(0));
        assertJerry(list.get(1));

        Map<String, Object> simpleMap = sql.queryForValue(BlobObjectFieldMapper.ofMap());
        list = MapUtil.get(simpleMap, "A", new TypeReference<>() {
        });
        assertTom(list.get(0));
        assertJerry(list.get(1));
    }

}
