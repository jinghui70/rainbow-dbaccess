package io.github.jinghui70.rainbow.dbaccess.objecttest;

import io.github.jinghui70.rainbow.dbaccess.BaseTest;
import io.github.jinghui70.rainbow.dbaccess.DbaConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ObjectTest extends BaseTest {

    @BeforeEach
    void init() {
        DbaConfig.initTable(dba, "SIMPLE_OBJECT");
    }

    private List<SimpleObject> list() {
        List<SimpleObject> list = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            double[] score = new double[3];
            score[0] = i * 10;
            score[1] = i * 10 + 1;
            score[2] = i * 10 + 2;
            list.add(new SimpleObject(i, "name" + i, score));
        }
        return list;
    }

    @Test
    public void testInsert() {
        dba.insert(list());
        List<SimpleObject> list = dba.select(SimpleObject.class).orderBy("ID").queryForList();
        assertEquals(10, list.size());
        SimpleObject o = list.get(9);
        assertEquals(10, o.getId());
        assertEquals("name10", o.getName());
        assertEquals(100, o.getScore()[0]);
        assertEquals(101, o.getScore()[1]);
        assertEquals(102, o.getScore()[2]);
    }

    @Test
    public void testBatchInsert() {
        dba.insert(list(), 3);
        List<SimpleObject> list = dba.select(SimpleObject.class).orderBy("ID").queryForList();
        assertEquals(10, list.size());
        for (int i = 1; i <= 10; i++)
            assertEquals(i, list.get(i - 1).getId());
        List<Integer> ids = dba.select("id").from(SimpleObject.class).orderBy("ID").queryForList(Integer.class);
        for (int i = 1; i <= 10; i++)
            assertEquals(i, ids.get(i - 1));
    }

    @Test
    public void testUpdate() {
        SimpleObject o = new SimpleObject(27, "tom", null);
        dba.insert(o);
        o.setName("oldTom");
        o.setScore(new double[]{100, 110});
        dba.update(o);
        o = dba.select(SimpleObject.class).where("ID", 27).queryForObject();
        assertEquals("oldTom", o.getName());
        assertEquals(100, o.getScore()[0]);
        assertEquals(110, o.getScore()[1]);
        assertEquals(0, o.getScore()[2]);
    }

}
