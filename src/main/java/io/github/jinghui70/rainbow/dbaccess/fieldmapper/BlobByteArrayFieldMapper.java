package io.github.jinghui70.rainbow.dbaccess.fieldmapper;

/**
 * BLOB 字段映射器，直接处理字节数组。
 * <p>
 * 将 Java 的 {@code byte[]} 与数据库的 BLOB 类型进行互相转换，
 * 不进行额外的编码或解码，直接使用原始字节数据。
 */
public class BlobByteArrayFieldMapper extends BlobFieldMapper<byte[]> {

    /**
     * 将值转换为字节数组。
     *
     * @param value 字段值，必须是 {@code byte[]} 类型
     * @return 字节数组
     */
    @Override
    protected byte[] getBytes(Object value) {
        return (byte[]) value;
    }

    /**
     * 从字节数组解析值，直接返回原始字节数组。
     *
     * @param bytes 从数据库读取的字节数组
     * @return 原始字节数组
     */
    @Override
    protected byte[] parse(byte[] bytes) {
        return bytes;
    }
}
