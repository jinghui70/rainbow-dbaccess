package io.github.jinghui70.rainbow.dbaccess.map;


import io.github.jinghui70.rainbow.dbaccess.BaseTest;
import io.github.jinghui70.rainbow.dbaccess.DbaTestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
}