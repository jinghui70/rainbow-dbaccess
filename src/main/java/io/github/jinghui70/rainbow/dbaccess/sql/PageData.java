package io.github.jinghui70.rainbow.dbaccess.sql;

import cn.hutool.core.collection.CollUtil;
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

    /**
     * 获取查询的总记录数
     *
     * @return 总记录数
     */
    public int getTotal() {
        return total;
    }

    /**
     * 设置查询的总记录数
     *
     * @param total 总记录数
     */
    public void setTotal(int total) {
        this.total = total;
    }

    /**
     * 获取查询的结果列表
     *
     * @return 结果列表，如果未设置则返回空集合
     */
    public List<T> getData() {
        return data;
    }

    /**
     * 设置查询的结果列表
     *
     * @param data 结果列表
     */
    public void setData(List<T> data) {
        this.data = data;
    }

    /**
     * 判断当前分页数据是否为空
     *
     * @return 如果结果列表为空或 null 则返回 true，否则返回 false
     */
    public boolean isEmpty() {
        return CollUtil.isEmpty(data);
    }

    /**
     * 将当前对象转换为 JSON 格式的字符串
     * (重写自 Object 类的 toString 方法)
     *
     * @return 包含 total 和 data 信息的 JSON 字符串
     */
    @Override
    public String toString() {
        return JSONUtil.toJsonStr(this);
    }
}
