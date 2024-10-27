package io.github.jinghui70.rainbow.dbaccess.object;

import io.github.jinghui70.rainbow.dbaccess.annotation.ArrayField;
import io.github.jinghui70.rainbow.dbaccess.annotation.Column;
import io.github.jinghui70.rainbow.dbaccess.annotation.Id;

public class AutoIncrementObject {

    @Id(autoIncrement = true)
    private int id;

    private String name;

    @Column(name = "SCORE")
    @ArrayField(length = 3, start = 1, underline = true)
    private Double[] scores;

    public AutoIncrementObject() {
    }

    public AutoIncrementObject(String name, Double[] scores) {
        this.name = name;
        this.scores = scores;
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

    public Double[] getScores() {
        return scores;
    }

    public void setScores(Double[] scores) {
        this.scores = scores;
    }
}
