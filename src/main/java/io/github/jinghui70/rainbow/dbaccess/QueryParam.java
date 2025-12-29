package io.github.jinghui70.rainbow.dbaccess;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import io.github.jinghui70.rainbow.dbaccess.cnd.Cnd;
import io.github.jinghui70.rainbow.dbaccess.object.PropInfo;
import io.github.jinghui70.rainbow.dbaccess.object.PropInfoCache;
import org.springframework.jdbc.core.RowMapper;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class QueryParam {

    private String entity;

    private String fields;

    private List<Cnd> cnds;

    private List<OrderBy> orderBys;

    private int pageNo;

    private int pageSize;

    private String defaultOrderBys;

    public String getEntity() {
        return entity;
    }

    public QueryParam setEntity(String entity) {
        this.entity = entity;
        return this;
    }

    public QueryParam setEntity(Class<?> entityClass) {
        this.setEntity(DbaUtil.tableName(entityClass));
        if (StrUtil.isEmpty(defaultOrderBys)) {
            this.defaultOrderBys = PropInfoCache.get(entityClass).values().stream()
                    .filter(p -> p.getId() != null)
                    .map(PropInfo::getFieldName)
                    .collect(Collectors.joining(StrUtil.COMMA));
        }
        return this;
    }

    public QueryParam setFields(String fields) {
        this.fields = fields;
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

    public QueryParam addCnd(Cnd cnd) {
        if (cnd == null) return this;
        if (cnds == null)
            cnds = new LinkedList<>();
        cnds.add(cnd);
        return this;
    }

    public QueryParam defaultOrderBys(Object... defaultOrderBys) {
        this.defaultOrderBys = StrUtil.join(StrUtil.COMMA, defaultOrderBys);
        return this;
    }

    public Sql getSql(Dba dba) {
        Sql sql = dba.select(getFields());
        sql.from(getEntity());
        processCnd(sql);
        processOrderBy(sql);
        return sql;
    }

    protected String getFields() {
        return StrUtil.isEmpty(fields) ? "*" : this.fields;
    }

    protected void processCnd(Sql sql) {
        if (CollUtil.isNotEmpty(cnds)) {
            for (Cnd cnd: cnds)
                sql.where(cnd);
        }
    }

    protected void processOrderBy(Sql sql) {
        if (CollUtil.isNotEmpty(orderBys)) {
            sql.orderBy(orderBys);
        } else if (StrUtil.isNotEmpty(defaultOrderBys))
            sql.orderBy(defaultOrderBys);
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
