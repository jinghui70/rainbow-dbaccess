package io.github.jinghui70.rainbow.dbaccess.enumfield;

import io.github.jinghui70.rainbow.dbaccess.Cnd;
import io.github.jinghui70.rainbow.dbaccess.Sql;
import io.github.jinghui70.rainbow.dbaccess.memory.Field;
import io.github.jinghui70.rainbow.dbaccess.memory.MemoryDba;
import io.github.jinghui70.rainbow.dbaccess.memory.Table;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
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

    @AfterEach
    void down() {
        mDba.close();
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
        assertEquals("0", map.get("ARM"));
        assertEquals(2, map.get("LEG"));
        assertEquals("2", map.get("FOOT"));
        x = sql.queryForObject(Person.class);
        assertEquals(Strength.强, x.getArm());
        assertEquals(Strength.弱, x.getLeg());
        assertEquals(StrengthCode.中, x.getFoot());
    }

    @Test
    public void testQuery() {
        Person x = new Person();
        x.setId(1);
        x.setArm(Strength.强);
        x.setLeg(Strength.弱);
        x.setFoot(StrengthCode.中);
        mDba.insert(x);
        x = new Person();
        x.setId(2);
        x.setArm(Strength.中);
        x.setLeg(Strength.强);
        x.setFoot(StrengthCode.弱);
        mDba.insert(x);

        x = mDba.select("*").from("PERSON").where("ARM", Strength.强).queryForObject(Person.class);
        assertEquals(1, x.getId());

        List<Person> list = mDba.select("*").from("PERSON").where("ARM", Arrays.asList(Strength.强, Strength.中)).queryForList(Person.class);
        assertEquals(2, list.size());

        x = mDba.select("*").from("PERSON").where("LEG", Strength.弱).queryForObject(Person.class);
        assertEquals(1, x.getId());

        list = mDba.select("*").from("PERSON").where("LEG", Cnd.NOT_IN, Arrays.asList(Strength.中, Strength.弱)).queryForList(Person.class);
        assertEquals(1, list.size());
        assertEquals(2, list.get(0).getId());

        x = mDba.select("*").from("PERSON").where("FOOT", StrengthCode.中).queryForObject(Person.class);
        assertEquals(1, x.getId());

        list = mDba.select("*").from("PERSON").where("FOOT", "!=", StrengthCode.强).queryForList(Person.class);
        assertEquals(2, list.size());
    }
}
