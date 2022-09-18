package com.github.jinghui70.rainbow.dbaccess;

import java.util.Arrays;

public class QueryParam implements Pager {

    private Cnd[] cnds;

    private int pageNo;

    private int pageSize;

    private String sortField;

    private String sortOrder;

    public boolean hasCnd() {
        return cnds != null && cnds.length > 0;
    }

    public Cnd[] getCnds() {
        return cnds;
    }

    public void setCnds(Cnd[] cnds) {
        this.cnds = cnds;
    }

    @Override
    public int getPageNo() {
        return pageNo;
    }

    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }

    @Override
    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public String getSortField() {
        return sortField;
    }

    public void setSortField(String sortField) {
        this.sortField = sortField;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }

    @Override
    public String toString() {
        return "QueryParam [cnds:" + Arrays.toString(cnds) + ", pageNo:" + pageNo + ", pageSize:" + pageSize + "]";
    }

}
