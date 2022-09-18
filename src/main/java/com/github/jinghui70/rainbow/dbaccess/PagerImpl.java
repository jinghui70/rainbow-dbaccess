package com.github.jinghui70.rainbow.dbaccess;

public class PagerImpl implements Pager {

    private int pageNo;

    private int pageSize;

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
}
