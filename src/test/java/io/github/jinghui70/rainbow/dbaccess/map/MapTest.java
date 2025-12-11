package io.github.jinghui70.rainbow.dbaccess.map;


import io.github.jinghui70.rainbow.dbaccess.BaseTest;
import io.github.jinghui70.rainbow.dbaccess.DbaTestUtil;
import io.github.jinghui70.rainbow.dbaccess.mapper.CamelCaseMapMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class MapTest extends BaseTest {

    @BeforeEach
    void init() {
        DbaTestUtil.initTable(dba, "SIMPLE_DATA");
    }

    @Test
    public void test() {
        SimpleData data = new SimpleData();
        data.setId("1");
        data.setRevision(1);
        data.logCreate();
        dba.insert(data);
        Map<String, Object> map = dba.select().from("SIMPLE_DATA").limit(1).queryForMap();
        Map<String, Object> mapCamel = dba.select().from("SIMPLE_DATA").limit(1).queryForObject(new CamelCaseMapMapper());
        Object obj = map.get("CREATED_TIME");
        assertNotNull(obj);
        assertEquals(obj, mapCamel.get("createdTime"));
    }

    @Test
    public void testBatchInsert() {
        List<Map<String, Object>> list = List.of(
                Map.of("ID", "1", "REVISION", 1),
                Map.of("ID", "2", "REVISION", 2),
                Map.of("ID", "A", "REVISION", 1),
                Map.of("ID", "B", "REVISION", 1)
        );
        dba.insert("SIMPLE_DATA", list);
        List<SimpleData> result = dba.select().from(SimpleData.class).orderBy("ID")
                .queryForList(SimpleData.class);
        assertEquals(4, result.size());
        assertEquals("A", result.get(2).getId());
    }
}