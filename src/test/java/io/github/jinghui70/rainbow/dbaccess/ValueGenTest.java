package io.github.jinghui70.rainbow.dbaccess;

import io.github.jinghui70.rainbow.dbaccess.annotation.GeneratedValue;
import io.github.jinghui70.rainbow.dbaccess.memory.Field;
import io.github.jinghui70.rainbow.dbaccess.model.GenEntity;
import io.github.jinghui70.rainbow.dbaccess.valuegen.GenerateContext;
import io.github.jinghui70.rainbow.dbaccess.valuegen.ValueGenerator;
import io.github.jinghui70.rainbow.dbaccess.valuegen.ValueGeneratorRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link GeneratedValue} 自动生成字段值功能测试。
 */
public class ValueGenTest extends BaseTest {

    @BeforeEach
    void registerCustomGenerator() {
        // 模拟用户自定义生成器（等同于注册为 Spring Bean 后的效果）
        ValueGeneratorRegistry.register(new ValueGenerator() {
            @Override
            public String getName() {
                return "test-seq";
            }

            @Override
            public Object generate(GenerateContext context) {
                return context.param() + "-001";
            }
        });
    }

    private void createGenTable() {
        dba.createTable("T_GEN",
                Field.createKeyString("ID"),
                Field.createTimestamp("CREATE_TIME"),
                Field.createString("CREATE_DATE"),
                Field.createString("CODE"),
                Field.createTimestamp("UPDATE_TIME"),
                Field.createString("NAME"));
    }

    @Test
    void testGeneratedOnInsert() {
        createGenTable();
        dba.insert(new GenEntity("Alice"));

        GenEntity r = dba.select().from("T_GEN").where("NAME", "Alice").queryForObject(GenEntity.class);
        assertNotNull(r);
        // default 策略：String 主键非空
        assertNotNull(r.getId());
        assertFalse(r.getId().isEmpty());
        // now 策略：LocalDateTime
        assertNotNull(r.getCreateTime());
        // now 策略：String yyyyMMdd
        assertNotNull(r.getCreateDate());
        assertEquals(8, r.getCreateDate().length());
        assertTrue(r.getCreateDate().chars().allMatch(Character::isDigit));
        // 自定义策略：param + "-001"
        assertEquals("ORD-001", r.getCode());
    }

    @Test
    void testGeneratedValueBackfilled() {
        createGenTable();
        GenEntity e = new GenEntity("Alice");
        dba.insert(e);

        // 生成的值应回填到入参对象本身，使 insert 后即可拿到主键等生成字段
        assertNotNull(e.getId());
        assertFalse(e.getId().isEmpty());
        assertNotNull(e.getCreateTime());
        assertEquals("ORD-001", e.getCode());

        // 回填的值与库中实际写入的值一致
        GenEntity r = dba.selectByKey(GenEntity.class, e.getId());
        assertNotNull(r);
        assertEquals(e.getCode(), r.getCode());
    }

    @Test
    void testBackfillPerRowInBatch() {
        createGenTable();
        GenEntity a = new GenEntity("Bob");
        GenEntity b = new GenEntity("Carol");
        dba.insert(java.util.List.of(a, b));

        // 批量插入时每个对象各自被回填，且主键互不相同
        assertNotNull(a.getId());
        assertNotNull(b.getId());
        assertNotEquals(a.getId(), b.getId());
    }

    @Test
    void testExistingValueNotOverwritten() {
        createGenTable();
        GenEntity e = new GenEntity("Carol");
        e.setId("FIXED_ID");
        e.setCode("MY_CODE");
        dba.insert(e);

        GenEntity r = dba.selectByKey(GenEntity.class, "FIXED_ID");
        assertNotNull(r);
        // 已有值不应被生成器覆盖
        assertEquals("FIXED_ID", r.getId());
        assertEquals("MY_CODE", r.getCode());
        // 未赋值的字段仍被生成
        assertNotNull(r.getCreateTime());
    }

