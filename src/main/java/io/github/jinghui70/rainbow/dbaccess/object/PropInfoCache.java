package io.github.jinghui70.rainbow.dbaccess.object;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.PropDesc;
import cn.hutool.core.map.WeakConcurrentMap;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import io.github.jinghui70.rainbow.dbaccess.annotation.*;
import io.github.jinghui70.rainbow.dbaccess.enumSupport.EnumMapper;
import io.github.jinghui70.rainbow.dbaccess.fieldmapper.*;

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
            ArrayField arrayAnnotation = propDesc.getField().getAnnotation(ArrayField.class);
            if (arrayAnnotation == null) {
                Id id = propDesc.getField().getAnnotation(Id.class);
                result.put(fieldName, new PropInfo(fieldName, propDesc, mapper, id));
            } else {
                if (mapper == null) mapper = isEnumOrBooleanMapper(propDesc.getFieldClass().getComponentType());
                String join = arrayAnnotation.underline() ? "_" : "";
                for (int i = 0; i < arrayAnnotation.length(); i++) {
                    String field = String.format("%s%s%d", fieldName, join, i + arrayAnnotation.start());
                    result.put(field, new PropInfo(field, propDesc, mapper, i));
                }
            }
        });
        return result;
    }

    /**
     * 根据字段配置，获取 FieldMapper 对象
     *
     * @param column  字段配置
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
        LobType lobType = column.lobType();
        switch (lobType) {
            case BLOB:
                if (fieldClass == String.class)
                    return new BlobStringFieldMapper();
                if (fieldClass == byte[].class)
                    return new BlobByteArrayFieldMapper();
                return new BlobObjectFieldMapper(fieldClass, propDesc.getField());
            case CLOB:
                // 如果是字符串，暂时没有必要做特殊处理，因为对象中的字符串要读到内存中，当做普通的字符串处理了
                if (fieldClass == String.class)
                    return null;
                return new ClobObjectFieldMapper(fieldClass, propDesc.getField());
            default:
                return isEnumOrBooleanMapper(fieldClass);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static FieldMapper<?> isEnumOrBooleanMapper(Class<?> fieldClass) {
        if (fieldClass.isEnum()) {
            return new EnumMapper(fieldClass);
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
