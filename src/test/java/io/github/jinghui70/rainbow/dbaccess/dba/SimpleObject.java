package io.github.jinghui70.rainbow.dbaccess.dba;

import io.github.jinghui70.rainbow.dbaccess.annotation.Id;

public class SimpleObject {

    @Id
    private int id;

    private String name;

    public SimpleObject() {
    }

    public SimpleObject(int id, String name) {
        this.id = id;
        this.name = name;
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
}
