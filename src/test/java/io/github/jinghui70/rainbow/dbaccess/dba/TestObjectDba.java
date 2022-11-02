package io.github.jinghui70.rainbow.dbaccess.dba;

import io.github.jinghui70.rainbow.dbaccess.ObjectDba;
import io.github.jinghui70.rainbow.dbaccess.memory.Field;
import io.github.jinghui70.rainbow.dbaccess.memory.MemoryDba;
import io.github.jinghui70.rainbow.dbaccess.memory.Table;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestObjectDba {

    @Test
    public void test() {
        List<SimpleObject> list = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            list.add(new SimpleObject(i, "name" + i));
        }
        try (MemoryDba mDba = new MemoryDba()) {
            mDba.createTable(Field.createKeyInt("ID"), Field.createString("NAME"));
            ObjectDba<SimpleObject> oDba = new ObjectDba<>(mDba, SimpleObject.class);
            oDba.insert(Table.DEFAULT, list);
            list = mDba.select("*").from("X").orderBy("ID").queryForList(SimpleObject.class);
            assertEquals(10, list.size());
            assertEquals(10, list.get(9).getId());

            mDba.deleteFrom(Table.DEFAULT).execute();
            oDba.insert(Table.DEFAULT, list, 3);
            list = mDba.select("*").from("X").orderBy("ID").queryForList(SimpleObject.class);
            assertEquals(10, list.size());
            for (int i = 1; i <= 10; i++)
                assertEquals(i, list.get(i - 1).getId());
        }
    }
}
