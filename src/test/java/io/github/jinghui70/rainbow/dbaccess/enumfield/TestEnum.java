package io.github.jinghui70.rainbow.dbaccess.enumfield;

import io.github.jinghui70.rainbow.dbaccess.Sql;
import io.github.jinghui70.rainbow.dbaccess.memory.Field;
import io.github.jinghui70.rainbow.dbaccess.memory.MemoryDba;
import io.github.jinghui70.rainbow.dbaccess.memory.Table;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestEnum {

    private MemoryDba mDba;

    @BeforeEach
    void setUp() {
        mDba = new MemoryDba();
        mDba.createTable(Table.create("Person").add(
                Field.createKeyInt("ID"),
                Field.createString("ARM", 10),
                Field.createInt("LEG"),
                Field.createString("FOOT", 10)
        ));
    }

    @Test
    public void testInsert() {
        Person x = new Person();
        x.setId(1);
        x.setArm(Strength.强);
        x.setLeg(Strength.弱);
        x.setFoot(StrengthCode.中);
        mDba.insert(x);
        Sql sql = mDba.select("*").from("PERSON").where("ID", 1);
        Map<String, Object> map = sql.queryForMap();
        assertEquals("强", map.get("ARM"));
        assertEquals(2, map.get("LEG"));
        assertEquals("2", map.get("FOOT"));
        x = sql.queryForObject(Person.class);
        assertEquals(Strength.强, x.getArm());
        assertEquals(Strength.弱, x.getLeg());
        assertEquals(StrengthCode.中, x.getFoot());
    }
}
