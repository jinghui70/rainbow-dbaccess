package io.github.jinghui70.rainbow.dbaccess.fieldmapper;

public class BlobByteArrayFieldMapper extends BlobFieldMapper<byte[]> {

    public BlobByteArrayFieldMapper() {
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
