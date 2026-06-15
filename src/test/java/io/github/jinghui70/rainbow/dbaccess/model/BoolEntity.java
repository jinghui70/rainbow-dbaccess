package io.github.jinghui70.rainbow.dbaccess.model;

import io.github.jinghui70.rainbow.dbaccess.annotation.Column;
import io.github.jinghui70.rainbow.dbaccess.annotation.Id;
import io.github.jinghui70.rainbow.dbaccess.annotation.Table;

/**
 * 布尔类型实体，测试 Boolean 字段的 int 存储映射。
 */
@Table(name = "T_BOOL")
public class BoolEntity {

    @Id
    private String id;

    /** 默认 Boolean 映射 — 存为 1/0 */
    private Boolean active;

    @Column(name = "FLAG")
    private Boolean flag;

    public BoolEntity() {
    }

    public BoolEntity(String id, Boolean active, Boolean flag) {
        this.id = id;
        this.active = active;
        this.flag = flag;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public Boolean getFlag() { return flag; }
    public void setFlag(Boolean flag) { this.flag = flag; }
}
