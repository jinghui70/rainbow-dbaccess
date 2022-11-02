package io.github.jinghui70.rainbow.dbaccess;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.PropDesc;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import io.github.jinghui70.rainbow.dbaccess.annotation.*;
import io.github.jinghui70.rainbow.utils.CodeEnum;

import java.lang.reflect.Array;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class DbaUtil {

    public static final String MERGE = "merge";

    public static final String INSERT = "insert";

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
                value = checkEnumPropValue(prop, value);
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
     * 检查一个属性是否是枚举属性，如果是则转换一个值为枚举值
     * 转换的原则是：
     * 1 如果值是数字，则按枚举ordinal匹配
     * 2 如果枚举实现了ICodeObject，则去匹配code
     * 3 按枚举name匹配
     *
     * @param prop  属性
     * @param value 输入值
     * @return 输出值
     */
    @SuppressWarnings("unchecked")
    public static Object checkEnumPropValue(PropDesc prop, Object value) {
        if (value == null) return null;
        if (!prop.getFieldClass().isEnum()) return value;
        @SuppressWarnings("rawtypes")
        Class enumClass = prop.getFieldClass();
        if (CodeEnum.class.isAssignableFrom(enumClass)) {
            return CodeEnum.codeToEnum(enumClass, value.toString());
        }
        if (value instanceof Number)
            return enumClass.getEnumConstants()[((Number) value).intValue()];
        try {
            int index = Integer.parseInt(value.toString());
            return enumClass.getEnumConstants()[index];
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 把一个对象，转为保存到数据库用到Map之中
     *
     * @param bean            对象
     * @param ignoreNullValue 是否忽略为空的值
     * @return 转化后的Map
     */
    public static Map<String, Object> beanToMap(Object bean, boolean ignoreNullValue) {
        Map<String, Object> result = new LinkedHashMap<>();
        BeanUtil.descForEach(bean.getClass(), prop -> {
            if (prop.getField().getAnnotation(Transient.class) != null)
                return;
            String fieldName = prop.getRawFieldName();
            Column column = prop.getField().getAnnotation(Column.class);
            fieldName = column == null || StrUtil.isEmpty(column.name()) ? StrUtil.toUnderlineCase(fieldName) : column.name();
            Object value = prop.getValue(bean);
            if (null == value && ignoreNullValue)
                return;
            if (value instanceof Enum<?>) {
                if (value instanceof CodeEnum)
                    value = ((CodeEnum) value).code();
                else
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


    /**
     * 检查参数是否是枚举，枚举默认用取值ordinal()，除非它有code()函数
     *
     * @param value 值
     * @return 检查后的值
     */
    public static Object enumCheck(Object value) {
        if (value==null || !value.getClass().isEnum()) return value;
        if (value instanceof CodeEnum)
            return ((CodeEnum) value).code();
        return ((Enum<?>) value).ordinal();
    }

    /**
     * 检查一个数组参数是否是枚举
     *
     * @param arr 原数组
     * @return 处理后数组
     */
    public static Object[] enumCheck(Object[] arr) {
        if (arr==null || arr.length==0) return arr;
        Class<?> c = arr[0].getClass();
        boolean hasCode = CodeEnum.class.isAssignableFrom(c);
        if (!c.isEnum()) return arr;
        Object[] result = new Object[arr.length];
        for (int i=0;i<arr.length;i++) {
            result[i] = hasCode ? ((CodeEnum) arr[i]).code() : ((Enum<?>)arr[i]).ordinal();
        }
        return result;
    }
}
