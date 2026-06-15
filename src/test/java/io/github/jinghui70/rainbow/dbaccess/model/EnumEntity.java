package io.github.jinghui70.rainbow.dbaccess.model;

import io.github.jinghui70.rainbow.dbaccess.annotation.Column;
import io.github.jinghui70.rainbow.dbaccess.annotation.Id;
import io.github.jinghui70.rainbow.dbaccess.annotation.Table;

/**
 * 枚举类型实体，测试三种枚举映射方式。
 */
@Table(name = "T_ENUM")
public class EnumEntity {

    @Id
    private String id;

    /** 标准枚举 — 存 name() */
    private Status status;

    /** CodeEnum 枚举 — 存 code() */
    private Color color;

    public EnumEntity() {
    }

    public EnumEntity(String id, Status status, Color color) {
        this.id = id;
        this.status = status;
        this.color = color;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public Color getColor() { return color; }
    public void setColor(Color color) { this.color = color; }
}
