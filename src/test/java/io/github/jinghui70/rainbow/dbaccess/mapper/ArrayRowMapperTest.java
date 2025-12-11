package io.github.jinghui70.rainbow.dbaccess.mapper;

import io.github.jinghui70.rainbow.dbaccess.enumSupport.OrdinalEnum;
import io.github.jinghui70.rainbow.dbaccess.fieldmapper.EnumFieldMapper;
import io.github.jinghui70.rainbow.dbaccess.memory.Field;
import io.github.jinghui70.rainbow.dbaccess.memory.MemoryDba;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ArrayRowMapperTest {

    private static MemoryDba dba;

    private enum Gender implements OrdinalEnum {
        MALE, FEMALE
    }

    private record Person(String id, String name, Gender gender, int age, double salary, BigDecimal bonus,
                          LocalDateTime time) {
    }

    @BeforeAll
    public static void init() {
        dba = new MemoryDba();
        dba.createTable("person",
                Field.createKeyString("id"),
                Field.createString("name"),
                Field.createInt("gender"),
                Field.createInt("age"),
                Field.createDouble("salary"),
                Field.createMoney("bonus"),
                Field.createTimestamp("time")
        );
        Person person = new Person("1", "张三", Gender.MALE, 18, 20000.12, new BigDecimal("1000.1234"),
                LocalDateTime.of(2011, 11, 11, 11, 11, 11));
        dba.insert(person);
    }

    @Test
    public void test() {
        ObjectArrayRowMapper rowMapper = new ObjectArrayRowMapper().setFieldMapper(3, EnumFieldMapper.of(Gender.class));
        Object[] array = dba.select().from("person")
                .where("id", "1")
                .queryForObject(rowMapper);
        assertEquals(7, array.length);
        assertEquals("1", array[0]);
        assertEquals("张三", array[1]);
        assertEquals(Gender.MALE, array[2]); //
        assertEquals(18, array[3]);
        assertEquals(20000.12, array[4]);
        assertEquals(0, ((BigDecimal) array[5]).compareTo(new BigDecimal("1000.1234")));
        // 获取到的是 TimeStamp 对象
        long v = LocalDateTime.of(2011, 11, 11, 11, 11, 11)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();
        assertEquals(new Timestamp(v), array[6]);
    }

    @Test
    public void testStringArrayRowMapper() {
        String[] array = dba.select().from("person")
                .where("id", "1")
                .queryForObject(new StringArrayRowMapper());
        assertEquals(7, array.length);
        assertEquals("1", array[0]);
        assertEquals("张三", array[1]);
        assertEquals("0", array[2]);
        assertEquals("18", array[3]);
        assertEquals("20000.12", array[4]);
        assertEquals("1000.1234000000", array[5]);
        assertEquals("2011-11-11 11:11:11", array[6]);
    }
}
