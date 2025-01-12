package io.github.jinghui70.rainbow.dbaccess.fieldmapper;

import cn.hutool.core.util.ZipUtil;
import org.springframework.lang.NonNull;

import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class BlobFieldMapper<T> extends FieldMapper<T> {

    protected boolean compress;

    protected BlobFieldMapper() {
        this(true);
    }

    protected BlobFieldMapper(boolean compress) {
        this.compress = compress;
    }

    protected abstract byte[] getBytes(Object value);

    protected abstract T parse(byte[] bytes);

    @Override
    public T formDB(ResultSet rs, int index) throws SQLException {
        Blob blob = rs.getBlob(index);
        if (rs.wasNull()) return null;
        byte[] bytes = compress ? ZipUtil.unGzip(blob.getBinaryStream()) : blob.getBytes(1, (int) blob.length());
        return parse(bytes);
    }

    @Override
    public void saveToDB(PreparedStatement ps, int paramIndex, @NonNull Object value) throws SQLException {
        byte[] bytes = getBytes(value);
        if (compress)
            bytes = ZipUtil.gzip(bytes);
        ps.setBytes(paramIndex, bytes);
    }

}
