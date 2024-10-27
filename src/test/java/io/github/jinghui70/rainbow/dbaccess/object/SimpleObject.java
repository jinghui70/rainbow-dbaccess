package io.github.jinghui70.rainbow.dbaccess.object;

import io.github.jinghui70.rainbow.dbaccess.annotation.ArrayField;
import io.github.jinghui70.rainbow.dbaccess.annotation.Id;

public class SimpleObject {

    @Id
    private int id;

    private String name;

    @ArrayField(length = 3, underline = true, start = 1)
    private Double[] score;

    public SimpleObject() {
    }

    public SimpleObject(int id, String name, Double[] score) {
        this.id = id;
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

    public Double[] getScore() {
        return score;
    }

    public void setScore(Double[] score) {
        this.score = score;
    }
}
