package io.github.jinghui70.rainbow.dbaccess.cnd;

import cn.hutool.core.collection.CollUtil;
import io.github.jinghui70.rainbow.dbaccess.DbaUtil;
import io.github.jinghui70.rainbow.dbaccess.SqlWrapper;

import java.util.ArrayList;
import java.util.List;

/**
 * 一组条件，拼 sql 时会用括号包起来
 */
public class Cnds {

    private List<Object> children;

    public boolean isEmpty() {
        return CollUtil.isEmpty(children);
    }

    public boolean isSingle() {
        return children != null && children.size() == 1;
    }

    private void addJoiner(String joiner) {
        if (CollUtil.isNotEmpty(children))
            children.add(joiner);
    }

    private void add(Object cnd) {
        if (children == null) children = new ArrayList<>();
        children.add(cnd);
    }

    public Cnds and(String field, Object value) {
        return and(true, field, Op.EQ, value);
    }

    public Cnds and(String field, Op op, Object value) {
        return and(true, field, op, value);
    }

    public Cnds and(boolean condition, String field, Object value) {
        return and(condition, field, Op.EQ, value);
    }

    public Cnds and(boolean condition, String field, Op op, Object value) {
        if (condition) {
            addJoiner(DbaUtil.AND);
            add(new Cnd(field, op, value));
        }
        return this;
    }

    public Cnds and(Cnds cnds) {
        return and(true, cnds);
    }

    public Cnds and(boolean condition, Cnds cnds) {
        if (condition && !cnds.isEmpty()) {
            addJoiner(DbaUtil.AND);
            if (isSingle()) add(children.get(0));
            else add(cnds);
        }
        return this;
    }

    public Cnds or(String field, Object value) {
        return or(true, field, Op.EQ, value);
    }

    public Cnds or(String field, Op op, Object value) {
        return or(true, field, op, value);
    }

    public Cnds or(boolean condition, String field, Object value) {
        return or(condition, field, Op.EQ, value);
    }

    public Cnds or(boolean condition, String field, Op op, Object value) {
        if (condition) {
            addJoiner(DbaUtil.OR);
            add(new Cnd(field, op, value));
        }
        return this;
    }

    public Cnds or(Cnds cnds) {
        return or(true, cnds);
    }

    public Cnds or(boolean condition, Cnds cnds) {
        if (condition && !cnds.isEmpty()) {
            addJoiner(DbaUtil.OR);
            if (isSingle()) add(children.get(0));
            else add(cnds);
        }
        return this;
    }

    public void toSql(SqlWrapper<?> sql) {
        if (isEmpty()) return;
        if (isSingle()) {
            Cnd cnd = (Cnd) children.get(0);
            sql.append(cnd);
        } else {
            sql.append("(");
            for (Object child : children) {
                if (child instanceof Cnd)
                    sql.append((Cnd) child);
                else if (child instanceof Cnds)
                    sql.append((Cnds) child);
                else
                    sql.append(child);
            }
            sql.append(")");
        }
    }

    public static Cnds of(String field, Object value) {
        return of(true, field, Op.EQ, value);
    }

    public static Cnds of(String field, Op op, Object value) {
        return of(true, field, op, value);
    }

    public static Cnds of(boolean condition, String field, Object value) {
        return of(condition, field, Op.EQ, value);
    }

    public static Cnds of(boolean condition, String field, Op op, Object value) {
        Cnds result = new Cnds();
        if (condition) {
            result.and(field, op, value);
        }
        return result;
    }
}
