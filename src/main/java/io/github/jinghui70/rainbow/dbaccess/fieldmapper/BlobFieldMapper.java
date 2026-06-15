package io.github.jinghui70.rainbow.dbaccess.fieldmapper;

import cn.hutool.core.util.ZipUtil;
import org.springframework.lang.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class BlobFieldMapper<T> extends FieldMapper<T> {

    protected boolean compress = false;

    public boolean isCompress() {
        return compress;
    }

    public void setCompress(boolean compress) {
        this.compress = compress;
    }

    public BlobFieldMapper<T> compress() {
        this.compress = true;
        return this;
    }

    protected abstract byte[] getBytes(Object value);

    protected abstract T parse(byte[] bytes);

    @Override
    public T formDB(ResultSet rs, int index) throws SQLException {
        Blob blob = rs.getBlob(index);
        if (rs.wasNull()) return null;
        try {
            byte[] bytes;
            if (compress) {
                try (InputStream is = blob.getBinaryStream()) {
                    bytes = ZipUtil.unGzip(is);
                } catch (IOException e) {
                    throw new RuntimeException("unzip blob failed", e);
                }
            } else {
                bytes = blob.getBytes(1, (int) blob.length());
            }
            return parse(bytes);
        } finally {
            blob.free();
        }
    }

    @Override
    public void saveToDB(PreparedStatement ps, int paramIndex, @NonNull Object value) throws SQLException {
        byte[] bytes = getBytes(value);
        if (compress)
            bytes = ZipUtil.gzip(bytes);
        ps.setBytes(paramIndex, bytes);
    }

}
