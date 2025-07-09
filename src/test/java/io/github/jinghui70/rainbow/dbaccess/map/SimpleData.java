package io.github.jinghui70.rainbow.dbaccess.map;

import java.time.LocalDateTime;

public class SimpleData {

    private String id;

    private int revision;

    private String createdBy;

    private LocalDateTime createdTime;

    private String updatedBy;

    private LocalDateTime updatedTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getRevision() {
        return revision;
    }

    public void setRevision(int revision) {
        this.revision = revision;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public LocalDateTime getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(LocalDateTime updatedTime) {
        this.updatedTime = updatedTime;
    }

    public void logCreate() {
        this.createdBy = "admin";
        this.createdTime = LocalDateTime.now();
        this.updatedBy = "admin";
        this.updatedTime = LocalDateTime.now();
    }

    public void logUpdate() {
        this.updatedBy = "admin";
        this.updatedTime = LocalDateTime.now();
    }
}
