package io.github.jinghui70.rainbow.dbaccess;

import io.github.jinghui70.rainbow.dbaccess.cnd.Cnd;
import io.github.jinghui70.rainbow.dbaccess.cnd.Op;
import io.github.jinghui70.rainbow.dbaccess.fieldmapper.FieldMapper;
import io.github.jinghui70.rainbow.dbaccess.model.OrgNode;
import io.github.jinghui70.rainbow.dbaccess.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class SqlCoverageTest extends BaseTest {

    private void setupUsers() {
        createUserTable();
        dba.insert(new User[]{
                new User("1", "Alice", 25, 90.0),
                new User("2", "Bob", 30, 80.0),
                new User("3", "Charlie", 35, 70.0)
        });
    }

    private void setupOrg() {
        createOrgTable();
        dba.insert(new OrgNode("1", null, "Root1", "R1"));
        dba.insert(new OrgNode("2", "1", "Child1", "C1"));
        dba.insert(new OrgNode("3", "1", "Child2", "C2"));
        dba.insert(new OrgNode("4", null, "Root2", "R2"));
    }

    // ===== 构造函数与基础方法 =====

    @Test
    void testDefaultConstructor() {
        Sql sql = new Sql();
        assertTrue(sql.getParams().isEmpty());
        assertEquals("", sql.toString());
    }

    @Test
    void testStringConstructor() {
        Sql sql = new Sql("SELECT 1");
        assertEquals("SELECT 1", sql.toString());
    }

    @Test
    void testDbaConstructor() {
        Sql sql = new Sql(dba);
        assertNotNull(sql);
    }

    @Test
    void testAddParamsList() {
        setupUsers();
        List<User> list = dba.sql("SELECT * FROM T_USER WHERE AGE>? AND SCORE>?")
                .addParams(List.of(30, 60))
                .queryForList(User.class);
        assertEquals(1, list.size());
    }

    @Test
    void testSetParamsList() {
        setupUsers();
        Sql sql = dba.update(User.class).set("SCORE", 88).where("ID", "1");
        sql.execute();
        User user = dba.selectByKey(User.class, "1");
        assertEquals(88, user.getScore());

        sql.setParams(List.of(99, "2")).execute();
        user = dba.selectByKey(User.class, "2");
        assertEquals(99, user.getScore());
    }

    @Test
    void testGetSqlWithRange() {
        setupUsers();
        List<User> result = dba.select().from("T_USER").limit(1).queryForList(User.class);
        assertEquals(1, result.size());
    }

    @Test
    void testGetSqlWithRangeFromTo() {
        setupUsers();
        List<User> result = dba.select().from("T_USER").orderBy("ID")
                .range(2,3).queryForList(User.class);
        assertEquals(2, result.size());
        assertEquals("2", result.get(0).getId());
    }

    @Test
    void testAppendCnd() {
        setupUsers();
        Cnd cnd = Cnd.where("ID", "1");
        List<User> list = dba.select().from("T_USER").where(cnd).queryForList(User.class);
        assertEquals(1, list.size());
    }

    // ===== where(boolean, String) =====

    @Test
    void testWhereConditionTrueString() {
        setupUsers();
        User u = dba.select().from("T_USER").where(true, "ID='1'").queryForObject(User.class);
        assertNotNull(u);
        assertEquals("Alice", u.getName());
    }

    @Test
    void testWhereConditionFalseString() {
        setupUsers();
        List<User> list = dba.select().from("T_USER").where(false, "ID='1'").queryForList(User.class);
        assertEquals(3, list.size());
    }

    // ===== where(boolean, String, Op, Object) =====

    @Test
    void testWhereConditionTrueWithOp() {
        setupUsers();
        List<User> list = dba.select().from("T_USER")
                .where(true, "AGE", Op.GT, 28)
                .queryForList(User.class);
        assertEquals(2, list.size());
    }

    @Test
    void testWhereConditionFalseWithOp() {
        setupUsers();
        List<User> list = dba.select().from("T_USER")
                .where(false, "AGE", Op.GT, 28)
                .queryForList(User.class);
        assertEquals(3, list.size());
    }

    // ===== where(List<Cnd>) =====

    @Test
    void testWhereCndList() {
        setupUsers();
        List<Cnd> cnds = List.of(Cnd.where("AGE", Op.GT, 20), Cnd.where("AGE", Op.LT, 35));
        List<User> list = dba.select().from("T_USER").where(cnds).queryForList(User.class);
        assertEquals(2, list.size());
    }

    @Test
    void testWhereEmptyCndList() {
        setupUsers();
        List<User> list = dba.select().from("T_USER").where(new ArrayList<>()).queryForList(User.class);
        assertEquals(3, list.size());
    }

    @Test
    void testWhereNullCndList() {
        setupUsers();
        List<User> list = dba.select().from("T_USER").where((List<Cnd>) null).queryForList(User.class);
        assertEquals(3, list.size());
    }

    // ===== and(String str) =====

    @Test
    void testAndString() {
        setupUsers();
        User u = dba.select().from("T_USER").where("ID", "1").and("NAME='Alice'").queryForObject(User.class);
        assertNotNull(u);
    }

    // ===== and(Cnd) =====

    @Test
    void testAndCnd() {
        setupUsers();
        User u = dba.select().from("T_USER").where("ID", "1").and(Cnd.where("NAME", "Alice")).queryForObject(User.class);
        assertNotNull(u);
    }

    // ===== and(boolean, String) =====

    @Test
    void testAndConditionTrueString() {
        setupUsers();
        User u = dba.select().from("T_USER").where("ID", "1").and(true, "NAME='Alice'").queryForObject(User.class);
        assertNotNull(u);
    }

    @Test
    void testAndConditionFalseString() {
        setupUsers();
        User u = dba.select().from("T_USER").where("ID", "1").and(false, "NAME='XXX'").queryForObject(User.class);
        assertNotNull(u);
    }

    // ===== and(boolean, String, Op, Object) =====

    @Test
    void testAndConditionTrueWithOp() {
        setupUsers();
        List<User> list = dba.select().from("T_USER").where("AGE", Op.GT, 20).and(true, "AGE", Op.LT, 35).queryForList(User.class);
        assertEquals(2, list.size());
    }

    @Test
    void testAndConditionFalseWithOp() {
        setupUsers();
        List<User> list = dba.select().from("T_USER").where("AGE", Op.GT, 20).and(false, "AGE", Op.LT, 10).queryForList(User.class);
        assertEquals(3, list.size());
    }

    // ===== or(String str) =====

    @Test
    void testOrString() {
        setupUsers();
        List<User> list = dba.select().from("T_USER").where("ID='1'").or("ID='2'").queryForList(User.class);
        assertEquals(2, list.size());
    }

    // ===== or(String, Op, Object) =====

    @Test
    void testOrWithOp() {
        setupUsers();
        List<User> list = dba.select().from("T_USER").where("AGE", Op.LT, 26).or("AGE", Op.GT, 34).queryForList(User.class);
        assertEquals(2, list.size());
    }

    // ===== or(boolean, String, Object) =====

    @Test
    void testOrConditionTrue() {
        setupUsers();
        List<User> list = dba.select().from("T_USER").where("ID", "1").or(true, "ID", "2").queryForList(User.class);
        assertEquals(2, list.size());
    }

    @Test
    void testOrConditionFalse() {
        setupUsers();
        List<User> list = dba.select().from("T_USER").where("ID", "1").or(false, "ID", "2").queryForList(User.class);
        assertEquals(1, list.size());
    }

    // ===== or(boolean, String, Op, Object) =====

    @Test
    void testOrConditionTrueWithOp() {
        setupUsers();
        List<User> list = dba.select().from("T_USER").where("AGE", Op.LT, 26).or(true, "AGE", Op.GT, 34).queryForList(User.class);
        assertEquals(2, list.size());
    }

    @Test
    void testOrConditionFalseWithOp() {
        setupUsers();
        List<User> list = dba.select().from("T_USER").where("AGE", Op.LT, 26).or(false, "AGE", Op.GT, 34).queryForList(User.class);
        assertEquals(1, list.size());
    }

    // ===== or(boolean, Supplier) =====

    @Test
    void testOrConditionTrueSupplier() {
        setupUsers();
        List<User> list = dba.select().from("T_USER").where("ID", "1").or(true, () -> Cnd.where("ID", "2")).queryForList(User.class);
        assertEquals(2, list.size());
    }

    @Test
    void testOrConditionFalseSupplier() {
        setupUsers();
        List<User> list = dba.select().from("T_USER").where("ID", "1").or(false, () -> Cnd.where("ID", "2")).queryForList(User.class);
        assertEquals(1, list.size());
    }

    // ===== queryForValueOptional(FieldMapper) =====

    @Test
    void testQueryForValueWithFieldMapper() {
        setupUsers();
        FieldMapper<String> upperMapper = new FieldMapper<>() {
            @Override
            public String formDB(ResultSet rs, int index) throws SQLException {
                String val = rs.getString(index);
                return val == null ? null : val.toUpperCase();
            }
        };
        String name = dba.select("NAME").from("T_USER").where("ID", "1").queryForValue(upperMapper);
        assertEquals("ALICE", name);
    }

    @Test
    void testQueryForValueOptionalWithFieldMapper() {
        setupUsers();
        FieldMapper<String> upperMapper = new FieldMapper<>() {
            @Override
            public String formDB(ResultSet rs, int index) throws SQLException {
                String val = rs.getString(index);
                return val == null ? null : val.toUpperCase();
            }
        };
        Optional<String> name = dba.select("NAME").from("T_USER").where("ID", "1").queryForValueOptional(upperMapper);
        assertTrue(name.isPresent());
        assertEquals("ALICE", name.get());

        Optional<String> empty = dba.select("NAME").from("T_USER").where("ID", "999").queryForValueOptional(upperMapper);
        assertFalse(empty.isPresent());
    }

    // ===== queryForDoubleOptional / queryForIntOptional / queryForStringOptional =====

    @Test
    void testQueryForDoubleOptional() {
        setupUsers();
        Optional<Double> score = dba.select("SCORE").from("T_USER").where("ID", "1").queryForDoubleOptional();
        assertTrue(score.isPresent());
        assertEquals(90.0, score.get(), 0.01);

        Optional<Double> empty = dba.select("SCORE").from("T_USER").where("ID", "999").queryForDoubleOptional();
        assertFalse(empty.isPresent());
    }

    @Test
    void testQueryForIntOptional() {
        setupUsers();
        Optional<Integer> age = dba.select("AGE").from("T_USER").where("ID", "1").queryForIntOptional();
        assertTrue(age.isPresent());
        assertEquals(25, age.get());

        Optional<Integer> empty = dba.select("AGE").from("T_USER").where("ID", "999").queryForIntOptional();
        assertFalse(empty.isPresent());
    }

    @Test
    void testQueryForStringOptional() {
        setupUsers();
        Optional<String> name = dba.select("NAME").from("T_USER").where("ID", "1").queryForStringOptional();
        assertTrue(name.isPresent());
        assertEquals("Alice", name.get());
    }

    // ===== queryForDate / queryForDateOptional =====

    @Test
    void testQueryForDate() {
        createUserTable();
        LocalDate d = dba.select("AGE").from("T_USER").where("ID", "999").queryForDate();
        assertNull(d);
    }

    @Test
    void testQueryForDateOptional() {
        createUserTable();
        Optional<LocalDate> d = dba.select("AGE").from("T_USER").where("ID", "999").queryForDateOptional();
        assertFalse(d.isPresent());
    }

    // ===== queryForObject(Class) simple type branch =====

    @Test
    void testQueryForObjectSimpleType() {
        setupUsers();
        String name = dba.select("NAME").from("T_USER").where("ID", "1").queryForObject(String.class);
        assertEquals("Alice", name);
    }

    @Test
    void testQueryForObjectSimpleTypeNotFound() {
        setupUsers();
        String name = dba.select("NAME").from("T_USER").where("ID", "999").queryForObject(String.class);
        assertNull(name);
    }

    // ===== queryForObjectOptional(Class) simple type branch =====

    @Test
    void testQueryForObjectOptionalSimpleType() {
        setupUsers();
        Optional<String> name = dba.select("NAME").from("T_USER").where("ID", "1").queryForObjectOptional(String.class);
        assertTrue(name.isPresent());
        assertEquals("Alice", name.get());

        Optional<String> empty = dba.select("NAME").from("T_USER").where("ID", "999").queryForObjectOptional(String.class);
        assertFalse(empty.isPresent());
    }

    // ===== queryForObject(RowMapper) with params =====

    @Test
    void testQueryForObjectRowMapperWithParams() {
        setupUsers();
        User u = dba.select().from("T_USER").where("ID", "1").queryForObject(User.class);
        assertNotNull(u);
        assertEquals("Alice", u.getName());
    }

    // ===== query(RowCallbackHandler) with params =====

    @Test
    void testQueryRowCallbackHandlerWithParams() {
        setupUsers();
        List<String> names = new ArrayList<>();
        dba.select("NAME").from("T_USER").where("AGE", Op.GT, 20).query(rs -> {
            names.add(rs.getString("NAME"));
        });
        assertEquals(3, names.size());
    }

    @Test
    void testQueryRowCallbackHandlerWithoutParams() {
        setupUsers();
        List<String> names = new ArrayList<>();
        dba.select("NAME").from("T_USER").query(rs -> {
            names.add(rs.getString("NAME"));
        });
        assertEquals(3, names.size());
    }

    // ===== query(ResultSetExtractor) =====

    @Test
    void testQueryResultSetExtractor() {
        setupUsers();
        List<String> names = dba.select("NAME").from("T_USER").query(rs -> {
            List<String> result = new ArrayList<>();
            while (rs.next()) {
                result.add(rs.getString("NAME"));
            }
            return result;
        });
        assertEquals(3, names.size());
    }

    @Test
    void testQueryResultSetExtractorWithParams() {
        setupUsers();
        List<String> names = dba.select("NAME").from("T_USER").where("AGE", Op.GT, 28).query(rs -> {
            List<String> result = new ArrayList<>();
            while (rs.next()) {
                result.add(rs.getString("NAME"));
            }
            return result;
        });
        assertEquals(2, names.size());
    }

    // ===== queryForList() no-arg (Map list) =====

    @Test
    void testQueryForListNoArg() {
        setupUsers();
        List<Map<String, Object>> list = dba.select().from("T_USER").queryForList();
        assertEquals(3, list.size());
        assertNotNull(list.get(0).get("NAME"));
    }

    // ===== queryForList(FieldMapper) =====

    @Test
    void testQueryForListFieldMapper() {
        setupUsers();
        FieldMapper<String> upperMapper = new FieldMapper<>() {
            @Override
            public String formDB(ResultSet rs, int index) throws SQLException {
                String val = rs.getString(index);
                return val == null ? null : val.toUpperCase();
            }
        };
        List<String> names = dba.select("NAME").from("T_USER").queryForList(upperMapper);
        assertEquals(3, names.size());
        assertTrue(names.contains("ALICE"));
    }

    // ===== queryForList(RowMapper) =====

    @Test
    void testQueryForListRowMapper() {
        setupUsers();
        RowMapper<User> mapper = (rs, rowNum) -> {
            User u = new User();
            u.setId(rs.getString("ID"));
            u.setName(rs.getString("NAME"));
            return u;
        };
        List<User> list = dba.select().from("T_USER").queryForList(mapper);
        assertEquals(3, list.size());
    }

    // ===== queryForObjectOptional(RowMapper) =====

    @Test
    void testQueryForObjectOptionalRowMapper() {
        setupUsers();
        RowMapper<User> mapper = (rs, rowNum) -> {
            User u = new User();
            u.setId(rs.getString("ID"));
            u.setName(rs.getString("NAME"));
            return u;
        };
        Optional<User> u = dba.select().from("T_USER").where("ID", "1").queryForObjectOptional(mapper);
        assertTrue(u.isPresent());
        assertEquals("Alice", u.get().getName());

        Optional<User> empty = dba.select().from("T_USER").where("ID", "999").queryForObjectOptional(mapper);
        assertFalse(empty.isPresent());
    }

    // ===== count with DISTINCT (optimizeInfo == null branch) =====

    @Test
    void testCountWithDistinct() {
        setupUsers();
        int c = dba.select("DISTINCT NAME").from("T_USER").count();
        assertEquals(3, c);
    }

    @Test
    void testCountWithUnion() {
        setupUsers();
        Sql sql = dba.sql("SELECT * FROM T_USER WHERE ID='1' UNION SELECT * FROM T_USER WHERE ID='2'");
        int c = sql.count();
        assertEquals(2, c);
    }

    // ===== exist with optimizeInfo == null (DISTINCT/GROUP BY/UNION) =====

    @Test
    void testExistWithDistinct() {
        setupUsers();
        assertTrue(dba.select("DISTINCT NAME").from("T_USER").where("ID", "1").exist());
        assertFalse(dba.select("DISTINCT NAME").from("T_USER").where("ID", "999").exist());
    }

    @Test
    void testExistWithGroupBy() {
        setupUsers();
        assertTrue(dba.select("NAME").from("T_USER").groupBy("NAME").exist());
    }

    // ===== pageQuery(int, int) Map 版本 =====

    @Test
    void testPageQueryMapVersion() {
        setupUsers();
        PageData<Map<String, Object>> page = dba.select().from("T_USER").orderBy("ID").pageQuery(1, 2);
        assertEquals(3, page.getTotal());
        assertEquals(2, page.getData().size());
    }

    // ===== pageQuery(Class, int, int) simple type branch =====

    @Test
    void testPageQuerySimpleType() {
        setupUsers();
        PageData<String> page = dba.select("NAME").from("T_USER").orderBy("NAME").pageQuery(String.class, 1, 2);
        assertEquals(3, page.getTotal());
        assertEquals(2, page.getData().size());
    }

    // ===== pageQuery non-first page =====

    @Test
    void testPageQuerySecondPage() {
        setupUsers();
        PageData<User> page = dba.select().from("T_USER").orderBy("ID").pageQuery(User.class, 2, 2);
        assertEquals(3, page.getTotal());
        assertEquals(1, page.getData().size());
    }

    // ===== pageQuery beyond range =====

    @Test
    void testPageQueryBeyondRange() {
        setupUsers();
        PageData<User> page = dba.select().from("T_USER").orderBy("ID").pageQuery(User.class, 10, 2);
        assertEquals(3, page.getTotal());
        assertTrue(page.getData().isEmpty());
    }

    // ===== queryToMap(keyFunc, valueFunc) =====

    @Test
    void testQueryToMapKeyValueFunc() {
        setupUsers();
        Map<String, String> map = dba.select().from("T_USER")
                .queryToMap(rs -> rs.getString("ID"), rs -> rs.getString("NAME"));
        assertEquals(3, map.size());
        assertEquals("Alice", map.get("1"));
    }

    // ===== queryToMap(keyFunc, RowMapper) =====

    @Test
    void testQueryToMapKeyRowMapper() {
        setupUsers();
        RowMapper<User> mapper = (rs, rowNum) -> {
            User u = new User();
            u.setId(rs.getString("ID"));
            u.setName(rs.getString("NAME"));
            return u;
        };
        Map<String, User> map = dba.select().from("T_USER").queryToMap(rs -> rs.getString("ID"), mapper);
        assertEquals(3, map.size());
        assertEquals("Alice", map.get("1").getName());
    }

    // ===== queryToMap(keyFunc, RowMapper, Supplier) =====

    @Test
    void testQueryToMapKeyRowMapperSupplier() {
        setupUsers();
        RowMapper<User> mapper = (rs, rowNum) -> {
            User u = new User();
            u.setId(rs.getString("ID"));
            u.setName(rs.getString("NAME"));
            return u;
        };
        LinkedHashMap<String, User> map = (LinkedHashMap<String, User>) dba.select().from("T_USER")
                .queryToMap(rs -> rs.getString("ID"), mapper, LinkedHashMap::new);
        assertEquals(3, map.size());
        assertTrue(map instanceof LinkedHashMap);
    }

    // ===== queryToMap(keyFunc, Class, Supplier) =====

    @Test
    void testQueryToMapKeyClassSupplier() {
        setupUsers();
        LinkedHashMap<String, User> map = (LinkedHashMap<String, User>) dba.select().from("T_USER")
                .queryToMap(rs -> rs.getString("ID"), User.class, LinkedHashMap::new);
        assertEquals(3, map.size());
        assertTrue(map instanceof LinkedHashMap);
    }

    // ===== queryToMap(keyFunc) Map<String, Map> =====

    @Test
    void testQueryToMapKeyOnly() {
        setupUsers();
        Map<String, Map<String, Object>> map = dba.select().from("T_USER").queryToMap(rs -> rs.getString("ID"));
        assertEquals(3, map.size());
        assertNotNull(map.get("1"));
        assertEquals("Alice", map.get("1").get("NAME"));
    }

    // ===== queryToGroup(keyFunc, RowMapper) =====

    @Test
    void testQueryToGroupKeyRowMapper() {
        setupUsers();
        RowMapper<User> mapper = (rs, rowNum) -> {
            User u = new User();
            u.setId(rs.getString("ID"));
            u.setName(rs.getString("NAME"));
            return u;
        };
        Map<String, List<User>> groups = dba.select().from("T_USER")
                .queryToGroup(rs -> rs.getString("NAME"), mapper);
        assertEquals(3, groups.size());
    }

    // ===== queryToGroup(keyFunc, valueFunc) =====

    @Test
    void testQueryToGroupKeyValueFunc() {
        setupUsers();
        Map<String, List<String>> groups = dba.select().from("T_USER")
                .queryToGroup(rs -> rs.getString("ID"), rs -> rs.getString("NAME"));
        assertEquals(3, groups.size());
        assertEquals(List.of("Alice"), groups.get("1"));
    }

    // ===== queryToGroup(keyFunc) Map<String, List<Map>> =====

    @Test
    void testQueryToGroupKeyOnly() {
        setupUsers();
        Map<String, List<Map<String, Object>>> groups = dba.select().from("T_USER")
                .queryToGroup(rs -> rs.getString("ID"));
        assertEquals(3, groups.size());
        assertEquals(1, groups.get("1").size());
    }

    // ===== queryForTree =====

    @Test
    void testQueryForTreeWithClass() {
        setupOrg();
        var tree = dba.select().from("T_ORG").queryForTree(OrgNode.class);
        assertNotNull(tree);
        assertEquals(2, tree.getRoots().size());
    }

    @Test
    void testQueryForTreeWithRowMapper() {
        setupOrg();
        RowMapper<OrgNode> mapper = (rs, rowNum) -> {
            OrgNode n = new OrgNode();
            n.setId(rs.getString("ID"));
            n.setPid(rs.getString("PID"));
            n.setName(rs.getString("NAME"));
            n.setCode(rs.getString("CODE"));
            return n;
        };
        var tree = dba.select().from("T_ORG").queryForTree(mapper);
        assertNotNull(tree);
        assertEquals(2, tree.getRoots().size());
        assertEquals(2, tree.getRoots().get(0).getChildren().size());
    }

    // ===== select(String...) empty args =====

    @Test
    void testSelectEmptyArgs() {
        Sql sql = Sql.select(new String[0]);
        assertTrue(sql.toString().contains("SELECT *"));
    }

    @Test
    void testSelectSingleField() {
        Sql sql = Sql.select("ID");
        assertTrue(sql.toString().contains("ID"));
    }

    @Test
    void testSelectMultipleFields() {
        Sql sql = Sql.select("ID", "NAME", "AGE");
        assertTrue(sql.toString().contains("ID"));
        assertTrue(sql.toString().contains("NAME"));
        assertTrue(sql.toString().contains("AGE"));
    }

    // ===== orderBy empty =====

    @Test
    void testOrderByEmpty() {
        setupUsers();
        List<User> list = dba.select().from("T_USER").orderBy().queryForList(User.class);
        assertEquals(3, list.size());
    }

    @Test
    void testOrderByEmptyList() {
        setupUsers();
        List<User> list = dba.select().from("T_USER").orderBy(new ArrayList<>()).queryForList(User.class);
        assertEquals(3, list.size());
    }

    // ===== execute with params =====

    @Test
    void testExecuteWithParams() {
        createUserTable();
        dba.insert(new User("1", "Alice", 25, 90.0));
        int updated = dba.sql("UPDATE T_USER SET NAME=? WHERE ID=?").addParam("Bob").addParam("1").execute();
        assertEquals(1, updated);
        User u = dba.selectByKey(User.class, "1");
        assertEquals("Bob", u.getName());
    }

    // ===== queryForList(Class) simple type branch =====

    @Test
    void testQueryForListSimpleType() {
        setupUsers();
        List<Integer> ages = dba.select("AGE").from("T_USER").queryForList(Integer.class);
        assertEquals(3, ages.size());
    }

    // ===== queryForValue enum type (typeToMapper enum branch) =====

    @Test
    void testQueryForValueEnumType() {
        createEnumTable();
        dba.sql("INSERT INTO T_ENUM(ID, STATUS, COLOR) VALUES('1', 'ACTIVE', 'RED')").execute();
        String status = dba.select("STATUS").from("T_ENUM").where("ID", "1").queryForValue(String.class);
        assertEquals("ACTIVE", status);
    }

    // ===== addParam / setParam chaining =====

    @Test
    void testParamChaining() {
        Sql sql = new Sql();
        sql.addParam("a").addParam("b");
        assertEquals(2, sql.getParams().size());
        sql.setParam("c");
        assertEquals(1, sql.getParams().size());
        assertEquals("c", sql.getParams().get(0));
    }

    // ===== queryToMap with params (RowCallbackHandler params branch) =====

    @Test
    void testQueryToMapWithParams() {
        setupUsers();
        Map<String, String> map = dba.select().from("T_USER").where("AGE", Op.GT, 20)
                .queryToMap(rs -> rs.getString("ID"), rs -> rs.getString("NAME"));
        assertEquals(3, map.size());
    }

    // ===== queryToGroup with params =====

    @Test
    void testQueryToGroupWithParams() {
        setupUsers();
        Map<String, List<String>> groups = dba.select().from("T_USER").where("AGE", Op.GT, 20)
                .queryToGroup(rs -> rs.getString("ID"), rs -> rs.getString("NAME"));
        assertEquals(3, groups.size());
    }

    // ===== queryForTree with params =====

    @Test
    void testQueryForTreeWithParams() {
        setupOrg();
        var tree = dba.select().from("T_ORG").where("ID", "1").queryForTree(OrgNode.class);
        assertEquals(1, tree.getRoots().size());
        assertEquals(0, tree.getRoots().get(0).getChildren().size());
    }
}
