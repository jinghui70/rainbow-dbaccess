package io.github.jinghui70.rainbow.dbaccess.object;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.PropDesc;
import cn.hutool.core.map.WeakConcurrentMap;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import io.github.jinghui70.rainbow.dbaccess.annotation.Column;
import io.github.jinghui70.rainbow.dbaccess.annotation.Id;
import io.github.jinghui70.rainbow.dbaccess.annotation.Transient;
import io.github.jinghui70.rainbow.dbaccess.fieldmapper.*;

import java.sql.Types;
import java.util.LinkedHashMap;

public class PropInfoCache {

    private static final WeakConcurrentMap<Class<?>, LinkedHashMap<String, PropInfo>> cache = new WeakConcurrentMap<>();


    public static LinkedHashMap<String, PropInfo> get(Class<?> beanClass) {
        return cache.computeIfAbsent(beanClass, (key) -> createPropInfo(beanClass));
    }

    public static LinkedHashMap<String, PropInfo> createPropInfo(Class<?> clazz) {
        LinkedHashMap<String, PropInfo> result = new LinkedHashMap<>();
        BeanUtil.descForEach(clazz, propDesc -> {
            if (propDesc.getField().getAnnotation(Transient.class) != null)
                return;
            Column column = propDesc.getField().getAnnotation(Column.class);
            FieldMapper<?> mapper = getMapper(column, propDesc);
            String fieldName = column == null || StrUtil.isEmpty(column.name()) ?
                    StrUtil.toUnderlineCase(propDesc.getRawFieldName()) : column.name();
            fieldName = fieldName.toLowerCase();
            Id id = propDesc.getField().getAnnotation(Id.class);
            result.put(fieldName, new PropInfo(fieldName, propDesc, mapper, id));
        });
        return result;
    }

    /**
     * 根据字段配置，获取 FieldMapper 对象
     *
     * @param column   字段配置
     * @param propDesc 属性描述
     * @return FieldMapper 对象
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static FieldMapper<?> getMapper(Column column, PropDesc propDesc) {
        Class<?> fieldClass = propDesc.getFieldClass();
        if (column == null) return isEnumOrBooleanMapper(fieldClass);
        // 自定义的映射器
        Class<? extends FieldMapper> mapperClass = column.mapper();
        if (mapperClass != FieldMapper.class) {
            return ReflectUtil.newInstance(mapperClass);
        }
        // Lob 类型的映射器
        int sqlType = column.sqlType();
        switch (sqlType) {
            case Types.BLOB:
                if (fieldClass == String.class)
                    return new BlobStringFieldMapper();
                if (fieldClass == byte[].class)
                    return new BlobByteArrayFieldMapper();
                return new BlobObjectFieldMapper(fieldClass, propDesc.getField());
            case Types.CLOB:
            case Types.VARCHAR:
                // 如果是字符串，暂时没有必要做特殊处理
                if (fieldClass == String.class)
                    return null;
                return new StringObjectFieldMapper(fieldClass, propDesc.getField());
            default:
                return isEnumOrBooleanMapper(fieldClass);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static FieldMapper<?> isEnumOrBooleanMapper(Class<?> fieldClass) {
        if (fieldClass.isEnum()) {
            return new EnumFieldMapper(fieldClass);
        } else if (Boolean.class.equals(fieldClass) || boolean.class.equals(fieldClass)) {
            return BoolFieldMapper.INSTANCE;
        }
        return null;
    }

    /**
     * 清空全局的Bean属性缓存
     *
     * @since 5.7.21
     */
    public static void clear() {
        cache.clear();
    }

}
