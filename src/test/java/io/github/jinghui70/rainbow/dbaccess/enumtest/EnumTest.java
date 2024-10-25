package io.github.jinghui70.rainbow.dbaccess.enumtest;

import io.github.jinghui70.rainbow.dbaccess.BaseTest;
import io.github.jinghui70.rainbow.dbaccess.DbaConfig;
import io.github.jinghui70.rainbow.dbaccess.cnd.Op;
import io.github.jinghui70.rainbow.dbaccess.enumSupport.EnumMapper;
import io.github.jinghui70.rainbow.dbaccess.mapper.MapRowMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class EnumTest extends BaseTest {

    @BeforeEach
    void init() {
        DbaConfig.initTable(dba, "T_ENUM");
    }

    @Test
    public void test() {
        TEnum t = new TEnum();
        t.setId(1);
        t.setNormalEnum(MyEnum.NORMAL);
        t.setCodeEnum(MyCode.A);
        t.setNumberEnum(MyNumber.ONE);
        t.setE(new MyEnum[]{MyEnum.NORMAL, MyEnum.LOCKED, MyEnum.DELETED});
        dba.insert(t);

        t = dba.select("*").from("T_ENUM").queryForObject(TEnum.class);
        assertEquals(MyEnum.NORMAL, t.getNormalEnum());
        assertEquals(MyCode.A, t.getCodeEnum());
        assertEquals(MyNumber.ONE, t.getNumberEnum());
        assertArrayEquals(new MyEnum[]{MyEnum.NORMAL, MyEnum.LOCKED, MyEnum.DELETED}, t.getE());

        Map<String, Object> map = dba.select("*").from("T_ENUM").queryForMap();
        assertEquals(MyEnum.NORMAL.name(), map.get("Normal_Enum"));
        assertEquals(MyCode.A.code(), map.get("code_enum"));
        assertEquals(MyNumber.ONE.ordinal(), map.get("number_enum"));
        assertEquals(MyEnum.NORMAL.name(), map.get("e_1"));
        assertEquals(MyEnum.LOCKED.name(), map.get("e_2"));
        assertEquals(MyEnum.DELETED.name(), map.get("e_3"));

        map = dba.select("*").from("T_ENUM")
                .where("NORMAL_ENUM", MyEnum.NORMAL)
                .and("CODE_ENUM", MyCode.A)
                .and("NUMBER_ENUM", MyNumber.ONE)
                .queryForMap();
        assertEquals(MyEnum.NORMAL.name(), map.get("e_1"));
        assertEquals(MyEnum.LOCKED.name(), map.get("e_2"));
        assertEquals(MyEnum.DELETED.name(), map.get("e_3"));
    }

    @Test
    public void testNull() {
        TEnum t = new TEnum();
        t.setId(1);
        dba.insert(t);
        t = dba.selectById(TEnum.class, 1);
        assertNull(t.getNumberEnum());
        assertNull(t.getCodeEnum());
        assertNull(t.getNormalEnum());

        t.setNumberEnum(MyNumber.ONE);
        dba.update(t);
        t = dba.selectById(TEnum.class, 1);
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
                .setFieldMapper(1, new EnumMapper<>(MyCode.class))
                .setFieldMapper("NUMBER_ENUM", new EnumMapper<>(MyNumber.class))
                .setFieldMapper(3, new EnumMapper<>(MyEnum.class));

        Map<String, Object> map = dba.select("CODE_ENUM,NUMBER_ENUM,NORMAL_ENUM").from("T_ENUM")
                .where("ID", 2)
                .queryForObject(mapper);
        assertEquals(MyCode.A, map.get("CODE_ENUM"));
        assertEquals(MyNumber.TWO, map.get("NUMBER_ENUM"));
        assertEquals(MyEnum.LOCKED, map.get("NORMAL_ENUM"));

        // 测试在 IN 条件中的设值
        t = dba.select(TEnum.class).where("NORMAL_ENUM", MyEnum.values())
                .queryForObject();
        assertEquals(2, t.getId());

        t = dba.select(TEnum.class).where("NUMBER_ENUM", MyNumber.values())
                .queryForObject();
        assertEquals(2, t.getId());

        t = dba.select(TEnum.class).where("CODE_ENUM", Op.IN, MyCode.values())
                .where("id", 2).queryForObject();
        assertEquals(MyCode.A, t.getCodeEnum());
    }
}
