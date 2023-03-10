package io.github.jinghui70.rainbow.dbaccess;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;

import java.util.List;
import java.util.Map;

public class QueryParam {

    private String entity;

    private List<Cnd> cnds;

    private String sortField;

    private boolean sortDesc;

    private int pageNo;

    private int pageSize;

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public List<Cnd> getCnds() {
        return cnds;
    }

    public void setCnds(List<Cnd> cnds) {
        this.cnds = cnds;
    }

    public String getSortField() {
        return sortField;
    }

    public void setSortField(String sortField) {
        this.sortField = sortField;
    }

    public boolean isSortDesc() {
        return sortDesc;
    }

    public void setSortDesc(boolean sortDesc) {
        this.sortDesc = sortDesc;
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

    @Override
    public String toString() {
        return "QueryParam[" + JSONUtil.toJsonStr(this) + "]";
    }

    public QueryParam setDefaultSort(String sortField) {
        return setDefaultSort(sortField, false);
    }

    public QueryParam setDefaultSort(String sortField, boolean sortDesc) {
        if (StrUtil.isBlank(this.sortField)) {
            this.sortField = sortField;
            this.sortDesc = sortDesc;
        }
        return this;
    }

    public Sql toSql(Dba dba) {
        Sql sql = dba.sql("select *").from(entity);
        if (CollUtil.isNotEmpty(cnds))
            for (Cnd cnd: cnds)
                sql.where(cnd);
        if (StrUtil.isNotBlank(sortField)) {
            sql.orderBy(sortField);
            if (sortDesc)
                sql.append((" DESC"));
        }
        return sql;
    }

    public PageData<Map<String, Object>> pageQuery(Dba dba) {
        return toSql(dba).pageQuery(pageNo, pageSize);
    }

    public <T> PageData<T> pageQuery(Dba dba, Class<T> clazz) {
        return toSql(dba).pageQuery(clazz, pageNo, pageSize);
    }
}
