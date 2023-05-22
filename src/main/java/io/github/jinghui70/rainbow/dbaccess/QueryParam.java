package io.github.jinghui70.rainbow.dbaccess;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class QueryParam {

    private String entity;

    private List<Cnd> cnds;

    private List<SortField> sortFields;

    private int pageNo;

    private int pageSize;

    public String getEntity() {
        return entity;
    }

    public QueryParam setEntity(String entity) {
        this.entity = entity;
        return this;
    }

    public List<Cnd> getCnds() {
        return cnds;
    }

    public void setCnds(List<Cnd> cnds) {
        this.cnds = cnds;
    }

    public List<SortField> getSortFields() {
        return sortFields;
    }

    public void setSortFields(List<SortField> sortFields) {
        this.sortFields = sortFields;
    }

    /**
     * 设置默认的排序字段，只有当排序字段没有值的时候有效
     *
     * @param sortFields 默认的排序字段
     * @return 返回自己，链式调用
     */
    public QueryParam setDefaultSortFields(SortField... sortFields) {
        if (CollUtil.isEmpty(this.sortFields)) {
            this.sortFields = Arrays.asList(sortFields);
        }
        return this;
    }

    /**
     * 设置默认的排序字段名，简化的一个字段排序
     *
     * @param field 字段名
     * @return 返回自己，链式调用
     */
    public QueryParam setDefaultSortField(String field) {
        return setDefaultSortFields(new SortField(field));
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

    public Sql toSql(Dba dba) {
        return dba.sql("select *").from(entity).where(cnds).orderBy(sortFields);
    }

    /**
     * 不分页查询
     *
     * @param dba Dba
     * @return 结果列表
     */
    public List<Map<String, Object>> query(Dba dba) {
        return toSql(dba).queryForList();
    }

    /**
     * 不分页查询
     *
     * @param dba   Dba
     * @param clazz 查询对象类
     * @param <T>   查询对象泛型
     * @return 结果列表
     */
    public <T> List<T> query(Dba dba, Class<T> clazz) {
        return toSql(dba).queryForList(clazz);
    }

    /**
     * 分页查询
     *
     * @param dba Dba
     * @return 查询结果
     */
    public PageData<Map<String, Object>> pageQuery(Dba dba) {
        if (pageNo == 0) { // 不分页
            List<Map<String, Object>> rows = toSql(dba).queryForList();
            return new PageData<>(rows.size(), rows);
        }
        return toSql(dba).pageQuery(pageNo, pageSize);
    }

    /**
     * 分页查询
     *
     * @param dba   Dba
     * @param clazz 查询对象类
     * @param <T>   查询对象泛型
     * @return 查询结果
     */
    public <T> PageData<T> pageQuery(Dba dba, Class<T> clazz) {
        if (pageNo == 0) { // 不分页
            List<T> rows = toSql(dba).queryForList(clazz);
            return new PageData<>(0, rows);
        }
        return toSql(dba).pageQuery(clazz, pageNo, pageSize);
    }
}
