package io.github.jinghui70.rainbow.dbaccess.fieldmapper;

import java.nio.charset.StandardCharsets;

/**
 * BLOB 字符串字段映射器。
 * <p>
 * 将 Java 字符串以 UTF-8 编码存储为 BLOB，
 * 读取时从 BLOB 解码为字符串。
 */
public class BlobStringFieldMapper extends BlobFieldMapper<String> {

    /**
     * 将字符串转换为 UTF-8 字节数组。
     *
     * @param value 字符串值
     * @return UTF-8 编码的字节数组
     */
    @Override
    protected byte[] getBytes(Object value) {
        String string = (String) value;
        return string.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * 从 UTF-8 字节数组解析为字符串。
     *
     * @param bytes UTF-8 编码的字节数组
     * @return 解析后的字符串
     */
    @Override
    protected String parse(byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8);
    }

}
