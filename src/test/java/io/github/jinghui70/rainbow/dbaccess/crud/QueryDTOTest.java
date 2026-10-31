package io.github.jinghui70.rainbow.dbaccess.crud;

import io.github.jinghui70.rainbow.dbaccess.BaseTest;
import io.github.jinghui70.rainbow.dbaccess.model.ComplexKeyEntity;
import io.github.jinghui70.rainbow.dbaccess.model.User;
import io.github.jinghui70.rainbow.dbaccess.sql.OrderBy;
import io.github.jinghui70.rainbow.dbaccess.sql.Sql;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * QueryDTO 排序组装测试。
 * <p>
 * 核心约定：主键列永远追加到排序末尾，保证分页顺序全序确定。
 */
class QueryDTOTest extends BaseTest {

    /** 取出 DTO 生成的 SQL 里 ORDER BY 之后的部分，没有 ORDER BY 时返回 null */
    private String orderByOf(QueryDTO dto) {
        Sql sql = dto.getSql(dba);
        String text = sql.toString();
        int index = text.indexOf(" ORDER BY ");
        return index < 0 ? null : text.substring(index + " ORDER BY ".length()).trim();
    }

    @Test
    void noOrderBysUsesKey() {
        QueryDTO dto = new QueryDTO().setEntity(User.class);
        assertEquals("ID", orderByOf(dto));
    }

    @Test
    void clientOrderByGetsKeyAppended() {
        QueryDTO dto = new QueryDTO().setEntity(User.class);
        dto.setOrderBys(List.of(new OrderBy("AGE", true)));
        // 原来的写法这里只有 AGE DESC，AGE 并列时分页顺序不确定
        assertEquals("AGE DESC,ID", orderByOf(dto));
    }

    @Test
    void clientOrderByOnKeyIsNotDuplicated() {
        QueryDTO dto = new QueryDTO().setEntity(User.class);
        dto.setOrderBys(List.of(new OrderBy("ID", true)));
        assertEquals("ID DESC", orderByOf(dto));
    }

    @Test
    void keyDedupIsCaseInsensitive() {
        QueryDTO dto = new QueryDTO().setEntity(User.class);
        dto.setOrderBys(List.of(new OrderBy("ID", false)));
        assertEquals("ID", orderByOf(dto));
    }

    @Test
    void defaultOrderByGetsKeyAppended() {
        QueryDTO dto = new QueryDTO().setEntity(User.class).defaultOrderBy("NAME");
        assertEquals("NAME,ID", orderByOf(dto));
    }

    @Test
    void defaultOrderByIsIgnoredWhenClientSends() {
        QueryDTO dto = new QueryDTO().setEntity(User.class).defaultOrderBy("NAME");
        dto.setOrderBys(List.of(new OrderBy("AGE", true)));
        // 客户端传了就不用默认排序，但主键兜底仍在
        assertEquals("AGE DESC,ID", orderByOf(dto));
    }

    @Test
    void defaultOrderByOnKeyIsNotDuplicated() {
        QueryDTO dto = new QueryDTO().setEntity(User.class).defaultOrderBy("ID DESC");
        assertEquals("ID DESC", orderByOf(dto));
    }

    @Test
    void setEntityDoesNotTouchDefaultOrderBys() {
        QueryDTO dto = new QueryDTO().defaultOrderBy("NAME").setEntity(User.class);
        assertEquals("NAME,ID", orderByOf(dto));
        // 反过来调用顺序结果相同
        QueryDTO other = new QueryDTO().setEntity(User.class).defaultOrderBy("NAME");
        assertEquals("NAME,ID", orderByOf(other));
    }

    @Test
    void complexKeyAppendsAllKeyColumns() {
        QueryDTO dto = new QueryDTO().setEntity(ComplexKeyEntity.class);
        assertEquals("KEY_A,KEY_B", orderByOf(dto));
    }

    @Test
    void complexKeyDedupsThePartAlreadySorted() {
        QueryDTO dto = new QueryDTO().setEntity(ComplexKeyEntity.class);
        dto.setOrderBys(List.of(new OrderBy("KEY_B", true)));
        assertEquals("KEY_B DESC,KEY_A", orderByOf(dto));
    }

    @Test
    void entityByNameHasNoKeyFallback() {
        // setEntity(String) 不知道主键，退回原有行为
        QueryDTO dto = new QueryDTO().setEntity("T_USER");
        assertNull(orderByOf(dto));
        dto.setOrderBys(List.of(new OrderBy("AGE", false)));
        assertEquals("AGE", orderByOf(dto));
    }

    @Test
    void entityByNameClearsKeyFallback() {
        // CrudService.queryPage 先 setEntity(clazz) 填好主键，派生 Service 的"写法一"
        // 再把表换成 JOIN。此时主键必须失效，否则裸 ID 在 JOIN 里有歧义
        QueryDTO dto = new QueryDTO().setEntity(User.class);
        assertEquals("ID", orderByOf(dto));
        dto.setEntity("T_USER U JOIN T_ORG O ON U.ID = O.ID");
        assertNull(orderByOf(dto));
        dto.setOrderBys(List.of(new OrderBy("U.AGE", false)));
        assertEquals("U.AGE", orderByOf(dto));
    }

