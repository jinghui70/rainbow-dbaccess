package io.github.jinghui70.rainbow.dbaccess.fieldmapper;

public class BoolYN extends BoolStringFieldMapper {

    @Override
    protected String getTrue() {
        return "Y";
    }

    @Override
    protected String getFalse() {
        return "N";
    }

}
