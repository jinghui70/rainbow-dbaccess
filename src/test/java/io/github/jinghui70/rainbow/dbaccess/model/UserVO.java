package io.github.jinghui70.rainbow.dbaccess.model;

/**
 * 用户 VO — 裁剪过的投影类，只保留部分字段，用于验证查询结果直接映射为 VO。
 * <p>
 * 故意不实现主键相关的 setter 依赖，用来确认映射只依赖列名。
 */
public class UserVO {

    private String name;

    private Integer age;

    public UserVO() {
    }

    public UserVO(String name, Integer age) {
        this.name = name;
        this.age = age;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
}
