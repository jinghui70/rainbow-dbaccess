package io.github.jinghui70.rainbow.dbaccess.sql;

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
    private List<T> data;

    public PageData() {
        data = Collections.emptyList();
    }

    public PageData(int total) {
        this.total = total;
        data = Collections.emptyList();
    }

    public PageData(int total, List<T> data) {
        this.total = total;
        this.data = data;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return JSONUtil.toJsonStr(this);
    }
}
