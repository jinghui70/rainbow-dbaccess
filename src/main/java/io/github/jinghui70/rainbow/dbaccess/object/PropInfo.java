package io.github.jinghui70.rainbow.dbaccess.object;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.PropDesc;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import io.github.jinghui70.rainbow.dbaccess.annotation.*;
import io.github.jinghui70.rainbow.dbaccess.enumSupport.EnumMapper;
import io.github.jinghui70.rainbow.dbaccess.fieldmapper.*;
import org.h2.value.CaseInsensitiveMap;
import org.springframework.jdbc.support.JdbcUtils;

import java.lang.reflect.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PropInfo {

    private final String fieldName;
    private final PropDesc propDesc;
    private FieldMapper<?> mapper;
    private final int index;
    private Id id;

    public String getFieldName() {
        return fieldName;
    }

    public PropDesc getPropDesc() {
        return propDesc;
    }

    public int getIndex() {
        return index;
    }

    public Id getId() {
        return id;
    }

    public PropInfo(String fieldName, PropDesc propDesc, FieldMapper mapper, Id id) {
        this(fieldName, propDesc, mapper, -1);
        this.id = id;
    }

    public PropInfo(String fieldName, PropDesc propDesc, FieldMapper mapper, int index) {
        this.fieldName = fieldName;
        this.propDesc = propDesc;
        this.mapper = mapper;
        this.index = index;
    }

    /**
     * 如果是数组属性，创建一个新数组
     *
     * @return
     */
    public Object newArray() {
        Class<?> componentType = propDesc.getFieldClass().getComponentType();
        ArrayField annotation = propDesc.getField().getAnnotation(ArrayField.class);
        return Array.newInstance(componentType, annotation.length());
    }

    /**
     * 从对象中取值
     *
     * @param object
     * @return
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
     * @param rs
     * @param index
     * @return
     * @throws SQLException
     */
    public Object getValue(ResultSet rs, int index) throws SQLException {
        if (mapper != null)
            return mapper.formDB(rs, index);
        Class<?> type = propDesc.getFieldClass();
        if (this.index >= 0) type = type.getComponentType();
        return JdbcUtils.getResultSetValue(rs, index, type);
    }

    public boolean isAutoIncrement() {
        return id != null && id.autoIncrement();
    }

    /**
     * 根据字段配置，获取 FieldMapper 对象
     *
     * @param column
     * @param propDesc
     * @return
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static FieldMapper<?> getMapper(Column column, PropDesc propDesc) {
        Class<?> fieldClass = propDesc.getFieldClass();
        if (column == null) return fieldClass.isEnum() ? new EnumMapper(fieldClass) : null;
        Class<? extends FieldMapper> mapperClass = column.mapper();
        if (mapperClass != FieldMapper.class) {
            return ReflectUtil.newInstance(mapperClass);
        }
        LobType lobType = column.lobType();
        switch (lobType) {
            case BLOB:
                if (fieldClass == String.class)
                    return new BlobStringField();
                if (fieldClass == byte[].class)
                    return new BlobByteArrayField();
                return new BlobObjectField(fieldClass);
            case CLOB: // 暂时没有必要做特殊处理，因为对象中的字符串要读到内存中，当做普通的字符串处理了
                return null;
            default:
                return fieldClass.isEnum() ? new EnumMapper(fieldClass) : null;
        }
    }

    /**
     * 获取一个类数据库属性相关信息列表
     *
     * @param clazz
     * @return
     */
    public static List<PropInfo> getPropInfoList(Class<?> clazz) {
        List<PropInfo> result = new ArrayList<>();
        BeanUtil.descForEach(clazz, propDesc -> {
            if (propDesc.getField().getAnnotation(Transient.class) != null)
                return;
            Column column = propDesc.getField().getAnnotation(Column.class);
            FieldMapper<?> mapper = getMapper(column, propDesc);
            String fieldName = column == null || StrUtil.isEmpty(column.name()) ?
                    StrUtil.toUnderlineCase(propDesc.getRawFieldName()) : column.name();
            ArrayField arrayAnnotation = propDesc.getField().getAnnotation(ArrayField.class);
            if (arrayAnnotation == null) {
                Id id = propDesc.getField().getAnnotation(Id.class);
                result.add(new PropInfo(fieldName, propDesc, mapper, id));
            } else {
                String join = arrayAnnotation.underline() ? "_" : "";
                for (int i = 0; i < arrayAnnotation.length(); i++) {
                    String field = String.format("%s%s%d", fieldName, join, i + arrayAnnotation.start());
                    result.add(new PropInfo(field, propDesc, mapper, i));
                }
            }
        });
        return result;
    }

    public static Map<String, PropInfo> getPropInfoMap(Class<?> clazz) {
        Map<String, PropInfo> result = new CaseInsensitiveMap<>();
        for (PropInfo p : getPropInfoList(clazz)) {
            result.put(p.fieldName, p);
        }
        return result;
    }
}
