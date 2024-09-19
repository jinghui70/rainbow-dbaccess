package io.github.jinghui70.rainbow.dbaccess.dba;

import io.github.jinghui70.rainbow.dbaccess.memory.Field;
import io.github.jinghui70.rainbow.dbaccess.memory.MemoryDba;
import io.github.jinghui70.rainbow.dbaccess.memory.Table;
import io.github.jinghui70.rainbow.dbaccess.object.ObjectDao;
import io.github.jinghui70.rainbow.dbaccess.objecttest.AutoIncrementObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestAutoIncrement {

    private static MemoryDba mDba;
    private static ObjectDao<AutoIncrementObject> oDba;

    @BeforeAll
    static void createDB() {
        mDba = new MemoryDba();
        mDba.createTable(Field.createKeyInt("ID").setAutoIncrement(true),
                Field.createString("NAME"),
                Field.createDouble("SCORE_1"),
                Field.createDouble("SCORE_2"),
                Field.createDouble("SCORE_3")
        );
        oDba = new ObjectDao<>(mDba, AutoIncrementObject.class);
    }

    @AfterAll
    static void close() {
        mDba.close();
    }

    @BeforeEach
    public void reset() {
        mDba.deleteFrom(Table.DEFAULT).execute();
    }

    private List<AutoIncrementObject> list() {
        List<AutoIncrementObject> list = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            double[] score = new double[3];
            score[0] = i * 10;
            score[1] = i * 10 + 1;
            score[2] = i * 10 + 2;
            list.add(new AutoIncrementObject("name" + i, score));
        }
        return list;
    }

    @Test
    public void testInsert() {
        oDba.insert(list());
        List<AutoIncrementObject> list = mDba.select("*").from("X").orderBy("ID").queryForList(AutoIncrementObject.class);
        assertEquals(10, list.size());
        AutoIncrementObject o = list.get(9);
        assertEquals("name10", o.getName());
        assertEquals(100, o.getScores()[0]);
        assertEquals(101, o.getScores()[1]);
        assertEquals(102, o.getScores()[2]);
    }

    @Test
    public void testBatchInsert() {
        oDba.insert(Table.DEFAULT, list(), 3);
        List<AutoIncrementObject> list = mDba.select("*").from(Table.DEFAULT).orderBy("ID").queryForList(AutoIncrementObject.class);
        assertEquals(10, list.size());
        for (int i = 1; i <= 10; i++)
            assertEquals(i, list.get(i - 1).getId());
        List<Integer> ids = mDba.select("id").from(Table.DEFAULT).orderBy("ID").queryForList(Integer.class);
        for (int i = 1; i <= 10; i++)
            assertEquals(i, ids.get(i - 1));
    }

    @Test
    public void testUpdate() {
        AutoIncrementObject o = new AutoIncrementObject("tom", null);
        oDba.insert(Table.DEFAULT, o);
        o = mDba.select("*").from(Table.DEFAULT).where("NAME", "tom").queryForObject(AutoIncrementObject.class);
        o.setName("oldTom");
        o.setScores(new double[]{100, 110});
        oDba.update(Table.DEFAULT, o);
        o = mDba.select("*").from(Table.DEFAULT).where("ID", o.getId()).queryForObject(AutoIncrementObject.class);
        assertEquals("oldTom", o.getName());
        assertEquals(100, o.getScores()[0]);
        assertEquals(110, o.getScores()[1]);
        assertEquals(0, o.getScores()[2]);
    }
}
