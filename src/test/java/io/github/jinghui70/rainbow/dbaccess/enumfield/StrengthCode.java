package io.github.jinghui70.rainbow.dbaccess.enumfield;

import io.github.jinghui70.rainbow.utils.CodeEnum;

public enum StrengthCode implements CodeEnum {

    强("1"), 中("2"), 弱("3");

    private final String code;

    StrengthCode(String code) {
        this.code = code;
    }

    @Override
    public String code() {
        return code;
    }
}
