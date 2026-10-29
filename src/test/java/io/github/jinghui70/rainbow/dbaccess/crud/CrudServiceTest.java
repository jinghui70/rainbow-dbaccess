package io.github.jinghui70.rainbow.dbaccess.crud;

import io.github.jinghui70.rainbow.dbaccess.BaseTest;
import io.github.jinghui70.rainbow.dbaccess.model.ComplexKeyEntity;
import io.github.jinghui70.rainbow.dbaccess.model.User;
import io.github.jinghui70.rainbow.dbaccess.model.UserVO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CrudService.getByKey(Class, keys...) 测试 — 按主键查询并把结果直接映射为 VO。
 */
class CrudServiceTest extends BaseTest {

    /** 单主键 + VO 映射：只映射 VO 声明了的列，未声明的列忽略 */
    @Test
    void getByKeyWithVo() {
        createUserTable();
        dba.insert(new User("u1", "Alice", 30, 1.5));

        CrudService<User> service = new CrudService<>(dba, User.class);
        UserVO vo = service.getByKey(UserVO.class, "u1");

        assertNotNull(vo);
        assertEquals("Alice", vo.getName());
        assertEquals(30, vo.getAge());
    }

    /** 查不到返回 null，与 getByKey(Object...) 一致 */
    @Test
    void getByKeyWithVoNotFound() {
        createUserTable();
        CrudService<User> service = new CrudService<>(dba, User.class);
        assertNull(service.getByKey(UserVO.class, "notExist"));
    }

    /** 复合主键：keys 按实体 @Id 声明顺序传入，VO 映射同样生效 */
    @Test
    void getByKeyWithVoComplexKey() {
        createComplexKeyTable();
        dba.insert(new ComplexKeyEntity("A", "B", "V"));

        CrudService<ComplexKeyEntity> service = new CrudService<>(dba, ComplexKeyEntity.class);
        // VO 复用实体本身，验证复合主键能定位到行
        ComplexKeyEntity result = service.getByKey(ComplexKeyEntity.class, "A", "B");

        assertNotNull(result);
        assertEquals("V", result.getRemark());
    }

    /** 键值个数与 @Id 个数不匹配时，沿用实体主键的校验直接报错 */
    @Test
    void getByKeyWithVoArgMismatch() {
        CrudService<ComplexKeyEntity> service = new CrudService<>(dba, ComplexKeyEntity.class);
        assertThrows(IllegalArgumentException.class,
                () -> service.getByKey(ComplexKeyEntity.class, "A"));
        assertThrows(IllegalArgumentException.class,
                () -> service.getByKey(ComplexKeyEntity.class, "A", "B", "C"));
    }

    /** 旧重载不受影响：不传 voClass 时仍返回实体 */
    @Test
    void getByKeyWithoutVoStillWorks() {
        createUserTable();
        dba.insert(new User("u1", "Alice", 30, 1.5));

        CrudService<User> service = new CrudService<>(dba, User.class);
        assertEquals("Alice", service.getByKey("u1").getName());
        assertNull(service.getByKey("notExist"));
    }
}
