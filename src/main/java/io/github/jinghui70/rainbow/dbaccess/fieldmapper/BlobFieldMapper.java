package io.github.jinghui70.rainbow.dbaccess.fieldmapper;

import cn.hutool.core.util.ZipUtil;
import org.springframework.lang.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * BLOB 字段映射器的抽象基类。
 * <p>
 * 提供将 Java 对象与数据库 BLOB 类型进行互相转换的通用功能，
 * 支持可选的 GZIP 压缩。子类需要实现 {@link #getBytes(Object)} 和 {@link #parse(byte[])} 方法。
 *
 * @param <T> 目标 Java 类型
 */
public abstract class BlobFieldMapper<T> extends FieldMapper<T> {

    /**
     * 是否启用压缩
     */
    protected boolean compress = false;

    /**
     * 返回是否启用压缩。
     *
     * @return 如果启用压缩返回 {@code true}，否则返回 {@code false}
     */
    public boolean isCompress() {
        return compress;
    }

    /**
     * 设置是否启用压缩。
     *
     * @param compress 是否启用压缩
     */
    public void setCompress(boolean compress) {
        this.compress = compress;
    }

    /**
     * 启用压缩（链式方法）。
     *
     * @return 当前实例，用于链式调用
     */
    public BlobFieldMapper<T> compress() {
        this.compress = true;
        return this;
    }

    /**
     * 将 Java 值转换为字节数组。
     *
     * @param value Java 值
     * @return 字节数组
     */
    protected abstract byte[] getBytes(Object value);

    /**
     * 从字节数组解析为 Java 值。
     *
     * @param bytes 字节数组
     * @return Java 值
     */
    protected abstract T parse(byte[] bytes);

    /**
     * 从 ResultSet 读取 BLOB 字段并转换为目标类型。
     * <p>
     * 如果启用了压缩，会自动进行 GZIP 解压。
     *
     * @param rs    JDBC ResultSet
     * @param index 列索引
     * @return 转换后的 Java 值
     * @throws SQLException 如果数据库访问出错
     */
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

    /**
     * 将 Java 值保存到 PreparedStatement 的 BLOB 参数中。
     * <p>
     * 如果启用了压缩，会自动进行 GZIP 压缩。
     *
     * @param ps         JDBC PreparedStatement
     * @param paramIndex 参数索引
     * @param value      要保存的值
     * @throws SQLException 如果数据库访问出错
     */
    @Override
    public void saveToDB(PreparedStatement ps, int paramIndex, @NonNull Object value) throws SQLException {
        byte[] bytes = getBytes(value);
        if (compress)
            bytes = ZipUtil.gzip(bytes);
        ps.setBytes(paramIndex, bytes);
    }

}
