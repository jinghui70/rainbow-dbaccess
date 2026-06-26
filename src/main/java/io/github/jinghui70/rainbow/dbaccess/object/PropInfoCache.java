package io.github.jinghui70.rainbow.dbaccess.object;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.PropDesc;
import cn.hutool.core.map.WeakConcurrentMap;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import io.github.jinghui70.rainbow.dbaccess.annotation.Column;
import io.github.jinghui70.rainbow.dbaccess.annotation.Transient;
import io.github.jinghui70.rainbow.dbaccess.fieldmapper.*;

import java.sql.Types;
import java.util.LinkedHashMap;

/**
 * Bean属性信息缓存工具类。
 * 缓存类与属性信息的映射关系，避免重复解析。
 */
public class PropInfoCache {

    private static final WeakConcurrentMap<Class<?>, LinkedHashMap<String, PropInfo>> cache = new WeakConcurrentMap<>();


    /**
     * 获取指定类的属性信息缓存。
     *
     * @param beanClass Bean类
     * @return 属性信息Map，key为数据库列名，value为PropInfo
     */
    public static LinkedHashMap<String, PropInfo> get(Class<?> beanClass) {
        return cache.computeIfAbsent(beanClass, (key) -> createPropInfo(beanClass));
    }

    /**
     * 创建指定类的属性信息。
     *
     * @param clazz Bean类
     * @return 属性信息Map，key为数据库列名，value为PropInfo
     */
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
            result.put(fieldName, new PropInfo(fieldName, propDesc, mapper));
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
        switch (column.sqlType()) {
            case Types.BLOB:
                BlobFieldMapper mapper;
                if (fieldClass == String.class)
                    mapper = new BlobStringFieldMapper();
                else if (fieldClass == byte[].class)
                    mapper = new BlobByteArrayFieldMapper();
                else
                    mapper = new BlobObjectFieldMapper(fieldClass, propDesc.getField());
                mapper.setCompress(column.compress());
                return mapper;
            case Types.CLOB:
            case Types.VARCHAR:
                // 如果是字符串，暂时没有必要做特殊处理
                if (fieldClass == String.class)
                    return null;
                return new ObjectFieldMapper(fieldClass, propDesc.getField());
            default:
                return isEnumOrBooleanMapper(fieldClass);
        }
    }

    /**
     * 根据字段配置，获取 FieldMapper 对象。
     *
     * @param fieldClass 字段类型
     * @return FieldMapper 对象，如果不需要特殊映射则返回null
     */
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
