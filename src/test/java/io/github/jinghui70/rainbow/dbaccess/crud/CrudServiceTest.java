package io.github.jinghui70.rainbow.dbaccess.crud;

import io.github.jinghui70.rainbow.dbaccess.BaseTest;
import io.github.jinghui70.rainbow.dbaccess.model.ComplexKeyEntity;
import io.github.jinghui70.rainbow.dbaccess.model.User;
import io.github.jinghui70.rainbow.dbaccess.model.UserVO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CrudService.getByKeyAs(Class, keys...) 测试 — 按主键查询并把结果直接映射为 VO。
 */
class CrudServiceTest extends BaseTest {

    /** 单主键 + VO 映射：只映射 VO 声明了的列，未声明的列忽略 */
    @Test
    void getByKeyAsWithVo() {
        createUserTable();
        dba.insert(new User("u1", "Alice", 30, 1.5));

        CrudService<User> service = new CrudService<>(dba, User.class);
        UserVO vo = service.getByKeyAs(UserVO.class, "u1");

        assertNotNull(vo);
        assertEquals("Alice", vo.getName());
        assertEquals(30, vo.getAge());
    }

    /** 查不到返回 null，与 getByKey(Object...) 一致 */
    @Test
    void getByKeyAsVoNotFound() {
        createUserTable();
        CrudService<User> service = new CrudService<>(dba, User.class);
        assertNull(service.getByKeyAs(UserVO.class, "notExist"));
    }

    /** 复合主键：keys 按实体 @Id 声明顺序传入，VO 映射同样生效 */
    @Test
    void getByKeyAsVoComplexKey() {
        createComplexKeyTable();
        dba.insert(new ComplexKeyEntity("A", "B", "V"));

        CrudService<ComplexKeyEntity> service = new CrudService<>(dba, ComplexKeyEntity.class);
        // VO 复用实体本身，验证复合主键能定位到行
        ComplexKeyEntity result = service.getByKeyAs(ComplexKeyEntity.class, "A", "B");

        assertNotNull(result);
        assertEquals("V", result.getRemark());
    }

    /** 键值个数与 @Id 个数不匹配时，沿用实体主键的校验直接报错 */
    @Test
    void getByKeyAsVoArgMismatch() {
        CrudService<ComplexKeyEntity> service = new CrudService<>(dba, ComplexKeyEntity.class);
        assertThrows(IllegalArgumentException.class,
                () -> service.getByKeyAs(ComplexKeyEntity.class, "A"));
        assertThrows(IllegalArgumentException.class,
                () -> service.getByKeyAs(ComplexKeyEntity.class, "A", "B", "C"));
    }

    /** 实体版不受影响：不传 voClass 时仍返回实体 */
    @Test
    void getByKeyWithoutVoStillWorks() {
        createUserTable();
        dba.insert(new User("u1", "Alice", 30, 1.5));

        CrudService<User> service = new CrudService<>(dba, User.class);
        assertEquals("Alice", service.getByKey("u1").getName());
        assertNull(service.getByKey("notExist"));
    }

    /**
     * 回归保护：旧重载的参数空间与本方法不再重叠。
     * <p>
     * 改名之前 {@code getByKey(Object...)} 能吞下 {@code Class + keys} 这整段实参，
     * 当第一个参数按 {@code Object} 静态类型传入时会绑错版本，报 "argument size not match"。
     */
    @Test
    void byKeyOverloadsDoNotOverlap() {
        createUserTable();
        dba.insert(new User("u1", "Alice", 30, 1.5));

        CrudService<User> service = new CrudService<>(dba, User.class);
        // 把 Class 当 Object 静态类型传，旧签名下会绑到 getByKey(Object...)
        Object voClassAsObject = UserVO.class;
        assertThrows(IllegalArgumentException.class, () -> service.getByKey(voClassAsObject, "u1"));
        // 新名字不受静态类型影响
        UserVO vo = service.getByKeyAs(UserVO.class, "u1");
        assertEquals("Alice", vo.getName());
    }
}
