package io.github.jinghui70.rainbow.dbaccess.object;

import cn.hutool.core.bean.PropDesc;
import io.github.jinghui70.rainbow.dbaccess.Dba;
import io.github.jinghui70.rainbow.dbaccess.annotation.GeneratedValue;
import io.github.jinghui70.rainbow.dbaccess.annotation.Id;
import io.github.jinghui70.rainbow.dbaccess.fieldmapper.FieldMapper;
import io.github.jinghui70.rainbow.dbaccess.fieldmapper.FieldValue;
import io.github.jinghui70.rainbow.dbaccess.valuegen.GenerateContext;
import io.github.jinghui70.rainbow.dbaccess.valuegen.ValueGenerator;
import io.github.jinghui70.rainbow.dbaccess.valuegen.ValueGeneratorRegistry;
import org.springframework.jdbc.support.JdbcUtils;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Bean属性信息，封装了属性的元数据和访问方法。
 * 用于数据库列与Bean属性之间的映射转换。
 */
public class PropInfo {

    private final String fieldName;
    private final PropDesc propDesc;
    private final FieldMapper<?> mapper;
    private final Id id;
    private final GeneratedValue generatedValue;

    /**
     * 获取字段名（数据库列名，下划线风格）。
     *
     * @return 数据库列名
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * 获取Id注解信息。
     *
     * @return Id注解实例，如果没有则返回null
     */
    public Id getId() {
        return id;
    }

    /**
     * 获取字段映射器。
     *
     * @return FieldMapper实例，如果没有则返回null
     */
    public FieldMapper<?> getMapper() {
        return mapper;
    }

    /**
     * 构造函数。
     *
     * @param fieldName 数据库列名
     * @param propDesc  属性描述
     * @param mapper    字段映射器
     */
    public PropInfo(String fieldName, PropDesc propDesc, FieldMapper<?> mapper) {
        this.fieldName = fieldName;
        this.propDesc = propDesc;
        this.mapper = mapper;
        this.id = propDesc.getField().getAnnotation(Id.class);
        this.generatedValue = propDesc.getField().getAnnotation(GeneratedValue.class);
    }

    /**
     * 获取属性名（Bean字段名，驼峰风格）。
     *
     * @return Bean属性名
     */
    public String getName() {
        return propDesc.getRawFieldName();
    }

    /**
     * 从对象中取值，准备用来保存到数据库中
     *
     * @param object 取值的对象
     * @return 对应属性值
     */
    public Object getValue(Object object) {
        Object value = propDesc.getValue(object);
        if (value == null) return null;
        return mapper == null ? value : new FieldValue(value, mapper);
    }

    /**
     * 从数据库中取值
     *
     * @param rs    is the ResultSet holding the data
     * @param index is the column index
     * @return the value object
     * @throws SQLException if thrown by the JDBC API
     */
    public Object getValue(ResultSet rs, int index) throws SQLException {
        if (mapper != null)
            return mapper.formDB(rs, index);
        Class<?> type = propDesc.getFieldClass();
        return JdbcUtils.getResultSetValue(rs, index, type);
    }

    /**
     * 保存一个值到对象对应的属性中
     *
     * @param object 对象
     * @param value  需要保存的值
     */
    public void setValue(Object object, Object value) {
        propDesc.setValue(object, value);
    }

    /**
     * 判断是否为自增主键。
     *
     * @return 如果是自增主键返回true，否则返回false
     */
    public boolean isAutoIncrement() {
        Id id = getId();
        return id != null && id.autoIncrement();
    }

    /**
     * 返回用于插入语句的属性值。
     * <p>
     * 若对象当前值非 {@code null}，直接返回；否则当属性标注了 {@link GeneratedValue} 时，
     * 调用对应生成器生成值，回填到对象后返回，使插入后即可从入参对象拿到该值。
     * 既无值也无注解时返回 {@code null}。
     *
     * @param dba 数据库访问对象，传递给生成器供其使用
     * @param row 当前行对象
     * @return 用于插入的值，可能为 {@code null}
     */
    public Object getInsertValue(Dba dba, Object row) {
        Object value = getValue(row);
        if (value != null) return value;
        if (generatedValue == null) return null;
        ValueGenerator generator = ValueGeneratorRegistry.get(generatedValue.strategy());
        GenerateContext context = new GenerateContext(dba, row, propDesc.getField(), generatedValue.param());
        Object result = generator.generate(context);
        propDesc.setValue(row, result);
        return mapper == null ? result : new FieldValue(result, mapper);
    }
}
