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
                g.generate(new GenerateContext(null, null, f, "PRE_", true)));
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
     * 验证用户自定义 ValueGenerator 注册为 Spring Bean 后，
     * 由容器在初始化阶段自动注册到 {@link ValueGeneratorRegistry}，
     * 不依赖任何自动配置类。
     */
    @Test
    void testSpringAutoRegister() {
        try (AnnotationConfigApplicationContext ctx =
                     new AnnotationConfigApplicationContext(SpringGenConfig.class)) {
            // context refresh 过程中 bean 初始化即自注册，无需手动触发
            ValueGenerator g = ValueGeneratorRegistry.get("spring-gen");
            assertNotNull(g);
            assertEquals("SPRING", g.generate(new GenerateContext(null, null, null, "", true)));
        }
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

    /**
     * 测试 INSERT_UPDATE 策略：被 include 过滤掉的字段仍然参与更新。
     * <p>
     * 更新时间这类字段无论更新哪些业务字段都应刷新，因此不受字段过滤影响。
     */
    @Test
    void testInsertUpdateFieldBypassesInclude() throws InterruptedException {
        createGenTable();
        GenEntity e = new GenEntity("Grace");
        dba.insert(e);
        LocalDateTime insertUpdateTime = e.getUpdateTime();
        Thread.sleep(2);

        // include 只更新 name，updateTime 不在列表中，但仍应强制刷新
        e.setName("Grace Updated");
        dba.updateOf(e).include("name").execute();

        GenEntity r = dba.selectByKey(GenEntity.class, e.getId());
        assertEquals("Grace Updated", r.getName());
        assertTrue(r.getUpdateTime().isAfter(insertUpdateTime),
                "include 过滤不应跳过 INSERT_UPDATE 字段：插入时=" + insertUpdateTime
                        + ", 更新后=" + r.getUpdateTime());
    }

    /**
     * 测试 INSERT_UPDATE 策略：被 exclude 排除的字段仍然参与更新。
     */
    @Test
    void testInsertUpdateFieldBypassesExclude() throws InterruptedException {
        createGenTable();
        GenEntity e = new GenEntity("Henry");
        dba.insert(e);
        LocalDateTime insertUpdateTime = e.getUpdateTime();
        Thread.sleep(2);

        e.setName("Henry Updated");
        dba.updateOf(e).exclude("updateTime").execute();

        GenEntity r = dba.selectByKey(GenEntity.class, e.getId());
        assertEquals("Henry Updated", r.getName());
        assertTrue(r.getUpdateTime().isAfter(insertUpdateTime),
                "exclude 过滤不应跳过 INSERT_UPDATE 字段：插入时=" + insertUpdateTime
                        + ", 更新后=" + r.getUpdateTime());
    }

    /**
     * 测试 INSERT_UPDATE 策略：字段值为 null 时 excludeNull 不生效，仍然参与更新。
     */
    @Test
    void testInsertUpdateFieldBypassesExcludeNull() throws InterruptedException {
        createGenTable();
        GenEntity e = new GenEntity("Ivy");
        dba.insert(e);
        LocalDateTime insertUpdateTime = e.getUpdateTime();
        Thread.sleep(2);

        // updateTime 为 null，excludeNull 模式下普通 null 字段会被排除
        GenEntity partial = new GenEntity("Ivy Updated");
        partial.setId(e.getId());
        partial.setUpdateTime(null);
        dba.updateOf(partial).excludeNull().execute();

        GenEntity r = dba.selectByKey(GenEntity.class, e.getId());
        assertEquals("Ivy Updated", r.getName());
        assertNotNull(r.getUpdateTime(), "excludeNull 不应跳过 INSERT_UPDATE 字段");
        assertTrue(r.getUpdateTime().isAfter(insertUpdateTime),
                "插入时=" + insertUpdateTime + ", 更新后=" + r.getUpdateTime());
    }

    /**
     * DTO 模拟前端提交的表单：只声明 id 和 name 两个属性，不含 code / createDate / updateTime。
     */
    static class GenDto {
        private String id;
        private String name;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    /**
     * 测试 update(Class, Object)：DTO 只声明部分属性。
     * 声明的属性全部参与更新（值可为 null），未声明的属性不更新，INSERT_UPDATE 字段强制刷新。
     */
    @Test
    void testUpdateDtoForcesInsertUpdateField() throws InterruptedException {
        createGenTable();
        GenEntity e = new GenEntity("Jack");
        e.setCode("ORD-001");
        dba.insert(e);
        String createDateBefore = e.getCreateDate();
        LocalDateTime insertUpdateTime = e.getUpdateTime();
        Thread.sleep(2);

        // DTO 只声明 id 和 name；name 显式置 null
        GenDto dto = new GenDto();
        dto.setId(e.getId());
        int rows = dba.update(GenEntity.class, dto);

        assertEquals(1, rows);
        GenEntity r = dba.selectByKey(GenEntity.class, e.getId());
        // DTO 声明的属性值为 null，清库
        assertNull(r.getName(), "DTO 声明的属性即使为 null 也应更新");
        // DTO 未声明的属性不更新，保持原值
        assertEquals("ORD-001", r.getCode());
        assertEquals(createDateBefore, r.getCreateDate());
        // INSERT_UPDATE 字段强制刷新
        assertNotNull(r.getUpdateTime());
        assertTrue(r.getUpdateTime().isAfter(insertUpdateTime),
                "DTO 未带 updateTime 也应强制刷新：插入时=" + insertUpdateTime
                        + ", 更新后=" + r.getUpdateTime());
    }

    /**
     * 测试 update(Class, Object)：DTO 必须声明主键且值非 null，否则直接报错，
     * 避免拼出 WHERE ID = null 静默更新 0 行。
     */
    @Test
    void testUpdateDtoRequiresKey() {
        createGenTable();
        // 未声明主键属性
        assertThrows(IllegalArgumentException.class, () -> dba.update(GenEntity.class, new Object()));
        // 声明了主键但值为 null
        GenDto noKeyDto = new GenDto();
        assertThrows(IllegalArgumentException.class, () -> dba.update(GenEntity.class, noKeyDto));
    }

    /**
     * DTO 只声明主键，没有任何非主键属性。
     */
    static class KeyOnlyDto {
        private String id;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
    }

    /**
     * 测试 update(Class, Object)：DTO 与实体没有同名的非主键属性时直接报错，
     * 避免 include 为空拼出没有 SET 内容的 UPDATE。
     */
    @Test
    void testUpdateDtoWithoutMatchingPropsThrows() {
        KeyOnlyDto dto = new KeyOnlyDto();
        dto.setId("any");
        assertThrows(IllegalArgumentException.class, () -> dba.update(GenEntity.class, dto));
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
