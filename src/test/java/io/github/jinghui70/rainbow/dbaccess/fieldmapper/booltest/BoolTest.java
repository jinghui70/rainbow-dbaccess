package io.github.jinghui70.rainbow.dbaccess.fieldmapper.booltest;

import io.github.jinghui70.rainbow.dbaccess.BaseTest;
import io.github.jinghui70.rainbow.dbaccess.DbaTestUtil;
import io.github.jinghui70.rainbow.dbaccess.fieldmapper.BoolFieldMapper;
import io.github.jinghui70.rainbow.dbaccess.fieldmapper.BoolYN;
import io.github.jinghui70.rainbow.dbaccess.memory.DataType;
import io.github.jinghui70.rainbow.dbaccess.memory.Field;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.github.jinghui70.rainbow.dbaccess.StrConst.ID;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class BoolTest extends BaseTest {

    @BeforeEach
    void init() {
        dba.createTable("T_BOOL",
                Field.createKeyInt(ID),
                Field.create("INT_BOOL").setType(DataType.SMALLINT),
                Field.createString("STRING_BOOL", 10),
                Field.createString("YN_BOOL", 1)
        );
    }

    @Test
    public void test() {
        TBool t = new TBool();
        t.setId("1");
        t.setIntBool(true);
        t.setStringBool(true);
        t.setYnBool(true);
        dba.insert(t);

        Map<String, Object> map = dba.select().from("T_BOOL").where("ID", "1").queryForMap();
        assertEquals(1, map.get("int_bool"));
        assertEquals("1", map.get("string_bool"));
        assertEquals("Y", map.get("yn_bool"));

        t = dba.selectByKey(TBool.class, "1");
        assertEquals(Boolean.TRUE, t.getIntBool());
        assertEquals(Boolean.TRUE, t.getStringBool());
        assertEquals(Boolean.TRUE, t.getYnBool());

        dba.update("T_BOOL")
                .set("int_bool", false)
                .set("string_bool", new BoolFieldMapper(), false)
                .set("yn_bool", new BoolYN(), false)
                .where("id", "1")
                .execute();
        map = dba.select().from("T_BOOL").where("ID", "1").queryForMap();
        assertEquals(0, map.get("int_bool"));
        assertEquals("0", map.get("string_bool"));
        assertEquals("N", map.get("yn_bool"));

        t = dba.selectByKey(TBool.class, "1");
        assertEquals(Boolean.FALSE, t.getIntBool());
        assertEquals(Boolean.FALSE, t.getStringBool());
        assertEquals(Boolean.FALSE, t.getYnBool());

    }

    @Test
    public void testQuery() {
        TBool t = new TBool();
        t.setId("1");
        t.setIntBool(true);
        t.setStringBool(false);
        dba.insert(t);

        t = dba.select().from("T_BOOL")
                .where("INT_BOOL", true)
                .and("STRING_BOOL", false)
                .queryForObject(TBool.class);
        assertEquals(Boolean.TRUE, t.getIntBool());
        assertEquals(Boolean.FALSE, t.getStringBool());
    }
}