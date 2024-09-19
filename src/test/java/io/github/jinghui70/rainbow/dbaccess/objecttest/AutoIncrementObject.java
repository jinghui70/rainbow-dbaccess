package io.github.jinghui70.rainbow.dbaccess.objecttest;

import io.github.jinghui70.rainbow.dbaccess.annotation.ArrayField;
import io.github.jinghui70.rainbow.dbaccess.annotation.Column;
import io.github.jinghui70.rainbow.dbaccess.annotation.Id;
import io.github.jinghui70.rainbow.dbaccess.annotation.Table;

@Table(name = "X")
public class AutoIncrementObject {

    @Id(autoIncrement = true)
    private int id;

    private String name;

    @Column(name = "SCORE")
    @ArrayField(length = 3, start = 1, underline = true)
    private double[] scores;

    public AutoIncrementObject() {
    }

    public AutoIncrementObject(String name, double[] scores) {
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

    public double[] getScores() {
        return scores;
    }

    public void setScores(double[] scores) {
        this.scores = scores;
    }
}
