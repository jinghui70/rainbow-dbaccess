package io.github.jinghui70.rainbow.dbaccess.crud;

import io.github.jinghui70.rainbow.dbaccess.BaseTest;
import io.github.jinghui70.rainbow.dbaccess.model.User;
import io.github.jinghui70.rainbow.dbaccess.model.UserVO;
import io.github.jinghui70.rainbow.dbaccess.sql.OrderBy;
import io.github.jinghui70.rainbow.dbaccess.sql.PageData;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CrudService.queryPage 排序参数、以及 QueryDTO.defaultOrderBy 空参数处理的测试。
 */
class CrudServiceQueryPageTest extends BaseTest {

    private CrudService<User> service() {
        return new CrudService<>(dba, User.class);
    }

    /** 建一个带分页大小的 dto（setPageSize 返回 void，不能链式） */
    private QueryDTO pageDto() {
        QueryDTO dto = new QueryDTO();
        dto.setPageNo(1);
        dto.setPageSize(10);
        return dto;
    }

    private void insertUsers() {
        createUserTable();
        dba.insert(new User("u2", "Bob", 20, 1.0));
        dba.insert(new User("u3", "Carl", 30, 2.0));
        dba.insert(new User("u1", "Alice", 40, 3.0));
    }

    // ===== queryPage 排序参数 =====

    /** 传排序字段：作为默认排序生效，主键仍兜底 */
    @Test
    void queryPageWithOrderBy() {
        insertUsers();
        PageData<User> page = service().queryPage(pageDto(), "NAME DESC");

        assertEquals(List.of("u3", "u2", "u1"),
                page.getData().stream().map(User::getId).toList());
        assertEquals(3, page.getTotal());
    }

    /** 不传排序字段：沿用 dto 上已有的设定 */
    @Test
    void queryPageWithoutOrderByKeepsDtoSetting() {
        insertUsers();
        PageData<User> page = service().queryPage(pageDto().defaultOrderBy("NAME"));

        assertEquals(List.of("u1", "u2", "u3"),
                page.getData().stream().map(User::getId).toList());
    }

    /** 客户端已传 orderBys 时，以客户端为准，方法参数不覆盖它 */
    @Test
    void clientOrderByWinsOverMethodArg() {
        insertUsers();
        QueryDTO dto = pageDto();
        dto.setOrderBys(List.of(new OrderBy("NAME", true)));

        PageData<User> page = service().queryPage(dto, "AGE");
        assertEquals(List.of("u3", "u2", "u1"),
                page.getData().stream().map(User::getId).toList());
    }

    /** 传空数组等价于不传，不影响 dto 原有排序 */
    @Test
    void emptyOrderByArgIsIgnored() {
        insertUsers();
        PageData<User> page = service().queryPage(
                pageDto().defaultOrderBy("NAME DESC"), new String[0]);

        assertEquals(List.of("u3", "u2", "u1"),
                page.getData().stream().map(User::getId).toList());
    }

    /** VO 版 queryPage 仍可用，与排序参数重载不冲突 */
    @Test
    void queryPageWithVoStillWorks() {
        insertUsers();
        PageData<UserVO> page = service().queryPage(pageDto(), UserVO.class);
        assertEquals(3, page.getTotal());
        assertEquals(List.of("Alice", "Bob", "Carl"),
                page.getData().stream().map(UserVO::getName).toList());
    }

    /** VO 版也支持排序：queryPage(dto, VO.class, "NAME DESC") */
    @Test
    void queryPageWithVoAndOrderBy() {
        insertUsers();
        PageData<UserVO> page = service().queryPage(pageDto(), UserVO.class, "NAME DESC");

        assertEquals(3, page.getTotal());
        assertEquals(List.of("Carl", "Bob", "Alice"),
                page.getData().stream().map(UserVO::getName).toList());
    }

    // ===== queryList 排序参数 =====

    /** queryList 传排序字段：作为默认排序生效 */
    @Test
    void queryListWithOrderBy() {
        insertUsers();
        assertEquals(List.of("u3", "u2", "u1"),
                service().queryList(pageDto(), "NAME DESC").stream().map(User::getId).toList());
    }

    /** queryList 不传排序字段：沿用 dto 已有设定 */
    @Test
    void queryListWithoutOrderByKeepsDtoSetting() {
        insertUsers();
        assertEquals(List.of("u1", "u2", "u3"),
                service().queryList(pageDto().defaultOrderBy("NAME")).stream().map(User::getId).toList());
    }

    /** queryList VO 版也支持排序 */
    @Test
    void queryListWithVoAndOrderBy() {
        insertUsers();
        assertEquals(List.of("Carl", "Bob", "Alice"),
                service().queryList(pageDto(), UserVO.class, "NAME DESC")
                        .stream().map(UserVO::getName).toList());
    }

    // ===== QueryDTO.defaultOrderBy 空参数 =====

    /** 空数组/空串表示清除默认排序，只留主键兜底，不能拼出 "ORDER BY ,ID" */
    @Test
    void defaultOrderByEmptyArgsFallsBackToKeyOnly() {
        QueryDTO dto = new QueryDTO().setEntity(User.class).defaultOrderBy(new String[0]);
        assertOrderBy(dto, "ID");

        dto.defaultOrderBy("");
        assertOrderBy(dto, "ID");

        dto.defaultOrderBy("   ");
        assertOrderBy(dto, "ID");
    }

    /** 空串混在有效字段里时只被忽略，其余字段照常生效 */
    @Test
    void blankEntriesAreSkipped() {
        QueryDTO dto = new QueryDTO().setEntity(User.class).defaultOrderBy("", "NAME DESC");
        assertOrderBy(dto, "NAME DESC,ID");
    }

    /** OrderBy 版空数组同样只是清除默认排序 */
    @Test
    void defaultOrderByEmptyOrderByArrayFallsBackToKeyOnly() {
        QueryDTO dto = new QueryDTO().setEntity(User.class)
                .defaultOrderBy(new OrderBy[0]);
        assertOrderBy(dto, "ID");
    }

    /** 取出 ORDER BY 之后的部分做断言 */
    private void assertOrderBy(QueryDTO dto, String expected) {
        String text = dto.getSql(dba).toString();
        int index = text.indexOf(" ORDER BY ");
        assertNotNull(index >= 0 ? text : null, "应有 ORDER BY：" + text);
        assertEquals(expected, text.substring(index + " ORDER BY ".length()).trim());
    }
}
