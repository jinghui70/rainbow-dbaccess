package io.github.jinghui70.rainbow.dbaccess.crud;

/**
 * 通用实体对象，包含 {@code id} 和 {@code name} 两个公共字段。
 * <p>
 * 可用作字典表、配置项等简单实体的基类或被 {@code @Table} 注解的实体引用。
 */
public class CommonObject {

    private String id;

    private String name;

    /**
     * 获取主键标识。
     *
     * @return id
     */
    public String getId() {
        return id;
    }

    /**
     * 设置主键标识。
     *
     * @param id 主键值
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * 获取名称。
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * 设置名称。
     *
     * @param name 名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 无参构造。
     */
    public CommonObject() {
    }

    /**
     * 带参构造。
     *
     * @param id   主键标识
     * @param name 名称
     */
    public CommonObject (String id, String name) {
        this.id = id;
        this.name = name;
    }
}
