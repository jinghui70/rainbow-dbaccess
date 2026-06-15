package io.github.jinghui70.rainbow.dbaccess.model;

import io.github.jinghui70.rainbow.dbaccess.annotation.Id;
import io.github.jinghui70.rainbow.dbaccess.annotation.Table;
import io.github.jinghui70.rainbow.dbaccess.annotation.Transient;

/**
 * 自增主键实体，测试 autoIncrement 属性。
 */
@Table(name = "T_AUTO")
public class AutoEntity {

    @Id(autoIncrement = true)
    private Integer id;

    private String name;

    private Double score;

    @Transient
    private String memo;

    public AutoEntity() {
    }

    public AutoEntity(String name, Double score) {
        this.name = name;
        this.score = score;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }
    public String getMemo() { return memo; }
    public void setMemo(String memo) { this.memo = memo; }
}
