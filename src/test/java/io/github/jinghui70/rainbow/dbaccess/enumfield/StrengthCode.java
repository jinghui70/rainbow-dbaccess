package io.github.jinghui70.rainbow.dbaccess.enumfield;

import io.github.jinghui70.rainbow.utils.ICodeObject;

public enum StrengthCode implements ICodeObject {

    强("1"), 中("2"), 弱("3");

    private String code;

    StrengthCode(String code) {
        this.code = code;
    }

    @Override
    public String getCode() {
        return code;
    }
}
