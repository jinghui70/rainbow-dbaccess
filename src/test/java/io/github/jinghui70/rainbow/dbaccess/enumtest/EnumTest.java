package io.github.jinghui70.rainbow.dbaccess.enumtest;

import io.github.jinghui70.rainbow.dbaccess.BaseTest;
import io.github.jinghui70.rainbow.dbaccess.DbaConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
