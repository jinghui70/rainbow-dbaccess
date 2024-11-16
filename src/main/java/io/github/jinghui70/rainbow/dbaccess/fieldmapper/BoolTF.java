package io.github.jinghui70.rainbow.dbaccess.fieldmapper;

public class BoolTF extends BoolStringFieldMapper {

    @Override
    protected String getTrue() {
        return "T";
    }

    @Override
    protected String getFalse() {
        return "F";
    }

}
