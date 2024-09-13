package io.github.jinghui70.rainbow.dbaccess.dba.lob;

import io.github.jinghui70.rainbow.dbaccess.memory.DataType;
import io.github.jinghui70.rainbow.dbaccess.memory.Field;
import io.github.jinghui70.rainbow.dbaccess.memory.MemoryDba;
import io.github.jinghui70.rainbow.dbaccess.memory.Table;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BlobFieldTest {


    private static MemoryDba dba;

    @BeforeAll
    static void createDB() {
        dba = new MemoryDba();
        dba.createTable(Field.createKeyInt("ID"),
                Field.create("CONTENT").setType(DataType.CLOB),
                Field.create("INFO").setType(DataType.BLOB)
        );
    }

    @Test
    public void testInsertObj() {
        String contentStr = "内容测试字符串";
        byte[] contentByte = contentStr.getBytes(StandardCharsets.UTF_8);
        LobObj obj = new LobObj();

        obj.setId(1);
        obj.setContent(contentByte);
        obj.setInfo(contentStr);

        dba.insert(obj);
        
        obj = dba.select("*").from(Table.DEFAULT).where("id", 1).queryForObject(LobObj.class);
        assertEquals(contentStr, new String(obj.getContent(), StandardCharsets.UTF_8));
        assertEquals(contentStr, obj.getInfo());
    }
}
