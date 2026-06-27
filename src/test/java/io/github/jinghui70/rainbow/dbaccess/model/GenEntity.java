package io.github.jinghui70.rainbow.dbaccess.model;

import io.github.jinghui70.rainbow.dbaccess.annotation.GeneratedValue;
import io.github.jinghui70.rainbow.dbaccess.annotation.GenerationTiming;
import io.github.jinghui70.rainbow.dbaccess.annotation.Id;
import io.github.jinghui70.rainbow.dbaccess.annotation.Table;

import java.time.LocalDateTime;

/**
 * 自动生成值实体，测试 {@link GeneratedValue} 各策略。
 */
@Table(name = "T_GEN")
public class GenEntity {

    /** default 策略：String 主键，雪花 id 的 36 进制 */
    @Id
    @GeneratedValue
    private String id;

    /** now 策略：LocalDateTime 当前时间 */
    @GeneratedValue(strategy = "now")
    private LocalDateTime createTime;

    /** now 策略：String 按 param 格式化的当前时间 */
    @GeneratedValue(strategy = "now", param = "yyyyMMdd")
    private String createDate;

    /** 用户自定义策略：返回 param + "-001" */
    @GeneratedValue(strategy = "test-seq", param = "ORD")
    private String code;

    /** INSERT_UPDATE 策略：每次更新时自动生成新值 */
    @GeneratedValue(strategy = "now", timing = GenerationTiming.INSERT_UPDATE)
    private LocalDateTime updateTime;

    private String name;

    public GenEntity() {
    }

    public GenEntity(String name) {
        this.name = name;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public String getCreateDate() { return createDate; }
    public void setCreateDate(String createDate) { this.createDate = createDate; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
