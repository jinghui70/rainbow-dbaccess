package io.github.jinghui70.rainbow.dbaccess.basic;

import io.github.jinghui70.rainbow.dbaccess.memory.Field;
import io.github.jinghui70.rainbow.dbaccess.memory.MemoryDba;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.github.jinghui70.rainbow.dbaccess.StrConst.*;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class BasicTest {

    public static MemoryDba dba;

    @BeforeAll
    public static void init() {
        dba = new MemoryDba();
        dba.createTable("RECORD", Field.createInt(ID),
                Field.createString("ORG"),
                Field.createString(NAME),
                Field.createString(STATUS)
        );
    }

    @Test
    public void testGroupBy() {
        dba.insert(new Record("B", 4, "丁", "STOP"));
        dba.insert(new Record("B", 3, "丙", "STOP"));
        dba.insert(new Record("A", 2, "乙", "OK"));
        dba.insert(new Record("A", 1, "甲", "OK"));

        List<Map<String, Object>> result = dba.select("ORG", STATUS, "COUNT(*) as COUNT")
                .from(Record.class)
                .groupBy("ORG", STATUS)
                .orderBy("ORG", STATUS)
                .queryForList();

        assertEquals(2, result.size());
        Map<String, Object> map = result.get(0);
        assertEquals("A", map.get("ORG"));
        assertEquals("OK", map.get(STATUS));
        assertEquals(2L, map.get("COUNT"));

        map = result.get(1);
        assertEquals("B", map.get("ORG"));
        assertEquals("STOP", map.get(STATUS));
        assertEquals(2L, map.get("COUNT"));
    }
}
