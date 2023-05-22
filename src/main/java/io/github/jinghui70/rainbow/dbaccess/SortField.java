package io.github.jinghui70.rainbow.dbaccess;

public class SortField {

    // 排序字段
    private String field;

    // 排序方向
    private boolean desc;

    public SortField() {
    }
    
    public SortField(String field) {
        this.field = field;
    }

    public SortField(String field, boolean desc) {
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

    public String toString() {
        return desc ? field + " DESC" : field;
    }

}
