package io.github.jinghui70.rainbow.dbaccess.booltest;

import io.github.jinghui70.rainbow.dbaccess.BaseTest;
import io.github.jinghui70.rainbow.dbaccess.DbaTestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class BoolTest extends BaseTest {

    @BeforeEach
    void init() {
        DbaTestUtil.initTable(dba, "T_BOOL");
    }

    @Test
    public void test() {
        Boolean[] array = new Boolean[]{true, true, true};

        TBool t = new TBool();
        t.setId("1");
        t.setIntBool(true);
        t.setStringBool(true);
        t.setTfBool(true);
        t.setYnBool(true);
        t.setArray(array);
        dba.insert(t);

        Map<String, Object> map = dba.select().from("T_BOOL").where("ID", "1").queryForMap();
        assertEquals(1, map.get("int_bool"));
        assertEquals("TRUE", map.get("string_bool"));
        assertEquals("T", map.get("tf_bool"));
        assertEquals("Y", map.get("yn_bool"));
        assertEquals("Y", map.get("ARRAY_1"));
        assertEquals("Y", map.get("ARRAY_2"));
        assertEquals("Y", map.get("ARRAY_3"));

        t = dba.selectByKey(TBool.class, "1");
        assertEquals(Boolean.TRUE, t.getIntBool());
        assertEquals(Boolean.TRUE, t.getStringBool());
        assertEquals(Boolean.TRUE, t.getTfBool());
        assertEquals(Boolean.TRUE, t.getYnBool());
        assertArrayEquals(array, t.getArray());

        dba.update(TBool.class)
                .set("int_bool", false)
                .set("string_bool", false)
                .set("yn_bool", false)
                .set("tf_bool", false)
                .set("array_1", false)
                .set("array_2", false)
                .set("array_3", false)
                .where("id", "1")
                .execute();
        map = dba.select().from("T_BOOL").where("ID", "1").queryForMap();
        assertEquals(0, map.get("int_bool"));
        assertEquals("FALSE", map.get("string_bool"));
        assertEquals("F", map.get("tf_bool"));
        assertEquals("N", map.get("yn_bool"));
        assertEquals("N", map.get("ARRAY_1"));
        assertEquals("N", map.get("ARRAY_2"));
        assertEquals("N", map.get("ARRAY_3"));

        t = dba.selectByKey(TBool.class, "1");
        assertEquals(Boolean.FALSE, t.getIntBool());
        assertEquals(Boolean.FALSE, t.getStringBool());
        assertEquals(Boolean.FALSE, t.getTfBool());
        assertEquals(Boolean.FALSE, t.getYnBool());

    }

    @Test
    public void testQuery() {
        Boolean[] array = new Boolean[]{true, false, true};

        TBool t = new TBool();
        t.setId("1");
        t.setIntBool(true);
        t.setStringBool(false);
        t.setArray(array);
        dba.insert(t);

        t = dba.select(TBool.class)
                .where("ARRAY_2", false)
                .and("ARRAY_3", true)
                .queryForObject();
        assertEquals(Boolean.TRUE, t.getIntBool());
        assertEquals(Boolean.FALSE, t.getStringBool());
        assertArrayEquals(array, t.getArray());
    }
}