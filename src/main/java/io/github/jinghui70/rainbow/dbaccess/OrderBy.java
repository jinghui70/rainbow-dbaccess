package io.github.jinghui70.rainbow.dbaccess;

import io.github.jinghui70.rainbow.utils.StringBuilderX;

public class OrderBy {

    private String field;
    private boolean desc;

    public OrderBy() {
    }

    public OrderBy(String field, boolean desc) {
        this.field = field;
        this.desc = desc;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public boolean isDesc() {
        return desc;
    }

    public void setDesc(boolean desc) {
        this.desc = desc;
    }

    @Override
    public String toString() {
        return new StringBuilderX(field).append(desc, " DESC").toString();
    }
}
