package io.github.jinghui70.rainbow.dbaccess.object;

import cn.hutool.core.map.MapUtil;
import io.github.jinghui70.rainbow.dbaccess.BaseTest;
import io.github.jinghui70.rainbow.dbaccess.DbaConfig;
import io.github.jinghui70.rainbow.dbaccess.PageData;
import io.github.jinghui70.rainbow.dbaccess.Sql;
import io.github.jinghui70.rainbow.dbaccess.cnd.Op;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ObjectTest extends BaseTest {

    @BeforeEach
    void init() {
        DbaConfig.initTable(dba, "SIMPLE_OBJECT");
    }

    private List<SimpleObject> list() {
        List<SimpleObject> list = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            Double[] score = new Double[3];
            score[0] = i * 10.0;
            score[1] = i * 10.0 + 1;
            score[2] = i * 10.0 + 2;
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
        o.setScore(new Double[]{100.0, 110.0});
        dba.update(o);
        o = dba.select(SimpleObject.class).where("ID", 27).queryForObject();
        assertEquals("oldTom", o.getName());
        assertEquals(100, o.getScore()[0]);
        assertEquals(110, o.getScore()[1]);
        assertNull(o.getScore()[2]);
    }

    @Test
    public void testPageQuery() {
        dba.insert(list());
        PageData<SimpleObject> data = dba.select(SimpleObject.class).orderBy("ID").pageQuery(2, 2);
        assertEquals(10, data.getTotal());
        assertEquals(2, data.getRows().size());

        SimpleObject o = data.getRows().get(0);
        assertEquals(3, o.getId());
        assertEquals("name3", o.getName());
        assertEquals(30, o.getScore()[0]);
        assertEquals(31, o.getScore()[1]);
        assertEquals(32, o.getScore()[2]);

        data = dba.select(SimpleObject.class).pageQuery(2, 2);
        assertEquals(10, data.getTotal());
        assertEquals(2, data.getRows().size());

        data = dba.select(SimpleObject.class)
                .where("id", Op.IN, new Sql("select id from SIMPLE_OBJECT order by id"))
                .orderBy("ID")
                .pageQuery(2, 2);
        assertEquals(10, data.getTotal());
        assertEquals(2, data.getRows().size());
        assertEquals("name3", o.getName());
    }

    @Test
    public void testSelect() {
        dba.insert(list());
        SimpleObject obj = dba.selectById(SimpleObject.class, 1);
        assertEquals("name1", obj.getName());
        obj = dba.selectByKey(SimpleObject.class, 1);
        assertEquals("name1", obj.getName());

        dba.delete(obj);
        dba.deleteById(SimpleObject.class, 2);
        dba.deleteByKey(SimpleObject.class, 3);

        int count = dba.select().from("SIMPLE_OBJECT").count();
        assertEquals(7, count);
    }

    @Test
    public void testCopyInsert() {
        dba.insert(list());
        Map<String, Object> map = MapUtil.<String, Object>builder()
                .put("id", 88)
                .put("score_1", 88)
                .put("SCORE_3", 88)
                .build();
        dba.insertInto(SimpleObject.class).selectFields(map).where("id", 1).execute();
        SimpleObject obj = dba.selectById(SimpleObject.class, 88);
        assertEquals(88, obj.getId());
        assertEquals("name1", obj.getName());
        assertEquals(88, obj.getScore()[0]);
        assertEquals(11, obj.getScore()[1]);
        assertEquals(88, obj.getScore()[2]);

        obj = dba.select(SimpleObject.class, map).where("id", 1).queryForObject();
        assertEquals(88, obj.getId());
        assertEquals("name1", obj.getName());
        assertEquals(88, obj.getScore()[0]);
        assertEquals(11, obj.getScore()[1]);
        assertEquals(88, obj.getScore()[2]);
    }
}
