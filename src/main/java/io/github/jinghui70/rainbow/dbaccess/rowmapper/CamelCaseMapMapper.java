package io.github.jinghui70.rainbow.dbaccess.rowmapper;

import cn.hutool.core.util.StrUtil;
import org.springframework.lang.NonNull;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 将数据库下划线列名转换为驼峰风格键名的Map RowMapper。
 * 使用LinkedHashMap保持列的顺序。
 */
public class CamelCaseMapMapper extends MapRowMapper {

    /**
     * 将数据库列名转换为驼峰风格的键名。
     *
     * @param columnName 数据库列名
     * @return 驼峰风格的键名
     */
    @Override
    @NonNull
    protected String getColumnKey(@NonNull String columnName) {
        return StrUtil.toCamelCase(columnName);
    }

    /**
     * 创建LinkedHashMap实例以保持列的顺序。
     *
     * @param columnCount 列数，用于初始化Map容量
     * @return LinkedHashMap实例
     */
    @Override
    @NonNull
    protected Map<String, Object> createColumnMap(int columnCount) {
        return new LinkedHashMap<>(columnCount);
    }
}
