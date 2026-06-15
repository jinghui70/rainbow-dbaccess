package io.github.jinghui70.rainbow.dbaccess.model;

import io.github.jinghui70.rainbow.dbaccess.annotation.Column;
import io.github.jinghui70.rainbow.dbaccess.annotation.Id;
import io.github.jinghui70.rainbow.dbaccess.annotation.Table;

import java.sql.Types;
import java.util.List;
import java.util.Map;

/**
 * 使用 ObjectFieldMapper 的实体 — 测试 CLOB 类型自动匹配。
 * List/Map/Set 等复杂类型通过 @Column(sqlType=CLOB/VARCHAR) 触发 JSON 序列化。
 */
@Table(name = "T_OBJECT")
public class ObjectEntity {

    @Id
    private String id;

    @Column(sqlType = Types.CLOB)
    private List<String> tags;

    @Column(sqlType = Types.CLOB)
    private Map<String, Object> attributes;

    public ObjectEntity() {
    }

    public ObjectEntity(String id, List<String> tags, Map<String, Object> attributes) {
        this.id = id;
        this.tags = tags;
        this.attributes = attributes;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    public Map<String, Object> getAttributes() { return attributes; }
    public void setAttributes(Map<String, Object> attributes) { this.attributes = attributes; }
}
