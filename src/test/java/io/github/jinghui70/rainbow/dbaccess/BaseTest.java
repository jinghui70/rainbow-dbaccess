package io.github.jinghui70.rainbow.dbaccess;

import io.github.jinghui70.rainbow.dbaccess.memory.Field;
import io.github.jinghui70.rainbow.dbaccess.memory.MemoryDba;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 * 测试基类 — 提供 H2 内存数据库环境。
 */
public abstract class BaseTest {

    protected MemoryDba dba;

    @BeforeEach
    void setUp() {
        dba = new MemoryDba();
    }

    @AfterEach
    void tearDown() {
        if (dba != null) {
            dba.close();
        }
    }

    /** 快捷建表：主键字符串 + 名称字段 */
    protected void createUserTable() {
        dba.createTable("T_USER",
                Field.createKeyString("ID"),
                Field.createString("NAME"),
                Field.createInt("AGE"),
                Field.createDouble("SCORE"));
    }

    /** 快捷建表：自增主键 */
    protected void createAutoTable() {
        dba.createTable("T_AUTO",
                Field.create("ID").setType(io.github.jinghui70.rainbow.dbaccess.memory.DataType.INT).setKey(true).setAutoIncrement(true),
                Field.createString("NAME"),
                Field.createDouble("SCORE"));
    }

    /** 快捷建表：枚举字段 */
    protected void createEnumTable() {
        dba.createTable("T_ENUM",
                Field.createKeyString("ID"),
                Field.createString("STATUS"),
                Field.createString("COLOR"));
    }

    /** 快捷建表：布尔字段 */
    protected void createBoolTable() {
        dba.createTable("T_BOOL",
                Field.createKeyString("ID"),
                Field.createInt("ACTIVE"),
                Field.createInt("FLAG"));
    }

    /** 快捷建表：产品表（默认表名推导） */
    protected void createProductTable() {
        dba.createTable("PRODUCT_ENTITY",
                Field.createKeyString("ID"),
                Field.createString("PRODUCT_NAME"),
                Field.createDouble("PRICE"));
    }

    /** 快捷建表：Blob 字段 */
    protected void createBlobTable() {
        dba.createTable("T_BLOB",
                Field.createKeyString("ID"),
                Field.create("LOB_STRING").setType(io.github.jinghui70.rainbow.dbaccess.memory.DataType.BLOB),
                Field.create("LOB_BYTES").setType(io.github.jinghui70.rainbow.dbaccess.memory.DataType.BLOB),
                Field.create("LOB_OBJECT").setType(io.github.jinghui70.rainbow.dbaccess.memory.DataType.BLOB));
    }

    /** 快捷建表：Object CLOB 字段 */
    protected void createObjectTable() {
        dba.createTable("T_OBJECT",
                Field.createKeyString("ID"),
                Field.create("TAGS").setType(io.github.jinghui70.rainbow.dbaccess.memory.DataType.CLOB),
                Field.create("ATTRIBUTES").setType(io.github.jinghui70.rainbow.dbaccess.memory.DataType.CLOB));
    }

    /** 快捷建表：树形组织 */
    protected void createOrgTable() {
        dba.createTable("T_ORG",
                Field.createKeyString("ID"),
                Field.createString("PID"),
                Field.createString("NAME"),
                Field.createString("CODE"));
    }

    /** 快捷建表：复合主键 */
    protected void createComplexKeyTable() {
        // VALUE 是 H2 保留字，需用引号转义
        dba.sql("CREATE TABLE T_COMPLEX_KEY (KEY_A VARCHAR(32), KEY_B VARCHAR(32), \"VALUE\" VARCHAR(32), PRIMARY KEY(KEY_A, KEY_B))").execute();
    }
}
