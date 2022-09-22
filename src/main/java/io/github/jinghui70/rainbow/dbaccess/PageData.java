package io.github.jinghui70.rainbow.dbaccess;

import cn.hutool.json.JSONUtil;

import java.util.Collections;
import java.util.List;

/**
 * 分页查询用数据封装对象
 *
 * @param <T> 对象的泛型
 */
public class PageData<T> {

    /**
     * 查询的总记录数
     */
    private int total;

    /**
     * 查询的结果列表
     */
    private List<T> rows;

    public PageData() {
        rows = Collections.emptyList();
    }

    public PageData(int total) {
        this.total = total;
        rows = Collections.emptyList();
    }

    public PageData(int total, List<T> rows) {
        this.total = total;
        this.rows = rows;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<T> getRows() {
        return rows;
    }

    public void setRows(List<T> rows) {
        this.rows = rows;
    }

    @Override
    public String toString() {
        return JSONUtil.toJsonStr(this);
    }
}
