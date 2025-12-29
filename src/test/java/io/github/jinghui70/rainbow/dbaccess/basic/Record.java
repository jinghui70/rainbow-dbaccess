package io.github.jinghui70.rainbow.dbaccess.basic;

import io.github.jinghui70.rainbow.dbaccess.annotation.Id;

public class Record {
    @Id
    private String orgId;
    @Id
    private int id;
    private String name;
    private String status;

    public Record() {
    }

    public Record(String orgId, int id, String name, String status) {
        this.orgId = orgId;
        this.id = id;
        this.name = name;
        this.status = status;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
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
