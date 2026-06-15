package io.github.jinghui70.rainbow.dbaccess.model;

import io.github.jinghui70.rainbow.dbaccess.annotation.Id;
import io.github.jinghui70.rainbow.dbaccess.annotation.Table;

/**
 * 简单用户实体，测试基本 CRUD。
 */
@Table(name = "T_USER")
public class User {

    @Id
    private String id;

    private String name;

    private Integer age;

    private Double score;

    public User() {
    }

    public User(String id, String name, Integer age, Double score) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.score = score;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }
}
