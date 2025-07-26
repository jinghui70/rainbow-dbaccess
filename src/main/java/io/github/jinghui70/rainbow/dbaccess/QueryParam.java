package io.github.jinghui70.rainbow.dbaccess;

import cn.hutool.core.collection.CollUtil;
import io.github.jinghui70.rainbow.dbaccess.cnd.Cnd;
import io.github.jinghui70.rainbow.dbaccess.cnd.Op;
import org.springframework.jdbc.core.RowMapper;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class QueryParam {

    private String entity;

    private List<String> fields;

    private List<Cnd> cnds;

    private List<OrderBy> orderBys;

    private int pageNo;

    private int pageSize;

    private Consumer<Sql> sqlConsumer;

    public String getEntity() {
        return entity;
    }

    public QueryParam setEntity(String entity) {
        this.entity = entity;
        return this;
    }

    public List<String> getFields() {
        return fields;
    }

    public QueryParam setFields(List<String> fields) {
        this.fields = fields;
        return this;
    }

    public QueryParam addField(String field) {
        if (fields == null) fields = new LinkedList<>();
        fields.add(field);
        return this;
    }

    public List<Cnd> getCnds() {
        return cnds;
    }

    public void setCnds(List<Cnd> cnds) {
        this.cnds = cnds;
    }

    public List<OrderBy> getOrderBys() {
        return orderBys;
    }

    public void setOrderBys(List<OrderBy> orderBys) {
        this.orderBys = orderBys;
    }

    public int getPageNo() {
        return pageNo;
    }

    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public QueryParam orderBy(String field) {
        return orderBy(field, false);
    }

    public QueryParam orderBy(String field, boolean desc) {
        if (orderBys == null) {
            orderBys = new LinkedList<>();
        }
        orderBys.add(new OrderBy(field, desc));
        return this;
    }

    public QueryParam addCnd(Cnd cnd) {
        if (cnd==null) return this;
        if (cnds == null)
            cnds = new LinkedList<>();
        cnds.add(cnd);
        return this;
    }

    public QueryParam addCnd(String field, Op op, Object value) {
        return addCnd(Cnd.where(field, op, value));
    }

    public QueryParam addCnd(Consumer<Sql> consumer) {
        this.sqlConsumer = consumer;
        return this;
    }

    public Sql getSql(Dba dba) {
        Sql sql = dba.sql("select ");
        if (CollUtil.isEmpty(fields)) {
            sql.append("*");
        } else {
            sql.join(fields);
        }
        sql.from(entity);
        if (CollUtil.isNotEmpty(cnds)) {
            for (Cnd cnd : cnds) {
                sql.where(cnd);
            }
        }
        if (sqlConsumer != null) {
            sqlConsumer.accept(sql);
        }
        return sql.orderBy(orderBys);
    }

    public <T> PageData<T> pageQuery(Dba dba, Class<T> objectType) {
        return getSql(dba).pageQuery(objectType, pageNo, pageSize);
    }

    public <T> PageData<T> pageQuery(Dba dba, RowMapper<T> mapper) {
        return getSql(dba).pageQuery(mapper, pageNo, pageSize);
    }

    public PageData<Map<String, Object>> pageQuery(Dba dba) {
        return getSql(dba).pageQuery(pageNo, pageSize);
    }

    public <T> List<T> query(Dba dba, Class<T> objectType) {
        return getSql(dba).queryForList(objectType);
    }

    public <T> List<T> query(Dba dba, RowMapper<T> mapper) {
        return getSql(dba).queryForList(mapper);
    }

    public List<Map<String, Object>> query(Dba dba) {
        return getSql(dba).queryForList();
    }
}
