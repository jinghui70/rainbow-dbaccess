package io.github.jinghui70.rainbow.dbaccess.object;

import io.github.jinghui70.rainbow.dbaccess.BaseTest;
import io.github.jinghui70.rainbow.dbaccess.DbaConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class AutoIncrementTest extends BaseTest {

    @BeforeEach
    void init() {
        DbaConfig.initTable(dba, "AUTO_INCREMENT_OBJECT");
    }

    private List<AutoIncrementObject> list() {
        List<AutoIncrementObject> list = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            Double[] score = new Double[3];
            score[0] = i * 10.0;
            score[1] = i * 10.0 + 1;
            score[2] = i * 10. + 2;
            list.add(new AutoIncrementObject("name" + i, score));
        }
        return list;
    }

    @Test
    public void testInsert() {
        dba.insert(list());
        List<AutoIncrementObject> list = dba.select(AutoIncrementObject.class).orderBy("ID").queryForList();
        assertEquals(10, list.size());
        AutoIncrementObject o = list.get(9);
        assertEquals("name10", o.getName());
        assertEquals(100, o.getScores()[0]);
        assertEquals(101, o.getScores()[1]);
        assertEquals(102, o.getScores()[2]);
    }

    @Test
    public void testBatchInsert() {
        dba.insert(list(), 3);
        List<AutoIncrementObject> list = dba.select(AutoIncrementObject.class).orderBy("ID").queryForList();
        assertEquals(10, list.size());
        for (int i = 1; i <= 10; i++)
            assertEquals(i, list.get(i - 1).getId());
        List<Integer> ids = dba.select("id").from(AutoIncrementObject.class).orderBy("ID").queryForList(Integer.class);
        for (int i = 1; i <= 10; i++)
            assertEquals(i, ids.get(i - 1));
    }

    @Test
    public void testUpdate() {
        AutoIncrementObject o = new AutoIncrementObject("tom", null);
        dba.insert(o);
        o = dba.select(AutoIncrementObject.class).where("NAME", "tom").queryForObject();
        o.setName("oldTom");
        o.setScores(new Double[]{100.0, 110.0});
        dba.update(o);
        o = dba.select(AutoIncrementObject.class).where("ID", o.getId()).queryForObject();
        assertEquals("oldTom", o.getName());
        assertEquals(100, o.getScores()[0]);
        assertEquals(110, o.getScores()[1]);
        assertNull(o.getScores()[2]);
    }
}
