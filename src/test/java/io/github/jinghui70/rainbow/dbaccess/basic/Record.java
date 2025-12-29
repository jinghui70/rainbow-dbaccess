package io.github.jinghui70.rainbow.dbaccess.basic;

public class Record {
    private String org;
    private int id;
    private String name;
    private String status;

    public Record() {
    }

    public Record(String org, int id, String name, String status) {
        this.org = org;
        this.id = id;
        this.name = name;
        this.status = status;
    }

    public String getOrg() {
        return org;
    }

    public void setOrg(String org) {
        this.org = org;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
