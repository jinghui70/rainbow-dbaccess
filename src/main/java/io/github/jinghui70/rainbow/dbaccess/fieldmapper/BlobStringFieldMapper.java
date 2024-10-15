package io.github.jinghui70.rainbow.dbaccess.fieldmapper;

import java.nio.charset.StandardCharsets;

public class BlobStringFieldMapper extends BlobFieldMapper<String> {

    public BlobStringFieldMapper() {
        super(true);
    }

    @Override
    protected byte[] getBytes(Object value) {
        String string = (String) value;
        return string.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    protected String parse(byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8);
    }

}
