package io.github.jinghui70.rainbow.dbaccess.crud;

import io.github.jinghui70.rainbow.dbaccess.BaseTest;
import io.github.jinghui70.rainbow.dbaccess.memory.Field;
import io.github.jinghui70.rainbow.dbaccess.model.ComplexKeyEntity;
import io.github.jinghui70.rainbow.dbaccess.model.NoKeyEntity;
import io.github.jinghui70.rainbow.dbaccess.model.User;
import io.github.jinghui70.rainbow.dbaccess.model.UserVO;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CrudService.getAll 排序测试。
 * <p>
 * 规则：传了排序字段就完全用它，没传才按实体主键排序。不追加主键兜底。
 */
class CrudServiceGetAllTest extends BaseTest {

    private void insertUsers() {
        createUserTable();
        dba.insert(new User("u2", "Bob", 20, 1.0));
        dba.insert(new User("u3", "Carl", 30, 2.0));
        dba.insert(new User("u1", "Alice", 40, 3.0));
    }

    /** 不传排序字段时按主键升序（原有行为） */
    @Test
    void noArgsSortsByKey() {
        insertUsers();
        CrudService<User> service = new CrudService<>(dba, User.class);
        assertEquals(List.of("u1", "u2", "u3"),
                service.getAll().stream().map(User::getId).toList());
    }

    /** 传了排序字段就完全用它，不再按主键 */
    @Test
    void explicitOrderByReplacesKey() {
        insertUsers();
        CrudService<User> service = new CrudService<>(dba, User.class);
        assertEquals(List.of("u3", "u2", "u1"),
                service.getAll("NAME DESC").stream().map(User::getId).toList());
        assertEquals(List.of("u2", "u3", "u1"),
                service.getAll("AGE").stream().map(User::getId).toList());
    }

    /** 只有主键能决定顺序时，显式传主键排序即可 */
    @Test
    void explicitKeyOrderWorks() {
        createUserTable();
        dba.insert(new User("u3", "C", 30, 1.0));
        dba.insert(new User("u1", "A", 30, 1.0));
        dba.insert(new User("u2", "B", 30, 1.0));

        CrudService<User> service = new CrudService<>(dba, User.class);
        // 不追加主键兜底，所以 AGE 并列时顺序不确定——要稳定就显式排主键
        assertEquals(List.of("u1", "u2", "u3"),
                service.getAll("ID").stream().map(User::getId).toList());
        assertEquals(List.of("u3", "u2", "u1"),
                service.getAll("ID DESC").stream().map(User::getId).toList());
    }

    /** 多字段排序按传入顺序拼接 */
    @Test
    void multipleOrderFields() {
        insertUsers();
        CrudService<User> service = new CrudService<>(dba, User.class);
        List<User> all = service.getAll("AGE DESC", "NAME");
        assertEquals(List.of("u1", "u3", "u2"), all.stream().map(User::getId).toList());
    }

    /** 传空数组等价于不传：按主键排序 */
    @Test
    void emptyVarargsSortsByKey() {
        insertUsers();
        CrudService<User> service = new CrudService<>(dba, User.class);
        assertEquals(List.of("u1", "u2", "u3"),
                service.getAll(new String[0]).stream().map(User::getId).toList());
    }

    /** 复合主键：不传排序字段时按 KEY_A、KEY_B 声明顺序 */
    @Test
    void complexKeyDefaultOrder() {
        createComplexKeyTable();
        dba.insert(new ComplexKeyEntity("B", "2", "same"));
        dba.insert(new ComplexKeyEntity("A", "3", "same"));
        dba.insert(new ComplexKeyEntity("A", "1", "same"));

        CrudService<ComplexKeyEntity> service = new CrudService<>(dba, ComplexKeyEntity.class);
        assertEquals(List.of("A1", "A3", "B2"),
                service.getAll().stream().map(e -> e.getKeyA() + e.getKeyB()).toList());
    }

    // ===== VO 版本 =====

    /** VO 版本：不传排序字段时按主键 */
    @Test
    void voNoArgsKeepsDefaultKeyOrder() {
        insertUsers();
        CrudService<User> service = new CrudService<>(dba, User.class);
        assertEquals(List.of("Alice", "Bob", "Carl"),
                service.getAll(UserVO.class).stream().map(UserVO::getName).toList());
    }

    /** VO 版本：传了排序字段就完全用它 */
    @Test
    void voExplicitOrderByReplacesKey() {
        insertUsers();
        CrudService<User> service = new CrudService<>(dba, User.class);
        assertEquals(List.of("Carl", "Bob", "Alice"),
                service.getAll(UserVO.class, "NAME DESC").stream().map(UserVO::getName).toList());
    }

    // ===== 边界 =====

    /** 无 @Id 且没传排序字段：不能把 null 交给 orderBy 抛 NPE，应正常返回 */
    @Test
    void noKeyAndNoOrderDoesNotThrow() {
        // 表名与实体推导一致，本用例只关心"不抛 NPE"
        dba.createTable("NO_KEY_ENTITY", Field.createString("NAME"));
        dba.insertOf(new NoKeyEntity("B")).into("NO_KEY_ENTITY").execute();
        dba.insertOf(new NoKeyEntity("A")).into("NO_KEY_ENTITY").execute();

        CrudService<NoKeyEntity> service = new CrudService<>(dba, NoKeyEntity.class);
        assertDoesNotThrow(() -> service.getAll());
        assertEquals(2, service.getAll().size());
    }
}
