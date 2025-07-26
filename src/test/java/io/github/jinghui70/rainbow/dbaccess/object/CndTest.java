package io.github.jinghui70.rainbow.dbaccess.object;

import cn.hutool.core.collection.CollUtil;
import io.github.jinghui70.rainbow.dbaccess.BaseTest;
import io.github.jinghui70.rainbow.dbaccess.DbaTestUtil;
import io.github.jinghui70.rainbow.dbaccess.Range;
import io.github.jinghui70.rainbow.dbaccess.Sql;
import io.github.jinghui70.rainbow.dbaccess.cnd.Cnd;
import io.github.jinghui70.rainbow.dbaccess.cnd.Op;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class CndTest extends BaseTest {

    @BeforeEach
    void init() {
        DbaTestUtil.initTable(dba, "SIMPLE_OBJECT");
    }

    @Test
    public void testLike() {
        List<SimpleObject> list = CollUtil.toList(
                new SimpleObject(1, "李大宝", new Double[]{100.0, 80.0, 60d}),
                new SimpleObject(2, "刘思李", new Double[]{60.0, 80.0, 100d})
        );
        dba.insert(list);

        // 基本 like
        list = dba.select().from("SIMPLE_OBJECT")
                .where("name", Op.LIKE, "李").queryForList(SimpleObject.class);
        assertEquals(2, list.size());

        // 左侧李
        SimpleObject so = dba.select().from("SIMPLE_OBJECT")
                .where("name", Op.LIKE_LEFT, "李").queryForObject(SimpleObject.class);
        assertEquals("李大宝", so.getName());

        so = dba.select().from("SIMPLE_OBJECT")
                .where("name", Op.LIKE, "李%").queryForObject(SimpleObject.class);
        assertEquals("李大宝", so.getName());

        so = dba.select().from("SIMPLE_OBJECT")
                .where("name", Op.NOT_LIKE_LEFT, "李").queryForObject(SimpleObject.class);
        assertEquals("刘思李", so.getName());

        so = dba.select().from("SIMPLE_OBJECT")
                .where("name", Op.NOT_LIKE, "李%").queryForObject(SimpleObject.class);
        assertEquals("刘思李", so.getName());

        // 右侧李
        so = dba.select().from("SIMPLE_OBJECT")
                .where("name", Op.LIKE_RIGHT, "李").queryForObject(SimpleObject.class);
        assertEquals("刘思李", so.getName());

        so = dba.select().from("SIMPLE_OBJECT")
                .where("name", Op.LIKE, "%李").queryForObject(SimpleObject.class);
        assertEquals("刘思李", so.getName());

        so = dba.select().from("SIMPLE_OBJECT")
                .where("name", Op.NOT_LIKE_RIGHT, "李").queryForObject(SimpleObject.class);
        assertEquals("李大宝", so.getName());

        so = dba.select().from("SIMPLE_OBJECT")
                .where("name", Op.NOT_LIKE, "%李").queryForObject(SimpleObject.class);
        assertEquals("李大宝", so.getName());
    }

    @Test
    public void testRange() {
        List<SimpleObject> list = CollUtil.toList(
                new SimpleObject(1, "李大宝", new Double[]{100.0, 80.0, 60d}),
                new SimpleObject(2, "刘思李", new Double[]{60.0, 80.0, 100d})
        );
        dba.insert(list);

        Sql sql = dba.select().from("SIMPLE_OBJECT").where("SCORE_1", Range.of(70, null));
        assertEquals("SELECT * FROM SIMPLE_OBJECT WHERE SCORE_1>=?", sql.getSql().toUpperCase());
        assertEquals("李大宝", sql.queryForObject(SimpleObject.class).getName());

        sql = dba.select().from("SIMPLE_OBJECT").where("SCORE_1", Range.of(null, 99));
        assertEquals("SELECT * FROM SIMPLE_OBJECT WHERE SCORE_1<=?", sql.getSql().toUpperCase());
        assertEquals("刘思李", sql.queryForObject(SimpleObject.class).getName());

        sql = dba.select().from("SIMPLE_OBJECT").where("SCORE_1", Range.of(60, 100));
        assertEquals("SELECT * FROM SIMPLE_OBJECT WHERE SCORE_1 BETWEEN ? AND ?", sql.getSql().toUpperCase());
        assertEquals(2, sql.queryForList().size());
    }

    @Test
    public void testIn() {
        List<SimpleObject> list = CollUtil.toList(
                new SimpleObject(1, "李大宝", new Double[]{100.0, 80.0, 60.0}),
                new SimpleObject(2, "刘思李", new Double[]{60.0, null, 100.0}),
                new SimpleObject(3, "李小宝", new Double[]{100.0, 60.0, 60.0})
        );
        dba.insert(list);

        // IN
        try {
            // 不能是空参数
            dba.select().from("SIMPLE_OBJECT").where("SCORE_2", Op.IN, null);
            fail();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        try {
            // 不能是空数组参数
            dba.select().from("SIMPLE_OBJECT").where("SCORE_2", new Double[]{});
            fail();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        Sql sql = dba.select().from("SIMPLE_OBJECT").where("SCORE_2", new Double[]{80.0});
        assertEquals("SELECT * FROM SIMPLE_OBJECT WHERE SCORE_2=?", sql.getSql());
        assertEquals("李大宝", sql.queryForObject(SimpleObject.class).getName());

        sql = dba.select().from("SIMPLE_OBJECT").where("SCORE_2", new Double[]{null});
        assertEquals("SELECT * FROM SIMPLE_OBJECT WHERE SCORE_2 IS NULL", sql.getSql());
        assertEquals("刘思李", sql.queryForObject(SimpleObject.class).getName());

        sql = dba.select().from("SIMPLE_OBJECT").where("SCORE_2", new Double[]{80.0, null}).orderBy("ID");
        assertEquals("SELECT * FROM SIMPLE_OBJECT WHERE (SCORE_2=? OR SCORE_2 IS NULL) ORDER BY ID", sql.getSql());
        list = sql.queryForList(SimpleObject.class);
        assertEquals("李大宝", list.get(0).getName());
        assertEquals("刘思李", list.get(1).getName());

        sql = dba.select().from("SIMPLE_OBJECT").where("SCORE_2", new Double[]{80.0, 60.0}).orderBy("ID");
        assertEquals("SELECT * FROM SIMPLE_OBJECT WHERE SCORE_2 IN (?,?) ORDER BY ID", sql.getSql());
        list = sql.queryForList(SimpleObject.class);
        assertEquals("李大宝", list.get(0).getName());
        assertEquals("李小宝", list.get(1).getName());

        // NOT IN
        try {
            // 不能是空参数
            dba.select().from("SIMPLE_OBJECT").where("SCORE_2", Op.NOT_IN, null);
            fail();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        try {
            // 不能是空数组参数
            dba.select().from("SIMPLE_OBJECT").where("SCORE_2", Op.NOT_IN, new Double[]{});
            fail();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        sql = dba.select().from("SIMPLE_OBJECT").where("SCORE_2", Op.NOT_IN, new Double[]{80.0});
        assertEquals("SELECT * FROM SIMPLE_OBJECT WHERE SCORE_2!=?", sql.getSql());
        assertEquals("李小宝", sql.queryForObject(SimpleObject.class).getName()); // null 是匹配不出来的

        sql = dba.select().from("SIMPLE_OBJECT").where("SCORE_2", Op.NOT_IN, new Double[]{80.0, 60.0});
        assertEquals("SELECT * FROM SIMPLE_OBJECT WHERE SCORE_2 NOT IN (?,?)", sql.getSql());
        list = sql.queryForList(SimpleObject.class);
        assertEquals(0, list.size()); // null 是匹配不出来的
    }

    @Test
    public void testCnds() {
        final Cnd cnds = Cnd.or(Cnd.where("A", 1), Cnd.where("B", 2));
        String sql = new Sql().where(cnds).getSql();
        assertEquals(" WHERE (A=? OR B=?)", sql);

        sql = new Sql().where(Cnd.or(Cnd.where(false,"A", 1), Cnd.where("B", 2))).getSql();
        assertEquals(" WHERE B=?", sql);

        sql = new Sql().where(false, () -> cnds).getSql();
        assertEquals("", sql);

        Cnd cnds1 = Cnd.or(Cnd.where("A", 1), Cnd.where("B", 2));
        Cnd cnds2 = Cnd.or(Cnd.where("A", 1), Cnd.where("B", 2), cnds1);
        sql = new Sql().where(cnds2).getSql();
        assertEquals(" WHERE (A=? OR B=? OR (A=? OR B=?))", sql);

        sql = dba.select("*").from("X")
                .where(
                        Cnd.and(
                                Cnd.where("AGE", Op.GT, 60),
                                Cnd.where("NAME", Op.LIKE_LEFT, "李")
                        ))
                .or(
                        Cnd.and(
                                Cnd.where("AGE", Op.LT, 18),
                                Cnd.where("NAME", Op.LIKE_LEFT, "刘")
                        )
                )
                .getSql();
        assertEquals("SELECT * FROM X WHERE (AGE>? AND NAME LIKE ?) OR (AGE<? AND NAME LIKE ?)", sql);
    }
}
