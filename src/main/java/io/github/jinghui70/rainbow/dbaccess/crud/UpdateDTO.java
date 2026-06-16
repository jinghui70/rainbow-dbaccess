package io.github.jinghui70.rainbow.dbaccess.crud;

import cn.hutool.core.collection.CollUtil;

import java.util.List;

/**
 * 增量更新 DTO，携带待更新记录及变更属性列表。
 * <p>
 * {@code record} 包含主键和变更后的字段值，{@code changedProps} 指定哪些属性参与更新。
 * 与 {@link io.github.jinghui70.rainbow.dbaccess.UpdateBuilder#include} 配合使用。
 *
 * @param <T> 实体类型
 */
public class UpdateDTO<T> {

    private T record;

    private List<String> changedProps;

    /**
     * 无参构造。
     */
    public UpdateDTO() {}

    /**
     * 带参构造。
     *
     * @param record       包含主键的实体对象
     * @param changedProps 需更新的属性名列表
     */
    public UpdateDTO(T record, String... changedProps) {
        this.record = record;
        this.changedProps = CollUtil.toList(changedProps);
    }

    /**
     * 获取待更新的实体对象。
     *
     * @return 包含主键的实体对象
     */
    public T getRecord() {
        return record;
    }

    /**
     * 设置待更新的实体对象。
     *
     * @param record 包含主键的实体对象
     */
    public void setRecord(T record) {
        this.record = record;
    }

    /**
     * 获取需更新的属性名列表。
     *
     * @return 属性名列表
     */
    public List<String> getChangedProps() {
        return changedProps;
    }

    /**
     * 设置需更新的属性名列表。
     *
     * @param changedProps 属性名列表
     */
    public void setChangedProps(List<String> changedProps) {
        this.changedProps = changedProps;
    }
}