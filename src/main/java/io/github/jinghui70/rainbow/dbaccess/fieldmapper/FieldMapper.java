package io.github.jinghui70.rainbow.dbaccess.fieldmapper;

import org.springframework.lang.NonNull;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 字段映射器抽象基类。
 * <p>
 * 定义 Java 字段与数据库列之间的读写转换规则，子类实现具体的序列化和反序列化逻辑。
 *
 * @param <T> Java 字段类型
 */
public abstract class FieldMapper<T> {

    /**
     * 从 {@link ResultSet} 读取字段值。
     *
     * @param rs    结果集
     * @param index 列索引
     * @return 转换后的字段值
     * @throws SQLException 数据库访问异常
     */
    public abstract T formDB(ResultSet rs, int index) throws SQLException;

    /**
     * 将字段值写入 {@link PreparedStatement}。子类可覆写此方法，将值转为数据库可接受的类型。
     *
     * @param ps         预编译语句
     * @param paramIndex 参数索引
     * @param value      字段值
     * @throws SQLException 数据库访问异常
     */
    public void saveToDB(PreparedStatement ps, int paramIndex, @NonNull Object value) throws SQLException {
        ps.setObject(paramIndex, value);
    }

    /**
     * 创建一个包装了值和当前映射器的 FieldValue 对象。
     *
     * @param value 字段值
     * @return FieldValue 对象
     */
    public FieldValue ofValue(Object value) {
        return new FieldValue(value, this);
    }

}