    @Test
    void testUnknownStrategyThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> ValueGeneratorRegistry.get("no-such-strategy"));
    }

    @Test
    void testDefaultGeneratorStringPrefix() throws NoSuchFieldException {
        ValueGenerator g = ValueGeneratorRegistry.get("default");
        // id 是 String 字段，param 作为前缀。
        // 前缀用下划线——它不属于 36 进制 id 的字符集 [0-9A-Z]，
        // 故 startsWith 只可能来自前缀，排除 id 碰巧同字符开头的误判。
        java.lang.reflect.Field f = GenEntity.class.getDeclaredField("id");
        String s = assertInstanceOf(String.class,
                g.generate(new GenerateContext(null, null, f, "PRE_")));
        assertTrue(s.startsWith("PRE_"));
        String id = s.substring("PRE_".length());
        assertFalse(id.isEmpty(), "前缀后应有生成的 id");
        assertTrue(id.chars().allMatch(c -> Character.isDigit(c) || (c >= 'A' && c <= 'Z')),
                "id 部分应为 36 进制大写：" + id);
    }

    @Test
    void testBuiltinStrategiesLazyLoaded() {
        assertEquals("snowflake", ValueGeneratorRegistry.get("default").getName());
        assertEquals("now", ValueGeneratorRegistry.get("now").getName());
    }

    /**
     * 验证用户自定义 ValueGenerator 通过 Spring 容器被 DbaAutoConfiguration 自动注册。
     */
    @Test
    void testSpringAutoRegister() {
        try (AnnotationConfigApplicationContext ctx =
                     new AnnotationConfigApplicationContext(SpringGenConfig.class)) {
            // 模拟 DbaAutoConfiguration 启动时从容器拉取所有 ValueGenerator 注册
            new DbaAutoConfiguration(ctx.getBeanProvider(ValueGenerator.class)).afterPropertiesSet();
        }
        ValueGenerator g = ValueGeneratorRegistry.get("spring-gen");
        assertNotNull(g);
        assertEquals("SPRING", g.generate(new GenerateContext(null, null, null, "")));
    }

    /**
     * 测试 INSERT_UPDATE 策略：插入时生成值。
     */
    @Test
    void testInsertUpdateGeneratedOnInsert() {
        createGenTable();
        GenEntity e = new GenEntity("Dave");
        dba.insert(e);

        // INSERT_UPDATE 字段在插入时应被生成
        assertNotNull(e.getUpdateTime());
        GenEntity r = dba.selectByKey(GenEntity.class, e.getId());
        assertNotNull(r.getUpdateTime());
    }

    /**
     * 测试 INSERT_UPDATE 策略：更新时强制重新生成值。
     */
    @Test
    void testInsertUpdateRegeneratedOnUpdate() throws InterruptedException {
        createGenTable();
        GenEntity e = new GenEntity("Eve");
        dba.insert(e);

        LocalDateTime insertUpdateTime = e.getUpdateTime();
        assertNotNull(insertUpdateTime);

        // 等待至少 1ms 确保时间戳不同
        Thread.sleep(2);

        // 更新记录
        e.setName("Eve Updated");
        dba.update(e);

        // INSERT_UPDATE 字段应被强制重新生成，即使对象中有旧值
        LocalDateTime newUpdateTime = e.getUpdateTime();
        assertNotNull(newUpdateTime);
        assertTrue(newUpdateTime.isAfter(insertUpdateTime),
                "更新时间应被更新：插入时=" + insertUpdateTime + ", 更新后=" + newUpdateTime);

        // 验证数据库中的值与对象中回填的值一致
        GenEntity r = dba.selectByKey(GenEntity.class, e.getId());
        assertEquals(newUpdateTime, r.getUpdateTime());
        assertEquals("Eve Updated", r.getName());
    }

    /**
     * 测试 INSERT_UPDATE 策略：即使手动设置值也会被生成器覆盖。
     */
    @Test
    void testInsertUpdateOverwritesManualValue() throws InterruptedException {
        createGenTable();
        GenEntity e = new GenEntity("Frank");
        dba.insert(e);

        LocalDateTime originalUpdateTime = e.getUpdateTime();
        Thread.sleep(2);

        // 手动设置一个未来时间
        LocalDateTime manualTime = LocalDateTime.now().plusDays(1);
        e.setUpdateTime(manualTime);
        e.setName("Frank Updated");

        dba.update(e);

        // INSERT_UPDATE 策略会忽略手动设置的值，强制重新生成
        assertNotEquals(manualTime, e.getUpdateTime(),
                "INSERT_UPDATE 字段应忽略手动设置的值");
        assertTrue(e.getUpdateTime().isAfter(originalUpdateTime));
        // 生成的值应该是当前时间附近，不是未来的 manualTime
        assertTrue(e.getUpdateTime().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Configuration
    static class SpringGenConfig {
        @Bean
        ValueGenerator springGen() {
            return new ValueGenerator() {
                @Override
                public String getName() {
                    return "spring-gen";
                }

                @Override
                public Object generate(GenerateContext context) {
                    return "SPRING";
                }
            };
        }
    }
}
