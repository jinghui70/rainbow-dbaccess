package io.github.jinghui70.rainbow.dbaccess.object;

import io.github.jinghui70.rainbow.dbaccess.BaseTest;
import io.github.jinghui70.rainbow.dbaccess.memory.Field;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static io.github.jinghui70.rainbow.dbaccess.StrConst.ID;
import static io.github.jinghui70.rainbow.dbaccess.StrConst.NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class AutoIncrementTest extends BaseTest {

    @BeforeEach
    void init() {
        dba.sql("DROP TABLE IF EXISTS AUTO_INCREMENT_OBJECT").execute();
        dba.createTable("AUTO_INCREMENT_OBJECT",
                Field.createKeyInt(ID).setAutoIncrement(true),
                Field.createString(NAME),
                Field.createDouble("SCORE")
        );
    }

    private List<AutoIncrementObject> list() {
        List<AutoIncrementObject> list = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            list.add(new AutoIncrementObject("name" + i, i * 10.0));
        }
        return list;
    }

    @Test
    public void testInsert() {
        dba.insert(list());
        List<AutoIncrementObject> list = dba.select().from("AUTO_INCREMENT_OBJECT").orderBy("ID").queryForList(AutoIncrementObject.class);
        assertEquals(10, list.size());
        AutoIncrementObject o = list.get(9);
        assertEquals("name10", o.getName());
        assertEquals(100.0, o.getScore());
    }

    @Test
    public void testBatchInsert() {
        dba.insert(list(), 3);
        List<AutoIncrementObject> list = dba.select().from("AUTO_INCREMENT_OBJECT").orderBy("ID").queryForList(AutoIncrementObject.class);
        assertEquals(10, list.size());
        for (int i = 1; i <= 10; i++)
            assertEquals(i, list.get(i - 1).getId());
        List<Integer> ids = dba.select("id").from("AUTO_INCREMENT_OBJECT").orderBy("ID").queryForList(Integer.class);
        for (int i = 1; i <= 10; i++)
            assertEquals(i, ids.get(i - 1));
    }

    @Test
    public void testUpdate() {
        AutoIncrementObject o = new AutoIncrementObject("tom", null);
        dba.insert(o);
        o = dba.select().from("AUTO_INCREMENT_OBJECT").where("NAME", "tom").queryForObject(AutoIncrementObject.class);
        o.setName("oldTom");
        o.setScore(110.0);
        dba.update(o);
        o = dba.select().from("AUTO_INCREMENT_OBJECT").where("ID", o.getId()).queryForObject(AutoIncrementObject.class);
        assertEquals("oldTom", o.getName());
        assertEquals(110, o.getScore());
    }
}
