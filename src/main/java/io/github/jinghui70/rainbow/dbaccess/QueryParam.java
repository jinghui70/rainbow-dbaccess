package io.github.jinghui70.rainbow.dbaccess;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;

import java.util.Map;

public class QueryParam {

    private String entity;

    private Cnd[] cnds;

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

    public Cnd[] getCnds() {
        return cnds;
    }

    public void setCnds(Cnd[] cnds) {
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

    public boolean hasCnd() {
        return cnds != null && cnds.length > 0;
    }

    @Override
    public String toString() {
        return "QueryParam[" + JSONUtil.toJsonStr(this) + "]";
    }

    public Sql toSql(Dba dba) {
        Sql sql = dba.sql("select *").from(entity);
        if (hasCnd())
            sql.where(cnds);
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
