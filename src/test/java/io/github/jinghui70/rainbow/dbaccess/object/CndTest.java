package io.github.jinghui70.rainbow.dbaccess.object;

import cn.hutool.core.collection.CollUtil;
import io.github.jinghui70.rainbow.dbaccess.BaseTest;
import io.github.jinghui70.rainbow.dbaccess.DbaConfig;
import io.github.jinghui70.rainbow.dbaccess.Range;
import io.github.jinghui70.rainbow.dbaccess.Sql;
import io.github.jinghui70.rainbow.dbaccess.cnd.Cnds;
import io.github.jinghui70.rainbow.dbaccess.cnd.Op;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CndTest extends BaseTest {

    @BeforeEach
    void init() {
        DbaConfig.initTable(dba, "SIMPLE_OBJECT");
    }

    @Test
    public void testLike() {
        List<SimpleObject> list = CollUtil.toList(
                new SimpleObject(1, "李大宝", new double[] { 100, 80, 60}),
                new SimpleObject(2, "刘思李", new double[] { 60, 80, 100})
        );
        dba.insert(list);

        // 基本 like
        list = dba.select(SimpleObject.class)
                .where("name", Op.LIKE, "李").queryForList();
        assertEquals(2, list.size());

        // 左侧李
        SimpleObject so = dba.select(SimpleObject.class)
                .where("name", Op.LIKE_LEFT, "李").queryForObject();
        assertEquals("李大宝", so.getName());

        so = dba.select(SimpleObject.class)
                .where("name", Op.LIKE, "李%").queryForObject();
        assertEquals("李大宝", so.getName());

        so = dba.select(SimpleObject.class)
                .where("name", Op.NOT_LIKE_LEFT, "李").queryForObject();
        assertEquals("刘思李", so.getName());

        so = dba.select(SimpleObject.class)
                .where("name", Op.NOT_LIKE, "李%").queryForObject();
        assertEquals("刘思李", so.getName());

        // 右侧李
        so = dba.select(SimpleObject.class)
                .where("name", Op.LIKE_RIGHT, "李").queryForObject();
        assertEquals("刘思李", so.getName());

        so = dba.select(SimpleObject.class)
                .where("name", Op.LIKE, "%李").queryForObject();
        assertEquals("刘思李", so.getName());

        so = dba.select(SimpleObject.class)
                .where("name", Op.NOT_LIKE_RIGHT, "李").queryForObject();
        assertEquals("李大宝", so.getName());

        so = dba.select(SimpleObject.class)
                .where("name", Op.NOT_LIKE, "%李").queryForObject();
        assertEquals("李大宝", so.getName());
    }

    @Test
    public void testRange() {
        List<SimpleObject> list = CollUtil.toList(
                new SimpleObject(1, "李大宝", new double[] { 100, 80, 60}),
                new SimpleObject(2, "刘思李", new double[] { 60, 80, 100})
        );
        dba.insert(list);

        ObjectSql<SimpleObject> sql = dba.select(SimpleObject.class).where("SCORE_1", Range.of(70, null));
        assertEquals("SELECT * FROM SIMPLE_OBJECT WHERE SCORE_1>=?", sql.getSql().toUpperCase());
        assertEquals("李大宝", sql.queryForObject().getName());

        sql = dba.select(SimpleObject.class).where("SCORE_1", Range.of(null, 99));
        assertEquals("SELECT * FROM SIMPLE_OBJECT WHERE SCORE_1<=?", sql.getSql().toUpperCase());
        assertEquals("刘思李", sql.queryForObject().getName());

        sql = dba.select(SimpleObject.class).where("SCORE_1", Range.of(60, 100));
        assertEquals("SELECT * FROM SIMPLE_OBJECT WHERE SCORE_1 BETWEEN ? AND ?", sql.getSql().toUpperCase());
        assertEquals(2, sql.queryForList().size());
    }

    @Test
    public void testCnds() {
        Cnds cnds = Cnds.of("A", 1).or("B", 2);
        String sql = new Sql().where(cnds).getSql();
        assertEquals(" WHERE (A=? OR B=?)", sql);

        cnds = Cnds.of(false, "A", 1).or("B", 2);
        sql = new Sql().where(cnds).getSql();
        assertEquals(" WHERE B=?", sql);

        cnds = Cnds.of(false, "A", 1).or(false, "B", 2);
        sql = new Sql().where(cnds).getSql();
        assertEquals("", sql);

        Cnds cnds1 = Cnds.of("A", 1).or("B", 2);
        cnds = Cnds.of("A", 1).or("B", 2).and(cnds1);
        sql = new Sql().where(cnds).getSql();
        assertEquals(" WHERE (A=? OR B=? AND (A=? OR B=?))", sql);

        sql = dba.select("*").from("X")
                .where(Cnds.of("AGE", Op.GT, 60).and("NAME", Op.LIKE_LEFT, "李"))
                .or(Cnds.of("AGE", Op.LT, 18).and("NAME", Op.LIKE_LEFT, "刘"))
                .getSql();
        assertEquals("SELECT * FROM X WHERE (AGE>? AND NAME LIKE ?) OR (AGE<? AND NAME LIKE ?)", sql);
    }
}
