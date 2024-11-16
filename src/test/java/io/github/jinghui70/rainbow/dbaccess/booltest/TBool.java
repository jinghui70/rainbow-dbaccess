package io.github.jinghui70.rainbow.dbaccess.booltest;

import io.github.jinghui70.rainbow.dbaccess.annotation.ArrayField;
import io.github.jinghui70.rainbow.dbaccess.annotation.Column;
import io.github.jinghui70.rainbow.dbaccess.annotation.Id;
import io.github.jinghui70.rainbow.dbaccess.fieldmapper.BoolTF;
import io.github.jinghui70.rainbow.dbaccess.fieldmapper.BoolYN;

public class TBool {

    @Id
    private String id;

    private Boolean intBool;

    private Boolean stringBool;

    @Column(mapper= BoolYN.class)
    private Boolean ynBool;

    @Column(mapper= BoolTF.class)
    private Boolean tfBool;

    @ArrayField(length = 3, start = 1, underline = true)
    @Column(mapper= BoolYN.class)
    private Boolean[] array;

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

    public Boolean getTfBool() {
        return tfBool;
    }

    public void setTfBool(Boolean tfBool) {
        this.tfBool = tfBool;
    }

    public Boolean[] getArray() {
        return array;
    }

    public void setArray(Boolean[] array) {
        this.array = array;
    }
}
