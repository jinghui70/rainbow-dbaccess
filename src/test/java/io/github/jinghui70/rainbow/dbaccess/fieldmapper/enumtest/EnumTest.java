package io.github.jinghui70.rainbow.dbaccess.fieldmapper.enumtest;

import io.github.jinghui70.rainbow.dbaccess.BaseTest;
import io.github.jinghui70.rainbow.dbaccess.DbaTestUtil;
import io.github.jinghui70.rainbow.dbaccess.cnd.Op;
import io.github.jinghui70.rainbow.dbaccess.fieldmapper.EnumFieldMapper;
import io.github.jinghui70.rainbow.dbaccess.mapper.MapRowMapper;
import io.github.jinghui70.rainbow.dbaccess.memory.Field;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;
import java.util.Map;

import static io.github.jinghui70.rainbow.dbaccess.StrConst.ID;
import static io.github.jinghui70.rainbow.dbaccess.StrConst.NAME;
import static org.junit.jupiter.api.Assertions.*;

public class EnumTest extends BaseTest {

    @BeforeEach
    void init() {
        dba.sql("DROP TABLE IF EXISTS T_ENUM").execute();
        dba.createTable("T_ENUM",
                Field.createKeyInt(ID),
                Field.createInt("NUMBER_ENUM"),
                Field.createString("CODE_ENUM", 50),
                Field.createString("NORMAL_ENUM", 50)
        );
    }

    @Test
    public void test() {
        TEnum t = new TEnum();
        t.setId(1);
        t.setNormalEnum(MyEnum.NORMAL);
        t.setCodeEnum(MyCode.A);
        t.setNumberEnum(MyNumber.ONE);
        dba.insert(t);

        t = dba.select("*").from("T_ENUM").queryForObject(TEnum.class);
        assertEquals(MyEnum.NORMAL, t.getNormalEnum());
        assertEquals(MyCode.A, t.getCodeEnum());
        assertEquals(MyNumber.ONE, t.getNumberEnum());

        Map<String, Object> map = dba.select("*").from("T_ENUM").queryForMap();
        assertEquals(MyEnum.NORMAL.name(), map.get("Normal_Enum"));
        assertEquals(MyCode.A.code(), map.get("code_enum"));
        assertEquals(MyNumber.ONE.ordinal(), map.get("number_enum"));

        map = dba.select("*").from("T_ENUM")
                .where("NORMAL_ENUM", MyEnum.NORMAL)
                .and("CODE_ENUM", MyCode.A)
                .and("NUMBER_ENUM", MyNumber.ONE)
                .queryForMap();
    }

    @Test
    public void testNull() {
        TEnum t = new TEnum();
        t.setId(1);
        dba.insert(t);
        t = dba.selectByKey(TEnum.class, 1);
        assertNull(t.getNumberEnum());
        assertNull(t.getCodeEnum());
        assertNull(t.getNormalEnum());

        t.setNumberEnum(MyNumber.ONE);
        dba.update(t);
        t = dba.selectByKey(TEnum.class, 1);
        assertEquals(MyNumber.ONE, t.getNumberEnum());
    }

    @Test
    public void testQuery() {
        TEnum t = new TEnum();
        t.setId(1);
        t.setCodeEnum(MyCode.C);
        dba.insert(t);

        MyCode code = dba.select("CODE_ENUM").from("T_ENUM").where("CODE_ENUM", MyCode.C).queryForObject(MyCode.class);
        assertEquals(MyCode.C, code);

        t.setId(2);
        t.setCodeEnum(MyCode.A);
        dba.insert(t);

        List<MyCode> list = dba.select("CODE_ENUM").from("T_ENUM").orderBy("CODE_ENUM").queryForList(MyCode.class);
        assertEquals(2, list.size());
        assertEquals(MyCode.C, list.get(0)); // 丙 < 甲，因为保存的是 CODE，返回第一条记录CODE是丙
        assertEquals(MyCode.A, list.get(1)); // 甲

        t.setNumberEnum(MyNumber.TWO);
        t.setNormalEnum(MyEnum.LOCKED);
        dba.update(t);

        RowMapper<Map<String, Object>> mapper = MapRowMapper.create()
                .setFieldMapper(1, new EnumFieldMapper<>(MyCode.class))
                .setFieldMapper("NUMBER_ENUM", new EnumFieldMapper<>(MyNumber.class))
                .setFieldMapper(3, new EnumFieldMapper<>(MyEnum.class));

        Map<String, Object> map = dba.select("CODE_ENUM,NUMBER_ENUM,NORMAL_ENUM").from("T_ENUM")
                .where("ID", 2)
                .queryForObject(mapper);
        assertEquals(MyCode.A, map.get("CODE_ENUM"));
        assertEquals(MyNumber.TWO, map.get("NUMBER_ENUM"));
        assertEquals(MyEnum.LOCKED, map.get("NORMAL_ENUM"));

        // 测试在 IN 条件中的设值
        t = dba.select().from("T_ENUM").where("NORMAL_ENUM", MyEnum.values())
                .queryForObject(TEnum.class);
        assertEquals(2, t.getId());

        t = dba.select().from("T_ENUM").where("NUMBER_ENUM", MyNumber.values())
                .queryForObject(TEnum.class);
        assertEquals(2, t.getId());

        t = dba.select().from("T_ENUM").where("CODE_ENUM", Op.IN, MyCode.values())
                .where("id", 2).queryForObject(TEnum.class);
        assertEquals(MyCode.A, t.getCodeEnum());
    }
}