    @Test
    void emptyOrderBysFallsBackToKey() {
        QueryDTO dto = new QueryDTO().setEntity(User.class);
        dto.setOrderBys(List.of());
        assertEquals("ID", orderByOf(dto));
    }

    @Test
    void illegalOrderByFieldRejected() {
        QueryDTO dto = new QueryDTO().setEntity(User.class);
        List<OrderBy> evil = List.of(new OrderBy("1 AND (SELECT COUNT(*) FROM T_USER)>0", false));
        assertThrows(IllegalArgumentException.class, () -> dto.setOrderBys(evil));
    }

    @Test
    void aliasedOrderByFieldAccepted() {
        QueryDTO dto = new QueryDTO().setEntity(User.class);
        dto.setOrderBys(List.of(new OrderBy("U.AGE", false)));
        assertEquals("U.AGE,ID", orderByOf(dto));
    }

    @Test
    void keyOrderBysFillsKeyFallbackForJoin() {
        // 裸表名/JOIN 场景：setEntity(String) 清掉兜底后，用 setKeyOrderBys 补回带前缀的唯一列
        QueryDTO dto = new QueryDTO().setEntity("T_USER U JOIN T_ORG O ON U.ID = O.ID");
        assertNull(orderByOf(dto));
        dto.setKeyOrderBys("U.ID DESC");
        assertEquals("U.ID DESC", orderByOf(dto));
    }

    @Test
    void keyOrderBysOverridesEntityKey() {
        QueryDTO dto = new QueryDTO().setEntity(User.class).setKeyOrderBys("AGE DESC", "ID");
        assertEquals("AGE DESC,ID", orderByOf(dto));
        dto.setOrderBys(List.of(new OrderBy("AGE", false)));
        // 客户端已排 AGE，兜底剩余 ID
        assertEquals("AGE,ID", orderByOf(dto));
    }

    @Test
    void keyOrderBysIsAppendedAfterDefaultOrderBy() {
        QueryDTO dto = new QueryDTO().setEntity(User.class)
                .setKeyOrderBys("AGE")
                .defaultOrderBy("NAME DESC");
        assertEquals("NAME DESC,AGE", orderByOf(dto));
    }

    @Test
    void blankKeyOrderBysIsIgnored() {
        // 空白串不能被当成排序项，否则拼出 ORDER BY 后无内容的坏 SQL
        QueryDTO dto = new QueryDTO().setEntity("T_USER U JOIN T_ORG O ON U.ID = O.ID");
        dto.setKeyOrderBys("", "O.ID DESC");
        assertEquals("O.ID DESC", orderByOf(dto));
        dto.setKeyOrderBys("   ", "O.ID");
        assertEquals("O.ID", orderByOf(dto));
    }

    @Test
    void blankKeyOrderBysAloneClearsFallback() {
        QueryDTO dto = new QueryDTO().setEntity(User.class).setKeyOrderBys("   ");
        dto.setOrderBys(List.of(new OrderBy("AGE", false)));
        assertEquals("AGE", orderByOf(dto));
    }

    @Test
    void keyOrderBysAcceptsAscSuffix() {
        QueryDTO dto = new QueryDTO().setEntity("T_USER U JOIN T_ORG O ON U.ID = O.ID");
        dto.setKeyOrderBys("O.ID asc");
        assertEquals("O.ID", orderByOf(dto));
        dto.setKeyOrderBys("O.ID DESC");
        assertEquals("O.ID DESC", orderByOf(dto));
    }

    @Test
    void emptyKeyOrderBysClearsFallback() {
        QueryDTO dto = new QueryDTO().setEntity(User.class);
        assertEquals("ID", orderByOf(dto));
        dto.setKeyOrderBys(new String[0]);
        assertNull(orderByOf(dto));
    }

    @Test
    void aliasedKeyOrderByFieldAccepted() {
        QueryDTO dto = new QueryDTO().setEntity(User.class).setKeyOrderBys("U.ID DESC", "U.AGE");
        assertEquals("U.ID DESC,U.AGE", orderByOf(dto));
    }

    @Test
    void pagingIsStableOnDuplicateSortValues() {
        createUserTable();
        // AGE 全部相同，只有主键能决定顺序
        for (int i = 1; i <= 6; i++) {
            dba.insertOf(new User("U" + i, "N" + i, 30, 1.0)).execute();
        }
        QueryDTO dto = new QueryDTO().setEntity(User.class);
        dto.setOrderBys(List.of(new OrderBy("AGE", false)));
        dto.setPageSize(2);

        dto.setPageNo(1);
        List<User> page1 = dto.queryPage(dba, User.class).getData();
        dto.setPageNo(2);
        List<User> page2 = dto.queryPage(dba, User.class).getData();
        dto.setPageNo(3);
        List<User> page3 = dto.queryPage(dba, User.class).getData();

        assertEquals(List.of("U1", "U2"), page1.stream().map(User::getId).toList());
        assertEquals(List.of("U3", "U4"), page2.stream().map(User::getId).toList());
        assertEquals(List.of("U5", "U6"), page3.stream().map(User::getId).toList());
    }
}
