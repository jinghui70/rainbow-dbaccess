package io.github.jinghui70.rainbow.dbaccess.fieldmapper.enumtest;

import io.github.jinghui70.rainbow.dbaccess.annotation.Id;

public class TEnum {

    @Id
    private int id;

    private MyEnum normalEnum;

    private MyCode codeEnum;

    private MyNumber numberEnum;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public MyEnum getNormalEnum() {
        return normalEnum;
    }

    public void setNormalEnum(MyEnum normalEnum) {
        this.normalEnum = normalEnum;
    }

    public MyCode getCodeEnum() {
        return codeEnum;
    }

    public void setCodeEnum(MyCode codeEnum) {
        this.codeEnum = codeEnum;
    }

    public MyNumber getNumberEnum() {
        return numberEnum;
    }

    public void setNumberEnum(MyNumber numberEnum) {
        this.numberEnum = numberEnum;
    }

}
