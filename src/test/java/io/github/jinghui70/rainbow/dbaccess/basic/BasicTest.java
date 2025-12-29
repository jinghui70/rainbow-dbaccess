package io.github.jinghui70.rainbow.dbaccess.basic;

import io.github.jinghui70.rainbow.dbaccess.PageData;
import io.github.jinghui70.rainbow.dbaccess.QueryParam;
import io.github.jinghui70.rainbow.dbaccess.Sql;
import io.github.jinghui70.rainbow.dbaccess.memory.Field;
import io.github.jinghui70.rainbow.dbaccess.memory.MemoryDba;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.github.jinghui70.rainbow.dbaccess.StrConst.*;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class BasicTest {

    public static final String ORG_ID = "ORG_ID";
    public static final String RECORD = "RECORD";

    public static MemoryDba dba;

    @BeforeAll
    public static void init() {
        dba = new MemoryDba();
        dba.createTable(RECORD, Field.createInt(ID),
                Field.createString(ORG_ID),
                Field.createString(NAME),
                Field.createString(STATUS)
        );
        dba.insert(new Record("B", 4, "丁", "STOP"));
        dba.insert(new Record("B", 3, "丙", "STOP"));
        dba.insert(new Record("A", 2, "乙", "OK"));
        dba.insert(new Record("A", 1, "甲", "OK"));
    }

    @Test
    public void testGroupBy() {
        List<Map<String, Object>> result = dba.select(ORG_ID, STATUS, "COUNT(*) as COUNT")
                .from(Record.class)
                .groupBy(ORG_ID, STATUS)
                .orderBy(ORG_ID, STATUS)
                .queryForList();

        assertEquals(2, result.size());
        Map<String, Object> map = result.get(0);
        assertEquals("A", map.get(ORG_ID));
        assertEquals("OK", map.get(STATUS));
        assertEquals(2L, map.get("COUNT"));

        map = result.get(1);
        assertEquals("B", map.get(ORG_ID));
        assertEquals("STOP", map.get(STATUS));
        assertEquals(2L, map.get("COUNT"));
    }

    @Test
    public void testQuery() {
        QueryParam queryParam = new QueryParam();
        queryParam.setPageNo(1);
        queryParam.setPageSize(2);

        queryParam.setEntity(Record.class);

        Sql sql = queryParam.getSql(dba);
        assertEquals("SELECT * FROM RECORD ORDER BY ORG_ID,ID", sql.getSql().toUpperCase());
        PageData<Record> pageData = queryParam.pageQuery(dba, Record.class);
        assertEquals(4, pageData.getTotal());
        assertEquals(2, pageData.getData().size());
        Record record = pageData.getData().get(1);
        assertEquals("乙", record.getName());
    }
}
