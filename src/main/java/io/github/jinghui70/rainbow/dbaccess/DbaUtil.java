package io.github.jinghui70.rainbow.dbaccess;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.PropDesc;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import io.github.jinghui70.rainbow.dbaccess.annotation.*;

import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Collectors;

public abstract class DbaUtil {

    /**
     * 根据一个对象的类，得到它对应的数据表名
     *
     * @param clazz 对象类
     * @return 数据表名
     */
    public static String tableName(Class<?> clazz) {
        Table entityAnnotation = clazz.getAnnotation(Table.class);
        return entityAnnotation == null ? StrUtil.toUnderlineCase(clazz.getSimpleName()) :
                entityAnnotation.name();
    }

    /**
     * 根据一个对象的类定义，找到所有的主键对应的字段名
     *
     * @param clazz 对象类
     * @return 主键字段名列表
     */
    public static List<String> keyProps(Class<?> clazz) {
        List<String> keys = BeanUtil.getBeanDesc(clazz).getProps().stream()
                .map(PropDesc::getField)
                .filter(field -> field.getAnnotation(Id.class) != null)
                .map(field -> {
                    Column fieldAnnotation = field.getAnnotation(Column.class);
                    return fieldAnnotation == null ? StrUtil.toUnderlineCase(field.getName()) :
                            fieldAnnotation.name();
                }).collect(Collectors.toList());
        Assert.isTrue(keys.size() > 0, "no key field set of {}", clazz);
        return keys;
    }

    /**
     * 从数据库中获取的Map，用的是下划线链接的字段名，转为对象，对象属性是驼峰
     *
     * @param <T>   对象泛型
     * @param map   数据库中获取的map
     * @param clazz 对象类型
     * @return 转换后的对象
     */
    public static <T> T mapToBean(Map<String, Object> map, Class<T> clazz) {
        T result = org.springframework.beans.BeanUtils.instantiateClass(clazz);
        for (PropDesc prop : BeanUtil.getBeanDesc(clazz).getProps()) {
            Column fieldAnnotation = prop.getField().getAnnotation(Column.class);
            String fieldName = fieldAnnotation == null ? StrUtil.toUnderlineCase(prop.getRawFieldName()) :
                    fieldAnnotation.name();
            ArrayField a = prop.getField().getAnnotation(ArrayField.class);
            if (a == null) {
                Object value = map.get(fieldName);
                if (value != null)
                    prop.setValue(result, value);
            } else {
                Class<?> type = prop.getFieldClass().getComponentType();
                Object array = Array.newInstance(type, a.length());
                prop.setValue(result, array);
                String join = a.underline() ? "_" : "";
                for (int i = 0; i < a.length(); i++) {
                    String field = String.format("%s%s%d", fieldName, join, i + a.start());
                    Object value = map.get(field);
                    if (value != null)
                        Array.set(array, i, value);
                }
            }
        }
        return result;
    }

    /**
     * 把一个对象，转为保存到数据库用到Map之中
     *
     * @param bean             对象
     * @param ignoreNullValue  是否忽略为空的值
     * @return 转化后的Map
     */
    public static Map<String, Object> beanToMap(Object bean, boolean ignoreNullValue) {
        Map<String, Object> result = new LinkedHashMap<>();
        BeanUtil.descForEach(bean.getClass(), prop -> {
            if (prop.getField().getAnnotation(Transient.class) != null)
                return;
            String fieldName = prop.getRawFieldName();
            Column filedAnnotation = prop.getField().getAnnotation(Column.class);
            fieldName = filedAnnotation == null ? StrUtil.toUnderlineCase(fieldName) : filedAnnotation.name();
            Object value = prop.getValue(bean);
            if (null == value && ignoreNullValue)
                return;
            if (value instanceof Enum<?>) {
                value = ((Enum<?>) value).ordinal();
                result.put(fieldName, value);
                return;
            }
            ArrayField arrayAnnotation = prop.getField().getAnnotation(ArrayField.class);
            if (arrayAnnotation == null) {
                result.put(fieldName, value);
                return;
            }
            String join = arrayAnnotation.underline() ? "_" : "";
            if (ArrayUtil.isEmpty(value)) {
                if (!ignoreNullValue) {
                    for (int i = 0; i < arrayAnnotation.length(); i++) {
                        String field = String.format("%s%s%d", fieldName, join, i + arrayAnnotation.start());
                        result.put(field, null);
                    }
                }
            } else {
                int realLength = Array.getLength(value);
                for (int i = 0; i < arrayAnnotation.length(); i++) {
                    Object element = i >= realLength ? null : Array.get(value, i);
                    if (element != null || !ignoreNullValue) {
                        String field = String.format("%s%s%d", fieldName, join, i + arrayAnnotation.start());
                        result.put(field, element);
                    }
                }
            }
        });
        return result;
    }

}