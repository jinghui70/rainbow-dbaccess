package io.github.jinghui70.rainbow.dbaccess.object;

import io.github.jinghui70.rainbow.dbaccess.annotation.Column;
import io.github.jinghui70.rainbow.dbaccess.annotation.Id;

public class AutoIncrementObject {

    @Id(autoIncrement = true)
    private int id;

    private String name;

    private Double score;

    public AutoIncrementObject() {
    }

    public AutoIncrementObject(String name, Double score) {
        this.name = name;
        this.score = score;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }
}
