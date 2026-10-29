package io.github.jinghui70.rainbow.dbaccess.model;

/**
 * 无主键实体 — 用于验证按主键操作（whereByKey / whereByObj / delete / update）的前置校验。
 */
public class NoKeyEntity {

    private String name;

    public NoKeyEntity() {
    }

    public NoKeyEntity(String name) {
        this.name = name;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
