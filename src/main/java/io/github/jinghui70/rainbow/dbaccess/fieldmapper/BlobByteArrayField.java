package io.github.jinghui70.rainbow.dbaccess.fieldmapper;

public class BlobByteArrayField extends BlobField<byte[]> {

    public BlobByteArrayField() {
        super(false);
    }

    @Override
    protected byte[] getBytes(Object value) {
        return (byte[]) value;
    }

    @Override
    protected byte[] parse(byte[] bytes) {
        return bytes;
    }
}
