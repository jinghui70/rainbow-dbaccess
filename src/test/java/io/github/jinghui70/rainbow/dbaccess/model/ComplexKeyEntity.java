package io.github.jinghui70.rainbow.dbaccess.model;

import io.github.jinghui70.rainbow.dbaccess.annotation.Id;
import io.github.jinghui70.rainbow.dbaccess.annotation.Table;

/**
 * 复合主键实体 — 测试多主键场景。
 */
@Table(name = "T_COMPLEX_KEY")
public class ComplexKeyEntity {

    @Id
    private String keyA;

    @Id
    private String keyB;

    private String value;

    public ComplexKeyEntity() {
    }

    public ComplexKeyEntity(String keyA, String keyB, String value) {
        this.keyA = keyA;
        this.keyB = keyB;
        this.value = value;
    }

    public String getKeyA() { return keyA; }
    public void setKeyA(String keyA) { this.keyA = keyA; }
    public String getKeyB() { return keyB; }
    public void setKeyB(String keyB) { this.keyB = keyB; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}
