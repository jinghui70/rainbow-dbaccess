package io.github.jinghui70.rainbow.dbaccess.rowmapper;

import cn.hutool.core.util.StrUtil;
import org.springframework.lang.NonNull;

import java.util.LinkedHashMap;
import java.util.Map;

public class CamelCaseMapMapper extends MapRowMapper {

    @Override
    @NonNull
    protected String getColumnKey(@NonNull String columnName) {
        return StrUtil.toCamelCase(columnName);
    }

    @Override
    @NonNull
    protected Map<String, Object> createColumnMap(int columnCount) {
        return new LinkedHashMap<>(columnCount);
    }
}
