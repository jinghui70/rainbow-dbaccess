package io.github.jinghui70.rainbow.dbaccess.object;

import cn.hutool.core.bean.PropDesc;
import io.github.jinghui70.rainbow.dbaccess.annotation.ArrayField;
import io.github.jinghui70.rainbow.dbaccess.annotation.Id;
import io.github.jinghui70.rainbow.dbaccess.fieldmapper.FieldMapper;
import io.github.jinghui70.rainbow.dbaccess.fieldmapper.FieldValue;
import org.springframework.jdbc.support.JdbcUtils;

import java.lang.reflect.Array;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PropInfo {

    private final String fieldName;
    private final PropDesc propDesc;
    private final FieldMapper<?> mapper;
    private final int index;
    private Id id;

    public String getFieldName() {
        return fieldName;
    }

    public int getIndex() {
        return index;
    }

    public Id getId() {
        return id;
    }

    public FieldMapper<?> getMapper() {
        return mapper;
    }

    public PropInfo(String fieldName, PropDesc propDesc, FieldMapper<?> mapper, Id id) {
        this(fieldName, propDesc, mapper, -1);
        this.id = id;
    }

    public PropInfo(String fieldName, PropDesc propDesc, FieldMapper<?> mapper, int index) {
        this.fieldName = fieldName;
        this.propDesc = propDesc;
        this.mapper = mapper;
        this.index = index;
    }

    /**
     * 如果是数组属性，创建一个新数组
     *
     * @return 新建的数组
     */
    private Object newArray() {
        Class<?> componentType = propDesc.getFieldClass().getComponentType();
        ArrayField annotation = propDesc.getField().getAnnotation(ArrayField.class);
        return Array.newInstance(componentType, annotation.length());
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
        if (index >= 0) {
            try {
                value = Array.get(value, index);
            } catch (ArrayIndexOutOfBoundsException e) {
                return null;
            }
        }
        return mapper == null ? value : new FieldValue(value, mapper);
    }

    /**
     * 从数据库中取值
     *
     * @param rs is the ResultSet holding the data
     * @param index is the column index
     * @return the value object
     * @throws SQLException if thrown by the JDBC API
     */
    public Object getValue(ResultSet rs, int index) throws SQLException {
        if (mapper != null)
            return mapper.formDB(rs, index);
        Class<?> type = propDesc.getFieldClass();
        if (this.index >= 0) type = type.getComponentType();
        return JdbcUtils.getResultSetValue(rs, index, type);
    }

    /**
     * 保存一个值到对象对应的属性中
     *
     * @param object 对象
     * @param value 需要保存的值
     */
    public void setValue(Object object, Object value) {
        if (index >= 0) {
            Object array = propDesc.getValue(object);
            if (array == null) {
                array = newArray();
                propDesc.setValue(object, array);
            }
            if (value != null)
                Array.set(array, index, value);
        } else {
            propDesc.setValue(object, value);
        }
    }

    public boolean isAutoIncrement() {
        return id != null && id.autoIncrement();
    }

}
