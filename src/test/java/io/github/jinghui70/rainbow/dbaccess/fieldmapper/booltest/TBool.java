package io.github.jinghui70.rainbow.dbaccess.fieldmapper.booltest;

import io.github.jinghui70.rainbow.dbaccess.annotation.Column;
import io.github.jinghui70.rainbow.dbaccess.annotation.Id;
import io.github.jinghui70.rainbow.dbaccess.fieldmapper.BoolYN;

public class TBool {

    @Id
    private String id;

    private Boolean intBool;

    private Boolean stringBool;

    @Column(mapper= BoolYN.class)
    private Boolean ynBool;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Boolean getIntBool() {
        return intBool;
    }

    public void setIntBool(Boolean intBool) {
        this.intBool = intBool;
    }

    public Boolean getStringBool() {
        return stringBool;
    }

    public void setStringBool(Boolean stringBool) {
        this.stringBool = stringBool;
    }

    public Boolean getYnBool() {
        return ynBool;
    }

    public void setYnBool(Boolean ynBool) {
        this.ynBool = ynBool;
    }

}
