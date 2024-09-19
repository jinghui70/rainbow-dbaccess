package io.github.jinghui70.rainbow.dbaccess.enumtest;

import io.github.jinghui70.rainbow.dbaccess.enumSupport.CodeEnum;

public enum MyCode implements CodeEnum {

    A("甲"),
    B("乙"),
    C("丙");

    private String code;

    @Override
    public String code() {
        return code;
    }

    MyCode(String code) {
        this.code = code;
    }
}
