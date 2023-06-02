package io.github.jinghui70.rainbow.dbaccess.dba;

import io.github.jinghui70.rainbow.dbaccess.ObjectDba;
import io.github.jinghui70.rainbow.dbaccess.memory.Field;
import io.github.jinghui70.rainbow.dbaccess.memory.MemoryDba;
import io.github.jinghui70.rainbow.dbaccess.memory.Table;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestObjectDba {

    private static MemoryDba mDba;
    private static ObjectDba<SimpleObject> oDba;

    @BeforeAll
    static void createDB() {
        mDba = new MemoryDba();
        mDba.createTable(Field.createKeyInt("ID"), Field.createString("NAME"));
        oDba = new ObjectDba<>(mDba, SimpleObject.class);
    }

    @AfterAll
    static void close() {
        mDba.close();
    }

    @BeforeEach
    public void reset() {
        mDba.deleteFrom(Table.DEFAULT).execute();
    }

    private List<SimpleObject> list() {
        List<SimpleObject> list = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            list.add(new SimpleObject(i, "name" + i));
        }
        return list;
    }

    @Test
    public void testInsert() {
        oDba.insert(Table.DEFAULT, list());
        List<SimpleObject> list = mDba.select("*").from("X").orderBy("ID").queryForList(SimpleObject.class);
        assertEquals(10, list.size());
        assertEquals(10, list.get(9).getId());
    }

    @Test
    public void testBatchInsert() {
        oDba.insert(Table.DEFAULT, list(), 3);
        List<SimpleObject> list = mDba.select("*").from(Table.DEFAULT).orderBy("ID").queryForList(SimpleObject.class);
        assertEquals(10, list.size());
        for (int i = 1; i <= 10; i++)
            assertEquals(i, list.get(i - 1).getId());
        List<Integer> ids = mDba.select("id").from(Table.DEFAULT).orderBy("ID").queryForList(Integer.class);
        for (int i = 1; i <= 10; i++)
            assertEquals(i, ids.get(i - 1));
    }

    @Test
    public void testUpdate() {
        SimpleObject o = new SimpleObject(27, "tom");
        oDba.insert(Table.DEFAULT, o);
        o.setName("oldTom");
        oDba.update(Table.DEFAULT, o);
        String name = mDba.select("name").from(Table.DEFAULT).where("ID", 27).queryForString();
        assertEquals("oldTom", name);
    }
}
